package com.javafx.reciWins.controllers;

import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.ApiClient;

import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
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
    private TextField nombreUsuario;

    @FXML
    private PasswordField contraseniaUsuario;

    @FXML
    void changeToMain(ActionEvent event) {
        if(validarLogin()) {
            StartWin.mostrarMain();
        }
    }

    @FXML
    void changeToSingUp(MouseEvent event) {
        StartWin.mostrarRegistro();
    }

    @FXML
    void killApp(ActionEvent event) {
        ((Stage)this.btn_cancelLogin.getScene().getWindow()).close();
    }

    private boolean validarLogin() {
        String nombre = nombreUsuario.getText().trim();
        String contrasenia = contraseniaUsuario.getText();

        if(nombre.isEmpty() || contrasenia.isEmpty()) {
            mostrarError("Campos vacios", "Por favor, ingresa tu nombre de usuario y contrasena.");
            return false;
        }

        try {
            ApiClient api = ApiClient.getInstance();
            JsonObject result = api.login(nombre, contrasenia);
            System.out.println("[LOGIN] Respuesta del servidor: " + result);

            // La API devuelve { "token": "...", "user": { "id": ..., "nombre": ... } }
            if (result.has("user") && result.getAsJsonObject("user").has("id")) {
                int idUsuario = result.getAsJsonObject("user").get("id").getAsInt();
                MainController.id_user = idUsuario;
                System.out.println("[LOGIN] Login exitoso. ID Usuario: " + idUsuario);
                return true;
            } else if (result.has("id")) {
                int idUsuario = result.get("id").getAsInt();
                MainController.id_user = idUsuario;
                System.out.println("[LOGIN] Login exitoso (formato alternativo). ID Usuario: " + idUsuario);
                return true;
            } else {
                System.err.println("[LOGIN] Respuesta inesperada: " + result);
                mostrarError("Error de login", "Respuesta inesperada del servidor.");
                return false;
            }

        } catch (Exception e) {
            System.err.println("[LOGIN] Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            String msg = e.getMessage();
            if (msg != null && (msg.contains("connection") || msg.contains("Connection") || msg.contains("timeout"))) {
                StartWin.manejarPerdidaConexion(msg);
            } else {
                mostrarError("Error de login", msg != null ? msg : "Error desconocido");
            }
            return false;
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(AlertType.ERROR);
        alerta.setOnShown(e -> {
                    Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
