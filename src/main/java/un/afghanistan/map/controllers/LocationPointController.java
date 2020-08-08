package un.afghanistan.map.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import un.afghanistan.map.models.Location;
import un.afghanistan.map.utility.database.LocationDAO;

import java.io.File;
import java.util.Optional;

import static javafx.scene.paint.Color.RED;

public class LocationPointController {
    private final LocationDAO locationTableService = LocationDAO.getInstance();
    private final FileChooser fileChooser = new FileChooser();
    private final boolean isInEditMode;
    @FXML
    private Button saveBtn, deleteBtn, cancelBtn;
    @FXML
    private TextField latitudeTextField, longitudeTextField, nameTextField, fileTextField;
    @FXML
    private CheckBox isInKabulCheckbox;
    @FXML
    private ButtonBar buttonBar;
    private Location location;
    private Stage primaryStage;

    public LocationPointController(boolean editMode) {
        this.isInEditMode = editMode;
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel & PDF", "*.xlsx", "*.XLSX", "*.PDF", "*.pdf"));
        fileChooser.setTitle("Choose a file");
    }

    public LocationPointController(Stage primaryStage, boolean editMode) {
        this(editMode);
        this.primaryStage = primaryStage;
    }

    public LocationPointController(Stage primaryStage, Location location, boolean editMode) {
        this(primaryStage, editMode);
        this.location = location;
    }

    @FXML
    public void initialize() {
        if (isInEditMode) {
            latitudeTextField.setText(Double.toString(location.getLatitude()));
            longitudeTextField.setText(Double.toString(location.getLongitude()));
            nameTextField.setText(location.getName());
            fileTextField.setText(location.getFilePath());
            isInKabulCheckbox.setSelected(location.isInKabul());

            deleteBtn = new Button("Delete");
            deleteBtn.setTextFill(RED);
            deleteBtn.toFront();
            deleteBtn.setOnAction(actionEvent -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete this location?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    locationTableService.deleteLocation(location);
                    Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
                    alertInfo.setTitle("Confirmation");
                    alertInfo.setHeaderText(null);
                    alertInfo.setContentText("Location deleted");
                    alertInfo.showAndWait();
                    closeWindow();
                }
            });

            buttonBar.getButtons().add(0, deleteBtn);

            saveBtn.setText("Save");
        }

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

    public void browseButtonAction() {
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            fileTextField.setText(file.getAbsolutePath());
            fileTextField.setStyle("-fx-background-color: WHITE;");
        }
    }

    public void cancelButtonAction() {
        closeWindow();
    }

    private void closeWindow() {
        cancelBtn.getScene().getWindow().hide();
    }

    /* ADD POINT */
    public void saveButtonAction() {
        String name = nameTextField.getText();
        String latitude = latitudeTextField.getText();
        String longitude = longitudeTextField.getText();
        String file = fileTextField.getText();
        boolean isInKabul = isInKabulCheckbox.isSelected();

        if (validInputs(name, latitude, longitude, file)) {
            if (isInEditMode)
                locationTableService.editLocation(location, new Location(location.getId(), name,
                        Double.parseDouble(latitude), Double.parseDouble(longitude), file, isInKabul));
            else if (!locationTableService.doesExist(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                locationTableService.addLocation(name, Double.parseDouble(latitude), Double.parseDouble(longitude), file, isInKabul);
            else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Compound with the same coordinates already exists!");
                alert.show();
            }

            closeWindow();
        }
    }
}
