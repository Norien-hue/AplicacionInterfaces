package com.javafx.reciWins.controllers;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;

import com.javafx.reciWins.start.StartWin;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class SettingsController implements Initializable {

    @FXML
    private Button btn_accept;

    @FXML
    private Button btn_cancel;

    @FXML
    private Button btn_passwd;

    @FXML
    private TextField txtFld_nombre;

    @FXML
    private TextField txtFld_tap;

    @FXML
    private Button btn_mostrarTap;

    private boolean tapVisible = false;
    private String tapOriginal = "";
    private int idUsuario;

    @FXML
    void launch_cambiarPwd(ActionEvent event) {
        StartWin.lanzarCambioContraseña();
    }

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_passwd.getScene().getWindow()).close();
    }

    @FXML
    void guardarCambios(ActionEvent event) {
        // Verificar conexión antes de proceder
        if (!StartWin.verificarConexion()) {
            StartWin.manejarPerdidaConexion("La conexión con la base de datos no está disponible");
            return;
        }
        
        String nuevoNombre = txtFld_nombre.getText().trim();
        
        if(nuevoNombre.isEmpty()) {
            mostrarError("Nombre vacío", "El nombre no puede estar vacío.");
            return;
        }
        
        if(nuevoNombre.contains("@") || nuevoNombre.contains("?") || 
           nuevoNombre.contains("=") || nuevoNombre.contains("'") || 
           nuevoNombre.contains("\"") || nuevoNombre.contains("|") || 
           nuevoNombre.contains("&") || nuevoNombre.contains("*") || 
           nuevoNombre.contains("+") || nuevoNombre.contains("\\")) {
            mostrarError("Caracteres inválidos", "El nombre contiene caracteres inválidos (@, ?, =, ', \", |, &, *, +, \\)");
            return;
        }
        
        try {
            String query = "SELECT COUNT(*) FROM Usuarios WHERE Nombre = ? AND Id_Usuario != ?";
            PreparedStatement pst = StartWin.conn.prepareStatement(query);
            pst.setString(1, nuevoNombre);
            pst.setInt(2, idUsuario);
            ResultSet rs = pst.executeQuery();
            
            if(rs.next() && rs.getInt(1) > 0) {
                mostrarError("Nombre duplicado", "El nombre '" + nuevoNombre + "' ya está en uso por otro usuario.");
                return;
            }
        } catch (SQLException e) {
            if (esErrorDeConexion(e)) {
                System.err.println("Error de conexión detectado: " + e.getMessage());
                StartWin.manejarPerdidaConexion(e.getMessage());
            } else {
                mostrarError("Error de base de datos", "No se pudo verificar la disponibilidad del nombre.");
                e.printStackTrace();
            }
            return;
        }
        
        try {
            Statement stmt = StartWin.conn.createStatement();
            stmt.executeUpdate(
                "UPDATE Usuarios SET Nombre = '" + nuevoNombre + "' WHERE Id_Usuario = " + idUsuario
            );
            
            // ACTUALIZAR TODAS LAS VISTAS DESPUÉS DE MODIFICAR
            MainController.actualizarVistasDesdeExterno();
            
            Alert exito = new Alert(AlertType.INFORMATION);
            exito.setOnShown(ex -> {
                    Stage stage = (Stage) exito.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
            exito.setHeaderText("Cambios guardados");
            exito.setContentText("Los cambios se han guardado correctamente.");
            exito.showAndWait();
            
            kill(event);
        } catch (SQLException e) {
            if (esErrorDeConexion(e)) {
                System.err.println("Error de conexión detectado: " + e.getMessage());
                StartWin.manejarPerdidaConexion(e.getMessage());
            } else {
                mostrarError("Error al guardar", "No se pudieron guardar los cambios: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            mostrarError("Error al guardar", "No se pudieron guardar los cambios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void alternarMostrarTap(ActionEvent event) {
        if (tapVisible) {
            txtFld_tap.setText(ocultarTap(tapOriginal));
            btn_mostrarTap.setText("Mostrar");
        } else {
            txtFld_tap.setText(tapOriginal);
            btn_mostrarTap.setText("Ocultar");
        }
        tapVisible = !tapVisible;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idUsuario = MainController.id_user;
        cargarDatosUsuario();
        
        Platform.runLater(() -> {
            ((Stage)btn_passwd.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private void cargarDatosUsuario() {
        try {
            String query = "SELECT Nombre, TAP FROM Usuarios WHERE Id_Usuario = ?";
            PreparedStatement pst = StartWin.conn.prepareStatement(query);
            pst.setInt(1, idUsuario);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("Nombre");
                txtFld_nombre.setText(nombre);
                
                String tap = rs.getString("TAP");
                if (tap == null) {
                    tap = "No asignado";
                }
                tapOriginal = tap;
                txtFld_tap.setText(ocultarTap(tapOriginal));
            } else {
                txtFld_nombre.setText("Usuario no encontrado");
                txtFld_tap.setText("No disponible");
            }
        } catch (SQLException e) {
            if (esErrorDeConexion(e)) {
                System.err.println("Error de conexión detectado: " + e.getMessage());
                StartWin.manejarPerdidaConexion(e.getMessage());
            } else {
                e.printStackTrace();
                txtFld_nombre.setText("Error al cargar");
                txtFld_tap.setText("Error");
            }
        }
    }

    private String ocultarTap(String tap) {
        if (tap == null || tap.isEmpty() || tap.equals("No asignado")) {
            return tap;
        }
        return tap.replaceAll(".", "*");
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert a = new Alert(AlertType.ERROR);
        a.setOnShown(ex -> {
                    Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
        a.setHeaderText(titulo);
        a.setContentText(mensaje);
        a.showAndWait();
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