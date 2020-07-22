package un.afghanistan.map.controllers;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.loadable.LoadStatusChangedEvent;
import com.esri.arcgisruntime.loadable.LoadStatusChangedListener;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalInfo;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.portal.PortalUser;
import com.esri.arcgisruntime.security.CredentialChangedEvent;
import com.esri.arcgisruntime.security.CredentialChangedListener;
import com.esri.arcgisruntime.security.UserCredential;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;


import java.awt.*;
import java.util.Map;


public class MapController {
    @FXML
    private BorderPane pane;
    @FXML
    private StackPane centerPane;
    @FXML
    private ComboBox<String> comboBox;

    private MapView mapView;
    private ArcGISMap map;

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

        Basemap.Type[] values = Basemap.Type.values();
        for (int i=0; i<values.length; i++) {
            System.out.println(values[i]);
        }

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

                GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
                mapView.getGraphicsOverlays().add(graphicsOverlay);

                // create a red (0xFFFF0000) simple marker symbol
                SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, 0xFFFF0000, 12);

                // create a point
                Point point = new Point(64.79, 35.92, SpatialReference.create(4326));

                // create a new graphic with a our point and symbol
                Graphic graphic = new Graphic(point, symbol);
                graphicsOverlay.getGraphics().add(graphic);
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
