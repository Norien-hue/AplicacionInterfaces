package com.javafx.reciWins.controllers;

import com.javafx.reciWins.start.StartWin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private Button btn_cancelLogin;

    @FXML
    private Button btn_login;

    @FXML
    private Label txt_createAccount;

    @FXML
    void changeToMain(MouseEvent event) {
        StartWin.mostrarMain();
    }

    @FXML
    void changeToSingUp(MouseEvent event) {
        StartWin.mostrarRegistro();
    }

    @FXML
    void killApp(ActionEvent event) {
        ((Stage)this.btn_cancelLogin.getScene().getWindow()).close();
    }

}
