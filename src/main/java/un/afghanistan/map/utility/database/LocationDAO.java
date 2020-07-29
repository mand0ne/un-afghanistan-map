package un.afghanistan.map.utility.database;

import un.afghanistan.map.interfaces.UpdateMapInterface;
import un.afghanistan.map.models.Location;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class LocationDAO {
    private static LocationDAO instance = null;
    private PreparedStatement addLocation, editLocation, deleteLocation, getLocations, fetchLatestLocation, getSelectedLocation, addFile, editFile;
    private Connection conn;
    private UpdateMapInterface updateMapInterface;

    public void setUpdateMapInterface(UpdateMapInterface updateMapInterface) {
        this.updateMapInterface = updateMapInterface;
    }

    private LocationDAO() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            prepareStatements();
        } catch (SQLException e) {
            regenerateDatabase();
            try {
                prepareStatements();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void regenerateDatabase() {
        Scanner ulaz = null;
        try {
            ulaz = new Scanner(new FileInputStream("src/main/resources/un/afghanistan/map/database/generateDatabase.sql"));
            StringBuilder sqlUpit = new StringBuilder("");
            while (ulaz.hasNext()) {
                sqlUpit.append(ulaz.nextLine());
                if (sqlUpit.charAt(sqlUpit.length() - 1) == ';') {
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.execute(sqlUpit.toString());
                        sqlUpit = new StringBuilder("");
                    } catch (SQLException e) {
                        System.out.println(conn == null);
                        e.printStackTrace();
                    }
                }
            }
            ulaz.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void prepareStatements() throws SQLException {
        getLocations = conn.prepareStatement("SELECT l.id, l.name, l.latitude, l.longitude, f.path FROM location l, file f WHERE l.id = f.point_id");
        addLocation = conn.prepareStatement("INSERT INTO location (name, latitude, longitude) VALUES (?,?,?);");
        editLocation = conn.prepareStatement("UPDATE location set name=?, longitude=?, latitude=? where id = ?");
        deleteLocation = conn.prepareStatement("DELETE FROM location WHERE id = ?");
        fetchLatestLocation = conn.prepareStatement("SELECT max(id) FROM location");
        getSelectedLocation = conn.prepareStatement("SELECT * FROM location WHERE latitude = ? AND longitude = ?");

        addFile = conn.prepareStatement("INSERT INTO file (path, point_id) VALUES (?, ?)");
        editFile = conn.prepareStatement("UPDATE file SET PATH=? WHERE point_id=?");
    }

    public static LocationDAO getInstance() {
        if (instance == null) instance = new LocationDAO();
        return instance;
    }

    public static void removeInstance() {
        if (instance != null) {
            try {
                instance.conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        instance = null;
    }

    public ArrayList<Location> getLocations() {
        ArrayList<Location> locations = new ArrayList<>();

        try {
            ResultSet result = getLocations.executeQuery();
            while (result.next()) {
                Location location = new Location(result.getInt(1), result.getString(2), result.getDouble(3),
                        result.getDouble(4), "");
                System.out.println(location.getId() + " " + location.getName());
                locations.add(location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }

    public void addLocation(String name, double latitude, double longitude, String filePath) {
        try {
            addLocation.setString(1, name);
            addLocation.setDouble(2, latitude);
            addLocation.setDouble(3, longitude);
            addLocation.executeUpdate();

            ResultSet result = fetchLatestLocation.executeQuery();
            int latestId = result.getInt(1);
            while (result.next())
                latestId = result.getInt(1);

            addFile.setString(1, filePath);
            addFile.setInt(2, latestId);
            addFile.executeUpdate();

            updateMapInterface.onMapUpdateRequest(new Location(latestId, name, latitude, longitude, filePath));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteLocation(int id) {
        try {
            deleteLocation.setInt(1, id);
            deleteLocation.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editLocation(Location location) {
        try {
            editLocation.setString(1, location.getName());
            editLocation.setDouble(2, location.getLongitude());
            editLocation.setDouble(3, location.getLatitude());
            editLocation.setInt(4, location.getId());
            editLocation.executeUpdate();

            editFile.setString(1, location.getFilePath());
            editFile.setInt(2, location.getId());
            editFile.executeUpdate();

            updateMapInterface.onMapUpdateRequest(new Location(location.getId(), location.getName(), location.getLatitude(), location.getLongitude(), location.getFilePath()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public Location getGetSelectedLocation(double lat, double lon) {
        Location location = new Location();

        try {
            getSelectedLocation.setDouble(1, lat);
            getSelectedLocation.setDouble(2, lon);
            ResultSet result = getSelectedLocation.executeQuery();
            while (result.next()) {
                location = new Location(result.getInt(1), result.getString(2), result.getDouble(3),
                        result.getDouble(4), "");

                System.out.println(location.getId() + " " + location.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return location;
    }
}
