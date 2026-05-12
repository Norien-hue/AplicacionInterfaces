package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.javafx.model.Usuario;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.ApiClient;
import com.javafx.reciWins.utiles.StorageSharer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class ModUser implements Initializable {

    @FXML private RadioButton adminRoleUsuarioMod;
    @FXML private Button btn_cancelar;
    @FXML private TextField emisionesModUsuario;
    @FXML private TextField idModUsuario;
    @FXML private Button modUsuarioConfirmar;
    @FXML private TextField nombreModUsuario;
    @FXML private TextField tapModUsuario;
    @FXML private RadioButton userRoleUsuarioMod;

    @FXML
    void kill(ActionEvent event) {
        StorageSharer.itemStorage.clear();
        StorageSharer.itemToMod = null;
        StorageSharer.itemPre = null;
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idModUsuario.setText(StorageSharer.itemStorage.get(0));
        nombreModUsuario.setText(StorageSharer.itemStorage.get(1));

        String role = StorageSharer.itemStorage.get(2);
        if(role.equalsIgnoreCase("cliente")) {
            userRoleUsuarioMod.setSelected(true);
        } else {
            adminRoleUsuarioMod.setSelected(true);
        }

        tapModUsuario.setText(StorageSharer.itemStorage.get(3));
        emisionesModUsuario.setText(StorageSharer.itemStorage.get(4));

        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    @FXML
    void modUsuarioPulsado(ActionEvent event) {
        if(!launchAlertsModUser()) {
            int id = Integer.parseInt(idModUsuario.getText().trim());
            String nombre = nombreModUsuario.getText().trim();
            String role = userRoleUsuarioMod.isSelected() ? "cliente" : "administrador";
            int tap = Integer.parseInt(tapModUsuario.getText().trim());
            float emisiones = Float.parseFloat(emisionesModUsuario.getText().trim());

            try {
                ApiClient api = ApiClient.getInstance();
                api.updateUsuario(id, nombre, role, tap, emisiones);

                MainController.actualizarVistasDesdeExterno();

                StorageSharer.itemToMod = null;
                StorageSharer.itemPre = null;
                StorageSharer.itemStorage.clear();

                ((Stage)btn_cancelar.getScene().getWindow()).close();
            } catch (Exception e) {
                mostrarAlerta(AlertType.ERROR, "Error al modificar", "No se pudo modificar el usuario: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static boolean checkAlert = false;
    private static String alertMessage = "";

    private void checkForAlertModUser() {
        alertMessage = "";

        ArrayList<TextField> camposTexto = new ArrayList<>();
        camposTexto.add(nombreModUsuario);

        for(TextField campo : camposTexto) {
            if(campo.getText().contains("@") || campo.getText().contains("?") ||
               campo.getText().contains("=") || campo.getText().contains("'") ||
               campo.getText().contains("\"") || campo.getText().contains("|") ||
               campo.getText().contains("&") || campo.getText().contains("*") ||
               campo.getText().contains("+") || campo.getText().contains("\\") ||
               campo.getText().strip().equals("")) {
                checkAlert = true;
                alertMessage = "El nombre contiene caracteres invalidos o esta vacio";
                return;
            }
        }

        try { Integer.parseInt(tapModUsuario.getText().trim()); }
        catch (NumberFormatException e) { checkAlert = true; alertMessage = "El TAP debe ser un numero entero valido"; return; }

        try { Float.parseFloat(emisionesModUsuario.getText().trim()); }
        catch (NumberFormatException e) { checkAlert = true; alertMessage = "Las emisiones deben ser un numero valido"; return; }

        if(!adminRoleUsuarioMod.isSelected() && !userRoleUsuarioMod.isSelected()) {
            checkAlert = true; alertMessage = "Debes seleccionar un rol (Admin o User)"; return;
        }
    }

    private boolean launchAlertsModUser() {
        checkForAlertModUser();
        boolean ret = false;
        if(checkAlert) {
            mostrarAlerta(AlertType.ERROR, "Campo invalido", alertMessage);
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
