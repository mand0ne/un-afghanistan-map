package un.afghanistan.map.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import un.afghanistan.map.utility.database.LocationDAO;

import java.io.Console;

public class AddPointControler {
    public Button addBtn;
    public Button cancelBtn;
    public TextField longitudeTextField;
    public TextField latitudeTextField;
    public TextField nameTextField;
    private MapController mapController;
    private LocationDAO database = LocationDAO.getInstance();

    private void closeWindow() {
        addBtn.getScene().getWindow().hide();
    }

    public void addButtonAction() {
        database.addLocation(nameTextField.getText(), Double.parseDouble(latitudeTextField.getText()), Double.parseDouble(longitudeTextField.getText()));
        closeWindow();
    }

    public void cancelButtonAction() {
        closeWindow();
    }
}
