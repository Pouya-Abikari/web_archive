package ldn.ldn;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.net.URL;

public class DatabaseHandler {
    private static final String URL = "jdbc:sqlite:src/main/resources/data.db";
    private Connection connection;

    public DatabaseHandler() {
        connect();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL);
            System.out.println("Connected to SQLite database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLinkUrl(String selectedLink) {
        String query = "SELECT url FROM links WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, selectedLink);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("url");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addFolder(String folderName) {
        String query = "INSERT INTO folders (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, folderName);
            pstmt.executeUpdate();
            System.out.println("Folder added: " + folderName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addLink(String folderName, String linkName, String url) {
        String getFolderIdQuery = "SELECT id FROM folders WHERE name = ?";
        String addLinkQuery = "INSERT INTO links (folder_id, name, url) VALUES (?, ?, ?)";

        try (PreparedStatement folderStmt = connection.prepareStatement(getFolderIdQuery);
             PreparedStatement linkStmt = connection.prepareStatement(addLinkQuery)) {

            folderStmt.setString(1, folderName);
            ResultSet rs = folderStmt.executeQuery();

            if (rs.next()) {
                int folderId = rs.getInt("id");

                linkStmt.setInt(1, folderId);
                linkStmt.setString(2, linkName);
                linkStmt.setString(3, url);
                linkStmt.executeUpdate();

                System.out.println("Link added: " + linkName + " (" + url + ")");
            } else {
                System.out.println("Folder not found: " + folderName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFolder(String folderName) {
        String deleteFolderQuery = "DELETE FROM folders WHERE name = ?";
        String deleteLinksQuery = "DELETE FROM links WHERE folder_id IN (SELECT id FROM folders WHERE name = ?)";

        try (PreparedStatement linksStmt = connection.prepareStatement(deleteLinksQuery);
             PreparedStatement folderStmt = connection.prepareStatement(deleteFolderQuery)) {

            linksStmt.setString(1, folderName);
            linksStmt.executeUpdate();

            folderStmt.setString(1, folderName);
            folderStmt.executeUpdate();

            System.out.println("Folder and associated links deleted: " + folderName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteLink(String linkName) {
        String query = "DELETE FROM links WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, linkName);
            pstmt.executeUpdate();
            System.out.println("Link deleted: " + linkName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLink(String oldLinkName, String newLinkName, String newUrl) {
        String query = "UPDATE links SET name = ?, url = ? WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newLinkName);
            pstmt.setString(2, newUrl);
            pstmt.setString(3, oldLinkName);
            pstmt.executeUpdate();
            System.out.println("Link updated: " + oldLinkName + " to " + newLinkName + " (" + newUrl + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Folder> getFolders() {
        List<Folder> folders = new ArrayList<>();
        String query = "SELECT id, name FROM folders";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                folders.add(new Folder(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return folders;
    }

    public List<Link> getLinksByFolderId(int folderId) {
        List<Link> links = new ArrayList<>();
        String query = "SELECT name FROM links WHERE folder_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, folderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                links.add(new Link(rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return links;
    }

    public void importLinksFromFile(String filePath, String folderName) {
        // Ensure the folder exists or create it
        int folderId = ensureFolderExists(folderName);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String link;
            while ((link = reader.readLine()) != null) {
                String websiteName = fetchWebsiteTitle(link);
                if (websiteName != null) {
                    addLink(folderName, websiteName, link); // Add link to the database
                    System.out.println("Added: " + websiteName + " (" + link + ")");
                } else {
                    System.out.println("Failed to fetch name for: " + link);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int ensureFolderExists(String folderName) {
        String getFolderIdQuery = "SELECT id FROM folders WHERE name = ?";
        String insertFolderQuery = "INSERT INTO folders (name) VALUES (?)";

        try (PreparedStatement getFolderStmt = connection.prepareStatement(getFolderIdQuery)) {
            getFolderStmt.setString(1, folderName);
            ResultSet rs = getFolderStmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id"); // Folder already exists, return its ID
            } else {
                // Folder doesn't exist, create it
                try (PreparedStatement insertFolderStmt = connection.prepareStatement(insertFolderQuery)) {
                    insertFolderStmt.setString(1, folderName);
                    insertFolderStmt.executeUpdate();
                    System.out.println("Created folder: " + folderName);
                    return ensureFolderExists(folderName); // Recursive call to get the new folder's ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Indicate failure
    }

    private String fetchWebsiteTitle(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 seconds timeout
            connection.setReadTimeout(5000);

            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                StringBuilder htmlContent = new StringBuilder();
                while (scanner.hasNextLine()) {
                    htmlContent.append(scanner.nextLine());
                }
                return parseTitle(htmlContent.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if the title couldn't be fetched
    }

    private String parseTitle(String html) {
        // Extract <title> content from HTML
        int titleStart = html.indexOf("<title>");
        int titleEnd = html.indexOf("</title>");
        if (titleStart != -1 && titleEnd != -1) {
            return html.substring(titleStart + 7, titleEnd).trim();
        }
        return "Unknown Title";
    }

}