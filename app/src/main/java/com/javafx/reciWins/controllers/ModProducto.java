package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.javafx.model.Producto;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.SQLstatementStorage;
import com.javafx.reciWins.utiles.StorageSharer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
    private TextField tipoProducto;
    
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
            String tipo = tipoProducto.getText().trim();
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
        tipoProducto.setText(StorageSharer.itemStorage.get(0));
        codigoBarrasProducto.setText(StorageSharer.itemStorage.get(1));
        nombreProducto.setText(StorageSharer.itemStorage.get(2));
        emisionesProducto.setText(StorageSharer.itemStorage.get(3));
        materialesProducto.setText(StorageSharer.itemStorage.get(4));
        
        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    private static boolean checkAlert = false;

    private void checkForAlertModProducto() {
        ArrayList<TextField> camposTexto = new ArrayList<>();
        camposTexto.add(nombreProducto);
        camposTexto.add(tipoProducto);
        camposTexto.add(materialesProducto);

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

        try {
            Long.parseLong(codigoBarrasProducto.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
        }

        try {
            Float.parseFloat(emisionesProducto.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
        }
    }

    private boolean launchAlertsModProducto() {
        checkForAlertModProducto();
        boolean ret = false;

        if(checkAlert) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Campo inválido");
            a.setContentText("Verifica que:\n- No haya campos vacíos\n- El código de barras sea numérico\n- Las emisiones sean numéricas\n- No uses caracteres especiales: @, ?, =, ', \", |, *, &, +, \\");
            a.showAndWait();
            ret = true;
        }

        checkAlert = false;
        return ret;
    }
}