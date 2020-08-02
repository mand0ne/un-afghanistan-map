package un.afghanistan.map.utility.database;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import un.afghanistan.map.interfaces.UpdateMapInterface;
import un.afghanistan.map.models.Location;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class LocationDAO {
    private static LocationDAO instance = null;
    private PreparedStatement addLocation, editLocation, deleteLocation, getLocations, fetchLatestLocation, getSelectedLocation, addFile, editFile, fetchLocatinByLatLong;
    private Connection conn;
    private UpdateMapInterface updateMapInterface;

    public void setUpdateMapInterface(UpdateMapInterface updateMapInterface) {
        this.updateMapInterface = updateMapInterface;
    }

    private LocationDAO() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:database.db");
            prepareStatements();
            conn.prepareStatement("PRAGMA foreign_keys = ON").executeUpdate();
        } catch (SQLException e) {
            regenerateDatabase();
            try {
                prepareStatements();
                conn.prepareStatement("PRAGMA foreign_keys = ON").executeUpdate();
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
        getSelectedLocation = conn.prepareStatement("SELECT l.id, l.name, l.latitude, l.longitude, f.path FROM location l, file f WHERE l.latitude = ? AND l.longitude = ? AND l.id = f.point_id");
        addFile = conn.prepareStatement("INSERT INTO file (path, point_id) VALUES (?, ?)");
        editFile = conn.prepareStatement("UPDATE file SET PATH=? WHERE point_id=?");
        fetchLocatinByLatLong = conn.prepareStatement("SELECT id FROM location WHERE latitude = ? and longitude = ?");
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
                        result.getDouble(4), result.getString(5));
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

            if(!filePath.equals("")) {
                addFile.setString(1, filePath);
                addFile.setInt(2, latestId);
                addFile.executeUpdate();
            }

            updateMapInterface.onAddLocationRequest(new Location(latestId, name, latitude, longitude, filePath));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteLocation(Location location) {
        try {
            deleteLocation.setInt(1, location.getId());
            deleteLocation.executeUpdate();

            updateMapInterface.onDeleteLocationRequest(location);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editLocation(Location oldLocation, Location newLocation) {
        try {
            editLocation.setString(1, newLocation.getName());
            editLocation.setDouble(2, newLocation.getLongitude());
            editLocation.setDouble(3, newLocation.getLatitude());
            editLocation.setInt(4, newLocation.getId());
            editLocation.executeUpdate();

            editFile.setString(1, newLocation.getFilePath());
            editFile.setInt(2, newLocation.getId());
            editFile.executeUpdate();

            updateMapInterface.onDeleteLocationRequest(oldLocation);
            updateMapInterface.onAddLocationRequest(newLocation);
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

    }

    public Location getSelectedLocation(double lat, double lon) {
        Location location = new Location();

        try {
            getSelectedLocation.setDouble(1, lat);
            getSelectedLocation.setDouble(2, lon);
            ResultSet result = getSelectedLocation.executeQuery();
            while (result.next()) {
                location = new Location(result.getInt(1), result.getString(2), result.getDouble(3),
                        result.getDouble(4), result.getString(5));

                System.out.println(location.getId() + " " + location.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return location;
    }

    public boolean doesLocationExistInDatabase(double latitude, double longitude) {
        try {
            fetchLocatinByLatLong.setDouble(1, latitude);
            fetchLocatinByLatLong.setDouble(2, longitude);
            ResultSet result = fetchLocatinByLatLong.executeQuery();
            if(result.next())
                return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    private void setWindowsLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void unsetWindowsLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }


    public void loadDataFromFile() {
        setWindowsLook();
        JFrame parentFrame = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV file", "csv");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(parentFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String line = "";
            try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                br.readLine();
                while ((line = br.readLine()) != null) {
                    line = line.replace("\"", "");
                    String[] location = line.split("\\?");
                    System.out.println(location[0] + " " + location[1] + " " + location[2] + " " + location[3] + " " + location[4]);
                    if(!doesLocationExistInDatabase(Double.parseDouble(location[2]), Double.parseDouble(location[3]))) {
                        if(location.length == 5)
                            this.addLocation(location[1], Double.parseDouble(location[2]), Double.parseDouble(location[3]), location[4]);
                        else
                            this.addLocation(location[1], Double.parseDouble(location[2]), Double.parseDouble(location[3]), "");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveDataToFile() {
        setWindowsLook();
        JFrame parentFrame = new JFrame();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV file", "csv");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setDialogTitle("Specify a file to save");

        int userSelection = fileChooser.showSaveDialog(parentFrame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
                try {
                    // Execute query.
                    ResultSet results    = getLocations.executeQuery();

                    // Open CSV file.
                    BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileToSave.getAbsolutePath()));

                    // Add table headers to CSV file.
                    CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                            .withHeader(results.getMetaData()).withQuoteMode(QuoteMode.ALL).withDelimiter('?'));

                    // Add data rows to CSV file.
                    while (results.next()) {
                        csvPrinter.printRecord(
                                results.getInt(1),
                                results.getString(2),
                                results.getDouble(3),
                                results.getDouble(4),
                                results.getString(5));
                    }
                    // Close CSV file.
                    csvPrinter.flush();
                    csvPrinter.close();

                    // Message stating export successful.
                    System.out.println("Data export successful.");

                } catch (SQLException e) {

                    // Message stating export unsuccessful.
                    System.out.println("Data export unsuccessful.");
                    System.exit(0);

                } catch (IOException e) {

                    // Message stating export unsuccessful.
                    System.out.println("Data export unsuccessful.");
                    System.exit(0);

                }

        }

    }
}
