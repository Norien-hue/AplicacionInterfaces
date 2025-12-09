package com.javafx.reciWins.controllers;

import com.javafx.reciWins.start.StartWin;

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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            mostrarError("Campos vacíos", "Por favor, ingresa tu nombre de usuario y contraseña.");
            return false;
        }
        
        try {
            String query = "SELECT Id_Usuario, Hash_Contraseña FROM Usuarios WHERE Nombre = ?";
            PreparedStatement pst = StartWin.conn.prepareStatement(query);
            pst.setString(1, nombre);
            
            ResultSet rs = pst.executeQuery();
            
            if(rs.next()) {
                String hashAlmacenado = rs.getString("Hash_Contraseña");
                int idUsuario = rs.getInt("Id_Usuario");
                
                String hashIngresado = "$2y$10$" + contrasenia.hashCode();
                
                if(hashAlmacenado.equals(hashIngresado)) {
                    MainController.id_user = idUsuario;
                    System.out.println("Login exitoso. ID Usuario: " + idUsuario);
                    return true;
                } else {
                    mostrarError("Contraseña incorrecta", "La contraseña ingresada no es correcta.");
                    return false;
                }
            } else {
                mostrarError("Usuario no encontrado", "No existe un usuario con ese nombre.");
                return false;
            }
            
        } catch (SQLException e) {
            mostrarError("Error de conexión", "No se pudo conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(AlertType.ERROR);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}