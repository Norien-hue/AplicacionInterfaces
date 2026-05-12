package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.ApiClient;

import com.google.gson.JsonObject;
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
        StartWin.lanzarCambioContrasena();
    }

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_passwd.getScene().getWindow()).close();
    }

    @FXML
    void guardarCambios(ActionEvent event) {
        if (!StartWin.verificarConexion()) {
            StartWin.manejarPerdidaConexion("La conexion con el servidor no esta disponible");
            return;
        }

        String nuevoNombre = txtFld_nombre.getText().trim();

        if(nuevoNombre.isEmpty()) {
            mostrarError("Nombre vacio", "El nombre no puede estar vacio.");
            return;
        }

        if(nuevoNombre.contains("@") || nuevoNombre.contains("?") ||
           nuevoNombre.contains("=") || nuevoNombre.contains("'") ||
           nuevoNombre.contains("\"") || nuevoNombre.contains("|") ||
           nuevoNombre.contains("&") || nuevoNombre.contains("*") ||
           nuevoNombre.contains("+") || nuevoNombre.contains("\\")) {
            mostrarError("Caracteres invalidos", "El nombre contiene caracteres invalidos (@, ?, =, ', \", |, &, *, +, \\)");
            return;
        }

        try {
            ApiClient api = ApiClient.getInstance();

            // Obtener datos actuales del usuario para mantener los demas campos
            JsonObject perfil = api.getProfile(idUsuario);
            String roleActual = perfil.has("permisos") ? perfil.get("permisos").getAsString() : "cliente";
            int tapActual = (perfil.has("tap") && !perfil.get("tap").isJsonNull()) ? perfil.get("tap").getAsInt() : 0;
            float emisionesActual = perfil.has("emisiones") ? perfil.get("emisiones").getAsFloat() : 0.0f;

            // Actualizar usuario via API (solo cambia el nombre, mantiene los demas campos)
            api.updateUsuario(idUsuario, nuevoNombre, roleActual, tapActual, emisionesActual);

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
            ApiClient api = ApiClient.getInstance();
            JsonObject perfil = api.getProfile(idUsuario);

            if (perfil.has("nombre") && !perfil.get("nombre").isJsonNull()) {
                txtFld_nombre.setText(perfil.get("nombre").getAsString());
            } else {
                txtFld_nombre.setText("Usuario no encontrado");
            }

            if (perfil.has("tap") && !perfil.get("tap").isJsonNull()) {
                tapOriginal = String.valueOf(perfil.get("tap").getAsInt());
            } else {
                tapOriginal = "No asignado";
            }
            txtFld_tap.setText(ocultarTap(tapOriginal));

        } catch (Exception e) {
            e.printStackTrace();
            txtFld_nombre.setText("Error al cargar");
            txtFld_tap.setText("Error");
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
}
