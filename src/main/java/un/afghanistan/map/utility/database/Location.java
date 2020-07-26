package un.afghanistan.map.utility.database;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Location {
    private SimpleIntegerProperty id       = new SimpleIntegerProperty(0);
    private SimpleStringProperty name      = new SimpleStringProperty("");
    private SimpleDoubleProperty latitude  = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty longitude = new SimpleDoubleProperty(0);

    public Location() {}

    public Location(int id, String name, double latitude, double longitude) {
        this.id        = new SimpleIntegerProperty(id);
        this.name      = new SimpleStringProperty(name);
        this.latitude  = new SimpleDoubleProperty(latitude);
        this.longitude = new SimpleDoubleProperty(longitude);
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public double getLatitude() {
        return latitude.get();
    }

    public SimpleDoubleProperty latitudeProperty() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude.set(latitude);
    }

    public double getLongitude() {
        return longitude.get();
    }

    public SimpleDoubleProperty longitudeProperty() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude.set(longitude);
    }
}
