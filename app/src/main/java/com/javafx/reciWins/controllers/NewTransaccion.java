package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.javafx.reciWins.start.StartWin;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class NewTransaccion implements Initializable{

    @FXML
    private Button btn_cancelar;

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

}
