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
            
            if(usuarioExiste(nombre)) {
                Alert error = new Alert(AlertType.ERROR);
                error.setOnShown(ex -> {
                    Stage stage = (Stage) error.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                error.setHeaderText("Usuario ya existe");
                error.setContentText("El nombre de usuario '" + nombre + "' ya está registrado. Por favor, elige otro.");
                error.showAndWait();
                return;
            }
            
            String hashContrasenia = "$2y$10$" + contrasenia.hashCode();

            String role = "cliente";

            String insertQuery = "INSERT INTO Usuarios (Nombre, Hash_Contraseña, Permisos, Emisiones_Reducidas, TAP) VALUES ('"
                + nombre + "', '"
                + hashContrasenia + "', '"
                + role + "', "
                + "0, "
                + "NULL)";

            try {
                Statement stmt = StartWin.conn.createStatement();
                stmt.executeUpdate(insertQuery, Statement.RETURN_GENERATED_KEYS);

                ResultSet rs = stmt.getGeneratedKeys();
                if(rs.next()) {
                    int nuevoId = rs.getInt(1);
                    
                    Alert success = new Alert(AlertType.INFORMATION);
                    success.setOnShown(ex -> {
                    Stage stage = (Stage) success.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                    success.setHeaderText("Registro exitoso");
                    success.setContentText("¡Bienvenido " + nombre + "! Tu cuenta ha sido creada exitosamente.\nID de usuario: " + nuevoId);
                    success.showAndWait();
                    
                    StartWin.mostrarLogin();
                } else {
                    throw new SQLException("No se pudo obtener el ID generado");
                }
                
            } catch (SQLException e) {
                Alert error = new Alert(AlertType.ERROR);
                error.setOnShown(ex -> {
                    Stage stage = (Stage) error.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
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
    
    private boolean validarCampos() {
        boolean hayError = false;
        String mensaje = "";
        
        if(nombreSingUp.getText().trim().isEmpty()) {
            hayError = true;
            mensaje = "El nombre de usuario no puede estar vacío";
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
            mensaje = "Las contraseñas no pueden estar vacías";
        }
        else if(!passwdSingUp.getText().equals(repPasswdSingUp.getText())) {
            hayError = true;
            mensaje = "Las contraseñas no coinciden. Por favor, verifica que ambas contraseñas sean iguales.";
        }
        else if(passwdSingUp.getText().contains("@") || passwdSingUp.getText().contains("?") || 
                passwdSingUp.getText().contains("=") || passwdSingUp.getText().contains("'") || 
                passwdSingUp.getText().contains("\"") || passwdSingUp.getText().contains("|") || 
                passwdSingUp.getText().contains("&") || passwdSingUp.getText().contains("*") || 
                passwdSingUp.getText().contains("+") || passwdSingUp.getText().contains("\\")) {
            hayError = true;
            mensaje = "La contraseña contiene caracteres especiales no permitidos: @, ?, =, ', \", |, *, &, +, \\\n\n" +
                      "Por seguridad, evita estos caracteres en tu contraseña.";
        }
        else if(passwdSingUp.getText().length() < 6) {
            hayError = true;
            mensaje = "La contraseña debe tener al menos 6 caracteres";
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
            if (esErrorDeConexion(e)) {
                System.err.println("Error de conexión detectado: " + e.getMessage());
                StartWin.manejarPerdidaConexion(e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Determina si una SQLException es debido a un problema de conexión
     * @param e La excepción SQL a verificar
     * @return true si es un error de conexión, false en caso contrario
     */
    private boolean esErrorDeConexion(SQLException e) {
        // Códigos de error comunes para problemas de conexión
        String sqlState = e.getSQLState();
        String mensaje = e.getMessage().toLowerCase();
        
        // SQLState codes para problemas de comunicación
        if (sqlState != null && (
            sqlState.startsWith("08") ||  // Connection exception
            sqlState.equals("HY000"))) {   // General error (puede ser conexión)
            return true;
        }
        
        // Mensajes comunes de error de conexión
        if (mensaje.contains("connection") || 
            mensaje.contains("timeout") ||
            mensaje.contains("closed") ||
            mensaje.contains("socket") ||
            mensaje.contains("communications link failure")) {
            return true;
        }
        
        return false;
    }
}