package un.afghanistan.map.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import un.afghanistan.map.models.Location;
import un.afghanistan.map.utility.database.LocationDAO;

import javax.swing.*;

public class EditPointControler {
    public Button deleteBtn;
    public Button saveBtn;
    public Button cancelBtn;
    public TextField latitudeTextField;
    public TextField longitudeTextField;
    public TextField nameTextField;
    private Location location;
    private MapController mapController;
    private LocationDAO database = LocationDAO.getInstance();

    public EditPointControler(Location location) {
        this.location = location;
    }

    @FXML
    public void initialize() {
        latitudeTextField.setText(Double.toString(location.getLatitude()));
        longitudeTextField.setText(Double.toString(location.getLongitude()));
        nameTextField.setText(location.getName());
        deleteBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                int input = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this location?");
                if(input == 0) {
                    database.deleteLocation(location.getId());
                    int inputAlert = JOptionPane.showOptionDialog(null, "Location deleted", "Alert", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

                    if(inputAlert == JOptionPane.OK_OPTION) {
                        closeWindow();
                    }
                }
            }
        });
        saveBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                database.editLocation(new Location(location.getId(),nameTextField.getText(), Double.parseDouble(latitudeTextField.getText()), Double.parseDouble(longitudeTextField.getText())));
                closeWindow();
            }
        });
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                closeWindow();
            }
        });
    }

    private void closeWindow() {
        deleteBtn.getScene().getWindow().hide();
    }

    public void setController(MapController mapController) {
        this.mapController = mapController;
    }
}
