package un.afghanistan.map.controllers;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.CoordinateFormatter;
import com.esri.arcgisruntime.geometry.GeometryDimension;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.UserCredential;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import un.afghanistan.map.App;
import un.afghanistan.map.gui.BasemapListCell;
import un.afghanistan.map.interfaces.UpdateMapInterface;
import un.afghanistan.map.models.Location;
import un.afghanistan.map.utility.database.LocationDAO;
import un.afghanistan.map.utility.javafx.FXMLUtils;
import un.afghanistan.map.utility.javafx.StageUtils;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;
import static javafx.scene.paint.Color.WHITE;
import static un.afghanistan.map.App.primaryStage;


public class MapController implements UpdateMapInterface {
    private final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    @FXML
    private StackPane centerPane;
    @FXML
    private ComboBox<String> comboBox;
    @FXML
    private ListView<Location> locationListView;
    @FXML
    private Button editPointBtn;
    @FXML
    private JFXToggleButton kabulToggle;
    @FXML
    private Label locationLabel;
    private MapView mapView;
    private ArcGISMap map;
    private ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics;
    private Graphic previouslySelectedGraphic = new Graphic();
    private Callout callout;
    private Basemap defaultBasemap;
    private PictureMarkerSymbol markerSymbol;
    private final JFXProgressBar mapLoader = new JFXProgressBar();

    public MapController(MapView mapView) {
        this.mapView = mapView;
        LocationDAO.getInstance().setUpdateMapInterface(this);
    }

    @FXML
    public void initialize() {

        centerPane.getChildren().addAll(mapLoader);
        comboBox.getItems().removeAll(comboBox.getItems());
        comboBox.getItems().addAll("Charted Territory Map", "Dark Gray Canvas", "Light Gray Canvas", "Imagery", "Imagery Hybrid",
                "National Geographic", "Navigation", "OpenStreetMap", "Streets", "Streets (Night)", "Terrain with Labels", "Topographic");
        comboBox.setCellFactory(c -> new BasemapListCell());

        // Customize listview
        locationListView.setCellFactory(lv -> {

            ListCell<Location> cell = new ListCell<>() {
                @Override
                protected void updateItem(Location l, boolean empty) {
                    super.updateItem(l, empty);

                    if (empty || l == null || l.getName() == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(l.getName());
                        ImageView imageView = new ImageView(new Image(App.class.getResourceAsStream("img/marker.png")));
                        imageView.setFitWidth(15);
                        imageView.setFitHeight(25);
                        setGraphic(imageView);
                    }
                }
            };

            cell.setOnMouseClicked(mouseEvent -> {
                if (!cell.isEmpty()) {
                    Location l = cell.getItem();
                    Viewpoint viewpoint = new Viewpoint(l.getLatitude(), l.getLongitude(), 0.83e6);

                    // Take a second to move to viewpoint
                    final ListenableFuture<Boolean> viewpointSetFuture = mapView.setViewpointAsync(viewpoint, 1);
                    viewpointSetFuture.addDoneListener(() -> {
                        try {
                            boolean completed = viewpointSetFuture.get();
                            if (completed) {
                                editPointBtn.setDisable(false);

                                Point mapPoint = new Point(l.getLongitude(), l.getLatitude(), SpatialReference.create(4326));
                                Point2D screenPoint = mapView.locationToScreen(mapPoint);
                                // Identify graphics on the graphics overlay
                                identifyGraphics = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 5, false);
                                identifyGraphics.addDoneListener(() -> Platform.runLater(() -> createGraphicDialog(mapPoint, l, mouseEvent.getClickCount())));
                            } else
                                System.out.println("Animation not completed successfully");
                        } catch (Exception e) {
                            System.out.println("Animation interrupted");
                        }
                    });
                } else
                    System.out.println("You clicked on an empty cell");
            });
            return cell;
        });


        UserCredential credential = new UserCredential("mand0ne", "657feebc6953700d976cc16203d6ed58c2fe3b");
        final Portal portal = new Portal("https://www.arcgis.com");
        portal.setCredential(credential);
        portal.addDoneLoadingListener(() -> {
            if (portal.getLoadStatus() == LoadStatus.LOADED) {
                PortalItem mapPortalItem = new PortalItem(portal, "28fa460ad8734437ba8b86f7fdde3e2e");


                new Thread(() -> {
                    fillProgressBar();
                    Platform.runLater(() -> {
                        centerPane.getChildren().clear();
                        centerPane.getChildren().addAll(mapView);
                        finishLoading();
                    });
                }).start();

                // Create a view and set ArcGISMap to it
                map = new ArcGISMap(mapPortalItem);
                defaultBasemap = map.getBasemap();
                mapView = new MapView();
                mapView.setMap(map);
            } else
                portal.retryLoadAsync();
        });

