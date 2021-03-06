package un.afghanistan.map.models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

public class Location {
    private SimpleIntegerProperty id = new SimpleIntegerProperty(0);
    private SimpleStringProperty name = new SimpleStringProperty("");
    private SimpleDoubleProperty latitude = new SimpleDoubleProperty(0);
    private SimpleDoubleProperty longitude = new SimpleDoubleProperty(0);
    private SimpleStringProperty filePath = new SimpleStringProperty("");
    private SimpleBooleanProperty isInKabul = new SimpleBooleanProperty(false);

    public Location() {
    }

    public Location(int id, String name, double latitude, double longitude, String filePath, boolean isInKabul) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.latitude = new SimpleDoubleProperty(latitude);
        this.longitude = new SimpleDoubleProperty(longitude);
        this.filePath = new SimpleStringProperty(filePath);
        this.isInKabul = new SimpleBooleanProperty(isInKabul);
    }

    public int getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public double getLatitude() {
        return latitude.get();
    }

    public void setLatitude(double latitude) {
        this.latitude.set(latitude);
    }

    public double getLongitude() {
        return longitude.get();
    }

    public String getFilePath() {
        return filePath.get();
    }

    public boolean isInKabul() {
        return isInKabul.get();
    }

    public SimpleBooleanProperty isInKabulProperty() {
        return isInKabul;
    }

    @Override
    public String toString() {
        return getLatitude() + " " + getLongitude();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return getId() == location.getId() &&
                getName().equals(location.getName()) &&
                getLatitude() == location.getLatitude() &&
                getLongitude() == location.getLongitude() &&
                isInKabul() == location.isInKabul();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getLatitude(), getLongitude(), isInKabulProperty());
    }
}
