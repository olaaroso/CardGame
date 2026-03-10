package com.example.cardgame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(main.class.getResource("ui.fxml"));
        Scene scene = new Scene(loader.load(), 550, 350);

        primaryStage.setTitle("Card Game - 24");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}