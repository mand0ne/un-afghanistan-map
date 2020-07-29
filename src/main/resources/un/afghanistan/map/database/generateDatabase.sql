BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS location (
    id INTEGER,
    name TEXT,
    latitude REAL,
    longitude REAL,
    PRIMARY KEY(id)
);
CREATE TABLE IF NOT EXISTS file (
    id    INTEGER,
    point_id INTEGER,
    path TEXT,
    PRIMARY KEY(id),
    FOREIGN KEY(point_id) REFERENCES location(id) ON DELETE CASCADE
);
COMMIT;