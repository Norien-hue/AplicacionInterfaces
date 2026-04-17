package com.javafx.reciWins.controllers;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.BCryptUtils;

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
            String hashNuevaContrasenia = BCryptUtils.hash(nuevaContrasenia);

            try {
                PreparedStatement pst = StartWin.conn.prepareStatement(
                    "UPDATE Usuarios SET Hash_Contraseña = ? WHERE Id_Usuario = ?"
                );
                pst.setString(1, hashNuevaContrasenia);
                pst.setInt(2, MainController.id_user);
                pst.executeUpdate();
                
                Alert exito = new Alert(AlertType.INFORMATION);
                exito.setOnShown(e -> {
                    Stage stage = (Stage) exito.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                exito.setHeaderText("Contraseña cambiada");
                exito.setContentText("La contraseña se ha cambiado correctamente.");
                exito.showAndWait();

                
                
                kill(event);
            } catch (Exception e) {
                Alert error = new Alert(AlertType.ERROR);
                error.setOnShown(ex -> {
                    Stage stage = (Stage) error.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                error.setHeaderText("Error al cambiar contraseña");
                error.setContentText("No se pudo cambiar la contraseña: " + e.getMessage());
                error.showAndWait();
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

    private boolean validarCambioContraseña() {
        try {
            String query = "SELECT Hash_Contraseña FROM Usuarios WHERE Id_Usuario = ?";
            PreparedStatement pst = StartWin.conn.prepareStatement(query);
            pst.setInt(1, MainController.id_user);
            ResultSet rs = pst.executeQuery();
            
            if(rs.next()) {
                String hashAlmacenado = rs.getString("Hash_Contraseña");

                if(!BCryptUtils.verify(passwdActual.getText(), hashAlmacenado)) {
                    mostrarError("Contraseña incorrecta", "La contraseña actual no es correcta.");
                    return false;
                }
            }
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No se pudo verificar la contraseña actual.");
            e.printStackTrace();
            return false;
        }
        
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