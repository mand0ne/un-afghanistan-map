package un.afghanistan.map.utility.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class LocationDAO {
    private static LocationDAO instance = null;
    private PreparedStatement getLocations, addLocation, editLocation;
    private Connection conn;

    private LocationDAO() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite::resource:un/afghanistan/map/database/database.db");
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
            String sqlUpit = "";
            while (ulaz.hasNext()) {
                sqlUpit += ulaz.nextLine();
                if ( sqlUpit.charAt( sqlUpit.length()-1 ) == ';') {
                    try {
                        Statement stmt = conn.createStatement();
                        stmt.execute(sqlUpit);
                        sqlUpit = "";
                    } catch (SQLException e) {
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
        getLocations = conn.prepareStatement("SELECT id, name, latitude, longitude FROM location");
        //addLocation  = conn.prepareStatement("INSERT INTO ...");
        //editLocation = conn.prepareStatement("UPDATE ...");
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
                Location location = new Location(result.getInt(1), result.getString(2), result.getDouble(3), result.getDouble(4));
                locations.add(location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }
}
