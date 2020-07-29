package un.afghanistan.map.interfaces;

import un.afghanistan.map.models.Location;

public interface UpdateMapInterface {
    void onAddLocationRequest(Location location);
    void onDeleteLocationRequest(Location location);
}
