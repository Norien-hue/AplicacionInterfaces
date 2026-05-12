package com.javafx.reciWins.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import com.javafx.model.Producto;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.ApiClient;
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

    @FXML private Button btn_cancelar;
    @FXML private ComboBox<String> usuarioTransaccion;
    @FXML private ComboBox<String> tipoTransaccion;
    @FXML private ComboBox<String> codigoTransaccion;
    @FXML private DatePicker fechaTransaccion;
    @FXML private TextField horaTransaccion;
    @FXML private TextField minutoTransaccion;
    @FXML private TextField segundoTransaccion;
    @FXML private TextField emisionesTransaccion;
    @FXML private Button btn_aceptar;

    @FXML
    void kill(ActionEvent event) {
        StorageSharer.itemStorage.clear();
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void modificarTransaccion(ActionEvent event) {
        if(!launchAlertsModTransaccion()) {
            int idUsuario = MainController.obtenerIdUsuarioDesdeBusqueda(usuarioTransaccion.getValue());
            String tipo = tipoTransaccion.getValue();
            long codigoBarras = Long.parseLong(codigoTransaccion.getValue());

            LocalDate localFecha = fechaTransaccion.getValue();
            String fecha = localFecha.toString();

            int hora = Integer.parseInt(horaTransaccion.getText().trim());
            int minuto = Integer.parseInt(minutoTransaccion.getText().trim());
            int segundo = Integer.parseInt(segundoTransaccion.getText().trim());
            String horaStr = String.format("%02d:%02d:%02d", hora, minuto, segundo);

            // Datos originales para borrar la transaccion antigua
            int idUsuarioOriginal = Integer.parseInt(StorageSharer.itemStorage.get(0));
            String tipoOriginal = StorageSharer.itemStorage.get(1);
            long codigoOriginal = Long.parseLong(StorageSharer.itemStorage.get(2));
            String fechaOriginal = StorageSharer.itemStorage.get(3);
            String horaOriginal = StorageSharer.itemStorage.get(4);

            try {
                ApiClient api = ApiClient.getInstance();

                // Modificar = borrar antigua + crear nueva (la API gestiona las emisiones)
                api.deleteTransaccion(idUsuarioOriginal, tipoOriginal, codigoOriginal, fechaOriginal, horaOriginal);
                api.createTransaccion(idUsuario, tipo, codigoBarras, fecha, horaStr);

                MainController.actualizarVistasDesdeExterno();

                StorageSharer.itemToMod = null;
                StorageSharer.itemStorage.clear();
                ((Stage)btn_cancelar.getScene().getWindow()).close();

            } catch (Exception e) {
                mostrarAlerta(AlertType.ERROR, "Error al modificar",
                    "No se pudo modificar la transaccion: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> tipos = MainController.getTiposProductos();
        tipoTransaccion.setItems(tipos);

        ObservableList<Long> codigosBarrasLong = MainController.getCodigosBarras();
        ObservableList<String> codigosBarrasStr = FXCollections.observableArrayList();
        for (Long codigo : codigosBarrasLong) {
            codigosBarrasStr.add(codigo.toString());
        }
        FilteredList<String> filteredCodigos = new FilteredList<>(codigosBarrasStr, p -> true);
        codigoTransaccion.setItems(filteredCodigos);
        configurarAutocompletado(codigoTransaccion, filteredCodigos);

        ObservableList<String> nombresUsuarios = MainController.getNombresUsuarios();
        FilteredList<String> filteredUsuarios = new FilteredList<>(nombresUsuarios, p -> true);
        usuarioTransaccion.setItems(filteredUsuarios);
        configurarAutocompletado(usuarioTransaccion, filteredUsuarios);

        String idUsuarioOriginal = StorageSharer.itemStorage.get(0);
        String tipoOriginal = StorageSharer.itemStorage.get(1);
        String codigoOriginal = StorageSharer.itemStorage.get(2);
        String fechaOriginal = StorageSharer.itemStorage.get(3);
        String horaOriginal = StorageSharer.itemStorage.get(4);

        for (String usuario : nombresUsuarios) {
            if (usuario.startsWith(idUsuarioOriginal + " - ")) {
                usuarioTransaccion.setValue(usuario);
                break;
            }
        }
        tipoTransaccion.setValue(tipoOriginal);
        codigoTransaccion.setValue(codigoOriginal);

        LocalDate fecha = LocalDate.parse(fechaOriginal);
        fechaTransaccion.setValue(fecha);

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

        emisionesTransaccion.setEditable(false);
        float emisionesOriginales = obtenerEmisionesLocal(tipoOriginal, Long.parseLong(codigoOriginal));
        emisionesTransaccion.setText(String.format("%.1f", emisionesOriginales));

        tipoTransaccion.valueProperty().addListener((obs, oldVal, newVal) -> actualizarEmisiones());
        codigoTransaccion.valueProperty().addListener((obs, oldVal, newVal) -> actualizarEmisiones());

        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private void configurarAutocompletado(ComboBox<String> comboBox, FilteredList<String> filteredItems) {
        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final String selected = comboBox.getSelectionModel().getSelectedItem();
            if (newValue == null || newValue.trim().isEmpty()) {
                filteredItems.setPredicate(p -> true); comboBox.hide(); return;
            }
            if (selected == null || !selected.equals(newValue)) {
                filteredItems.setPredicate(p -> p.toLowerCase().contains(newValue.toLowerCase().trim()));
                comboBox.setVisibleRowCount(5); comboBox.show();
            } else {
                filteredItems.setPredicate(p -> true);
            }
        });
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
        });
    }

    private void actualizarEmisiones() {
        String tipo = tipoTransaccion.getValue();
        String codigo = codigoTransaccion.getValue();
        if (tipo != null && codigo != null && !tipo.trim().isEmpty() && !codigo.trim().isEmpty()) {
            try {
                long codigoBarras = Long.parseLong(codigo.trim());
                float emisiones = obtenerEmisionesLocal(tipo, codigoBarras);
                emisionesTransaccion.setText(String.format("%.1f", emisiones));
            } catch (NumberFormatException e) { emisionesTransaccion.setText("0.0"); }
        } else { emisionesTransaccion.setText("0.0"); }
    }

    private float obtenerEmisionesLocal(String tipo, long codigoBarras) {
        if (MainController.tablaProductosObservable != null) {
            for (Producto p : MainController.tablaProductosObservable) {
                if (p.getTipo().equals(tipo) && p.getNumeroBarras() == codigoBarras) {
                    return p.getEmisionesReducibles();
                }
            }
        }
        return 0.0f;
    }

    private static boolean checkAlert = false;
    private static String alertMessage = "";

    private void checkForAlertModTransaccion() {
        alertMessage = "";
        if(usuarioTransaccion.getValue() == null || usuarioTransaccion.getValue().trim().isEmpty()) {
            checkAlert = true; alertMessage = "Debes seleccionar un usuario"; return;
        }
        int idUsuario = MainController.obtenerIdUsuarioDesdeBusqueda(usuarioTransaccion.getValue());
        if(idUsuario == -1) { checkAlert = true; alertMessage = "Formato de usuario invalido"; return; }
        if(tipoTransaccion.getValue() == null || tipoTransaccion.getValue().trim().isEmpty()) {
            checkAlert = true; alertMessage = "Debes seleccionar un tipo de producto"; return;
        }
        if(codigoTransaccion.getValue() == null || codigoTransaccion.getValue().trim().isEmpty()) {
            checkAlert = true; alertMessage = "Debes seleccionar un codigo de barras"; return;
        }
        try { Long.parseLong(codigoTransaccion.getValue().trim()); }
        catch (NumberFormatException e) { checkAlert = true; alertMessage = "El codigo de barras debe ser un numero valido"; return; }
        if(fechaTransaccion.getValue() == null) { checkAlert = true; alertMessage = "Debes seleccionar una fecha"; return; }
        try {
            int hora = Integer.parseInt(horaTransaccion.getText().trim());
            int minuto = Integer.parseInt(minutoTransaccion.getText().trim());
            int segundo = Integer.parseInt(segundoTransaccion.getText().trim());
            if(hora < 0 || hora > 23) { checkAlert = true; alertMessage = "La hora debe estar entre 0 y 23"; return; }
            if(minuto < 0 || minuto > 59) { checkAlert = true; alertMessage = "Los minutos deben estar entre 0 y 59"; return; }
            if(segundo < 0 || segundo > 59) { checkAlert = true; alertMessage = "Los segundos deben estar entre 0 y 59"; return; }
        } catch (NumberFormatException e) { checkAlert = true; alertMessage = "La hora, minutos y segundos deben ser numeros enteros"; return; }
    }

    private boolean launchAlertsModTransaccion() {
        checkForAlertModTransaccion();
        boolean ret = false;
        if(checkAlert) {
            mostrarAlerta(AlertType.ERROR, "Error en los campos", alertMessage);
            ret = true;
        }
        checkAlert = false;
        alertMessage = "";
        return ret;
    }

    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo);
        a.setOnShown(e -> {
            Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
            stage.getIcons().add(StartWin.icon);
        });
        a.setHeaderText(titulo);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
