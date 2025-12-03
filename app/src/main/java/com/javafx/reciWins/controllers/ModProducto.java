package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.javafx.model.Producto;
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
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class ModProducto implements Initializable {

    @FXML
    private Button btn_cancelar;
    
    @FXML
    private TextField nombreProducto;
    
    @FXML
    private TextField codigoBarrasProducto;
    
    @FXML
    private ComboBox<String> tipoProducto;
    
    @FXML
    private TextField emisionesProducto;
    
    @FXML
    private TextField materialesProducto;
    
    @FXML
    private Button btn_aceptar;

    @FXML
    void kill(ActionEvent event) {
        StorageSharer.itemStorage.clear();
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void modificarProducto(ActionEvent event) {
        if(!launchAlertsModProducto()) {
            String nombre = nombreProducto.getText().trim();
            String tipo = tipoProducto.getEditor().getText().trim();
            long codigoBarras = Long.parseLong(codigoBarrasProducto.getText().trim());
            float emisiones = Float.parseFloat(emisionesProducto.getText().trim());
            String material = materialesProducto.getText().trim();
            
            // Obtener valores originales para el WHERE
            String tipoOriginal = StorageSharer.itemStorage.get(0);
            String codigoOriginal = StorageSharer.itemStorage.get(1);
            
            SQLstatementStorage.storeStatement(
                "UPDATE Productos SET Tipo = '" + tipo + "', " +
                "Numero_barras = '" + codigoBarras + "', " +
                "Nombre = '" + nombre + "', " +
                "Emisiones_Reducibles = '" + emisiones + "', " +
                "Material = '" + material + "' " +
                "WHERE Tipo = '" + tipoOriginal + "' AND Numero_barras = '" + codigoOriginal + "'"
            );
            
            StorageSharer.itemToMod = new Producto(tipo, codigoBarras, nombre, emisiones, material);
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
        
        // Usar FilteredList para autocompletado
        FilteredList<String> filteredItems = new FilteredList<>(tipos, p -> true);
        tipoProducto.setItems(filteredItems);
        
        // Cargar datos del producto seleccionado
        tipoProducto.getEditor().setText(StorageSharer.itemStorage.get(0));
        codigoBarrasProducto.setText(StorageSharer.itemStorage.get(1));
        nombreProducto.setText(StorageSharer.itemStorage.get(2));
        emisionesProducto.setText(StorageSharer.itemStorage.get(3));
        materialesProducto.setText(StorageSharer.itemStorage.get(4));
        
        // Configurar autocompletado
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
        
        // Posicionar cursor al final al seleccionar
        tipoProducto.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tipoProducto.getEditor().positionCaret(tipoProducto.getEditor().getText().length());
            }
        });
        
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
        camposTexto.add(materialesProducto);

        // Validar campos de texto
        for(TextField campo : camposTexto) {
            if(campo.getText().contains("@") || campo.getText().contains("?") || 
               campo.getText().contains("=") || campo.getText().contains("'") || 
               campo.getText().contains("\"") || campo.getText().contains("|") || 
               campo.getText().contains("&") || campo.getText().contains("*") || 
               campo.getText().contains("+") || campo.getText().contains("\\") || 
               campo.getText().strip().equals("")) {
                checkAlert = true;
                alertMessage = "Algún campo contiene caracteres inválidos o está vacío";
                return;
            }
        }
        
        // Validar que el tipo no esté vacío
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
            alertMessage = "El tipo contiene caracteres inválidos o está vacío";
            return;
        }

        try {
            Long.parseLong(codigoBarrasProducto.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
            alertMessage = "El código de barras debe ser un número válido";
            return;
        }

        try {
            Float.parseFloat(emisionesProducto.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
            alertMessage = "Las emisiones deben ser un número válido";
            return;
        }
    }

    private boolean launchAlertsModProducto() {
        checkForAlertModProducto();
        boolean ret = false;

        if(checkAlert) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Error en los campos");
            a.setContentText(alertMessage + "\n\nAsegúrate de que:\n- Todos los campos están completos\n- El código de barras es numérico\n- Las emisiones son numéricas\n- No uses caracteres especiales: @, ?, =, ', \", |, *, &, +, \\");
            a.showAndWait();
            ret = true;
        }

        checkAlert = false;
        alertMessage = "";
        return ret;
    }
}