package com.javafx.reciWins.controllers;

import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.javafx.model.Transaccion;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.SQLstatementStorage;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class NewTransaccion implements Initializable {

    @FXML
    private Button btn_cancelar;
    
    @FXML
    private TextField idUsuarioTransaccion;
    
    @FXML
    private ComboBox<String> tipoTransaccion;
    
    @FXML
    private TextField codigoTransaccion;
    
    @FXML
    private DatePicker fechaTransaccion;
    
    @FXML
    private TextField horaTransaccion;
    
    @FXML
    private TextField minutoTransaccion;
    
    @FXML
    private TextField segundoTransaccion;
    
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
            String tipo = tipoTransaccion.getValue();
            long codigoBarras = Long.parseLong(codigoTransaccion.getText().trim());
            
            // Obtener fecha del DatePicker
            LocalDate localFecha = fechaTransaccion.getValue();
            Date fecha = Date.valueOf(localFecha);
            
            // Construir hora a partir de los tres campos
            int hora = Integer.parseInt(horaTransaccion.getText().trim());
            int minuto = Integer.parseInt(minutoTransaccion.getText().trim());
            int segundo = Integer.parseInt(segundoTransaccion.getText().trim());
            LocalTime localTime = LocalTime.of(hora, minuto, segundo);
            Time horaTime = Time.valueOf(localTime);
            
            SQLstatementStorage.storeStatement(
                "INSERT INTO Recicla (Id_Usuario, Tipo, Numero_barras, Fecha, Hora) VALUES ('" 
                + idUsuario + "', '" 
                + tipo + "', '" 
                + codigoBarras + "', '" 
                + fecha + "', '" 
                + horaTime + "')"
            );
            
            Transaccion nuevaTransaccion = new Transaccion(idUsuario, tipo, codigoBarras, fecha, horaTime);
            MainController.tablaTransaccionesObservable.add(nuevaTransaccion);
            
            ((Stage)btn_cancelar.getScene().getWindow()).close();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cargar tipos de productos existentes
        ObservableList<String> tipos = MainController.getTiposProductos();
        tipoTransaccion.setItems(tipos);
        
        // Establecer fecha por defecto a hoy
        fechaTransaccion.setValue(LocalDate.now());
        
        // Establecer hora por defecto a la hora actual
        LocalTime ahora = LocalTime.now();
        horaTransaccion.setText(String.format("%02d", ahora.getHour()));
        minutoTransaccion.setText(String.format("%02d", ahora.getMinute()));
        segundoTransaccion.setText(String.format("%02d", ahora.getSecond()));
        
        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private static boolean checkAlert = false;
    private static String alertMessage = "";

    private void checkForAlertNewTransaccion() {
        alertMessage = "";
        
        // Validar ID Usuario
        try {
            Integer.parseInt(idUsuarioTransaccion.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
            alertMessage = "El ID Usuario debe ser un número entero válido";
            return;
        }

        // Validar que se haya seleccionado un tipo
        if(tipoTransaccion.getValue() == null || tipoTransaccion.getValue().trim().isEmpty()) {
            checkAlert = true;
            alertMessage = "Debes seleccionar un tipo de producto";
            return;
        }

        // Validar código de barras
        try {
            Long.parseLong(codigoTransaccion.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
            alertMessage = "El código de barras debe ser un número válido";
            return;
        }

        // Validar fecha
        if(fechaTransaccion.getValue() == null) {
            checkAlert = true;
            alertMessage = "Debes seleccionar una fecha";
            return;
        }

        // Validar hora
        try {
            int hora = Integer.parseInt(horaTransaccion.getText().trim());
            int minuto = Integer.parseInt(minutoTransaccion.getText().trim());
            int segundo = Integer.parseInt(segundoTransaccion.getText().trim());
            
            if(hora < 0 || hora > 23) {
                checkAlert = true;
                alertMessage = "La hora debe estar entre 0 y 23";
                return;
            }
            if(minuto < 0 || minuto > 59) {
                checkAlert = true;
                alertMessage = "Los minutos deben estar entre 0 y 59";
                return;
            }
            if(segundo < 0 || segundo > 59) {
                checkAlert = true;
                alertMessage = "Los segundos deben estar entre 0 y 59";
                return;
            }
        } catch (NumberFormatException e) {
            checkAlert = true;
            alertMessage = "La hora, minutos y segundos deben ser números enteros";
            return;
        }
    }

    private boolean launchAlertsNewTransaccion() {
        checkForAlertNewTransaccion();
        boolean ret = false;

        if(checkAlert) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Error en los campos");
            a.setContentText(alertMessage + "\n\nAsegúrate de que:\n- Todos los campos están completos\n- Los valores de hora son válidos (HH:0-23, MM:0-59, SS:0-59)\n- Has seleccionado un tipo de producto existente");
            a.showAndWait();
            ret = true;
        }

        checkAlert = false;
        alertMessage = "";
        return ret;
    }
}