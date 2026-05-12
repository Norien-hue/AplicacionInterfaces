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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class NewUser implements Initializable {

    @FXML private Button btn_cancelar;
    @FXML private TextField nombreUsuario;
    @FXML private PasswordField contraseniaUsuario;
    @FXML private PasswordField repetirContraseniaUsuario;
    @FXML private RadioButton adminRoleUsuario;
    @FXML private RadioButton userRoleUsuario;
    @FXML private Button btn_aceptar;

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void crearUsuario(ActionEvent event) {
        if(!launchAlertsNewUser()) {

            String nombre = nombreUsuario.getText().trim();
            String contrasenia = contraseniaUsuario.getText();
            String role = userRoleUsuario.isSelected() ? "cliente" : "administrador";

            try {
                ApiClient api = ApiClient.getInstance();
                api.createUsuario(nombre, contrasenia, role);

                MainController.actualizarVistasDesdeExterno();

                ((Stage)btn_cancelar.getScene().getWindow()).close();
            } catch (Exception e) {
                mostrarAlerta(AlertType.ERROR, "Error al crear", "No se pudo crear el usuario: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userRoleUsuario.setSelected(true);

        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private static boolean checkAlert = false;
    private static String alertMessage = "";

    private void checkForAlertNewUser() {
        alertMessage = "";

        if(nombreUsuario.getText().trim().isEmpty()) {
            checkAlert = true; alertMessage = "El nombre de usuario no puede estar vacio."; return;
        }
        if(nombreUsuario.getText().contains("@") || nombreUsuario.getText().contains("?") ||
           nombreUsuario.getText().contains("=") || nombreUsuario.getText().contains("'") ||
           nombreUsuario.getText().contains("\"") || nombreUsuario.getText().contains("|") ||
           nombreUsuario.getText().contains("&") || nombreUsuario.getText().contains("*") ||
           nombreUsuario.getText().contains("+") || nombreUsuario.getText().contains("\\")) {
            checkAlert = true; alertMessage = "El nombre contiene caracteres invalidos"; return;
        }
        if(!contraseniaUsuario.getText().equals(repetirContraseniaUsuario.getText())) {
            checkAlert = true; alertMessage = "Las contrasenas no coinciden."; return;
        }
        if(contraseniaUsuario.getText().isEmpty() || repetirContraseniaUsuario.getText().isEmpty()) {
            checkAlert = true; alertMessage = "Las contrasenas no pueden estar vacias."; return;
        }
        if(contraseniaUsuario.getText().length() < 6) {
            checkAlert = true; alertMessage = "La contrasena debe tener al menos 6 caracteres."; return;
        }
        if(!adminRoleUsuario.isSelected() && !userRoleUsuario.isSelected()) {
            checkAlert = true; alertMessage = "Debes seleccionar un rol (Admin o User)."; return;
        }
    }

    private boolean launchAlertsNewUser() {
        checkForAlertNewUser();
        boolean ret = false;
        if(checkAlert) {
            mostrarAlerta(AlertType.ERROR, "Error en los campos", alertMessage);
            ret = true;
        }
        checkAlert = false;
        alertMessage = "";
        return ret;
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setOnShown(ex -> {
            Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
            stage.getIcons().add(StartWin.icon);
        });
        a.setHeaderText(titulo);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
