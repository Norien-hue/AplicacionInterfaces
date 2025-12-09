package com.javafx.reciWins.controllers;

import java.net.URL;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import com.javafx.model.Transaccion;
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
import javafx.stage.Stage;

public class Escanear implements Initializable {

    @FXML
    private Button btn_cancelar;
    
    @FXML
    private Button btn_aceptar;
    
    @FXML
    private ComboBox<String> tipoProducto;
    
    @FXML
    private ComboBox<String> codigoBarras;
    
    @FXML
    private TextField emisionesField;
    
    @FXML
    private TextField tapField;
    
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
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("TAP inválido");
                alert.setContentText("El TAP debe ser un número de 6 dígitos.");
                alert.showAndWait();
                return;
            }
            
            int tapUsuario = obtenerTapUsuarioActual();
            if (tapUsuario == -1) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Error de usuario");
                alert.setContentText("No se pudo verificar el usuario. Vuelve a iniciar sesión.");
                alert.showAndWait();
                return;
            }
            
            if (tapIngresado != tapUsuario) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("TAP incorrecto");
                alert.setContentText("El TAP ingresado no coincide con tu código personal.\n" +
                                   "Verifica tu TAP en la pestaña Personal.");
                alert.showAndWait();
                tapField.clear();
                tapField.requestFocus();
                return;
            }
            
            String tipo = tipoProducto.getValue();
            long codigo = Long.parseLong(codigoBarras.getValue());
            
            if(!productoExisteEnBD(tipo, codigo)) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Producto no encontrado");
                alert.setContentText("El producto con tipo '" + tipo + "' y código '" + codigo + "' no existe.\n" +
                                   "Debes crear el producto primero en la sección de Productos.");
                alert.showAndWait();
                return;
            }
            
            float emisionesProducto = obtenerEmisionesProducto(tipo, codigo);
            
            LocalDate fechaActual = LocalDate.now();
            LocalTime horaActual = LocalTime.now();
            
            Date fecha = Date.valueOf(fechaActual);
            Time hora = Time.valueOf(horaActual);
            
            int idUsuario = MainController.id_user;
            
            try {
                Statement stmt = StartWin.conn.createStatement();
                stmt.executeUpdate(
                    "INSERT INTO Recicla (Id_Usuario, Tipo, Numero_barras, Fecha, Hora) VALUES ('" 
                    + idUsuario + "', '" 
                    + tipo + "', '" 
                    + codigo + "', '" 
                    + fecha + "', '" 
                    + hora + "')"
                );
                
                stmt.executeUpdate(
                    "UPDATE Usuarios SET Emisiones_Reducidas = Emisiones_Reducidas + " 
                    + emisionesProducto + " WHERE Id_Usuario = " + idUsuario
                );
                
                MainController.actualizarEmisionesUsuarioObservable(idUsuario, emisionesProducto);
                
                Transaccion nuevaTransaccion = new Transaccion(idUsuario, tipo, codigo, fecha, hora);
                if (MainController.tablaTransaccionesObservable != null) {
                    MainController.tablaTransaccionesObservable.add(nuevaTransaccion);
                }
                
                // ACTUALIZAR TODAS LAS VISTAS DESPUÉS DEL ESCANEO
                MainController.actualizarVistasDesdeExterno();
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("Escaneo completado");
                alert.setContentText("Producto escaneado correctamente.\n" +
                                   "Se han añadido " + emisionesProducto + " kg CO₂ a tu cuenta.");
                alert.showAndWait();
                
                ((Stage)btn_cancelar.getScene().getWindow()).close();
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Error al escanear");
                alert.setContentText("No se pudo registrar el escaneo: " + e.getMessage());
                alert.showAndWait();
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
        
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
            }
        });
    }

    private void actualizarEmisiones() {
        String tipo = tipoProducto.getValue();
        String codigo = codigoBarras.getValue();
        
        if (tipo != null && codigo != null && !tipo.trim().isEmpty() && !codigo.trim().isEmpty()) {
            try {
                long codigoBarrasLong = Long.parseLong(codigo.trim());
                float emisiones = obtenerEmisionesProducto(tipo, codigoBarrasLong);
                emisionesField.setText(String.format("%.1f", emisiones));
            } catch (NumberFormatException e) {
                emisionesField.setText("0.0");
            }
        } else {
            emisionesField.setText("0.0");
        }
    }

    private static boolean checkAlert = false;
    private static String alertMessage = "";

    private void checkForAlertEscanear() {
        alertMessage = "";
        
        if(tipoProducto.getValue() == null || tipoProducto.getValue().trim().isEmpty()) {
            checkAlert = true;
            alertMessage = "Debes seleccionar un tipo de producto";
            return;
        }

        if(codigoBarras.getValue() == null || codigoBarras.getValue().trim().isEmpty()) {
            checkAlert = true;
            alertMessage = "Debes seleccionar o escribir un código de barras";
            return;
        }
        
        try {
            Long.parseLong(codigoBarras.getValue().trim());
        } catch (NumberFormatException e) {
            checkAlert = true;
            alertMessage = "El código de barras debe ser un número válido";
            return;
        }

        if(tapField.getText() == null || tapField.getText().trim().isEmpty()) {
            checkAlert = true;
            alertMessage = "Debes introducir tu TAP";
            return;
        }
        
        try {
            int tap = Integer.parseInt(tapField.getText().trim());
            if (String.valueOf(tap).length() != 6) {
                checkAlert = true;
                alertMessage = "El TAP debe tener exactamente 6 dígitos";
                return;
            }
        } catch (NumberFormatException e) {
            checkAlert = true;
            alertMessage = "El TAP debe ser un número de 6 dígitos";
            return;
        }
    }

    private boolean launchAlertsEscanear() {
        checkForAlertEscanear();
        boolean ret = false;

        if(checkAlert) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Error en los campos");
            a.setContentText(alertMessage + "\n\nAsegúrate de que:\n- Has seleccionado un tipo de producto\n- Has seleccionado o escrito un código de barras válido\n- Has introducido tu TAP de 6 dígitos");
            a.showAndWait();
            ret = true;
        }

        checkAlert = false;
        alertMessage = "";
        return ret;
    }

    private boolean productoExisteEnBD(String tipo, long codigoBarras) {
        try {
            String query = "SELECT COUNT(*) FROM Productos WHERE Tipo = ? AND Numero_barras = ?";
            PreparedStatement pst = StartWin.conn.prepareStatement(query);
            pst.setString(1, tipo);
            pst.setLong(2, codigoBarras);
            ResultSet rs = pst.executeQuery();
            
            if(rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private float obtenerEmisionesProducto(String tipo, long codigoBarras) {
        try {
            String query = "SELECT Emisiones_Reducibles FROM Productos WHERE Tipo = ? AND Numero_barras = ?";
            PreparedStatement pst = StartWin.conn.prepareStatement(query);
            pst.setString(1, tipo);
            pst.setLong(2, codigoBarras);
            ResultSet rs = pst.executeQuery();
            
            if(rs.next()) {
                return rs.getFloat("Emisiones_Reducibles");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0f;
    }
    
    private int obtenerTapUsuarioActual() {
        try {
            String query = "SELECT TAP FROM Usuarios WHERE Id_Usuario = ?";
            PreparedStatement pst = StartWin.conn.prepareStatement(query);
            pst.setInt(1, MainController.id_user);
            ResultSet rs = pst.executeQuery();
            
            if(rs.next()) {
                return rs.getInt("TAP");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}