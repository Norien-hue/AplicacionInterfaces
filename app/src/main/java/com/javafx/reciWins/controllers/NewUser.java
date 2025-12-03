package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.javafx.model.Usuario;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.SQLstatementStorage;

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

    @FXML
    private Button btn_cancelar;
    
    @FXML
    private TextField nombreUsuario;
    
    @FXML
    private PasswordField contraseniaUsuario;
    
    @FXML
    private PasswordField repetirContraseniaUsuario;
    
    @FXML
    private RadioButton adminRoleUsuario;
    
    @FXML
    private RadioButton userRoleUsuario;
    
    @FXML
    private Button btn_aceptar;

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
            
            String hashContrasenia = "$2y$10$" + contrasenia.hashCode(); 
            
            SQLstatementStorage.storeStatement(
                "INSERT INTO Usuarios (Nombre, Hash_Contraseña, Permisos, Emisiones_Reducidas, TAP) VALUES ('" 
                + nombre + "', '" 
                + hashContrasenia + "', '" 
                + role + "', " 
                + "0, " 
                + "NULL)"
            );
            
            Usuario nuevoUsuario = new Usuario(
                0,
                0.0f,
                role,
                nombre,
                0
            );
            MainController.tablaUsuarioObservable.add(nuevoUsuario);
            
            ((Stage)btn_cancelar.getScene().getWindow()).close();
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
        
        if(nombreUsuario.getText().contains("@") || nombreUsuario.getText().contains("?") || 
           nombreUsuario.getText().contains("=") || nombreUsuario.getText().contains("'") || 
           nombreUsuario.getText().contains("\"") || nombreUsuario.getText().contains("|") || 
           nombreUsuario.getText().contains("&") || nombreUsuario.getText().contains("*") || 
           nombreUsuario.getText().contains("+") || nombreUsuario.getText().contains("\\") || 
           nombreUsuario.getText().strip().equals("")) {
            checkAlert = true;
            alertMessage = "El nombre contiene caracteres inválidos o está vacío";
            return;
        }
        
        if(contraseniaUsuario.getText().strip().equals("") || 
           repetirContraseniaUsuario.getText().strip().equals("")) {
            checkAlert = true;
            alertMessage = "Las contraseñas no pueden estar vacías";
            return;
        }
        
        if(!contraseniaUsuario.getText().equals(repetirContraseniaUsuario.getText())) {
            checkAlert = true;
            alertMessage = "Las contraseñas no coinciden";
            return;
        }
        
        if(contraseniaUsuario.getText().length() < 6) {
            checkAlert = true;
            alertMessage = "La contraseña debe tener al menos 6 caracteres";
            return;
        }
        
        if(!adminRoleUsuario.isSelected() && !userRoleUsuario.isSelected()) {
            checkAlert = true;
            alertMessage = "Debes seleccionar un rol (Admin o User)";
            return;
        }
    }

    private boolean launchAlertsNewUser() {
        checkForAlertNewUser();
        boolean ret = false;

        if(checkAlert) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Error en los campos");
            a.setContentText(alertMessage + "\n\nNo uses caracteres especiales: @, ?, =, ', \", |, *, &, +, \\");
            a.showAndWait();
            ret = true;
        }

        checkAlert = false;
        alertMessage = "";
        return ret;
    }
}