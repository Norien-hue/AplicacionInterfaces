package com.javafx.reciWins.controllers;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.javafx.model.Producto;
import com.javafx.reciWins.start.StartWin;
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
            
            String tipoOriginal = StorageSharer.itemStorage.get(0);
            String codigoOriginal = StorageSharer.itemStorage.get(1);
            float emisionesOriginales = Float.parseFloat(StorageSharer.itemStorage.get(3));
            
            // Calcular la diferencia de emisiones
            float diferenciaEmisiones = emisiones - emisionesOriginales;
            
            try {
                Statement stmt = StartWin.conn.createStatement();
                
                // 1. ACTUALIZAR EMISIONES DE USUARIOS SI HAY DIFERENCIA
                if (diferenciaEmisiones != 0) {
                    // Obtener todos los usuarios que han reciclado este producto
                    Map<Integer, Integer> transaccionesPorUsuario = obtenerTransaccionesPorUsuario(tipoOriginal, Long.parseLong(codigoOriginal));
                    
                    // Para cada usuario, actualizar sus emisiones totales
                    for (Map.Entry<Integer, Integer> entry : transaccionesPorUsuario.entrySet()) {
                        int idUsuario = entry.getKey();
                        int cantidadTransacciones = entry.getValue();
                        float cambioTotal = diferenciaEmisiones * cantidadTransacciones;
                        
                        // Actualizar emisiones del usuario en la base de datos
                        stmt.executeUpdate(
                            "UPDATE Usuarios SET Emisiones_Reducidas = Emisiones_Reducidas + " 
                            + cambioTotal + " WHERE Id_Usuario = " + idUsuario
                        );
                        
                        // Actualizar emisiones en la lista observable
                        MainController.actualizarEmisionesUsuarioObservable(idUsuario, cambioTotal);
                    }
                }
                
                // 2. ACTUALIZAR EL PRODUCTO
                stmt.executeUpdate(
                    "UPDATE Productos SET Tipo = '" + tipo + "', " +
                    "Numero_barras = '" + codigoBarras + "', " +
                    "Nombre = '" + nombre + "', " +
                    "Emisiones_Reducibles = '" + emisiones + "', " +
                    "Material = '" + material + "' " +
                    "WHERE Tipo = '" + tipoOriginal + "' AND Numero_barras = '" + codigoOriginal + "'"
                );
                
                // 3. ACTUALIZAR LAS TRANSACCIONES EXISTENTES
                if (!tipo.equals(tipoOriginal) || codigoBarras != Long.parseLong(codigoOriginal)) {
                    stmt.executeUpdate(
                        "UPDATE Recicla SET Tipo = '" + tipo + "', " +
                        "Numero_barras = '" + codigoBarras + "' " +
                        "WHERE Tipo = '" + tipoOriginal + "' AND Numero_barras = '" + codigoOriginal + "'"
                    );
                }
                
                StorageSharer.itemToMod = new Producto(tipo, codigoBarras, nombre, emisiones, material);
                MainController.modItem();
                
                // 4. ACTUALIZAR TODAS LAS VISTAS
                MainController.actualizarVistasDesdeExterno();
                
                // 5. ACTUALIZAR LA VISTA PERSONAL DEL USUARIO ACTUAL SI ES NECESARIO
                MainController.actualizarVistaPersonal();
                
                StorageSharer.itemToMod = null;
                StorageSharer.itemStorage.clear();
                ((Stage)btn_cancelar.getScene().getWindow()).close();
                
                // Mostrar mensaje de éxito
                Alert exito = new Alert(AlertType.INFORMATION);
                exito.setHeaderText("Producto modificado");
                exito.setContentText("El producto se modificó correctamente.\n" +
                                   "Las emisiones de los usuarios afectados se han actualizado.");
                exito.showAndWait();
                
            } catch (Exception e) {
                Alert a = new Alert(AlertType.ERROR);
                a.setHeaderText("Error al modificar");
                a.setContentText("No se pudo modificar el producto: " + e.getMessage());
                a.showAndWait();
                e.printStackTrace();
            }
        }
    }

    // Método para obtener las transacciones por usuario para un producto específico
    private Map<Integer, Integer> obtenerTransaccionesPorUsuario(String tipo, long codigoBarras) {
        Map<Integer, Integer> transaccionesPorUsuario = new HashMap<>();
        
        try {
            String query = "SELECT Id_Usuario, COUNT(*) as Cantidad " +
                          "FROM Recicla " +
                          "WHERE Tipo = ? AND Numero_barras = ? " +
                          "GROUP BY Id_Usuario";
            
            PreparedStatement pst = StartWin.conn.prepareStatement(query);
            pst.setString(1, tipo);
            pst.setLong(2, codigoBarras);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                int idUsuario = rs.getInt("Id_Usuario");
                int cantidad = rs.getInt("Cantidad");
                transaccionesPorUsuario.put(idUsuario, cantidad);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return transaccionesPorUsuario;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> tipos = MainController.getTiposProductos();
        
        FilteredList<String> filteredItems = new FilteredList<>(tipos, p -> true);
        tipoProducto.setItems(filteredItems);
        
        tipoProducto.getEditor().setText(StorageSharer.itemStorage.get(0));
        codigoBarrasProducto.setText(StorageSharer.itemStorage.get(1));
        nombreProducto.setText(StorageSharer.itemStorage.get(2));
        emisionesProducto.setText(StorageSharer.itemStorage.get(3));
        materialesProducto.setText(StorageSharer.itemStorage.get(4));
        
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