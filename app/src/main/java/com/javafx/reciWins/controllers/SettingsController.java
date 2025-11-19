package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.javafx.reciWins.start.StartWin;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class SettingsController implements Initializable{

    @FXML
    private Button btn_accept;

    @FXML
    private Button btn_cambio;

    @FXML
    private Button btn_cancel;

    @FXML
    private Button btn_passwd;

    @FXML
    private TextField txtFld_nombre;

    @FXML
    void launch_cambiarPwd(ActionEvent event) {
        StartWin.lanzarCambioContraseña();
    }

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_passwd.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            ((Stage)btn_passwd.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

}
