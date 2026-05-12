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

public class SingUpController {

    @FXML
    private Button btn_cancelSingUp;

    @FXML
    private Button btn_singUp;

    @FXML
    private Label txt_loginAccount;

    @FXML
    private TextField nombreSingUp;

    @FXML
    private PasswordField passwdSingUp;

    @FXML
    private PasswordField repPasswdSingUp;

    @FXML
    void changeToLogin(MouseEvent event) {
        StartWin.mostrarLogin();
    }

    @FXML
    void changeToMain(ActionEvent event) {
        if(!validarCampos()) {
            String nombre = nombreSingUp.getText().trim();
            String contrasenia = passwdSingUp.getText();

            try {
                ApiClient api = ApiClient.getInstance();
                JsonObject result = api.register(nombre, contrasenia);

                int nuevoId = -1;
                if (result.has("id")) {
                    nuevoId = result.get("id").getAsInt();
                }

                Alert success = new Alert(AlertType.INFORMATION);
                success.setOnShown(ex -> {
                    Stage stage = (Stage) success.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                success.setHeaderText("Registro exitoso");
                success.setContentText("Bienvenido " + nombre + "! Tu cuenta ha sido creada exitosamente."
                        + (nuevoId > 0 ? "\nID de usuario: " + nuevoId : ""));
                success.showAndWait();

                StartWin.mostrarLogin();

            } catch (Exception e) {
                Alert error = new Alert(AlertType.ERROR);
                error.setOnShown(ex -> {
                    Stage stage = (Stage) error.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                error.setHeaderText("Error al registrar usuario");
                error.setContentText("Ocurrio un error al crear la cuenta: " + e.getMessage());
                error.showAndWait();
                e.printStackTrace();
            }
        }
    }

    @FXML
    void killApp(ActionEvent event) {
        ((Stage)this.btn_cancelSingUp.getScene().getWindow()).close();
    }

    private boolean validarCampos() {
        boolean hayError = false;
        String mensaje = "";

        if(nombreSingUp.getText().trim().isEmpty()) {
            hayError = true;
            mensaje = "El nombre de usuario no puede estar vacio";
        }
        else if(nombreSingUp.getText().contains("@") || nombreSingUp.getText().contains("?") ||
           nombreSingUp.getText().contains("=") || nombreSingUp.getText().contains("'") ||
           nombreSingUp.getText().contains("\"") || nombreSingUp.getText().contains("|") ||
           nombreSingUp.getText().contains("&") || nombreSingUp.getText().contains("*") ||
           nombreSingUp.getText().contains("+") || nombreSingUp.getText().contains("\\") ||
           nombreSingUp.getText().contains(" ")) {
            hayError = true;
            mensaje = "El nombre de usuario no puede contener espacios ni caracteres especiales: @, ?, =, ', \", |, *, &, +, \\";
        }
        else if(passwdSingUp.getText().isEmpty() || repPasswdSingUp.getText().isEmpty()) {
            hayError = true;
            mensaje = "Las contrasenas no pueden estar vacias";
        }
        else if(!passwdSingUp.getText().equals(repPasswdSingUp.getText())) {
            hayError = true;
            mensaje = "Las contrasenas no coinciden. Por favor, verifica que ambas contrasenas sean iguales.";
        }
        else if(passwdSingUp.getText().contains("@") || passwdSingUp.getText().contains("?") ||
                passwdSingUp.getText().contains("=") || passwdSingUp.getText().contains("'") ||
                passwdSingUp.getText().contains("\"") || passwdSingUp.getText().contains("|") ||
                passwdSingUp.getText().contains("&") || passwdSingUp.getText().contains("*") ||
                passwdSingUp.getText().contains("+") || passwdSingUp.getText().contains("\\")) {
            hayError = true;
            mensaje = "La contrasena contiene caracteres especiales no permitidos: @, ?, =, ', \", |, *, &, +, \\\n\n" +
                      "Por seguridad, evita estos caracteres en tu contrasena.";
        }
        else if(passwdSingUp.getText().length() < 6) {
            hayError = true;
            mensaje = "La contrasena debe tener al menos 6 caracteres";
        }

        if(hayError) {
            Alert a = new Alert(AlertType.ERROR);
            a.setOnShown(ex -> {
                    Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
            a.setHeaderText("Error en el registro");
            a.setContentText(mensaje);
            a.showAndWait();
        }

        return hayError;
    }
}
