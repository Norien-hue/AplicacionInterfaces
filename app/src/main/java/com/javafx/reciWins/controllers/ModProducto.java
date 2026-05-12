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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

public class ModProducto implements Initializable {

    @FXML private Button btn_cancelar;
    @FXML private TextField nombreProducto;
    @FXML private TextField codigoBarrasProducto;
    @FXML private ComboBox<String> tipoProducto;
    @FXML private TextField emisionesProducto;
    @FXML private ComboBox<String> materialesProducto;
    @FXML private TextField rutaImagenProducto;
    @FXML private Label estadoImagenLabel;
    @FXML private Button btn_copiarImagen;
    @FXML private Button btn_aceptar;

    private String imagenBase64Actual;

    @FXML
    void kill(ActionEvent event) {
        StorageSharer.itemStorage.clear();
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void copiarImagenBase64(ActionEvent event) {
        if (imagenBase64Actual == null || imagenBase64Actual.isEmpty()) {
            mostrarAlerta(AlertType.INFORMATION, "Sin imagen", "Este producto no tiene imagen que copiar.");
            return;
        }
        ClipboardContent content = new ClipboardContent();
        content.putString(imagenBase64Actual);
        Clipboard.getSystemClipboard().setContent(content);
        mostrarAlerta(AlertType.INFORMATION, "Imagen copiada", "La imagen en Base64 se ha copiado al portapapeles.");
    }

    @FXML
    void modificarProducto(ActionEvent event) {
        if(!launchAlertsModProducto()) {

            String nombre = nombreProducto.getText().trim();
            // tipo y codigo de barras son read-only (la API no permite cambiar PK compuesta)
            String tipoOriginal = StorageSharer.itemStorage.get(0);
            long codigoOriginal = Long.parseLong(StorageSharer.itemStorage.get(1));
            float emisiones = Float.parseFloat(emisionesProducto.getText().trim());
            String material = materialesProducto.getValue();

            if (material == null || material.trim().isEmpty()) {
                mostrarAlerta(AlertType.ERROR, "Material requerido", "Debes seleccionar un material de la lista.");
                return;
            }

            // Procesar nueva imagen si se indico ruta, si no mantener la actual
            String nuevaImagenBase64 = imagenBase64Actual;
            String ruta = rutaImagenProducto.getText() == null ? "" : rutaImagenProducto.getText().trim();
            if (!ruta.isEmpty()) {
                try {
                    File f = new File(ruta);
                    if (!f.exists() || !f.isFile()) {
                        mostrarAlerta(AlertType.ERROR, "Imagen no encontrada", "La ruta de imagen no es valida: " + ruta);
                        return;
                    }
                    byte[] bytes = Files.readAllBytes(f.toPath());
                    nuevaImagenBase64 = Base64.getEncoder().encodeToString(bytes);
                } catch (Exception ex) {
                    mostrarAlerta(AlertType.ERROR, "Error al leer imagen", "No se pudo leer la imagen: " + ex.getMessage());
                    return;
                }
            }

            try {
                ApiClient api = ApiClient.getInstance();
                api.updateProducto(tipoOriginal, codigoOriginal, nombre, emisiones, material, nuevaImagenBase64);

                MainController.actualizarVistasDesdeExterno();

                StorageSharer.itemToMod = null;
                StorageSharer.itemStorage.clear();
                ((Stage)btn_cancelar.getScene().getWindow()).close();

                mostrarAlerta(AlertType.INFORMATION, "Producto modificado",
                    "El producto se modifico correctamente.\nLas emisiones de los usuarios afectados se han actualizado.");

            } catch (Exception e) {
                mostrarAlerta(AlertType.ERROR, "Error al modificar", "No se pudo modificar el producto: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tipos de productos
        ObservableList<String> tipos = MainController.getTiposProductos();
        FilteredList<String> filteredItems = new FilteredList<>(tipos, p -> true);
        tipoProducto.setItems(filteredItems);

        // Cargar datos del producto a modificar
        tipoProducto.getEditor().setText(StorageSharer.itemStorage.get(0));
        codigoBarrasProducto.setText(StorageSharer.itemStorage.get(1));
        nombreProducto.setText(StorageSharer.itemStorage.get(2));
        emisionesProducto.setText(StorageSharer.itemStorage.get(3));

        // Tipo y codigo de barras son read-only (PK compuesta no se puede cambiar via API)
        tipoProducto.setDisable(true);
        codigoBarrasProducto.setEditable(false);

        // Configurar materiales
        ObservableList<String> materiales = FXCollections.observableArrayList(StartWin.getMateriales());
        FXCollections.sort(materiales);
        materialesProducto.setItems(materiales);

        String materialActual = StorageSharer.itemStorage.get(4);
        if (materialActual != null && !materialActual.trim().isEmpty()) {
            materialesProducto.setValue(materialActual);
        }

        // Cargar imagen actual (base64) si existe
        if (StorageSharer.itemStorage.size() > 5) {
            String img = StorageSharer.itemStorage.get(5);
            imagenBase64Actual = (img == null || img.isEmpty()) ? null : img;
        } else {
            imagenBase64Actual = null;
        }
        if (imagenBase64Actual != null) {
            estadoImagenLabel.setText("Imagen actual: si (" + imagenBase64Actual.length() + " caracteres)");
            btn_copiarImagen.setDisable(false);
        } else {
            estadoImagenLabel.setText("Imagen actual: ninguna");
            btn_copiarImagen.setDisable(true);
        }

        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private static boolean checkAlert = false;
    private static String alertMessage = "";

    private void checkForAlertModProducto() {
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

        if(materialesProducto.getValue() == null || materialesProducto.getValue().trim().isEmpty()) {
            checkAlert = true; alertMessage = "Debes seleccionar un material de la lista"; return;
        }

        try { Float.parseFloat(emisionesProducto.getText().trim()); }
        catch (NumberFormatException e) { checkAlert = true; alertMessage = "Las emisiones deben ser un numero valido"; return; }
    }

    private boolean launchAlertsModProducto() {
        checkForAlertModProducto();
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
