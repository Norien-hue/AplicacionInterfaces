package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.javafx.model.Producto;
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

public class NewProducto implements Initializable {

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
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void crearProducto(ActionEvent event) {
        if(!launchAlertsNewProducto()) {
            String nombre = nombreProducto.getText().trim();
            String tipo = tipoProducto.getText().trim();
            long codigoBarras = Long.parseLong(codigoBarrasProducto.getText().trim());
            float emisiones = Float.parseFloat(emisionesProducto.getText().trim());
            String material = materialesProducto.getText().trim();
            
            SQLstatementStorage.storeStatement(
                "INSERT INTO Productos (Tipo, Numero_barras, Nombre, Emisiones_Reducibles, Material) VALUES ('" 
                + tipo + "', '" 
                + codigoBarras + "', '" 
                + nombre + "', '" 
                + emisiones + "', '" 
                + material + "')"
            );
            
            Producto nuevoProducto = new Producto(tipo, codigoBarras, nombre, emisiones, material);
            MainController.tablaProductosObservable.add(nuevoProducto);
            
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

    private void checkForAlertNewProducto() {
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

        // Validar que código de barras sea numérico
        try {
            Long.parseLong(codigoBarrasProducto.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
        }

        // Validar que emisiones sea numérico
        try {
            Float.parseFloat(emisionesProducto.getText().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
        }
    }

    private boolean launchAlertsNewProducto() {
        checkForAlertNewProducto();
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