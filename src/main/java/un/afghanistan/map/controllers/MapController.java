package un.afghanistan.map.controllers;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.CoordinateFormatter;
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
import un.afghanistan.map.utility.FXMLUtils;
import un.afghanistan.map.utility.database.Location;
import un.afghanistan.map.utility.database.LocationDAO;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static javafx.scene.paint.Color.WHITE;


public class MapController {
    @FXML
    private StackPane centerPane;
    @FXML
    private ComboBox<String> comboBox;
    @FXML
    private ListView<Location> locationListView;
    @FXML
    private Button editPointBtn;
    @FXML
    private Button addPointBtn;
    @FXML
    private Label locationLabel;

    private MapView mapView;
    private ArcGISMap map;
    private GraphicsOverlay graphicsOverlay;
    private ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphics;
    private Graphic selectedGraphic = new Graphic();

    private static class BasemapListCell extends ListCell<String> {
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(null);
            setText(null);
            if (item != null) {
                ImageView imageView = new ImageView(new Image("/un/afghanistan/map/img/basemap-styles/" + item + ".png"));
                imageView.setFitWidth(60);
                imageView.setFitHeight(40);
                setGraphic(imageView);
                setText(item);
            }
        }
    }

    public MapController(MapView mapView) {
        this.mapView = mapView;
    }

    @FXML
    public void initialize() {

        comboBox.getItems().removeAll(comboBox.getItems());
        comboBox.getItems().addAll("Charted Territory Map", "Dark Gray Canvas", "Light Gray Canvas", "Imagery", "Imagery Hybrid",
                "National Geographic", "Navigation", "Navigation (Dark mode)", "Newspaper Map",
                "OpenStreetMap", "Streets", "Streets (Night)", "Terrain with Labels", "Topographic");
        comboBox.setCellFactory(c -> new BasemapListCell());

        editPointBtn.setDisable(true);
        addPointBtn.setDisable(false);

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

            cell.setOnMouseClicked(e -> {
                if (!cell.isEmpty()) {
                    System.out.println("You clicked on cell");
                    Location l = cell.getItem();
                    System.out.println(l.getName());
                    Viewpoint viewpoint = new Viewpoint(l.getLatitude(), l.getLongitude(), 0.83e6);
                    mapView.setViewpointAsync(viewpoint, 1);

                    editPointBtn.setDisable(false);
                    e.consume();
                }
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

                finishLoadiong();
            } else
                portal.retryLoadAsync();
        });

        portal.loadAsync();
    }

    public void finishLoadiong() {
        // Create a graphic overlay
        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
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

        Callout callout = mapView.getCallout();

        mapView.setOnMouseClicked(mouseEvent -> {
            try {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.isStillSincePress()) {

                    // Create a point from location clicked
                    Point2D mousePoint = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                    Point mapPoint = mapView.screenToLocation(mousePoint);

                    // Set the callout's details
                    callout.setTitleColor(WHITE);
                    callout.setDetailColor(WHITE);
                    callout.setBackgroundColor(Paint.valueOf("#123456"));
                    callout.setTitle("Information about the location");
                    callout.setDetail("Sta ovdje");

                    // Identify graphics on the graphics overlay
                    identifyGraphics = mapView.identifyGraphicsOverlayAsync(graphicsOverlay, mousePoint, 5, false);
                    identifyGraphics.addDoneListener(() -> Platform.runLater(() -> createGraphicDialog(callout, mapPoint)));
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY && mouseEvent.isStillSincePress())
                    callout.dismiss();

            } catch (Exception ex) {
                callout.dismiss();
                System.out.println(ex.getMessage());
            }
        });

    }

    public void resetViewpoint(){
        // Latitude, longitude, scale
        Viewpoint viewpoint = new Viewpoint(33.9391, 67.7100, 0.83e7);

        // Take 2 seconds to move to viewpoint
        final ListenableFuture<Boolean> viewpointSetFuture = mapView.setViewpointAsync(viewpoint, 2);
        viewpointSetFuture.addDoneListener(() -> {
            try {
                boolean completed = viewpointSetFuture.get();
                if (completed) {
                    System.out.println("Animation completed successfully");
                }
                else
                    System.out.println("Animation not completed successfully");
            } catch (Exception e) {
                System.out.println("Animation interrupted");
            }
        });
    }

    /**
     * Indicates when a graphic is clicked by showing an Alert.
     */
    private void createGraphicDialog(Callout callout, Point point) {

        try {
            // Get the list of graphics returned by identify
            IdentifyGraphicsOverlayResult result = identifyGraphics.get();
            List<Graphic> graphics = result.getGraphics();

            if (!graphics.isEmpty()) {
                Graphic hoveredGraphic = graphics.get(0);
                hoveredGraphic.setSelected(!hoveredGraphic.isSelected());
                if (hoveredGraphic != selectedGraphic) {
                    selectedGraphic.setSelected(false);
                    selectedGraphic = hoveredGraphic;
                }

                // Show the callout where the user clicked
                callout.showCalloutAt(point, new Duration(500));
            } else {
                selectedGraphic.setSelected(false);
                selectedGraphic = new Graphic();
                callout.dismiss();
            }
        } catch (Exception e) {
            // on any error, display the stack trace
            e.printStackTrace();
        }
    }

    public void addPointAction(ActionEvent actionEvent) {
        Stage stage = new Stage();
        Parent root = null;
        stage.setTitle("Add point");
        stage.setScene(new Scene(FXMLUtils.loadController("fxml/addPoint.fxml")));
        stage.setResizable(false);
        stage.showAndWait();
    }

    public void basemapStyleChanged() {
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

}
