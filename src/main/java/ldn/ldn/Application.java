package ldn.ldn;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("home-view.fxml"));
        Pane root = fxmlLoader.load();

        // Add custom styling for rounded corners
        root.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 30; -fx-border-radius: 30; -fx-border-width: 2; -fx-border-color: rgba(0, 0, 0, 0.2);");

        Scene scene = new Scene(root, 1400, 900); // Adjusted size for larger resolution
        scene.setFill(Color.TRANSPARENT); // Set scene fill to transparent

        // Enable dragging for the undecorated stage
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        stage.initStyle(StageStyle.TRANSPARENT); // Set stage style to transparent
        stage.initStyle(StageStyle.UNDECORATED); // Set stage style to undecorated
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}