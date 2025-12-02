package com.javafx.reciWins.controllers;

import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.javafx.model.Transaccion;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.SQLstatementStorage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class NewTransaccion implements Initializable {

    @FXML
    private Button btn_cancelar;
    
    @FXML
    private TextField idUsuarioTransaccion;
    
    @FXML
    private TextField tipoTransaccion;
    
    @FXML
    private TextField codigoTransaccion;
    
    @FXML
    private TextField fechaTransaccion;
    
    @FXML
    private TextField horaTransaccion;
    
    @FXML
    private Button btn_aceptar;

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void crearTransaccion(ActionEvent event) {
        if(!launchAlertsNewTransaccion()) {
            int idUsuario = Integer.parseInt(idUsuarioTransaccion.getText().trim());
            String tipo = tipoTransaccion.getText().trim();
            long codigoBarras = Long.parseLong(codigoTransaccion.getText().trim());
            Date fecha = Date.valueOf(fechaTransaccion.getText().trim());
            Time hora = Time.valueOf(horaTransaccion.getText().trim() + ":00");
            
            SQLstatementStorage.storeStatement(
                "INSERT INTO Recicla (Id_Usuario, Tipo, Numero_barras, Fecha, Hora) VALUES ('" 
                + idUsuario + "', '" 
                + tipo + "', '" 
                + codigoBarras + "', '" 
                + fecha + "', '" 
                + hora + "')"
            );
            
            Transaccion nuevaTransaccion = new Transaccion(idUsuario, tipo, codigoBarras, fecha, hora);
            MainController.tablaTransaccionesObservable.add(nuevaTransaccion);
            
            ((Stage)btn_cancelar.getScene().getWindow()).close();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private static boolean checkAlert = false;

    private void checkForAlertNewTransaccion() {
        ArrayList<TextField> camposTexto = new ArrayList<>();
        camposTexto.add(tipoTransaccion);

        camposTexto.forEach((e) -> {
            if(e.getText().contains("@") || e.getText().contains("?") || 
               e.getText().contains("=") || e.getText().contains("'") || 
               e.getText().contains("\"") || e.getText().contains("|") || 
               e.getText().contains("&") || e.getText().contains("*") || 
               e.getText().contains("+") || e.getText().contains("\\") || 
               e.getText().strip().equals("")) {
                checkAlert = true;
            }
        });

        // Validar ID Usuario
        try {
            Integer.parseInt(idUsuarioTransaccion.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
        }

        // Validar código de barras
        try {
            Long.parseLong(codigoTransaccion.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
        }

        // Validar fecha (formato YYYY-MM-DD)
        try {
            Date.valueOf(fechaTransaccion.getText().trim());
        } catch (IllegalArgumentException e) {
            checkAlert = true;
        }

        // Validar hora (formato HH:MM)
        try {
            Time.valueOf(horaTransaccion.getText().trim() + ":00");
        } catch (IllegalArgumentException e) {
            checkAlert = true;
        }
    }

    private boolean launchAlertsNewTransaccion() {
        checkForAlertNewTransaccion();
        boolean ret = false;

        if(checkAlert) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Campo inválido");
            a.setContentText("Verifica que:\n- ID Usuario sea numérico\n- Código de barras sea numérico\n- Fecha tenga formato YYYY-MM-DD\n- Hora tenga formato HH:MM\n- No uses caracteres especiales");
            a.showAndWait();
            ret = true;
        }

        checkAlert = false;
        return ret;
    }
}