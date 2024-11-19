-- Table for folders
CREATE TABLE IF NOT EXISTS folders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL


);

-- Table for links
CREATE TABLE IF NOT EXISTS links (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     folder_id INTEGER,
     name TEXT NOT NULL,
     url TEXT NOT NULL,
     FOREIGN KEY(folder_id) REFERENCES folders(id)
    );