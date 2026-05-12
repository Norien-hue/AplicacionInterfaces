package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.ApiClient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class ChangePasswd implements Initializable {

    @FXML
    private Button btn_cancelar;

    @FXML
    private Button btn_aceptar;

    @FXML
    private PasswordField passwdActual;

    @FXML
    private PasswordField passwdNueva;

    @FXML
    private PasswordField passwdRepetida;

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void cambiarContrasena(ActionEvent event) {
        if(validarCambioContrasena()) {
            String contrasenaActual = passwdActual.getText();
            String nuevaContrasena = passwdNueva.getText();

            try {
                ApiClient api = ApiClient.getInstance();
                api.updatePassword(MainController.id_user, contrasenaActual, nuevaContrasena);

                Alert exito = new Alert(AlertType.INFORMATION);
                exito.setOnShown(e -> {
                    Stage stage = (Stage) exito.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                exito.setHeaderText("Contrasena cambiada");
                exito.setContentText("La contrasena se ha cambiado correctamente.");
                exito.showAndWait();

                kill(event);

            } catch (Exception e) {
                String errorMsg = e.getMessage();
                // La API devuelve mensajes descriptivos para contrasena incorrecta
                if (errorMsg != null && (errorMsg.contains("incorrecta") || errorMsg.contains("incorrect") || errorMsg.contains("wrong"))) {
                    mostrarError("Contrasena incorrecta", "La contrasena actual no es correcta.");
                } else {
                    mostrarError("Error al cambiar contrasena", "No se pudo cambiar la contrasena: " + errorMsg);
                }
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private boolean validarCambioContrasena() {
        if(passwdActual.getText().isEmpty()) {
            mostrarError("Campo vacio", "Debes introducir tu contrasena actual.");
            return false;
        }

        if(passwdNueva.getText().isEmpty() || passwdRepetida.getText().isEmpty()) {
            mostrarError("Campos vacios", "Debes completar todos los campos.");
            return false;
        }

        if(!passwdNueva.getText().equals(passwdRepetida.getText())) {
            mostrarError("Contrasenas no coinciden", "La nueva contrasena y su repeticion no coinciden.");
            return false;
        }

        if(passwdNueva.getText().length() < 6) {
            mostrarError("Contrasena muy corta", "La nueva contrasena debe tener al menos 6 caracteres.");
            return false;
        }

        if(passwdNueva.getText().contains("@") || passwdNueva.getText().contains("?") ||
           passwdNueva.getText().contains("=") || passwdNueva.getText().contains("'") ||
           passwdNueva.getText().contains("\"") || passwdNueva.getText().contains("|") ||
           passwdNueva.getText().contains("&") || passwdNueva.getText().contains("*") ||
           passwdNueva.getText().contains("+") || passwdNueva.getText().contains("\\")) {
            mostrarError("Caracteres invalidos", "La contrasena contiene caracteres invalidos (@, ?, =, ', \", |, &, *, +, \\)");
            return false;
        }

        return true;
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert a = new Alert(AlertType.ERROR);
        a.setOnShown(e -> {
            Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
            stage.getIcons().add(StartWin.icon);
        });
        a.setHeaderText(titulo);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
