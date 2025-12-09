package com.javafx.reciWins.controllers;

import com.javafx.model.Producto;
import com.javafx.model.Transaccion;
import com.javafx.model.Usuario;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.StorageSharer;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.axes.spi.format.SimpleFormatter;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.dataset.spi.DefaultErrorDataSet;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Random;

public class MainController implements Initializable {

    public static int id_user; 
    private boolean esAdministrador = false;

    @FXML private AnchorPane tab_graph_content;
    @FXML private ScrollPane scrollPaneGraph;
    @FXML private Button btn_graph;

    @FXML private Button btn_addProducto;
    @FXML private Button btn_addTransaccion;
    @FXML private Button btn_addUsuario;
    @FXML private Button btn_exit;
    @FXML private Button btn_personal;
    @FXML private Button btn_products;
    @FXML private Button btn_scan;
    @FXML private Button btn_settings;
    @FXML private Button btn_transactions;
    @FXML private Button btn_users;
    
    @FXML private Button btn_borrarProducto;
    @FXML private Button btn_borrarTransaccion;
    @FXML private Button btn_borrarUsuario;
    
    @FXML private Button btn_modProducto;
    @FXML private Button btn_modTransaccion;
    @FXML private Button btn_modUsuario;
    
    @FXML private TabPane tabMain;
    @FXML private AnchorPane tab_product;
    @FXML private AnchorPane tab_transaccion;
    @FXML private AnchorPane tab_usuario;
    
    @FXML private Label nombreBD;
    @FXML private Label saldoBD;
    @FXML private Label rolBD;
    
    @FXML private Button btn_generarTap;

    @FXML 
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, String> colTipoProducto;

    @FXML
    private TableColumn<Producto, Long> colBarrasProducto;

    @FXML
    private TableColumn<Producto, String> colNombreProducto;

    @FXML
    private TableColumn<Producto, Float> colEmisionesProducto;

    @FXML
    private TableColumn<Producto, String> colMaterialProducto;

    @FXML 
    private TableView<Transaccion> tablaTransacciones;

    @FXML
    private TableColumn<Transaccion, Integer> colUsuarioTransaccion;

    @FXML
    private TableColumn<Transaccion, String> colTipoTransaccion;

    @FXML
    private TableColumn<Transaccion, Long> colBarrasTransaccion;

    @FXML
    private TableColumn<Transaccion, Date> colFechaTransaccion;

    @FXML
    private TableColumn<Transaccion, Time> colHoraTransaccion;

    @FXML 
    private TableView<Usuario> tablaUsuario;

    @FXML
    private TableColumn<Usuario, Integer> colIdUsuario;

    @FXML
    private TableColumn<Usuario, Float> colEmisionesUsuario;

    @FXML
    private TableColumn<Usuario, String> colPermisosUsuario;

    @FXML
    private TableColumn<Usuario, String> colNombreUsuario;

    @FXML
    private TableColumn<Usuario, Integer> colTAPUsuario;

    private Connection conn;

    private static MainController instance;
    
    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        conn = StartWin.conn;
        
        cargarDatosUsuarioActual();
        
        configurarColumnasProductos();
        configurarColumnasTransacciones();
        configurarColumnasUsuarios();
        
        cargarDatosProductos();
        cargarDatosTransacciones();
        cargarDatosUsuarios();
        
        configurarPermisosSegunRol();
        
        configurarTooltips();
        
