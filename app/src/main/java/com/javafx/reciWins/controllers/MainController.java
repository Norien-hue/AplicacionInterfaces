package com.javafx.reciWins.controllers;

import com.javafx.model.Producto;
import com.javafx.model.Transaccion;
import com.javafx.model.Usuario;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.SQLstatementStorage;
import com.javafx.reciWins.utiles.StorageSharer;

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
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class MainController implements Initializable {

    public static int id_user; 

    @FXML private Button btn_addProducto;
    @FXML private Button btn_addTransaccion;
    @FXML private Button btn_addUsuario;
    @FXML private Button btn_exit;
    @FXML private Button btn_info;
    @FXML private Button btn_personal;
    @FXML private Button btn_products;
    @FXML private Button btn_save;
    @FXML private Button btn_scan;
    @FXML private Button btn_settings;
    @FXML private Button btn_transactions;
    @FXML private Button btn_users;
    @FXML private TabPane tabMain;
    @FXML private AnchorPane tab_product;
    @FXML private AnchorPane tab_transaccion;
    @FXML private AnchorPane tab_usuario;
    
    // Labels para la pestaña personal
    @FXML private Label nombreBD;
    @FXML private Label saldoBD;
    @FXML private Label tapBD;
    @FXML private Label rolBD;

    // TableView y columnas para Productos
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

    // TableView y columnas para Transacciones
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

    // TableView y columnas para Usuarios
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conn = StartWin.conn;
        configurarColumnasProductos();
        configurarColumnasTransacciones();
        configurarColumnasUsuarios();
        
        cargarDatosProductos();
        cargarDatosTransacciones();
        cargarDatosUsuarios();
        
        // Cargar datos del usuario actual en la pestaña personal
        cargarDatosUsuarioActual();
        
        // NUEVO: Configurar tooltips
        configurarTooltips();
    }

    // NUEVO MÉTODO: Configurar tooltips para los botones
    private void configurarTooltips() {
        btn_personal.setTooltip(new Tooltip("Personal"));
        btn_users.setTooltip(new Tooltip("Usuarios"));
        btn_products.setTooltip(new Tooltip("Productos"));
        btn_transactions.setTooltip(new Tooltip("Transacciones"));
        btn_settings.setTooltip(new Tooltip("Ajustes"));
    }

    // NUEVO MÉTODO: Deseleccionar todos los elementos de las tablas
    private void deseleccionarTodos() {
        tablaProductos.getSelectionModel().clearSelection();
        tablaTransacciones.getSelectionModel().clearSelection();
        tablaUsuario.getSelectionModel().clearSelection();
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

    // Método para cargar los datos del usuario actual en la pestaña personal
    private void cargarDatosUsuarioActual() {
        try {
            String query = "SELECT * FROM Usuarios WHERE Id_Usuario = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, id_user);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                nombreBD.setText(rs.getString("Nombre"));
                saldoBD.setText(String.format("%.1f", rs.getFloat("Emisiones_Reducidas")) + " kg CO₂");
                tapBD.setText(String.valueOf(rs.getInt("TAP")));
                rolBD.setText(rs.getString("Permisos"));
            } else {
                nombreBD.setText("Usuario no encontrado");
                saldoBD.setText("0.0 kg CO₂");
                tapBD.setText("0");
                rolBD.setText("N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            nombreBD.setText("Error");
            saldoBD.setText("0.0 kg CO₂");
            tapBD.setText("0");
            rolBD.setText("N/A");
        }
    }

    @FXML
    void saveClicked(ActionEvent event) {
        if(SQLstatementStorage.preparedStatements.size()>0){
           SQLstatementStorage.executeStatements();

            Alert a = new Alert(AlertType.INFORMATION);

            a.setHeaderText("Cambios guardados");
            a.setContentText("Se ha guardado correctamente los cambios");

            a.showAndWait();

            tablaProductos.refresh();
            tablaUsuario.refresh();
            tablaTransacciones.refresh();
            
            // Actualizar los datos del usuario actual después de guardar
            cargarDatosUsuarioActual();
            
            // NUEVO: Deseleccionar después de guardar
            deseleccionarTodos();
        }else{
            Alert a = new Alert(AlertType.WARNING);

            a.setHeaderText("No hay cambios");
            a.setContentText("No se pudieron guardar cambios, no hay cambios");

            a.showAndWait();
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

            tablaProductos.setItems(tablaProductosObservable);
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

            tablaTransacciones.setItems(tablaTransaccionesObservable);
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

            tablaUsuario.setItems(tablaUsuarioObservable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void launch_modUsuario(ActionEvent event) {
        Usuario m = tablaUsuario.getSelectionModel().getSelectedItem();
        if(m != null) {
            // MODIFICADO: Limpiar ANTES de agregar
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
    void kill_unsafe(ActionEvent event) {
        if(SQLstatementStorage.preparedStatements.size()>0){
            Alert a = new Alert(AlertType.CONFIRMATION);
            a.setHeaderText("Cambios sin guardar");
            a.setContentText("¿Está seguro de que quiere salir sin guardar los cambios?");

            Optional<ButtonType> botonPulsado = a.showAndWait();

            if(botonPulsado.isPresent() && botonPulsado.get().equals(ButtonType.OK)) {
                Platform.exit();
                System.exit(0);
            }
        } else {
            Platform.exit();
            System.exit(0);
        }
    }

    @FXML
    void launch_settings(ActionEvent event) {
        // NUEVO: Deseleccionar al cambiar de vista
        deseleccionarTodos();
        StartWin.lanzarAjustes();
    }

    @FXML
    void tab_personal(ActionEvent event) {
        // NUEVO: Deseleccionar al cambiar de pestaña
        deseleccionarTodos();
        tabMain.getSelectionModel().select(0);
    }

    @FXML
    void tab_products(ActionEvent event) {
        // NUEVO: Deseleccionar al cambiar de pestaña
        deseleccionarTodos();
        tabMain.getSelectionModel().select(1);
        tab_product.setVisible(true);
        tab_transaccion.setVisible(false);
        tab_usuario.setVisible(false);
    }

    @FXML
    void tab_transactions(ActionEvent event) {
        // NUEVO: Deseleccionar al cambiar de pestaña
        deseleccionarTodos();
        tabMain.getSelectionModel().select(1);
        tab_product.setVisible(false);
        tab_transaccion.setVisible(true);
        tab_usuario.setVisible(false);
    }

    @FXML
    void tab_users(ActionEvent event) {
        // NUEVO: Deseleccionar al cambiar de pestaña
        deseleccionarTodos();
        tabMain.getSelectionModel().select(1);
        tab_product.setVisible(false);
        tab_transaccion.setVisible(false);
        tab_usuario.setVisible(true);
    }

    @FXML
    void launch_newProducto(ActionEvent event) {
        // NUEVO: Deseleccionar antes de crear nuevo
        deseleccionarTodos();
        StartWin.lanzarNuevoProducto();
    }

    @FXML
    void launch_newTransaccion(ActionEvent event) {
        // NUEVO: Deseleccionar antes de crear nuevo
        deseleccionarTodos();
        StartWin.lanzarNuevaTransaccion();
    }

    @FXML
    void launch_newUser(ActionEvent event) {
        // NUEVO: Deseleccionar antes de crear nuevo
        deseleccionarTodos();
        StartWin.lanzarNuevoUsuario();
    }

    @FXML
    void launch_scan(ActionEvent event) {
        // NUEVO: Deseleccionar antes de escanear
        deseleccionarTodos();
        StartWin.lanzarEscanear();
    }

    @FXML
    void borrarProducto(ActionEvent event) {
        Producto e = tablaProductos.getSelectionModel().getSelectedItem();
        if(e!=null){
            SQLstatementStorage.storeStatement("DELETE FROM Productos WHERE Numero_barras = '"+e.getNumeroBarras()+"' AND Tipo = '"+ e.getTipo()+"'");
            tablaProductosObservable.remove(e);
            // NUEVO: Deseleccionar después de borrar
            deseleccionarTodos();
        }else{
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de seleccion");
            alerta.setContentText("Selecciona un elemento");
            alerta.showAndWait();
        }
    }

    @FXML
    void borrarTransaccion(ActionEvent event) {
        Transaccion e = tablaTransacciones.getSelectionModel().getSelectedItem();
        if(e!=null){
            // Obtener emisiones del producto para restarlas
            float emisionesProducto = obtenerEmisionesProducto(e.getTipo(), e.getNumeroBarras());
            
            // Sentencia 1: Borrar transacción
            SQLstatementStorage.storeStatement("DELETE FROM Recicla WHERE Fecha = '"+e.getFecha()+"' AND Hora = '"+ e.getHora()+"'");
            
            // Sentencia 2: Restar emisiones al usuario
            SQLstatementStorage.storeStatement(
                "UPDATE Usuarios SET Emisiones_Reducidas = Emisiones_Reducidas - " 
                + emisionesProducto + " WHERE Id_Usuario = " + e.getIdUsuario()
            );
            
            // Actualizar el observable de usuarios para reflejar el cambio en la tabla
            actualizarEmisionesUsuarioObservable(e.getIdUsuario(), -emisionesProducto);
            
            // Actualizar datos del usuario actual si es el afectado
            if (e.getIdUsuario() == id_user) {
                cargarDatosUsuarioActual();
            }
            
            tablaTransaccionesObservable.remove(e);
            // NUEVO: Deseleccionar después de borrar
            deseleccionarTodos();
        }else{
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de seleccion");
            alerta.setContentText("Selecciona un elemento");
            alerta.showAndWait();
        }
    }

    // Método para actualizar las emisiones de un usuario en el observable
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
        Usuario e = tablaUsuario.getSelectionModel().getSelectedItem();
        if(e!=null){
            SQLstatementStorage.storeStatement("DELETE FROM Usuarios WHERE Id_Usuario = '"+e.getIdUsuario()+"'");
            tablaUsuarioObservable.remove(e);
            // NUEVO: Deseleccionar después de borrar
            deseleccionarTodos();
        }else{
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de seleccion");
            alerta.setContentText("Selecciona un elemento");
            alerta.showAndWait();
        }
    }

    public static ObservableList<Usuario> tablaUsuarioObservable;
    public static ObservableList<Producto> tablaProductosObservable;
    public static ObservableList<Transaccion> tablaTransaccionesObservable;

    // NUEVO: Método para obtener tipos de productos únicos
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

    // NUEVO: Método para obtener códigos de barras únicos
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

    // NUEVO: Método para obtener nombres de usuarios únicos (para búsqueda)
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

    // NUEVO: Método para obtener ID de usuario desde texto de búsqueda
    public static int obtenerIdUsuarioDesdeBusqueda(String busqueda) {
        if (busqueda == null || busqueda.trim().isEmpty()) return -1;
        
        // Formato: "ID - Nombre"
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
    }

    @FXML
    void launch_modProducto(ActionEvent event) {
        Producto m = tablaProductos.getSelectionModel().getSelectedItem();
        if(m != null) {
            // MODIFICADO: Limpiar ANTES de agregar
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
        Transaccion m = tablaTransacciones.getSelectionModel().getSelectedItem();
        if(m != null) {
            // MODIFICADO: Limpiar ANTES de agregar
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
}