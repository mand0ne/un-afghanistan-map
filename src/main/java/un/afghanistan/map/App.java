/*
 * Copyright 2019 Esri
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package un.afghanistan.map;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.mapping.view.MapView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import un.afghanistan.map.controllers.MapController;
import un.afghanistan.map.utility.javafx.FXMLUtils;
import un.afghanistan.map.utility.javafx.StageUtils;


public class App extends Application {

    public static Stage primaryStage;
    private final MapView mapView = new MapView();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud5005476303,none,XXMFA0PL4P2J4P7EJ203");

        primaryStage = stage;
        StageUtils.setStage(primaryStage, "UN Afghanistan Map", true, StageStyle.DECORATED, null);
        primaryStage.getIcons().add(new Image(App.class.getResourceAsStream("img/unLogo.png")));
        StageUtils.centerStage(primaryStage, 1300, 800);
        Parent root = FXMLUtils.loadCustomController("fxml/map.fxml", c -> new MapController(mapView));

        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.widthProperty().addListener((observableValue, number, t1) -> {
            if (!primaryStage.maximizedProperty().get())
                primaryStage.setWidth(1300);
        });

        primaryStage.heightProperty().addListener((observableValue, number, t1) -> {
            if (!primaryStage.maximizedProperty().get())
                primaryStage.setHeight(800);
        });
    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {
        try {
            System.out.println("STOPPED!");
            super.stop();
            mapView.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
