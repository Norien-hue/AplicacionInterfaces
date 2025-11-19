package com.javafx.reciWins.controllers;

import com.javafx.reciWins.start.StartWin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class SingUpController {

    @FXML
    private Button btn_cancelSingUp;

    @FXML
    private Button btn_singUp;

    @FXML
    private Label txt_loginAccount;

    @FXML
    void changeToLogin(MouseEvent event) {
        StartWin.mostrarLogin();
    }

    @FXML
    void changeToMain(ActionEvent event) {
        StartWin.mostrarMain();
    }

    @FXML
    void killApp(ActionEvent event) {
        ((Stage)this.btn_cancelSingUp.getScene().getWindow()).close();
    }

}