        portal.loadAsync();
    }

    private void fillProgressBar() {
        for (int i = 0; i <= 100; i++) {
            double progress = i * 0.01;
            Platform.runLater(() -> mapLoader.setProgress(progress));
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void finishLoading() {

        // Create a graphic overlay
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        // Create picture marker symbol
        Image flag = new Image(App.class.getResourceAsStream("img/marker.png"));
        markerSymbol = new PictureMarkerSymbol(flag);
        markerSymbol.setHeight(30);
        markerSymbol.setWidth(18);

        onKabulToggle();

        // Define listeners
        mapView.setOnMouseMoved(mouseEvent -> {
            try {
                Point2D mousePoint = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                Point mapPoint = mapView.screenToLocation(mousePoint);

                String latLonDecimalDegrees = CoordinateFormatter.toLatitudeLongitude(mapPoint, CoordinateFormatter
                        .LatitudeLongitudeFormat.DECIMAL_DEGREES, 5);

                locationLabel.setText("Current coordinates are: " + latLonDecimalDegrees);
            } catch (Exception ignored) {
            }
        });

        callout = mapView.getCallout();
        // Set the callouts details
        callout.setBackgroundColor(Paint.valueOf("#123456"));
        callout.setTitleColor(WHITE);
        callout.setDetailColor(WHITE);
        callout.setBorderColor(WHITE);
        callout.setBorderWidth(2);

        mapView.setOnMouseClicked(mouseEvent -> {
            try {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.isStillSincePress()) {
                    // Create a point from location clicked
                    Point2D screenPoint = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                    Point mapPoint = mapView.screenToLocation(screenPoint);

                    // Identify graphics on the graphics overlay
                    identifyGraphics = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 5, false);
                    identifyGraphics.addDoneListener(() -> Platform.runLater(() -> createGraphicDialog(mapPoint, null, 1)));
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY && mouseEvent.isStillSincePress())
                    callout.dismiss();

            } catch (Exception ex) {
                callout.dismiss();
                System.out.println(ex.getMessage());
            }
        });
    }

    /**
     * Called when "Reset" button is clicked.
     * Will reset the Viewpoint to a default Point and scale (mode dependent).
     */
    @FXML
    private void resetViewpoint() {
        Viewpoint viewpoint = new Viewpoint(33.9391, 67.7100, 0.83e7);
        if (kabulToggle.isSelected())
            viewpoint = new Viewpoint(34.5249, 69.172251, 2e5);

        mapView.setViewpointAsync(viewpoint, 2);
    }

    @FXML
    private void onKabulToggle() {
        if (callout != null)
            callout.dismiss();

        ArrayList<Location> locations = LocationDAO.getInstance().getLocations(kabulToggle.isSelected());

        locationListView.getItems().clear();
        locationListView.getItems().addAll(locations);

        graphicsOverlay.getGraphics().clear();
        for (Location l : locations) {
            Point graphicPoint = new Point(l.getLongitude(), l.getLatitude(), SpatialReference.create(4326));
            Graphic symbolGraphic = new Graphic(graphicPoint, markerSymbol);
            graphicsOverlay.getGraphics().add(symbolGraphic);
        }

        resetViewpoint();
    }


    /**
     * Indicates when a graphic is clicked by outlining the symbol/marker associated with the graphic.
     */
    private void createGraphicDialog(Point mapPoint, Location selectedLocation, int mouseClickCount) {

        try {
            // Get the list of graphics returned by identify
            IdentifyGraphicsOverlayResult result = identifyGraphics.get();
            List<Graphic> graphics = result.getGraphics();

            if (!graphics.isEmpty()) {
                Graphic selectedGraphic = graphics.get(0);
                if (selectedGraphic.getGeometry().getDimension().equals(GeometryDimension.POINT) && selectedLocation == null) {
                    selectedLocation = LocationDAO.getInstance().getSelectedLocation(
                            ((Point) (selectedGraphic.getGeometry())).getY(), ((Point) (selectedGraphic.getGeometry())).getX());

                    locationListView.getSelectionModel().select(selectedLocation);
                    editPointBtn.setDisable(false);

                }

                // Show the callout where the user clicked
                callout.setTitle("Compound: " + selectedLocation.getName());
                callout.setDetail("Latitude: " + selectedLocation.getLatitude() + "\nLongitude: " + selectedLocation.getLongitude());
                callout.showCalloutAt(mapPoint, new Duration(500));

                selectedGraphic.setSelected(true);
                final String filePath = selectedLocation.getFilePath();
                callout.setOnMouseClicked(mouseEvent -> openFile(new File(filePath)));

                if (mouseClickCount == 2)
                    openFile(new File(filePath));

                if (selectedGraphic != previouslySelectedGraphic) {
                    previouslySelectedGraphic.setSelected(false);
                    previouslySelectedGraphic = selectedGraphic;
                }

            } else {
                previouslySelectedGraphic.setSelected(false);
                previouslySelectedGraphic = new Graphic();
                callout.dismiss();
                editPointBtn.setDisable(true);
                locationListView.getSelectionModel().clearSelection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFile(File file) {
        try {
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Awt Desktop is not supported!");
                    alert.show();

                }
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("File does not exist");
                alert.show();
            }
        } catch (IOException ex) {
            Logger.getLogger(MapController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addPointAction() {
        Stage stage = new Stage();
        StageUtils.setStage(stage, "Add new location", false, StageStyle.UNDECORATED, Modality.APPLICATION_MODAL);
        StageUtils.centerStage(stage, 300, 300);
        Parent root = FXMLUtils.loadCustomController("fxml/addEditPoint.fxml", c -> new LocationPointController(stage, false));
        stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void editButtonAction() {
        Stage stage = new Stage();
        StageUtils.setStage(stage, "Edit location", false, StageStyle.UNDECORATED, Modality.APPLICATION_MODAL);
        StageUtils.centerStage(stage, 300, 300);
        Parent root = FXMLUtils.loadCustomController("fxml/addEditPoint.fxml", c -> new LocationPointController(stage,
                locationListView.getSelectionModel().getSelectedItem(), true));
        stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void changeBasemapStyle() {
        String style = comboBox.getValue();
        switch (style) {
            case "Dark Gray Canvas":
                map.setBasemap(Basemap.createDarkGrayCanvasVector());
                break;
            case "Light Gray Canvas":
                map.setBasemap(Basemap.createLightGrayCanvas());
                break;
            case "Charted Territory Map":
                map.setBasemap(defaultBasemap);
                break;
            case "Imagery":
                map.setBasemap(Basemap.createImagery());
                break;
            case "Imagery Hybrid":
                map.setBasemap(Basemap.createImageryWithLabels());
                break;
            case "National Geographic":
                map.setBasemap(Basemap.createNationalGeographic());
                break;
            case "Navigation":
                map.setBasemap(Basemap.createNavigationVector());
                break;
            case "OpenStreetMap":
                map.setBasemap(Basemap.createOpenStreetMap());
                break;
            case "Streets":
                map.setBasemap(Basemap.createStreets());
                break;
            case "Streets (Night)":
                map.setBasemap(Basemap.createStreetsNightVector());
                break;
            case "Terrain with Labels":
                map.setBasemap(Basemap.createTerrainWithLabels());
                break;
            case "Topographic":
                map.setBasemap(Basemap.createTopographic());
                break;
        }
    }

    @Override
    public void onAddLocationRequest(Location location) {
        if (location.isInKabul() && !kabulToggle.isSelected() || !location.isInKabul() && kabulToggle.isSelected())
            return;

        locationListView.getItems().add(location);

        Point graphicPoint = new Point(location.getLongitude(), location.getLatitude(), SpatialReference.create(4326));
        Graphic symbolGraphic = new Graphic(graphicPoint, markerSymbol);
        graphicsOverlay.getGraphics().add(symbolGraphic);
    }

    @Override
    public void onDeleteLocationRequest(Location location) {
        locationListView.getItems().removeAll(location);
        graphicsOverlay.getGraphics().remove(previouslySelectedGraphic);
        editPointBtn.setDisable(true);
        callout.dismiss();
    }

    public void loadDataAction() {
        LocationDAO.getInstance().loadDataFromFile(primaryStage);
    }

    public void saveDataAction() {
        LocationDAO.getInstance().saveDataToFile(primaryStage);
    }

    public void deleteAllLocationsAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Are you sure you want to delete all locations?");
        alert.setContentText("You cannot undo this!");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            LocationDAO.getInstance().deleteAllLocations();
            Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
            alertInfo.setTitle("Confirmation");
            alertInfo.setHeaderText(null);
            alertInfo.setContentText("Locations deleted");
            alertInfo.showAndWait();

            graphicsOverlay.getGraphics().clear();
            locationListView.getItems().clear();
        }
    }

    public void aboutAction() {
        Stage stage = new Stage();
        StageUtils.setStage(stage, "About", false, StageStyle.DECORATED, Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image(App.class.getResourceAsStream("img/unLogo.png")));
        StageUtils.centerStage(stage, 400, 500);
        Parent root = FXMLUtils.loadController("fxml/about.fxml");
        stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void documentationAction() {
        InputStream jarPdf = App.class.getResourceAsStream("pdf/Documentation.pdf");

        try {
            File pdfTemp = new File("Documentation.pdf");
            FileOutputStream fos = new FileOutputStream(pdfTemp);
            while (jarPdf.available() > 0) {
                fos.write(jarPdf.read());
            }
            fos.close();
            openFile(pdfTemp);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeAction() {
        System.exit(0);
    }
}
