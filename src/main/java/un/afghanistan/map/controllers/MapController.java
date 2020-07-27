package un.afghanistan.map.controllers;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.UserCredential;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import un.afghanistan.map.utility.database.Location;
import un.afghanistan.map.utility.database.LocationDAO;
import javafx.stage.Stage;
import un.afghanistan.map.utility.FXMLUtils;


public class MapController {
    @FXML
    private BorderPane pane;
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

    private MapView mapView;
    private ArcGISMap map;

    public void addPointAction(ActionEvent actionEvent) {
        Stage stage = new Stage();
        Parent root = null;
        stage.setTitle("Add point");
        stage.setScene(new Scene(FXMLUtils.loadController("fxml/addPoint.fxml")));
        stage.setResizable(false);
        stage.showAndWait();
    }

    private class BasemapListCell extends ListCell<String> {
        protected void updateItem(String item, boolean empty){
            super.updateItem(item, empty);
            setGraphic(null);
            setText(null);
            if(item!=null){
                ImageView imageView = new ImageView(new Image("/un/afghanistan/map/img/basemap-styles/" + item + ".png"));
                imageView.setFitWidth(60);
                imageView.setFitHeight(40);
                setGraphic(imageView);
                setText(item);
            }
        }

    }

    @FXML
    public void initialize() {

        comboBox.getItems().removeAll(comboBox.getItems());
        comboBox.getItems().addAll("Charted Territory Map", "Dark Gray Canvas", "Light Gray Canvas", "Imagery", "Imagery Hybrid", "National Geographic", "Navigation", "Navigation (Dark mode)", "Newspaper Map", "OpenStreetMap", "Streets", "Streets (Night)", "Terrain with Labels", "Topographic");
        comboBox.setCellFactory(c -> new BasemapListCell());

        // ovo za sad nek stoji dok ne popravimo ono
        Basemap.Type[] values = Basemap.Type.values();
        for (int i=0; i<values.length; i++) {
            System.out.println(values[i]);
        }

        editPointBtn.setDisable(true);
        addPointBtn.setDisable(false);

        // cutomize listview
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
        System.out.println(portal.isLoginRequired());
        portal.addDoneLoadingListener(() -> {
            if (portal.getLoadStatus() == LoadStatus.LOADED) {
                PortalItem mapPortalItem = new PortalItem(portal, "28fa460ad8734437ba8b86f7fdde3e2e");
                map = new ArcGISMap(mapPortalItem);
                mapView = new MapView();

                // create a view and set ArcGISMap to it
                mapView = new MapView();
                mapView.setMap(map);

                centerPane.getChildren().add(mapView);

                // create a graphic overlay
                GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
                mapView.getGraphicsOverlays().add(graphicsOverlay);

                // create picture marker symbol
                Image flag = new Image("/un/afghanistan/map/img/marker.png");
                PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(flag);
                markerSymbol.setHeight(30);
                markerSymbol.setWidth(18);

                LocationDAO locationDAO = LocationDAO.getInstance();

                // add a marker for every location
                for (Location l : locationDAO.getLocations()) {
                    locationListView.getItems().add(l);
                    Point graphicPoint = new Point(l.getLongitude(), l.getLatitude(), SpatialReference.create(4326));
                    Graphic symbolGraphic = new Graphic(graphicPoint, markerSymbol);
                    graphicsOverlay.getGraphics().add(symbolGraphic);
                }


            }
        });
        portal.loadAsync();
    }

    public void basemapStyleChanged() {
        String style = comboBox.getValue();
        if (style.equals("Dark Gray Canvas"))
            map.setBasemap(Basemap.createDarkGrayCanvasVector());
        else if (style.equals("Light Gray Canvas"))
            map.setBasemap(Basemap.createLightGrayCanvas());
        else if (style.equals("Charted Territory Map"))
            map.setBasemap(Basemap.createTopographicVector()); // NEMA, A OVO JE DEFAULT
        else if (style.equals("Imagery"))
            map.setBasemap(Basemap.createImagery());
        else if (style.equals("Imagery Hybrid"))
            map.setBasemap(Basemap.createImageryWithLabels());
        else if (style.equals("National Geographic"))
            map.setBasemap(Basemap.createNationalGeographic());
        else if (style.equals("Navigation"))
            map.setBasemap(Basemap.createNavigationVector());
        else if (style.equals("Navigation (Dark mode)"))
            map.setBasemap(Basemap.createNavigationVector()); // NEMA?
        else if (style.equals("Newspaper Map"))
            map.setBasemap(Basemap.createNavigationVector()); // NEMA?
        else if (style.equals("OpenStreetMap"))
            map.setBasemap(Basemap.createOpenStreetMap());
        else if (style.equals("Streets"))
            map.setBasemap(Basemap.createStreets());
        else if (style.equals("Streets (Night)"))
            map.setBasemap(Basemap.createStreetsNightVector());
        else if (style.equals("Terrain with Labels"))
            map.setBasemap(Basemap.createTerrainWithLabels());
        else if (style.equals("Topographic"))
            map.setBasemap(Basemap.createTopographic());
    }

}
