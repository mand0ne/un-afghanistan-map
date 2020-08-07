package un.afghanistan.map.gui;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import un.afghanistan.map.App;

public class BasemapListCell extends ListCell<String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
        setText(null);
        if (item != null) {
            try {
                ImageView imageView = new ImageView(new Image(App.class.getResourceAsStream("img/basemap-styles/" + item + ".png")));
                imageView.setFitWidth(60);
                imageView.setFitHeight(40);
                setGraphic(imageView);
                setText(item);
            } catch (Exception e) {
                System.out.println("EXCEPTION: " + e.getMessage() + ", caused by: " + item);
            }
        }
    }
}
