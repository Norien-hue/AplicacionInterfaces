package com.javafx.reciWins.controllers;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.SQLstatementStorage;

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
    void cambiarContraseña(ActionEvent event) {
        if(validarCambioContraseña()) {
            String nuevaContrasenia = passwdNueva.getText();
            String hashNuevaContrasenia = "$2y$10$" + nuevaContrasenia.hashCode();
            
            SQLstatementStorage.storeStatement(
                "UPDATE Usuarios SET Hash_Contraseña = '" + hashNuevaContrasenia + 
                "' WHERE Id_Usuario = " + MainController.id_user
            );
            
            Alert exito = new Alert(AlertType.INFORMATION);
            exito.setHeaderText("Contraseña cambiada");
            exito.setContentText("La contraseña se cambiará cuando pulses 'Save' en la ventana principal.");
            exito.showAndWait();
            
            kill(event);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private boolean validarCambioContraseña() {
        // Verificar que la contraseña actual sea correcta
        try {
            String query = "SELECT Hash_Contraseña FROM Usuarios WHERE Id_Usuario = ?";
            PreparedStatement pst = StartWin.conn.prepareStatement(query);
            pst.setInt(1, MainController.id_user);
            ResultSet rs = pst.executeQuery();
            
            if(rs.next()) {
                String hashAlmacenado = rs.getString("Hash_Contraseña");
                String hashIngresado = "$2y$10$" + passwdActual.getText().hashCode();
                
                if(!hashAlmacenado.equals(hashIngresado)) {
                    mostrarError("Contraseña incorrecta", "La contraseña actual no es correcta.");
                    return false;
                }
            }
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No se pudo verificar la contraseña actual.");
            e.printStackTrace();
            return false;
        }
        
        // Validar nueva contraseña
        if(passwdNueva.getText().isEmpty() || passwdRepetida.getText().isEmpty()) {
            mostrarError("Campos vacíos", "Debes completar todos los campos.");
            return false;
        }
        
        if(!passwdNueva.getText().equals(passwdRepetida.getText())) {
            mostrarError("Contraseñas no coinciden", "La nueva contraseña y su repetición no coinciden.");
            return false;
        }
        
        if(passwdNueva.getText().length() < 6) {
            mostrarError("Contraseña muy corta", "La nueva contraseña debe tener al menos 6 caracteres.");
            return false;
        }
        
        if(passwdNueva.getText().contains("@") || passwdNueva.getText().contains("?") || 
           passwdNueva.getText().contains("=") || passwdNueva.getText().contains("'") || 
           passwdNueva.getText().contains("\"") || passwdNueva.getText().contains("|") || 
           passwdNueva.getText().contains("&") || passwdNueva.getText().contains("*") || 
           passwdNueva.getText().contains("+") || passwdNueva.getText().contains("\\")) {
            mostrarError("Caracteres inválidos", "La contraseña contiene caracteres inválidos (@, ?, =, ', \", |, &, *, +, \\)");
            return false;
        }
        
        return true;
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(AlertType.ERROR);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}