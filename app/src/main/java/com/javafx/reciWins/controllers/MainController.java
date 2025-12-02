package com.javafx.reciWins.controllers;

import com.javafx.model.Producto;
import com.javafx.model.Transaccion;
import com.javafx.model.Usuario;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.SQLstatementStorage;
import com.javafx.reciWins.utiles.StorageSharer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MainController implements Initializable {

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

    @FXML
    void saveClicked(ActionEvent event) {
        SQLstatementStorage.executeStatements();
        tablaProductos.refresh();
        tablaUsuario.refresh();
        tablaTransacciones.refresh();
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
    void launch_modProducto(ActionEvent event) {
        StartWin.lanzarModProducto();
    }

    @FXML
    void launch_modTransaccion(ActionEvent event) {
        StartWin.lanzarModTransaccion();
    }

    @FXML
    void launch_modUsuario(ActionEvent event) {
        Usuario m = tablaUsuario.getSelectionModel().getSelectedItem();
        StorageSharer.itemStorage.add(m.getIdUsuario()+"");
        StorageSharer.itemStorage.add(m.getNombre());
        StorageSharer.itemStorage.add(m.getPermisos());
        StorageSharer.itemStorage.add(m.getTap()+"");
        StorageSharer.itemStorage.add(m.getEmisionesReducidas()+"");
        StorageSharer.itemToMod = m;
        
        StorageSharer.itemPre = m;

        StartWin.lanzarModUser();
    }

    @FXML
    void kill_unsafe(ActionEvent event) {
        ((Stage)this.btn_exit.getScene().getWindow()).close();
    }

    @FXML
    void launch_settings(ActionEvent event) {
        StartWin.lanzarAjustes();
    }

    @FXML
    void tab_personal(ActionEvent event) {
        tabMain.getSelectionModel().select(0);
    }

    @FXML
    void tab_products(ActionEvent event) {
        tabMain.getSelectionModel().select(1);
        tab_product.setVisible(true);
        tab_transaccion.setVisible(false);
        tab_usuario.setVisible(false);
    }

    @FXML
    void tab_transactions(ActionEvent event) {
        tabMain.getSelectionModel().select(1);
        tab_product.setVisible(false);
        tab_transaccion.setVisible(true);
        tab_usuario.setVisible(false);
    }

    @FXML
    void tab_users(ActionEvent event) {
        tabMain.getSelectionModel().select(1);
        tab_product.setVisible(false);
        tab_transaccion.setVisible(false);
        tab_usuario.setVisible(true);
    }

    @FXML
    void launch_newProducto(ActionEvent event) {
        StartWin.lanzarNuevoProducto();
    }

    @FXML
    void launch_newTransaccion(ActionEvent event) {
        StartWin.lanzarNuevaTransaccion();
    }

    @FXML
    void launch_newUser(ActionEvent event) {
        StartWin.lanzarNuevoUsuario();
    }

    @FXML
    void launch_scan(ActionEvent event) {
        StartWin.lanzarEscanear();
    }

    @FXML
    void borrarProducto(ActionEvent event) {
        Producto e = tablaProductos.getSelectionModel().getSelectedItem();
        if(e!=null){
            SQLstatementStorage.storeStatement("DELETE FROM Productos WHERE Numero_barras = '"+e.getNumeroBarras()+"' AND Tipo = '"+ e.getTipo()+"'");
            tablaProductosObservable.remove(e);
            // tablaProductosObservable.set(0, new Producto(e.getTipo(), e.getNumeroBarras(), "Prueba", 0, e.getMaterial())); //QUE POR QUE AQUI SI ACTUALIZA LA TABLEVIEW
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
            SQLstatementStorage.storeStatement("DELETE FROM Recicla WHERE Fecha = '"+e.getFecha()+"' AND Hora = '"+ e.getHora()+"'");
            tablaTransaccionesObservable.remove(e);
            System.out.println(e.getFecha()+" y "+e.getHora());
        }else{
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setHeaderText("Error de seleccion");
            alerta.setContentText("Selecciona un elemento");
            alerta.showAndWait();
        }
    }

    @FXML
    void borrarUsuario(ActionEvent event) {
        Usuario e = tablaUsuario.getSelectionModel().getSelectedItem();
        if(e!=null){
            SQLstatementStorage.storeStatement("DELETE FROM Usuarios WHERE Id_Usuario = '"+e.getIdUsuario()+"'");
            tablaUsuarioObservable.remove(e);
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
        }else{
            System.out.println("???");
        }
    }
}
