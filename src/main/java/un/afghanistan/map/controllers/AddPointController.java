package un.afghanistan.map.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import un.afghanistan.map.utility.database.LocationDAO;

import java.awt.*;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddPointController {
    public Button addBtn;
    public Button cancelBtn;
    public Button browseBtn;
    public TextField longitudeTextField;
    public TextField latitudeTextField;
    public TextField nameTextField;
    public TextField fileTextField;
    private MapController mapController;
    private LocationDAO database = LocationDAO.getInstance();

    private Stage primaryStage;
    final FileChooser fileChooser = new FileChooser();

    public AddPointController() {
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.PDF", "*.pdf"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx", "*.XLSX"));
        fileChooser.setTitle("Choose a file");
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void closeWindow() {
        addBtn.getScene().getWindow().hide();
    }

    public void addButtonAction() {
        database.addLocation(nameTextField.getText(), Double.parseDouble(latitudeTextField.getText()), Double.parseDouble(longitudeTextField.getText()), fileTextField.getText());
        closeWindow();
    }

    public void browseButtonAction() {
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            System.out.println(file.getName());
            fileTextField.setText(file.getAbsolutePath());
        }
    }

    private void openFile(File file) {
        try {
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    System.out.println("Awt Desktop is not supported!");
                }
            } else {
                System.out.println("File is not exists!");
            }
        } catch (IOException ex) {
            Logger.getLogger(AddPointController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cancelButtonAction() {
        closeWindow();
    }
}
