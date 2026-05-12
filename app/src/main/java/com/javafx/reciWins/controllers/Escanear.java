package com.javafx.reciWins.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import com.javafx.model.Producto;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.ApiClient;

import com.google.gson.JsonObject;
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
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class Escanear implements Initializable {

    @FXML private Button btn_cancelar;
    @FXML private Button btn_aceptar;
    @FXML private ComboBox<String> tipoProducto;
    @FXML private ComboBox<String> codigoBarras;
    @FXML private TextField emisionesField;
    @FXML private TextField tapField;

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void crearTransaccionEscaner(ActionEvent event) {
        if(!launchAlertsEscanear()) {
            int tapIngresado;
            try {
                tapIngresado = Integer.parseInt(tapField.getText().trim());
            } catch (NumberFormatException e) {
                mostrarAlerta(AlertType.ERROR, "TAP invalido", "El TAP debe ser un numero de 6 digitos.");
                return;
            }

            // Verificar TAP del usuario actual via API
            int tapUsuario = obtenerTapUsuarioActual();
            if (tapUsuario == -1) {
                mostrarAlerta(AlertType.ERROR, "Error de usuario",
                    "No se pudo verificar el usuario. Vuelve a iniciar sesion.");
                return;
            }

            if (tapIngresado != tapUsuario) {
                mostrarAlerta(AlertType.ERROR, "TAP incorrecto",
                    "El TAP ingresado no coincide con tu codigo personal.\n"
                    + "Verifica tu TAP en la pestana Personal.");
                tapField.clear();
                tapField.requestFocus();
                return;
            }

            String tipo = tipoProducto.getValue();
            long codigo = Long.parseLong(codigoBarras.getValue());

            float emisionesProducto = obtenerEmisionesLocal(tipo, codigo);

            LocalDate fechaActual = LocalDate.now();
            LocalTime horaActual = LocalTime.now();
            String fecha = fechaActual.toString();
            String hora = String.format("%02d:%02d:%02d", horaActual.getHour(), horaActual.getMinute(), horaActual.getSecond());

            int idUsuario = MainController.id_user;

            try {
                ApiClient api = ApiClient.getInstance();
                api.createTransaccion(idUsuario, tipo, codigo, fecha, hora);

                MainController.actualizarVistasDesdeExterno();

                mostrarAlerta(AlertType.INFORMATION, "Escaneo completado",
                    "Producto escaneado correctamente.\n"
                    + "Se han anadido " + emisionesProducto + " kg CO2 a tu cuenta.");

                ((Stage)btn_cancelar.getScene().getWindow()).close();

            } catch (Exception e) {
                mostrarAlerta(AlertType.ERROR, "Error al escanear",
                    "No se pudo registrar el escaneo: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> tipos = MainController.getTiposProductos();
        tipoProducto.setItems(tipos);

        ObservableList<Long> codigosBarrasLong = MainController.getCodigosBarras();
        ObservableList<String> codigosBarrasStr = FXCollections.observableArrayList();
        for (Long codigo : codigosBarrasLong) {
            codigosBarrasStr.add(codigo.toString());
        }
        FilteredList<String> filteredCodigos = new FilteredList<>(codigosBarrasStr, p -> true);
        codigoBarras.setItems(filteredCodigos);
        configurarAutocompletado(codigoBarras, filteredCodigos);

        emisionesField.setEditable(false);
        emisionesField.setText("0.0");

        tipoProducto.valueProperty().addListener((obs, oldVal, newVal) -> actualizarEmisiones());
        codigoBarras.valueProperty().addListener((obs, oldVal, newVal) -> actualizarEmisiones());

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
            } else { filteredItems.setPredicate(p -> true); }
        });
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
        });
    }

    private void actualizarEmisiones() {
        String tipo = tipoProducto.getValue();
        String codigo = codigoBarras.getValue();
        if (tipo != null && codigo != null && !tipo.trim().isEmpty() && !codigo.trim().isEmpty()) {
            try {
                long codigoBarrasLong = Long.parseLong(codigo.trim());
                float emisiones = obtenerEmisionesLocal(tipo, codigoBarrasLong);
                emisionesField.setText(String.format("%.1f", emisiones));
            } catch (NumberFormatException e) { emisionesField.setText("0.0"); }
        } else { emisionesField.setText("0.0"); }
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

    private int obtenerTapUsuarioActual() {
        try {
            ApiClient api = ApiClient.getInstance();
            JsonObject user = api.getProfile(MainController.id_user);
            if (user.has("tap") && !user.get("tap").isJsonNull()) {
                return user.get("tap").getAsInt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static boolean checkAlert = false;
    private static String alertMessage = "";

    private void checkForAlertEscanear() {
        alertMessage = "";
        if(tipoProducto.getValue() == null || tipoProducto.getValue().trim().isEmpty()) {
            checkAlert = true; alertMessage = "Debes seleccionar un tipo de producto"; return;
        }
        if(codigoBarras.getValue() == null || codigoBarras.getValue().trim().isEmpty()) {
            checkAlert = true; alertMessage = "Debes seleccionar o escribir un codigo de barras"; return;
        }
        try { Long.parseLong(codigoBarras.getValue().trim()); }
        catch (NumberFormatException e) { checkAlert = true; alertMessage = "El codigo de barras debe ser un numero valido"; return; }
        if(tapField.getText() == null || tapField.getText().trim().isEmpty()) {
            checkAlert = true; alertMessage = "Debes introducir tu TAP"; return;
        }
        try {
            int tap = Integer.parseInt(tapField.getText().trim());
            if (String.valueOf(tap).length() != 6) {
                checkAlert = true; alertMessage = "El TAP debe tener exactamente 6 digitos"; return;
            }
        } catch (NumberFormatException e) { checkAlert = true; alertMessage = "El TAP debe ser un numero de 6 digitos"; return; }
    }

    private boolean launchAlertsEscanear() {
        checkForAlertEscanear();
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
