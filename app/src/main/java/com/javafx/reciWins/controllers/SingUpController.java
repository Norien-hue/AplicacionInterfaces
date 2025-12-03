package com.javafx.reciWins.controllers;

import com.javafx.model.Usuario;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
            
            // Verificar si el usuario ya existe
            if(usuarioExiste(nombre)) {
                Alert error = new Alert(AlertType.ERROR);
                error.setHeaderText("Usuario ya existe");
                error.setContentText("El nombre de usuario '" + nombre + "' ya está registrado. Por favor, elige otro.");
                error.showAndWait();
                return;
            }
            
            // Hashear la contraseña (simulación básica)
            // En producción usa: BCrypt.hashpw(contrasenia, BCrypt.gensalt())
            String hashContrasenia = "$2y$10$" + contrasenia.hashCode();
            
            // Por defecto los nuevos usuarios son "cliente"
            String role = "cliente";
            
            String insertQuery = "INSERT INTO Usuarios (Nombre, Hash_Contraseña, Permisos, Emisiones_Reducidas, TAP) VALUES ('" 
                + nombre + "', '" 
                + hashContrasenia + "', '" 
                + role + "', " 
                + "0, " 
                + "NULL)";
            
            try {
                // Ejecutar el INSERT
                Statement stmt = StartWin.conn.createStatement();
                stmt.executeUpdate(insertQuery, Statement.RETURN_GENERATED_KEYS);
                
                // Recuperar el ID autogenerado
                ResultSet rs = stmt.getGeneratedKeys();
                if(rs.next()) {
                    int nuevoId = rs.getInt(1);
                    
                    // Mostrar mensaje de éxito
                    Alert success = new Alert(AlertType.INFORMATION);
                    success.setHeaderText("Registro exitoso");
                    success.setContentText("¡Bienvenido " + nombre + "! Tu cuenta ha sido creada exitosamente.\nID de usuario: " + nuevoId);
                    success.showAndWait();
                    
                    // Cambiar a la pantalla de login
                    StartWin.mostrarLogin();
                } else {
                    throw new SQLException("No se pudo obtener el ID generado");
                }
                
            } catch (SQLException e) {
                Alert error = new Alert(AlertType.ERROR);
                error.setHeaderText("Error al registrar usuario");
                error.setContentText("Ocurrió un error al crear la cuenta: " + e.getMessage());
                error.showAndWait();
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    void killApp(ActionEvent event) {
        ((Stage)this.btn_cancelSingUp.getScene().getWindow()).close();
    }
    
    // Validar que los campos sean correctos
    private boolean validarCampos() {
        boolean hayError = false;
        String mensaje = "";
        
        // Validar que el nombre no esté vacío
        if(nombreSingUp.getText().trim().isEmpty()) {
            hayError = true;
            mensaje = "El nombre de usuario no puede estar vacío";
        }
        // Validar caracteres especiales en el nombre
        else if(nombreSingUp.getText().contains("@") || nombreSingUp.getText().contains("?") || 
           nombreSingUp.getText().contains("=") || nombreSingUp.getText().contains("'") || 
           nombreSingUp.getText().contains("\"") || nombreSingUp.getText().contains("|") || 
           nombreSingUp.getText().contains("&") || nombreSingUp.getText().contains("*") || 
           nombreSingUp.getText().contains("+") || nombreSingUp.getText().contains("\\") ||
           nombreSingUp.getText().contains(" ")) {
            hayError = true;
            mensaje = "El nombre de usuario no puede contener espacios ni caracteres especiales: @, ?, =, ', \", |, *, &, +, \\";
        }
        // Validar que las contraseñas no estén vacías
        else if(passwdSingUp.getText().isEmpty() || repPasswdSingUp.getText().isEmpty()) {
            hayError = true;
            mensaje = "Las contraseñas no pueden estar vacías";
        }
        // Validar que las contraseñas coincidan
        else if(!passwdSingUp.getText().equals(repPasswdSingUp.getText())) {
            hayError = true;
            mensaje = "Las contraseñas no coinciden";
        }
        // Validar longitud mínima de contraseña
        else if(passwdSingUp.getText().length() < 6) {
            hayError = true;
            mensaje = "La contraseña debe tener al menos 6 caracteres";
        }
        
        if(hayError) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Error en el registro");
            a.setContentText(mensaje);
            a.showAndWait();
        }
        
        return hayError;
    }
    
    // Verificar si un usuario ya existe en la base de datos
    private boolean usuarioExiste(String nombre) {
        try {
            String query = "SELECT COUNT(*) FROM Usuarios WHERE Nombre = '" + nombre + "'";
            Statement stmt = StartWin.conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if(rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}