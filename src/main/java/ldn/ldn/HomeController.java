package ldn.ldn;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HomeController {

    @FXML
    private TreeView<String> folderTreeView;
    @FXML
    private ListView<String> linksListView;
    @FXML
    private WebView webView;
    @FXML
    private MediaView mediaView;
    @FXML
    private DatabaseHandler dbHandler;
    @FXML
    private Map<String, Integer> folderIdMap; // Map to store folder names and their IDs
    @FXML
    private WebEngine webEngine;
    @FXML
    private TextField folderSearchBar;
    @FXML
    private TextField urlBar;

    public void initialize() {
        dbHandler = new DatabaseHandler();
        folderIdMap = new HashMap<>();
        loadFolders();
        setupListeners();
        String videoPath = new File("src/main/resources/16524409-uhd_1920_1440_60fps.mp4").toURI().toString();
        Media media = new Media(videoPath);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.setPreserveRatio(true);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.play();
        webEngine = webView.getEngine();
        webEngine.load("https://www.apple.com");
        //folderSearchBar.textProperty().addListener((observable, oldValue, newValue) -> filterFolders(newValue));
        if (linksListView == null) {
            System.err.println("linksListView is null! Check FXML connection.");
        }
    }

    private void loadFolders() {
        folderTreeView.setRoot(new TreeItem<>("Folders")); // Root of the TreeView
        folderTreeView.getRoot().setExpanded(true);

        try {
            // Load folders from the database
            var folders = dbHandler.getFolders(); // Method to fetch all folders
            for (var folder : folders) {
                folderIdMap.put(folder.getName(), folder.getId());
                TreeItem<String> treeItem = new TreeItem<>(folder.getName());
                folderTreeView.getRoot().getChildren().add(treeItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLinks(int folderId) {
        linksListView.getItems().clear();
        try {
            var links = dbHandler.getLinksByFolderId(folderId); // Method to fetch links by folder ID
            for (var link : links) {
                linksListView.getItems().add(link.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        // Listener for folder selection in the TreeView
        folderTreeView.setOnMouseClicked((MouseEvent event) -> {
            TreeItem<String> selectedItem = folderTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && folderIdMap.containsKey(selectedItem.getValue())) {
                int folderId = folderIdMap.get(selectedItem.getValue());
                loadLinks(folderId);
            }
        });

        // Listener for link selection in the ListView
        linksListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String selectedLink = linksListView.getSelectionModel().getSelectedItem();
                if (selectedLink != null) {
                    String url = dbHandler.getLinkUrl(selectedLink);
                    webView.getEngine().load(url);
                }
            }
        });
    }

    @FXML
    private void handleQuit() {
        // Close the application
        System.exit(0);
    }

    @FXML
    //handleAddLinks
    private void handleAddLinks() {
        // Import links from a file
        String filePath = "src/main/resources/links.txt";
        String folderName = "Bookmarks";
        dbHandler.importLinksFromFile(filePath, folderName);
        loadFolders();
    }

    @FXML
    private void navigateBack() {
        if (webEngine.getHistory().getCurrentIndex() > 0) {
            webEngine.getHistory().go(-1);
        }
    }

    @FXML
    private void navigateForward() {
        if (webEngine.getHistory().getCurrentIndex() < webEngine.getHistory().getEntries().size() - 1) {
            webEngine.getHistory().go(1);
        }
    }

    @FXML
    private void refreshPage() {
        webEngine.reload();
    }

    @FXML
    private void filterFolders(String query) {
        // Implement logic to filter folderTreeView items based on `query`
        System.out.println("Filter folders with query: " + query);
    }

    @FXML
    private void loadUrl() {
        webEngine.load(urlBar.getText());
    }
}