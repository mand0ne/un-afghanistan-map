package un.afghanistan.map.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import un.afghanistan.map.models.Location;
import un.afghanistan.map.utility.database.LocationDAO;

import javax.swing.*;
import java.io.File;
import java.util.Optional;

public class EditPointController {
    @FXML
    private Button deleteBtn;
    @FXML
    private TextField latitudeTextField, longitudeTextField, nameTextField, fileTextField;
    @FXML
    private CheckBox isInKabulCheckbox;

    private final Location location;
    private final LocationDAO locationTableService = LocationDAO.getInstance();
    private final Stage primaryStage;
    private final FileChooser fileChooser = new FileChooser();

    public EditPointController(Stage primaryStage, Location location) {
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel & PDF", "*.xlsx", "*.XLSX", "*.PDF", "*.pdf"));
        fileChooser.setTitle("Choose a file");
        this.primaryStage = primaryStage;
        this.location = location;
    }

    @FXML
    public void initialize() {
        latitudeTextField.setText(Double.toString(location.getLatitude()));
        longitudeTextField.setText(Double.toString(location.getLongitude()));
        nameTextField.setText(location.getName());
        fileTextField.setText(location.getFilePath());
        isInKabulCheckbox.setSelected(location.isInKabul());

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
        boolean isInKabul = isInKabulCheckbox.isSelected();

        if (validInputs(name, latitude, longitude, file)) {
            locationTableService.editLocation(location, new Location(location.getId(), name,
                    Double.parseDouble(latitude), Double.parseDouble(longitude), file, isInKabul));

            closeWindow();
        }
    }

    public void cancelButtonAction() {
        closeWindow();
    }

    public void deleteButtonAction() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this location?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            locationTableService.deleteLocation(location);
            Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
            alertInfo.setTitle("Confirmation");
            alertInfo.setHeaderText(null);
            alertInfo.setContentText("Location deleted");
            alertInfo.showAndWait();
            closeWindow();
        }
    }

    public void browseButtonAction() {
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            System.out.println(file.getName());
            fileTextField.setText(file.getAbsolutePath());
            fileTextField.setStyle("-fx-background-color: WHITE;");
        }
    }

    public boolean validInputs(String name, String latitude, String longitude, String file) {
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
}