        tabMain.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getText().equals("Gráfico de Emisiones")) {
                Platform.runLater(() -> {inicializarGrafico();});
            }
        });
        
    }

    private void inicializarGrafico() {
    try {
        tab_graph_content.getChildren().clear();
        
        XYChart chart = new XYChart();
        chart.setPrefSize(800, 400);
        
        DefaultNumericAxis xAxis = new DefaultNumericAxis("Usuarios", 0, 1, 1);
        DefaultNumericAxis yAxis = new DefaultNumericAxis("Emisiones Reducidas (kg CO₂)", 0, 1, 1);
        
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);
        
        chart.getAxes().clear();
        chart.getAxes().addAll(xAxis, yAxis);
        
        ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
        chart.getRenderers().clear();
        chart.getRenderers().add(renderer);
        
        DefaultErrorDataSet dataSet = new DefaultErrorDataSet("Emisiones por Usuario");
        
        if (tablaUsuarioObservable != null && !tablaUsuarioObservable.isEmpty()) {
            final Map<Integer, String> nombresPorIndice = new java.util.HashMap<>();
            
            yAxis.setAutoRanging(true);
            yAxis.setAutoRangePadding(0.1); 
            
            int index = 0;
            for (Usuario usuario : tablaUsuarioObservable) {
                float emisiones = usuario.getEmisionesReducidas();
                String nombre = usuario.getNombre();
                
                String label = nombre.length() > 8 ? 
                    nombre.substring(0, Math.min(12, nombre.length())) : 
                    nombre;
                if (nombre.length() > 12) {
                    label += "...";
                }
                
                nombresPorIndice.put(index, label);
                dataSet.add(index, emisiones, 0, 0);
                index++;
            }
            
            SimpleFormatter formatter = new SimpleFormatter(xAxis) {
                @Override
                public String toString(Number object) {
                    int idx = object.intValue();
                    return nombresPorIndice.getOrDefault(idx, "");
                }
            };
            
            xAxis.setAxisLabelFormatter(formatter);
            
            xAxis.setAutoRanging(true);
            xAxis.setAutoRangePadding(0.2);
            
            renderer.setDrawBars(true);
            renderer.setBarWidth(1); 
            
            chart.getDatasets().add(dataSet);
        } else {
            DefaultErrorDataSet emptyDataSet = new DefaultErrorDataSet("No hay datos disponibles");
            chart.getDatasets().add(emptyDataSet);
        }
        
        chart.setLegendVisible(false);
        
        AnchorPane.setTopAnchor(chart, 10.0);
        AnchorPane.setBottomAnchor(chart, 10.0);
        AnchorPane.setLeftAnchor(chart, 10.0);
        AnchorPane.setRightAnchor(chart, 10.0);
        
        tab_graph_content.getChildren().add(chart);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // MÉTODO PARA ACTUALIZAR TODAS LAS VISTAS
    public void actualizarTodasLasVistas() {
        cargarDatosUsuarioActual();
        cargarDatosProductos();
        cargarDatosTransacciones();
        cargarDatosUsuarios();
        
        // Actualizar el gráfico si está visible
        if (tabMain.getSelectionModel().getSelectedItem().getText().equals("Gráfico de Emisiones")) {
            inicializarGrafico();
        }
    }
    
    public static void actualizarVistasDesdeExterno() {
        if (instance != null) {
            Platform.runLater(() -> {
                instance.actualizarTodasLasVistas();
            });
        }
    }
    
    private void actualizarDatosUsuarioActualEnVista() {
        try {
            String query = "SELECT * FROM Usuarios WHERE Id_Usuario = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, id_user);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                nombreBD.setText(rs.getString("Nombre"));
                saldoBD.setText(String.format("%.1f", rs.getFloat("Emisiones_Reducidas")) + " kg CO₂");
                
                String permisos = rs.getString("Permisos");
                esAdministrador = permisos != null && permisos.equalsIgnoreCase("administrador");
                rolBD.setText(permisos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void tab_graph(ActionEvent event) {
        deseleccionarTodos();
        tabMain.getSelectionModel().select(2); 
        inicializarGrafico(); 
    }

    private void configurarPermisosSegunRol() {
        if (!esAdministrador) {
            tabMain.getTabs().get(1).setDisable(true);
            
            btn_users.setDisable(true);
            btn_products.setDisable(true);
            btn_transactions.setDisable(true);
            
        } else {
            tabMain.getTabs().get(1).setDisable(false);
            btn_users.setDisable(false);
            btn_products.setDisable(false);
            btn_transactions.setDisable(false);
            
            btn_addProducto.setDisable(false);
            btn_borrarProducto.setDisable(false);
            btn_modProducto.setDisable(false);
            btn_addTransaccion.setDisable(false);
            btn_borrarTransaccion.setDisable(false);
            btn_modTransaccion.setDisable(false);
            btn_addUsuario.setDisable(false);
            btn_borrarUsuario.setDisable(false);
            btn_modUsuario.setDisable(false);
        }
    }

    private void configurarTooltips() {
    btn_personal.setTooltip(new Tooltip("Personal"));
    btn_users.setTooltip(new Tooltip("Usuarios"));
    btn_products.setTooltip(new Tooltip("Productos"));
    btn_transactions.setTooltip(new Tooltip("Transacciones"));
    btn_graph.setTooltip(new Tooltip("Gráfico de Emisiones")); 
    btn_settings.setTooltip(new Tooltip("Ajustes"));
    btn_generarTap.setTooltip(new Tooltip("Generar un nuevo número TAP"));
    btn_exit.setTooltip(new Tooltip("Cerrar sesión"));
    }

    private void deseleccionarTodos() {
        if (tablaProductos != null) tablaProductos.getSelectionModel().clearSelection();
        if (tablaTransacciones != null) tablaTransacciones.getSelectionModel().clearSelection();
        if (tablaUsuario != null) tablaUsuario.getSelectionModel().clearSelection();
    }

    private void configurarColumnasProductos() {
        colTipoProducto.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colBarrasProducto.setCellValueFactory(new PropertyValueFactory<>("numeroBarras"));
        colNombreProducto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmisionesProducto.setCellValueFactory(new PropertyValueFactory<>("emisionesReducibles"));
        colMaterialProducto.setCellValueFactory(new PropertyValueFactory<>("material"));
    }

    private void configurarColumnasTransacciones() {
        colUsuarioTransaccion.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colTipoTransaccion.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colBarrasTransaccion.setCellValueFactory(new PropertyValueFactory<>("numeroBarras"));
        colFechaTransaccion.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHoraTransaccion.setCellValueFactory(new PropertyValueFactory<>("hora"));
    }

    private void configurarColumnasUsuarios() {
        colIdUsuario.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colEmisionesUsuario.setCellValueFactory(new PropertyValueFactory<>("emisionesReducidas"));
        colPermisosUsuario.setCellValueFactory(new PropertyValueFactory<>("permisos"));
        colNombreUsuario.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTAPUsuario.setCellValueFactory(new PropertyValueFactory<>("tap"));
    }

    private void cargarDatosUsuarioActual() {
        try {
            String query = "SELECT * FROM Usuarios WHERE Id_Usuario = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, id_user);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                nombreBD.setText(rs.getString("Nombre"));
                saldoBD.setText(String.format("%.1f", rs.getFloat("Emisiones_Reducidas")) + " kg CO₂");
                
                String permisos = rs.getString("Permisos");
                esAdministrador = permisos != null && permisos.equalsIgnoreCase("administrador");
                rolBD.setText(permisos);
            } else {
                nombreBD.setText("Usuario no encontrado");
                saldoBD.setText("0.0 kg CO₂");
                rolBD.setText("N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            nombreBD.setText("Error");
            saldoBD.setText("0.0 kg CO₂");
            rolBD.setText("N/A");
        }
    }

    private void cargarDatosProductos() {
        try {
            String query = "SELECT * FROM Productos";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            tablaProductosObservable = FXCollections.observableArrayList();

            while (rs.next()) {
                Producto producto = new Producto(
                    rs.getString("Tipo"),
                    rs.getLong("Numero_barras"),
                    rs.getString("Nombre"),
                    rs.getFloat("Emisiones_Reducibles"),
                    rs.getString("Material")
                );
                tablaProductosObservable.add(producto);
            }

            if (tablaProductos != null) {
                tablaProductos.setItems(tablaProductosObservable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarDatosTransacciones() {
        try {
            String query = "SELECT * FROM Recicla";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            tablaTransaccionesObservable = FXCollections.observableArrayList();

            while (rs.next()) {
                Transaccion transaccion = new Transaccion(
                    rs.getInt("Id_Usuario"),
                    rs.getString("Tipo"),
                    rs.getLong("Numero_barras"),
                    rs.getDate("Fecha"),
                    rs.getTime("Hora")
                );
                tablaTransaccionesObservable.add(transaccion);
            }

            if (tablaTransacciones != null) {
                tablaTransacciones.setItems(tablaTransaccionesObservable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarDatosUsuarios() {
        try {
            String query = "SELECT * FROM Usuarios";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            tablaUsuarioObservable = FXCollections.observableArrayList();

            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("Id_Usuario"),
                    rs.getFloat("Emisiones_Reducidas"),
                    rs.getString("Permisos"),
                    rs.getString("Nombre"),
                    rs.getInt("TAP")
                );
                tablaUsuarioObservable.add(usuario);
            }

            if (tablaUsuario != null) {
                tablaUsuario.setItems(tablaUsuarioObservable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void launch_modUsuario(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        Usuario m = tablaUsuario.getSelectionModel().getSelectedItem();
        if(m != null) {
            StorageSharer.itemStorage.clear();
            
            StorageSharer.itemStorage.add(m.getIdUsuario()+"");
            StorageSharer.itemStorage.add(m.getNombre());
            StorageSharer.itemStorage.add(m.getPermisos());
            StorageSharer.itemStorage.add(m.getTap()+"");
            StorageSharer.itemStorage.add(m.getEmisionesReducidas()+"");
            StorageSharer.itemToMod = m;
            StorageSharer.itemPre = m;

            StartWin.lanzarModUser();
        } else {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de selección");
            alerta.setContentText("Selecciona un usuario");
            alerta.showAndWait();
        }
    }

    @FXML
    void logout(ActionEvent event) {
        id_user = 0;
        esAdministrador = false;
        StorageSharer.itemStorage.clear();
        StorageSharer.itemToMod = null;
        StorageSharer.itemPre = null;
        
        StartWin.mostrarLogin();
    }

    @FXML
    void launch_settings(ActionEvent event) {
        deseleccionarTodos();
        StartWin.lanzarAjustes();
    }

    @FXML
    void tab_personal(ActionEvent event) {
        deseleccionarTodos();
        tabMain.getSelectionModel().select(0);
    }

    @FXML
    void tab_products(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        deseleccionarTodos();
        tabMain.getSelectionModel().select(1);
        tab_product.setVisible(true);
        tab_transaccion.setVisible(false);
        tab_usuario.setVisible(false);
    }

    @FXML
    void tab_transactions(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        deseleccionarTodos();
        tabMain.getSelectionModel().select(1);
        tab_product.setVisible(false);
        tab_transaccion.setVisible(true);
        tab_usuario.setVisible(false);
    }

    @FXML
    void tab_users(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        deseleccionarTodos();
        tabMain.getSelectionModel().select(1);
        tab_product.setVisible(false);
        tab_transaccion.setVisible(false);
        tab_usuario.setVisible(true);
    }

    @FXML
    void launch_newProducto(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        deseleccionarTodos();
        StartWin.lanzarNuevoProducto();
    }

    @FXML
    void launch_newTransaccion(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        deseleccionarTodos();
        StartWin.lanzarNuevaTransaccion();
    }

    @FXML
    void launch_newUser(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        deseleccionarTodos();
        StartWin.lanzarNuevoUsuario();
    }

    @FXML
    void launch_scan(ActionEvent event) {
        deseleccionarTodos();
        StartWin.lanzarEscanear();
    }

    @FXML
    void borrarProducto(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        Producto producto = tablaProductos.getSelectionModel().getSelectedItem();
        if(producto != null){
            // Primero: Restar emisiones de todos los usuarios que tengan transacciones con este producto
            try {
                // 1. Obtener todas las transacciones de este producto
                String queryTransacciones = "SELECT Id_Usuario, COUNT(*) as cantidad FROM Recicla WHERE Tipo = ? AND Numero_barras = ? GROUP BY Id_Usuario";
                PreparedStatement pstTrans = conn.prepareStatement(queryTransacciones);
                pstTrans.setString(1, producto.getTipo());
                pstTrans.setLong(2, producto.getNumeroBarras());
                ResultSet rsTrans = pstTrans.executeQuery();
                
                // 2. Para cada usuario, restar emisiones
                while (rsTrans.next()) {
                    int idUsuario = rsTrans.getInt("Id_Usuario");
                    int cantidad = rsTrans.getInt("cantidad");
                    float emisionesTotales = producto.getEmisionesReducibles() * cantidad;
                    
                    // Restar emisiones del usuario
                    String updateUsuario = "UPDATE Usuarios SET Emisiones_Reducidas = Emisiones_Reducidas - ? WHERE Id_Usuario = ?";
                    PreparedStatement pstUpdate = conn.prepareStatement(updateUsuario);
                    pstUpdate.setFloat(1, emisionesTotales);
                    pstUpdate.setInt(2, idUsuario);
                    pstUpdate.executeUpdate();
                    
                    // Actualizar observable
                    actualizarEmisionesUsuarioObservable(idUsuario, -emisionesTotales);
                }
                
                // 3. Eliminar todas las transacciones asociadas al producto
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("DELETE FROM Recicla WHERE Tipo = '" + producto.getTipo() + "' AND Numero_barras = '" + producto.getNumeroBarras() + "'");
                
                // 4. Finalmente, eliminar el producto
                stmt.executeUpdate("DELETE FROM Productos WHERE Tipo = '" + producto.getTipo() + "' AND Numero_barras = '" + producto.getNumeroBarras() + "'");
                
                // 5. Actualizar listas observables
                tablaProductosObservable.remove(producto);
                
                // Actualizar transacciones observables
                if (tablaTransaccionesObservable != null) {
                    tablaTransaccionesObservable.removeIf(t -> 
                        t.getTipo().equals(producto.getTipo()) && 
                        t.getNumeroBarras() == producto.getNumeroBarras()
                    );
                }
                
                // 6. Actualizar todas las vistas
                actualizarTodasLasVistas();
                deseleccionarTodos();
                
                Alert alerta = new Alert(AlertType.INFORMATION);
                alerta.setHeaderText("Producto eliminado");
                alerta.setContentText("El producto y todas sus transacciones asociadas han sido eliminados.\nLas emisiones de los usuarios afectados se han actualizado.");
                alerta.showAndWait();
                
            } catch (Exception ex) {
                Alert alerta = new Alert(AlertType.ERROR);
                alerta.setHeaderText("Error al eliminar");
                alerta.setContentText("No se pudo eliminar el producto: " + ex.getMessage());
                alerta.showAndWait();
                ex.printStackTrace();
            }
        } else {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de selección");
            alerta.setContentText("Selecciona un producto para eliminar");
            alerta.showAndWait();
        }
    }

    @FXML
    void borrarTransaccion(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        Transaccion e = tablaTransacciones.getSelectionModel().getSelectedItem();
        if(e!=null){
            try {
                Statement stmt = conn.createStatement();
                float emisionesProducto = obtenerEmisionesProducto(e.getTipo(), e.getNumeroBarras());
                
                stmt.executeUpdate("DELETE FROM Recicla WHERE Fecha = '"+e.getFecha()+"' AND Hora = '"+ e.getHora()+"'");
                
                stmt.executeUpdate(
                    "UPDATE Usuarios SET Emisiones_Reducidas = Emisiones_Reducidas - " 
                    + emisionesProducto + " WHERE Id_Usuario = " + e.getIdUsuario()
                );
                
                actualizarEmisionesUsuarioObservable(e.getIdUsuario(), -emisionesProducto);
                
                if (e.getIdUsuario() == id_user) {
                    cargarDatosUsuarioActual();
                }
                
                tablaTransaccionesObservable.remove(e);
                
                // Actualizar todas las vistas después de borrar
                actualizarTodasLasVistas();
                deseleccionarTodos();
            } catch (Exception ex) {
                Alert alerta = new Alert(AlertType.ERROR);
                alerta.setHeaderText("Error al eliminar");
                alerta.setContentText("No se pudo eliminar la transacción: " + ex.getMessage());
                alerta.showAndWait();
                ex.printStackTrace();
            }
        }else{
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de seleccion");
            alerta.setContentText("Selecciona un elemento");
            alerta.showAndWait();
        }
    }

    public static void actualizarEmisionesUsuarioObservable(int idUsuario, float cambio) {
        for (Usuario usuario : tablaUsuarioObservable) {
            if (usuario.getIdUsuario() == idUsuario) {
                float nuevasEmisiones = usuario.getEmisionesReducidas() + cambio;
                usuario.setEmisionesReducidas(nuevasEmisiones);
                break;
            }
        }
    }

    private float obtenerEmisionesProducto(String tipo, long codigoBarras) {
        try {
            String query = "SELECT Emisiones_Reducibles FROM Productos WHERE Tipo = ? AND Numero_barras = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, tipo);
            pst.setLong(2, codigoBarras);
            ResultSet rs = pst.executeQuery();
            
            if(rs.next()) {
                return rs.getFloat("Emisiones_Reducibles");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0.0f;
    }

    @FXML
    void borrarUsuario(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        Usuario usuario = tablaUsuario.getSelectionModel().getSelectedItem();
        if(usuario != null){
            try {
                // Primero: Obtener todas las transacciones del usuario para restar emisiones
                String queryTransacciones = "SELECT r.Tipo, r.Numero_barras, p.Emisiones_Reducibles " +
                                        "FROM Recicla r " +
                                        "JOIN Productos p ON r.Tipo = p.Tipo AND r.Numero_barras = p.Numero_barras " +
                                        "WHERE r.Id_Usuario = ?";
                PreparedStatement pstTrans = conn.prepareStatement(queryTransacciones);
                pstTrans.setInt(1, usuario.getIdUsuario());
                ResultSet rsTrans = pstTrans.executeQuery();
                
                // Calcular total de emisiones a restar
                float totalEmisiones = 0;
                while (rsTrans.next()) {
                    totalEmisiones += rsTrans.getFloat("Emisiones_Reducibles");
                }
                
                // Restar emisiones del usuario (aunque se vaya a eliminar, mantenemos consistencia)
                // NOTA: En realidad, como el usuario se eliminará, esto no es necesario pero mantiene consistencia
                
                // Eliminar todas las transacciones del usuario
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("DELETE FROM Recicla WHERE Id_Usuario = '" + usuario.getIdUsuario() + "'");
                
                // Finalmente, eliminar el usuario
                stmt.executeUpdate("DELETE FROM Usuarios WHERE Id_Usuario = '" + usuario.getIdUsuario() + "'");
                
                // Actualizar listas observables
                tablaUsuarioObservable.remove(usuario);
                
                // Actualizar transacciones observables
                if (tablaTransaccionesObservable != null) {
                    tablaTransaccionesObservable.removeIf(t -> t.getIdUsuario() == usuario.getIdUsuario());
                }
                
                // Actualizar todas las vistas
                actualizarTodasLasVistas();
                deseleccionarTodos();
                
                Alert alerta = new Alert(AlertType.INFORMATION);
                alerta.setHeaderText("Usuario eliminado");
                alerta.setContentText("El usuario y todas sus transacciones han sido eliminados.");
                alerta.showAndWait();
                
            } catch (Exception ex) {
                Alert alerta = new Alert(AlertType.ERROR);
                alerta.setHeaderText("Error al eliminar");
                alerta.setContentText("No se pudo eliminar el usuario: " + ex.getMessage());
                alerta.showAndWait();
                ex.printStackTrace();
            }
        } else {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de selección");
            alerta.setContentText("Selecciona un usuario para eliminar");
            alerta.showAndWait();
        }
    }

    public static ObservableList<Usuario> tablaUsuarioObservable;
    public static ObservableList<Producto> tablaProductosObservable;
    public static ObservableList<Transaccion> tablaTransaccionesObservable;

    public static ObservableList<String> getTiposProductos() {
        ObservableList<String> tipos = FXCollections.observableArrayList();
        if (tablaProductosObservable != null) {
            Set<String> tipoSet = new HashSet<>();
            for (Producto p : tablaProductosObservable) {
                if (p.getTipo() != null && !p.getTipo().trim().isEmpty()) {
                    tipoSet.add(p.getTipo());
                }
            }
            tipos.addAll(tipoSet);
            FXCollections.sort(tipos);
        }
        return tipos;
    }

    public static ObservableList<Long> getCodigosBarras() {
        ObservableList<Long> codigos = FXCollections.observableArrayList();
        if (tablaProductosObservable != null) {
            Set<Long> codigoSet = new HashSet<>();
            for (Producto p : tablaProductosObservable) {
                codigoSet.add(p.getNumeroBarras());
            }
            codigos.addAll(codigoSet);
            codigos.sort((a, b) -> Long.compare(a, b));
        }
        return codigos;
    }

    public static ObservableList<String> getNombresUsuarios() {
        ObservableList<String> nombres = FXCollections.observableArrayList();
        if (tablaUsuarioObservable != null) {
            for (Usuario u : tablaUsuarioObservable) {
                String display = u.getIdUsuario() + " - " + u.getNombre();
                nombres.add(display);
            }
            FXCollections.sort(nombres);
        }
        return nombres;
    }

    public static int obtenerIdUsuarioDesdeBusqueda(String busqueda) {
        if (busqueda == null || busqueda.trim().isEmpty()) return -1;
        
        String[] partes = busqueda.split(" - ");
        if (partes.length > 0) {
            try {
                return Integer.parseInt(partes[0].trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    public static void modItem(){
        Object o = StorageSharer.itemToMod;
        Object op = StorageSharer.itemPre;
        if(o instanceof Usuario && op instanceof Usuario){
            int reps = tablaUsuarioObservable.size();
            for(int i = 0; i<reps ; i++ ){
                if(tablaUsuarioObservable.get(i).equals(op)){
                    tablaUsuarioObservable.set(i,(Usuario) o);
                }
            }
        }else if(o instanceof Producto && op instanceof Producto){
            int reps = tablaProductosObservable.size();
            for(int i = 0; i<reps ; i++ ){
                if(tablaProductosObservable.get(i).equals(op)){
                    tablaProductosObservable.set(i,(Producto) o);
                }
            }
        }else if(o instanceof Transaccion && op instanceof Transaccion){
            int reps = tablaTransaccionesObservable.size();
            for(int i = 0; i<reps ; i++ ){
                if(tablaTransaccionesObservable.get(i).equals(op)){
                    tablaTransaccionesObservable.set(i,(Transaccion) o);
                }
            }
        }
        // Actualizar las vistas
        MainController controller = getInstance();
        if (controller != null) {
            if (o instanceof Usuario) {
                Usuario usuarioModificado = (Usuario) o;
                controller.tablaUsuario.refresh();
                controller.cargarDatosUsuarios();
                // Actualizar TODAS las vistas
                controller.actualizarTodasLasVistas();
                // Si es el usuario actual, actualizar también la vista personal
                if (usuarioModificado.getIdUsuario() == id_user) {
                    controller.actualizarDatosUsuarioActualEnVista();
                }
            } else if (o instanceof Producto) {
                controller.tablaProductos.refresh();
                controller.cargarDatosProductos();
                // Actualizar TODAS las vistas
                controller.actualizarTodasLasVistas();
            } else if (o instanceof Transaccion) {
                Transaccion transaccionModificada = (Transaccion) o;
                controller.tablaTransacciones.refresh();
                controller.cargarDatosTransacciones();
                controller.cargarDatosUsuarios();
                // Actualizar TODAS las vistas
                controller.actualizarTodasLasVistas();
                // Si la transacción es del usuario actual, actualizar la vista personal
                if (transaccionModificada.getIdUsuario() == id_user) {
                    controller.actualizarDatosUsuarioActualEnVista();
                }
            }
        }
    }

    // Método público para actualizar la vista personal desde otros controladores
    public static void actualizarVistaPersonal() {
        MainController controller = getInstance();
        if (controller != null) {
            controller.actualizarDatosUsuarioActualEnVista();
        }
    }

    @FXML
    void launch_modProducto(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        Producto m = tablaProductos.getSelectionModel().getSelectedItem();
        if(m != null) {
            StorageSharer.itemStorage.clear();
            
            StorageSharer.itemStorage.add(m.getTipo());
            StorageSharer.itemStorage.add(m.getNumeroBarras() + "");
            StorageSharer.itemStorage.add(m.getNombre());
            StorageSharer.itemStorage.add(m.getEmisionesReducibles() + "");
            StorageSharer.itemStorage.add(m.getMaterial());
            StorageSharer.itemToMod = m;
            StorageSharer.itemPre = m;
            
            StartWin.lanzarModProducto();
        } else {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de selección");
            alerta.setContentText("Selecciona un producto");
            alerta.showAndWait();
        }
    }

    @FXML
    void launch_modTransaccion(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }
        
        Transaccion m = tablaTransacciones.getSelectionModel().getSelectedItem();
        if(m != null) {
            StorageSharer.itemStorage.clear();
            
            StorageSharer.itemStorage.add(m.getIdUsuario() + "");
            StorageSharer.itemStorage.add(m.getTipo());
            StorageSharer.itemStorage.add(m.getNumeroBarras() + "");
            StorageSharer.itemStorage.add(m.getFecha().toString());
            StorageSharer.itemStorage.add(m.getHora().toString());
            StorageSharer.itemToMod = m;
            StorageSharer.itemPre = m;
            
            StartWin.lanzarModTransaccion();
        } else {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de selección");
            alerta.setContentText("Selecciona una transacción");
            alerta.showAndWait();
        }
    }
    
    @FXML
    void generarTap(ActionEvent event) {
        Random random = new Random();
        int nuevoTap = 100000 + random.nextInt(900000);
        
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(
                "UPDATE Usuarios SET TAP = " + nuevoTap + " WHERE Id_Usuario = " + id_user
            );
            
            for (Usuario usuario : tablaUsuarioObservable) {
                if (usuario.getIdUsuario() == id_user) {
                    usuario.setTap(nuevoTap);
                    break;
                }
            }
            
            cargarDatosUsuarios();
            actualizarDatosUsuarioActualEnVista();
            
            Alert alerta = new Alert(AlertType.INFORMATION);
            alerta.setHeaderText("Nuevo TAP generado");
            alerta.setContentText("Tu nuevo número TAP es: " + nuevoTap);
            alerta.showAndWait();
            
        } catch (Exception e) {
            Alert alerta = new Alert(AlertType.ERROR);
            alerta.setHeaderText("Error al generar TAP");
            alerta.setContentText("No se pudo generar el nuevo TAP: " + e.getMessage());
            alerta.showAndWait();
            e.printStackTrace();
        }
    }
    
    private void mostrarErrorAcceso() {
        Alert alerta = new Alert(AlertType.WARNING);
        alerta.setHeaderText("Acceso denegado");
        alerta.setContentText("Esta función solo está disponible para administradores.");
        alerta.showAndWait();
    }
}