package com.javafx.reciWins.controllers;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.ResourceBundle;

import com.javafx.model.Producto;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.ApiClient;

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

public class NewProducto implements Initializable {

    @FXML private Button btn_cancelar;
    @FXML private TextField nombreProducto;
    @FXML private TextField codigoBarrasProducto;
    @FXML private ComboBox<String> tipoProducto;
    @FXML private TextField emisionesProducto;
    @FXML private ComboBox<String> materialesProducto;
    @FXML private TextField rutaImagenProducto;
    @FXML private Button btn_aceptar;

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void crearProducto(ActionEvent event) {
        if(!launchAlertsNewProducto()) {

            String nombre = nombreProducto.getText().trim();
            String tipo = tipoProducto.getEditor().getText().trim();
            long codigoBarras = Long.parseLong(codigoBarrasProducto.getText().trim());
            float emisiones = Float.parseFloat(emisionesProducto.getText().trim());
            String material = materialesProducto.getValue();

            if (material == null || material.trim().isEmpty()) {
                mostrarAlerta(AlertType.ERROR, "Material requerido", "Debes seleccionar un material de la lista.");
                return;
            }

            // Procesar imagen si se indico ruta
            String imagenBase64 = null;
            String ruta = rutaImagenProducto.getText() == null ? "" : rutaImagenProducto.getText().trim();
            if (!ruta.isEmpty()) {
                try {
                    File f = new File(ruta);
                    if (!f.exists() || !f.isFile()) {
                        mostrarAlerta(AlertType.ERROR, "Imagen no encontrada", "La ruta de imagen no es valida: " + ruta);
                        return;
                    }
                    byte[] bytes = Files.readAllBytes(f.toPath());
                    imagenBase64 = Base64.getEncoder().encodeToString(bytes);
                } catch (Exception ex) {
                    mostrarAlerta(AlertType.ERROR, "Error al leer imagen", "No se pudo leer la imagen: " + ex.getMessage());
                    return;
                }
            }

            try {
                ApiClient api = ApiClient.getInstance();
                api.createProducto(tipo, codigoBarras, nombre, emisiones, material, imagenBase64);

                MainController.actualizarVistasDesdeExterno();

                ((Stage)btn_cancelar.getScene().getWindow()).close();
            } catch (Exception e) {
                mostrarAlerta(AlertType.ERROR, "Error al guardar", "No se pudo guardar el producto: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> tipos = MainController.getTiposProductos();

        FilteredList<String> filteredItems = new FilteredList<>(tipos, p -> true);
        tipoProducto.setItems(filteredItems);

        tipoProducto.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            final String selected = tipoProducto.getSelectionModel().getSelectedItem();

            if (newValue == null || newValue.trim().isEmpty()) {
                filteredItems.setPredicate(p -> true);
                tipoProducto.hide();
                return;
            }

            if (selected == null || !selected.equals(newValue)) {
                filteredItems.setPredicate(p -> p.toLowerCase().startsWith(newValue.toLowerCase().trim()));
                tipoProducto.setVisibleRowCount(5);
                tipoProducto.show();
            } else {
                filteredItems.setPredicate(p -> true);
            }
        });

        tipoProducto.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tipoProducto.getEditor().positionCaret(tipoProducto.getEditor().getText().length());
            }
        });

        // Configurar materiales (lista fija)
        ObservableList<String> materiales = FXCollections.observableArrayList(StartWin.getMateriales());
        FXCollections.sort(materiales);
        materialesProducto.setItems(materiales);

        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private static boolean checkAlert = false;
    private static String alertMessage = "";

    private void checkForAlertNewProducto() {
        alertMessage = "";

        ArrayList<TextField> camposTexto = new ArrayList<>();
        camposTexto.add(nombreProducto);

        for(TextField campo : camposTexto) {
            if(campo.getText().contains("@") || campo.getText().contains("?") ||
               campo.getText().contains("=") || campo.getText().contains("'") ||
               campo.getText().contains("\"") || campo.getText().contains("|") ||
               campo.getText().contains("&") || campo.getText().contains("*") ||
               campo.getText().contains("+") || campo.getText().contains("\\") ||
               campo.getText().strip().equals("")) {
                checkAlert = true;
                alertMessage = "Algun campo contiene caracteres invalidos o esta vacio";
                return;
            }
        }

        if(tipoProducto.getEditor().getText().contains("@") ||
           tipoProducto.getEditor().getText().contains("?") ||
           tipoProducto.getEditor().getText().contains("=") ||
           tipoProducto.getEditor().getText().contains("'") ||
           tipoProducto.getEditor().getText().contains("\"") ||
           tipoProducto.getEditor().getText().contains("|") ||
           tipoProducto.getEditor().getText().contains("&") ||
           tipoProducto.getEditor().getText().contains("*") ||
           tipoProducto.getEditor().getText().contains("+") ||
           tipoProducto.getEditor().getText().contains("\\") ||
           tipoProducto.getEditor().getText().strip().equals("")) {
            checkAlert = true;
            alertMessage = "El tipo contiene caracteres invalidos o esta vacio";
            return;
        }

        if(materialesProducto.getValue() == null || materialesProducto.getValue().trim().isEmpty()) {
            checkAlert = true;
            alertMessage = "Debes seleccionar un material de la lista";
            return;
        }

        try { Long.parseLong(codigoBarrasProducto.getText().trim()); }
        catch (NumberFormatException e) { checkAlert = true; alertMessage = "El codigo de barras debe ser un numero valido"; return; }

        try { Float.parseFloat(emisionesProducto.getText().trim()); }
        catch (NumberFormatException e) { checkAlert = true; alertMessage = "Las emisiones deben ser un numero valido"; return; }
    }

    private boolean launchAlertsNewProducto() {
        checkForAlertNewProducto();
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
        a.setOnShown(ex -> {
            Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
            stage.getIcons().add(StartWin.icon);
        });
        a.setHeaderText(titulo);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}
