package com.splitup.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class MainApp extends Application {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("SplitUp");
        stage.setMinWidth(900);
        stage.setMinHeight(620);

        showLogin();
        stage.show();
        log.info("SplitUp Desktop iniciado");
    }

    public static void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/com/splitup/desktop/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 480, 520);
        scene.getStylesheets().add(
                Objects.requireNonNull(MainApp.class.getResource(
                        "/com/splitup/desktop/styles/app.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setWidth(480);
        primaryStage.setHeight(520);
        primaryStage.centerOnScreen();
    }

    public static void showMain() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/com/splitup/desktop/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1050, 680);
        scene.getStylesheets().add(
                Objects.requireNonNull(MainApp.class.getResource(
                        "/com/splitup/desktop/styles/app.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setWidth(1050);
        primaryStage.setHeight(680);
        primaryStage.centerOnScreen();
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    public static void main(String[] args) {
        launch(args);
    }
}
