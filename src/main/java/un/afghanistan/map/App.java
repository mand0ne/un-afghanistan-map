/**
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package un.afghanistan.map;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import javafx.stage.StageStyle;
import un.afghanistan.map.utility.FXMLUtils;
import un.afghanistan.map.utility.StageUtils;

public class App extends Application {

    public static Stage primaryStage;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud5005476303,none,XXMFA0PL4P2J4P7EJ203");

        primaryStage = stage;
        StageUtils.setStage(primaryStage, "UN Afghanistan Map", false, StageStyle.DECORATED, null);
        primaryStage.getIcons().add(new Image("/un/afghanistan/map/img/unLogo.png"));
        StageUtils.centerStage(primaryStage, 1300, 800);
        primaryStage.setScene(new Scene(FXMLUtils.loadController("fxml/map.fxml")));
        primaryStage.show();
    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {
        try {
            super.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
