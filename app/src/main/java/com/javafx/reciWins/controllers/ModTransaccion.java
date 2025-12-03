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
import com.javafx.reciWins.utiles.StorageSharer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

public class ModTransaccion implements Initializable {

    @FXML
    private Button btn_cancelar;
    
    @FXML
    private ComboBox<String> usuarioTransaccion;
    
    @FXML
    private ComboBox<String> tipoTransaccion;
    
    @FXML
    private ComboBox<String> codigoTransaccion;
    
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
        StorageSharer.itemStorage.clear();
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void modificarTransaccion(ActionEvent event) {
        if(!launchAlertsModTransaccion()) {
            // Obtener ID de usuario desde la búsqueda (formato: "ID - Nombre")
            int idUsuario = MainController.obtenerIdUsuarioDesdeBusqueda(usuarioTransaccion.getValue());
            String tipo = tipoTransaccion.getValue();
            long codigoBarras = Long.parseLong(codigoTransaccion.getValue());
            
            // Obtener fecha del DatePicker
            LocalDate localFecha = fechaTransaccion.getValue();
            Date fecha = Date.valueOf(localFecha);
            
            // Construir hora a partir de los tres campos
            int hora = Integer.parseInt(horaTransaccion.getText().trim());
            int minuto = Integer.parseInt(minutoTransaccion.getText().trim());
            int segundo = Integer.parseInt(segundoTransaccion.getText().trim());
            LocalTime localTime = LocalTime.of(hora, minuto, segundo);
            Time horaTime = Time.valueOf(localTime);
            
            // Valores originales para el WHERE
            String fechaOriginal = StorageSharer.itemStorage.get(3);
            String horaOriginal = StorageSharer.itemStorage.get(4);
            
            SQLstatementStorage.storeStatement(
                "UPDATE Recicla SET Id_Usuario = '" + idUsuario + "', " +
                "Tipo = '" + tipo + "', " +
                "Numero_barras = '" + codigoBarras + "', " +
                "Fecha = '" + fecha + "', " +
                "Hora = '" + horaTime + "' " +
                "WHERE Fecha = '" + fechaOriginal + "' AND Hora = '" + horaOriginal + "'"
            );
            
            StorageSharer.itemToMod = new Transaccion(idUsuario, tipo, codigoBarras, fecha, horaTime);
            MainController.modItem();
            
            StorageSharer.itemToMod = null;
            StorageSharer.itemStorage.clear();
            ((Stage)btn_cancelar.getScene().getWindow()).close();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Cargar tipos de productos existentes
        ObservableList<String> tipos = MainController.getTiposProductos();
        tipoTransaccion.setItems(tipos);
        
        // Cargar códigos de barras existentes con autocompletado
        ObservableList<Long> codigosBarrasLong = MainController.getCodigosBarras();
        ObservableList<String> codigosBarrasStr = FXCollections.observableArrayList();
        for (Long codigo : codigosBarrasLong) {
            codigosBarrasStr.add(codigo.toString());
        }
        
        // Configurar autocompletado para códigos de barras
        FilteredList<String> filteredCodigos = new FilteredList<>(codigosBarrasStr, p -> true);
        codigoTransaccion.setItems(filteredCodigos);
        configurarAutocompletado(codigoTransaccion, filteredCodigos);
        
        // Cargar usuarios existentes con autocompletado
        ObservableList<String> nombresUsuarios = MainController.getNombresUsuarios();
        FilteredList<String> filteredUsuarios = new FilteredList<>(nombresUsuarios, p -> true);
        usuarioTransaccion.setItems(filteredUsuarios);
        configurarAutocompletado(usuarioTransaccion, filteredUsuarios);
        
        // Cargar datos existentes de la transacción
        String idUsuarioOriginal = StorageSharer.itemStorage.get(0);
        String tipoOriginal = StorageSharer.itemStorage.get(1);
        String codigoOriginal = StorageSharer.itemStorage.get(2);
        String fechaOriginal = StorageSharer.itemStorage.get(3);
        String horaOriginal = StorageSharer.itemStorage.get(4);
        
        // Buscar el usuario correspondiente para mostrarlo
        for (String usuario : nombresUsuarios) {
            if (usuario.startsWith(idUsuarioOriginal + " - ")) {
                usuarioTransaccion.setValue(usuario);
                break;
            }
        }
        
        // Establecer tipo y código de barras
        tipoTransaccion.setValue(tipoOriginal);
        codigoTransaccion.setValue(codigoOriginal);
        
        // Cargar fecha
        LocalDate fecha = LocalDate.parse(fechaOriginal);
        fechaTransaccion.setValue(fecha);
        
        // Cargar hora (formato HH:MM:SS)
        String[] partesHora = horaOriginal.split(":");
        if(partesHora.length >= 3) {
            horaTransaccion.setText(partesHora[0]);
            minutoTransaccion.setText(partesHora[1]);
            segundoTransaccion.setText(partesHora[2]);
        } else if(partesHora.length == 2) {
            horaTransaccion.setText(partesHora[0]);
            minutoTransaccion.setText(partesHora[1]);
            segundoTransaccion.setText("00");
        }
        
        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    // Método para configurar autocompletado en un ComboBox
    private void configurarAutocompletado(ComboBox<String> comboBox, FilteredList<String> filteredItems) {
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final String selected = comboBox.getSelectionModel().getSelectedItem();
            
            if (newValue == null || newValue.trim().isEmpty()) {
                filteredItems.setPredicate(p -> true);
                comboBox.hide();
                return;
            }
            
            if (selected == null || !selected.equals(newValue)) {
                filteredItems.setPredicate(p -> p.toLowerCase().contains(newValue.toLowerCase().trim()));
                comboBox.setVisibleRowCount(5);
                comboBox.show();
            } else {
                filteredItems.setPredicate(p -> true);
            }
        });
        
        // Posicionar cursor al final al seleccionar
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
            }
        });
    }

    private static boolean checkAlert = false;
    private static String alertMessage = "";

    private void checkForAlertModTransaccion() {
        alertMessage = "";
        
        // Validar usuario
        if(usuarioTransaccion.getValue() == null || usuarioTransaccion.getValue().trim().isEmpty()) {
            checkAlert = true;
            alertMessage = "Debes seleccionar un usuario";
            return;
        }
        
        // Validar que el formato del usuario sea correcto y obtener ID
        int idUsuario = MainController.obtenerIdUsuarioDesdeBusqueda(usuarioTransaccion.getValue());
        if(idUsuario == -1) {
            checkAlert = true;
            alertMessage = "Formato de usuario inválido. Debe ser: 'ID - Nombre'";
            return;
        }

        // Validar que se haya seleccionado un tipo
        if(tipoTransaccion.getValue() == null || tipoTransaccion.getValue().trim().isEmpty()) {
            checkAlert = true;
            alertMessage = "Debes seleccionar un tipo de producto";
            return;
        }

        // Validar código de barras
        if(codigoTransaccion.getValue() == null || codigoTransaccion.getValue().trim().isEmpty()) {
            checkAlert = true;
            alertMessage = "Debes seleccionar un código de barras";
            return;
        }
        
        try {
            Long.parseLong(codigoTransaccion.getValue().trim());
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

    private boolean launchAlertsModTransaccion() {
        checkForAlertModTransaccion();
        boolean ret = false;

        if(checkAlert) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Error en los campos");
            a.setContentText(alertMessage + "\n\nAsegúrate de que:\n- Todos los campos están completos\n- Los valores de hora son válidos (HH:0-23, MM:0-59, SS:0-59)\n- Has seleccionado un usuario, tipo y código de barras válidos");
            a.showAndWait();
            ret = true;
        }

        checkAlert = false;
        alertMessage = "";
        return ret;
    }
}