package com.javafx.reciWins.controllers;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.ResourceBundle;

import com.javafx.model.Producto;
import com.javafx.reciWins.start.StartWin;

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
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class NewProducto implements Initializable {

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
    private ComboBox<String> materialesProducto; // Cambiado de TextField a ComboBox<String>

    @FXML
    private TextField rutaImagenProducto;

    @FXML
    private Button btn_aceptar;

    @FXML
    void kill(ActionEvent event) {
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @FXML
    void crearProducto(ActionEvent event) {
        if(!launchAlertsNewProducto()) {
            
            // Verificar conexión antes de proceder
            if (!StartWin.verificarConexion()) {
                StartWin.manejarPerdidaConexion("La conexión con la base de datos no está disponible");
                return;
            }
            
            String nombre = nombreProducto.getText().trim();
            String tipo = tipoProducto.getEditor().getText().trim();
            long codigoBarras = Long.parseLong(codigoBarrasProducto.getText().trim());
            float emisiones = Float.parseFloat(emisionesProducto.getText().trim());
            String material = materialesProducto.getValue(); // Cambiado de getText() a getValue()
            
            if (material == null || material.trim().isEmpty()) {
                Alert a = new Alert(AlertType.ERROR);
                a.setOnShown(ex -> {
                    Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                a.setHeaderText("Material requerido");
                a.setContentText("Debes seleccionar un material de la lista.");
                a.showAndWait();
                return;
            }
            
            // Procesar imagen si se indicó ruta
            String imagenBase64 = null;
            String ruta = rutaImagenProducto.getText() == null ? "" : rutaImagenProducto.getText().trim();
            if (!ruta.isEmpty()) {
                try {
                    File f = new File(ruta);
                    if (!f.exists() || !f.isFile()) {
                        Alert a = new Alert(AlertType.ERROR);
                        a.setOnShown(ex -> {
                            Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(StartWin.icon);
                        });
                        a.setHeaderText("Imagen no encontrada");
                        a.setContentText("La ruta de imagen no es válida: " + ruta);
                        a.showAndWait();
                        return;
                    }
                    byte[] bytes = Files.readAllBytes(f.toPath());
                    imagenBase64 = Base64.getEncoder().encodeToString(bytes);
                } catch (Exception ex) {
                    Alert a = new Alert(AlertType.ERROR);
                    a.setOnShown(e2 -> {
                        Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                        stage.getIcons().add(StartWin.icon);
                    });
                    a.setHeaderText("Error al leer imagen");
                    a.setContentText("No se pudo leer la imagen: " + ex.getMessage());
                    a.showAndWait();
                    return;
                }
            }

            try {
                PreparedStatement pst = StartWin.conn.prepareStatement(
                    "INSERT INTO Productos (Tipo, Numero_barras, Nombre, Emisiones_Reducibles, Material, Imagen) VALUES (?, ?, ?, ?, ?, ?)"
                );
                pst.setString(1, tipo);
                pst.setLong(2, codigoBarras);
                pst.setString(3, nombre);
                pst.setFloat(4, emisiones);
                pst.setString(5, material);
                if (imagenBase64 == null) {
                    pst.setNull(6, java.sql.Types.LONGVARCHAR);
                } else {
                    pst.setString(6, imagenBase64);
                }
                pst.executeUpdate();

                Producto nuevoProducto = new Producto(tipo, codigoBarras, nombre, emisiones, material, imagenBase64);
                MainController.tablaProductosObservable.add(nuevoProducto);
                
                // ACTUALIZAR TODAS LAS VISTAS DESPUÉS DE CREAR
                MainController.actualizarVistasDesdeExterno();
                
                ((Stage)btn_cancelar.getScene().getWindow()).close();
            } catch (SQLException e) {
                // Detectar si es un error de conexión
                if (esErrorDeConexion(e)) {
                    System.err.println("Error de conexión detectado: " + e.getMessage());
                    StartWin.manejarPerdidaConexion(e.getMessage());
                } else {
                Alert a = new Alert(AlertType.ERROR);
                a.setOnShown(ex -> {
                    Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                a.setHeaderText("Error al guardar");
                a.setContentText("No se pudo guardar el producto: " + e.getMessage());
                a.showAndWait();
                e.printStackTrace();
            
                }
            } catch (Exception e) {
                Alert a = new Alert(AlertType.ERROR);
                a.setOnShown(ex -> {
                    Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                a.setHeaderText("Error al guardar");
                a.setContentText("No se pudo guardar el producto: " + e.getMessage());
                a.showAndWait();
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
        
        // Validar que se haya seleccionado un material
        if(materialesProducto.getValue() == null || materialesProducto.getValue().trim().isEmpty()) {
            checkAlert = true;
            alertMessage = "Debes seleccionar un material de la lista";
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

    private boolean launchAlertsNewProducto() {
        checkForAlertNewProducto();
        boolean ret = false;

        if(checkAlert) {
            Alert a = new Alert(AlertType.ERROR);
            a.setOnShown(ex -> {
                    Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
            a.setHeaderText("Error en los campos");
            a.setContentText(alertMessage + "\n\nAsegúrate de que:\n- Todos los campos están completos\n- El código de barras es numérico\n- Las emisiones son numéricas\n- No uses caracteres especiales: @, ?, =, ', \", |, *, &, +, \\\n- Selecciona un material de la lista");
            a.showAndWait();
            ret = true;
        }

        checkAlert = false;
        alertMessage = "";
        return ret;
    }

    /**
     * Determina si una SQLException es debido a un problema de conexión
     * @param e La excepción SQL a verificar
     * @return true si es un error de conexión, false en caso contrario
     */
    private boolean esErrorDeConexion(SQLException e) {
        // Códigos de error comunes para problemas de conexión
        String sqlState = e.getSQLState();
        String mensaje = e.getMessage().toLowerCase();
        
        // SQLState codes para problemas de comunicación
        if (sqlState != null && (
            sqlState.startsWith("08") ||  // Connection exception
            sqlState.equals("HY000"))) {   // General error (puede ser conexión)
            return true;
        }
        
        // Mensajes comunes de error de conexión
        if (mensaje.contains("connection") || 
            mensaje.contains("timeout") ||
            mensaje.contains("closed") ||
            mensaje.contains("socket") ||
            mensaje.contains("communications link failure")) {
            return true;
        }
        
        return false;
    }
}