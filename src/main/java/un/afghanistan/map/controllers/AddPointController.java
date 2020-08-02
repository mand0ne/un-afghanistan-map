package un.afghanistan.map.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import un.afghanistan.map.utility.database.LocationDAO;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddPointController {
    @FXML
    private Button addBtn;
    @FXML
    public TextField longitudeTextField, latitudeTextField, nameTextField, fileTextField;

    private final LocationDAO locationTableService = LocationDAO.getInstance();

    private Stage primaryStage;
    final FileChooser fileChooser = new FileChooser();

    public AddPointController() {
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel & PDF", "*.xlsx", "*.XLSX", "*.PDF", "*.pdf"));
        fileChooser.setTitle("Choose a file");
    }

    public AddPointController(Stage primaryStage) {
        this();
        this.primaryStage = primaryStage;
    }

    @FXML
    public void initialize() {
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

    public void addButtonAction() {
        String name = nameTextField.getText();
        String latitude = latitudeTextField.getText();
        String longitude = longitudeTextField.getText();
        String file = fileTextField.getText();

        if (validateInputs(name, latitude, longitude, file) &&
                !locationTableService.doesLocationExistInDatabase(Double.parseDouble(latitude), Double.parseDouble(longitude))) {
            locationTableService.addLocation(name, Double.parseDouble(latitude), Double.parseDouble(longitude), file);
            closeWindow();
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

    public void browseButtonAction() {
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            System.out.println(file.getName());
            fileTextField.setText(file.getAbsolutePath());
            fileTextField.setStyle("-fx-background-color: WHITE;");
        }
    }

    public void cancelButtonAction() {
        closeWindow();
    }

    private void closeWindow() {
        addBtn.getScene().getWindow().hide();
    }
}
