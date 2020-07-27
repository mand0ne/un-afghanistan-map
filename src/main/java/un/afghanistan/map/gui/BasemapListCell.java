package un.afghanistan.map.gui;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BasemapListCell extends ListCell<String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);
        setText(null);
        if (item != null) {
            ImageView imageView = new ImageView(new Image("/un/afghanistan/map/img/basemap-styles/" + item + ".png"));
            imageView.setFitWidth(60);
            imageView.setFitHeight(40);
            setGraphic(imageView);
            setText(item);
        }
    }
}
