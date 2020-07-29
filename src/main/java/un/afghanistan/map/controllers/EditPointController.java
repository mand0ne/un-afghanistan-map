package un.afghanistan.map.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import un.afghanistan.map.models.Location;
import un.afghanistan.map.utility.database.LocationDAO;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;

public class EditPointController {
    public Button deleteBtn;
    public Button saveBtn;
    public Button cancelBtn;
    public Button browseBtn;
    public TextField latitudeTextField;
    public TextField longitudeTextField;
    public TextField nameTextField;
    public TextField fileTextField;
    private Location location;
    private MapController mapController;
    private LocationDAO database = LocationDAO.getInstance();

    private Stage primaryStage;
    final FileChooser fileChooser = new FileChooser();

    public EditPointController(Location location, MapController mapController) {
        this.location = location;
        this.mapController = mapController;
    }

    @FXML
    public void initialize() {
        latitudeTextField.setText(Double.toString(location.getLatitude()));
        longitudeTextField.setText(Double.toString(location.getLongitude()));
        nameTextField.setText(location.getName());
        fileTextField.setText(location.getFilePath());

        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(""))
                nameTextField.setStyle("-fx-background-color: white;");
        });
        latitudeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(""))
                latitudeTextField.setStyle("-fx-background-color: white;");
        });
        longitudeTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(""))
                longitudeTextField.setStyle("-fx-background-color: white;");
        });
        fileTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(""))
                fileTextField.setStyle("-fx-background-color: white;");
        });

    }

    public void saveButtonAction() {

        String name = nameTextField.getText();
        String latitude = latitudeTextField.getText();
        String longitude = longitudeTextField.getText();
        String file = fileTextField.getText();

        if (validateInputs(name, latitude, longitude, file)) {
            database.editLocation(new Location(location.getId(), nameTextField.getText(), Double.parseDouble(latitudeTextField.getText()), Double.parseDouble(longitudeTextField.getText()), fileTextField.getText()));
            closeWindow();
        }
    }

    public void cancelButtonAction() {
        closeWindow();
    }

    public void deleteButtonAction() {

        String name = nameTextField.getText();
        String latitude = latitudeTextField.getText();
        String longitude = longitudeTextField.getText();
        String file = fileTextField.getText();

        if (validateInputs(name, latitude, longitude, file)) {
            int input = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this location?");
            if(input == 0) {
                database.deleteLocation(location.getId());
                int inputAlert = JOptionPane.showOptionDialog(null, "Location deleted", "Alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

                if(inputAlert == JOptionPane.OK_OPTION) {
                    closeWindow();
                }
            }
        }

    }

    public void browseButtonAction() {
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            System.out.println(file.getName());
            fileTextField.setText(file.getAbsolutePath());
            fileTextField.setStyle("-fx-background-color: #e28787;");
        }
    }

    public boolean validateInputs(String name, String latitude, String longitude, String file) {
        boolean validInputs = true;

        if (name == null || name.equals("")) {
            nameTextField.setStyle("-fx-background-color: #e28787;");
            validInputs = false;
        }

        try {
            double lat = Double.parseDouble(latitude);
            if (lat < -90 || lat > 90) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            latitudeTextField.setStyle("-fx-background-color: #e28787;");
            validInputs = false;
        }

        try {
            double lon = Double.parseDouble(longitude);
            if (lon < -180 || lon > 180) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            longitudeTextField.setStyle("-fx-background-color: #e28787;");
            validInputs = false;
        }

        if (file == null || !(new File(file)).exists()) {
            fileTextField.setStyle("-fx-background-color: #e28787;");
            validInputs = false;
        }

        return validInputs;
    }

    private void closeWindow() {
        deleteBtn.getScene().getWindow().hide();
    }

    public void setController(MapController mapController) {
        this.mapController = mapController;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
