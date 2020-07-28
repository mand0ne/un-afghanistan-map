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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;
import un.afghanistan.map.gui.BasemapListCell;
import un.afghanistan.map.interfaces.UpdateMapInterface;
import un.afghanistan.map.utility.FXMLUtils;
import un.afghanistan.map.models.Location;
import un.afghanistan.map.utility.database.LocationDAO;

import java.io.IOException;
import java.util.List;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;
import static javafx.scene.paint.Color.WHITE;


public class MapController implements UpdateMapInterface {
    @FXML
    private StackPane centerPane;
    @FXML
    private ComboBox<String> comboBox;
    @FXML
    private ListView<Location> locationListView;
    @FXML
    private Button editPointBtn, addPointBtn;
    @FXML
    private Label locationLabel;

    private MapView mapView;
    private ArcGISMap map;
    private ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics;
    private Graphic selectedGraphic = new Graphic();
    private Callout callout;
    private final GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
  
    public MapController(MapView mapView) {
        this.mapView = mapView;
        LocationDAO.getInstance().setUpdateMapInterface(this);
    }

    @FXML
    public void initialize() {

        comboBox.getItems().removeAll(comboBox.getItems());
        comboBox.getItems().addAll("Charted Territory Map", "Dark Gray Canvas", "Light Gray Canvas", "Imagery", "Imagery Hybrid",
                "National Geographic", "Navigation", "Navigation (Dark mode)", "Newspaper Map",
                "OpenStreetMap", "Streets", "Streets (Night)", "Terrain with Labels", "Topographic");
        comboBox.setCellFactory(c -> new BasemapListCell());

        // Cutomize listview
        locationListView.setCellFactory(lv -> {

            ListCell<Location> cell = new ListCell<Location>() {
                @Override
                protected void updateItem(Location l, boolean empty) {
                    super.updateItem(l, empty);

                    if (empty || l == null || l.getName() == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(l.getName());
                        ImageView imageView = new ImageView(new Image("/un/afghanistan/map/img/marker.png"));
                        imageView.setFitWidth(15);
                        imageView.setFitHeight(25);
                        setGraphic(imageView);
                    }
                }
            };

            cell.setOnMouseClicked(mouseEvent -> {
                if (!cell.isEmpty()) {
                    System.out.println("You clicked on cell");
                    Location l = cell.getItem();
                    System.out.println(l.getName());
                    Viewpoint viewpoint = new Viewpoint(l.getLatitude(), l.getLongitude(), 0.83e6);

                    // Take 2 seconds to move to viewpoint
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
                                identifyGraphics.addDoneListener(() -> Platform.runLater(() -> createGraphicDialog(mapPoint, l)));
                            }
                            else
                                System.out.println("Animation not completed successfully");
                        } catch (Exception e) {
                            System.out.println("Animation interrupted");
                        }
                    });
                }
                else
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

                // Create a view and set ArcGISMap to it
                map = new ArcGISMap(mapPortalItem);
                mapView = new MapView();
                mapView.setMap(map);
                centerPane.getChildren().add(mapView);

                finishLoading();
            } else
                portal.retryLoadAsync();
        });

        portal.loadAsync();
    }

    public void finishLoading() {
        resetViewpoint();

        // Create a graphic overlay
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        // Create picture marker symbol
        Image flag = new Image("/un/afghanistan/map/img/marker.png");
        PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(flag);
        markerSymbol.setHeight(30);
        markerSymbol.setWidth(18);

        LocationDAO locationDAO = LocationDAO.getInstance();

        // Add a marker for every location
        for (Location l : locationDAO.getLocations()) {
            locationListView.getItems().add(l);
            Point graphicPoint = new Point(l.getLongitude(), l.getLatitude(), SpatialReference.create(4326));
            Graphic symbolGraphic = new Graphic(graphicPoint, markerSymbol);
            graphicsOverlay.getGraphics().add(symbolGraphic);
        }

        // Define listeners
        mapView.setOnMouseMoved(mouseEvent -> {
            try {
                Point2D mousePoint = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                Point mapPoint = mapView.screenToLocation(mousePoint);

                String latLonDecimalDegrees = CoordinateFormatter.toLatitudeLongitude(mapPoint, CoordinateFormatter
                        .LatitudeLongitudeFormat.DECIMAL_DEGREES, 5);

                locationLabel.setText("Current coordinates are: " + latLonDecimalDegrees);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        callout = mapView.getCallout();
        // Set the callout's details
        callout.setTitleColor(WHITE);
        callout.setDetailColor(WHITE);
        callout.setBackgroundColor(Paint.valueOf("#123456"));

        mapView.setOnMouseClicked(mouseEvent -> {
            try {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.isStillSincePress()) {

                    // Create a point from location clicked
                    Point2D screenPoint = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                    Point mapPoint = mapView.screenToLocation(screenPoint);

                    // Identify graphics on the graphics overlay
                    identifyGraphics = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 5, false);
                    identifyGraphics.addDoneListener(() -> Platform.runLater(() -> createGraphicDialog(mapPoint, null)));
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
     * Will reset the Viewpoint to a default Point and scale.
     */
    public void resetViewpoint(){
        // Latitude, longitude, scale
        Viewpoint viewpoint = new Viewpoint(33.9391, 67.7100, 0.83e7);
        // Take 2 seconds to move to viewpoint
        mapView.setViewpointAsync(viewpoint, 2);
    }


    /**
     * Indicates when a graphic is clicked by outlining the symbol/marker associated with the graphic.
     */
    private void createGraphicDialog(Point mapPoint, Location selectedLocation) {

        try {
            // Get the list of graphics returned by identify
            IdentifyGraphicsOverlayResult result = identifyGraphics.get();
            List<Graphic> graphics = result.getGraphics();

            if (!graphics.isEmpty()) {
                Graphic hoveredGraphic = graphics.get(0);
                if(hoveredGraphic.getGeometry().getDimension().equals(GeometryDimension.POINT) && selectedLocation == null){
                    selectedLocation = LocationDAO.getInstance().getGetSelectedLocation(
                            ((Point)(hoveredGraphic.getGeometry())).getY(), ((Point)(hoveredGraphic.getGeometry())).getX());
                    System.out.println(selectedLocation);
                    locationListView.getSelectionModel().select(selectedLocation);
                    editPointBtn.setDisable(false);
                }

                // Show the callout where the user clicked
                callout.setTitle(selectedLocation.getName());
                callout.setDetail(selectedLocation.toString());
                callout.showCalloutAt(mapPoint, new Duration(500));

                hoveredGraphic.setSelected(true);
                if (hoveredGraphic != selectedGraphic) {
                    selectedGraphic.setSelected(false);
                    selectedGraphic = hoveredGraphic;
                }

                System.out.println("POZVOSAMSE");
            } else {
                selectedGraphic.setSelected(false);
                selectedGraphic = new Graphic();
                callout.dismiss();
                editPointBtn.setDisable(true);
                System.out.println("KURCINA");
                locationListView.getSelectionModel().clearSelection();
            }
        } catch (Exception e) {
            // on any error, display the stack trace
            e.printStackTrace();
        }
    }

    public void addPointAction() {
        Stage stage = new Stage();
        Parent root = null;
        stage.setTitle("Add point");
        stage.setScene(new Scene(FXMLUtils.loadController("fxml/addPoint.fxml")));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void editButtonAction(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/un/afghanistan/map/fxml/editPoint.fxml"));
        EditPointControler editPointControler = new EditPointControler(locationListView.getSelectionModel().getSelectedItem());
        editPointControler.setController(this);
        fxmlLoader.setController(editPointControler);
        Parent root = fxmlLoader.load();
        stage.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
        stage.setResizable(false);;
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
                map.setBasemap(Basemap.createTopographicVector()); // NEMA, A OVO JE DEFAULT
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
            case "Navigation (Dark mode)":
                map.setBasemap(Basemap.createNavigationVector()); // NEMA?
                break;
            case "Newspaper Map":
                map.setBasemap(Basemap.createNavigationVector()); // NEMA?
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
    public void onMapUpdateRequest(Location location) {
        locationListView.getItems().add(location);

        // Create picture marker symbol
        Image flag = new Image("/un/afghanistan/map/img/marker.png");
        PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(flag);
        markerSymbol.setHeight(30);
        markerSymbol.setWidth(18);
        Point graphicPoint = new Point(location.getLongitude(), location.getLatitude(), SpatialReference.create(4326));
        Graphic symbolGraphic = new Graphic(graphicPoint, markerSymbol);
        graphicsOverlay.getGraphics().add(symbolGraphic);
    }


}
