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
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.Map;


public class MapController {
    @FXML
    private BorderPane pane;

    private MapView mapView;

    @FXML
    public void initialize() {
        UserCredential credential = new UserCredential("mand0ne", "657feebc6953700d976cc16203d6ed58c2fe3b");
        final Portal portal = new Portal("https://www.arcgis.com");
        portal.setCredential(credential);
        System.out.println(portal.isLoginRequired());
        portal.addDoneLoadingListener(() -> {
            if (portal.getLoadStatus() == LoadStatus.LOADED) {
                PortalItem mapPortalItem = new PortalItem(portal, "28fa460ad8734437ba8b86f7fdde3e2e");
                ArcGISMap map = new ArcGISMap(mapPortalItem);
                mapView = new MapView();

                // create a initial viewpoint with a center point and scale
                Point point = new Point(64.79, 35.92, SpatialReference.create(4326));

                // create a view and set ArcGISMap to it
                mapView = new MapView();
                mapView.setMap(map);

                pane.setCenter(mapView);

                GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
                mapView.getGraphicsOverlays().add(graphicsOverlay);

                // create a red (0xFFFF0000) simple marker symbol
                SimpleMarkerSymbol symbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, 0xFFFF0000, 12);

                // create a new graphic with a our point and symbol
                Graphic graphic = new Graphic(point, symbol);
                graphicsOverlay.getGraphics().add(graphic);
            }
        });
        portal.loadAsync();
    }
}
