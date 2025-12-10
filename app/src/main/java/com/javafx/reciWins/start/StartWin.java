package com.javafx.reciWins.start;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 *
 * @author Aaron Sanchez Martin
 */
public class StartWin extends Application {

    private static Stage primaryStage;
    public static Connection conn;
    public static Image icon;

    public static void main(String[] args) {
        launch(args);
    }

    public static List<String> getMateriales() {
        return MATERIALES;
    }

    public static final List<String> MATERIALES = Arrays.asList(
        "PET", 
        "PP", 
        "Vidrio", 
        "Aluminio",
        "Papel",
        "Cartón",
        "Acero",
        "Cobre",
        "Bronce",
        "Latón",
        "Plástico mixto",
        "Orgánico",
        "Electrónico",
        "Textil",
        "Madera"
    );

    @Override
    public void start(Stage primeraEscena) throws Exception {
        primaryStage = primeraEscena;
        
        // Establecer conexión a BD
        establecerConexionBD();
        
        // Cargar icono
        cargarIcono();
        
        // Mostrar ventana de login
        mostrarLogin();
    }

    private void establecerConexionBD() {
        try {
            Properties props = new Properties();
            URL configUrl = getClass().getResource("/configuration.properties");
            InputStream input = configUrl.openStream();
            props.load(input);
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión a BD establecida correctamente");
        } catch (Exception e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Error de conexión");
            a.setContentText("Error al conectar con la base de datos: " + e.getMessage());
            a.setOnShown(ex -> {
                    Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
            a.showAndWait();
            System.exit(1);
        }
    }

    private void cargarIcono() {
        icon = new Image(getClass().getResourceAsStream("/img/logo.png"));
    }

    // ===== MÉTODOS PARA CSS =====
    private static void aplicarEstilosCSS(Scene scene) {
        try {
            // Cargar CSS desde resources/css/styles.css
            URL cssUrl = StartWin.class.getResource("/css/styles.css");
            if (cssUrl != null) {
                String cssUrlString = cssUrl.toExternalForm();
                scene.getStylesheets().add(cssUrlString);
                System.out.println("CSS cargado correctamente desde: " + cssUrlString);
            } else {
                System.err.println("CSS no encontrado en resources/css/styles.css");
            }
        } catch (Exception e) {
            System.err.println("Error al cargar CSS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== MÉTODOS PARA ICONOS =====
    private static void agregarIcono(Stage stage) {
        if (icon != null) {
            stage.getIcons().add(icon);
        }
    }

    private static void configurarIconoAlert(Alert alert) {
        try {
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            agregarIcono(alertStage);
        } catch (Exception e) {
            // Ignorar si no se puede configurar
        }
    }

    // ===== VENTANAS PRINCIPALES =====
    public static void mostrarLogin() {
        cargarVentana("/view/loginStart_win.fxml", "Reci Inventario");
    }

    public static void mostrarRegistro(){
        cargarVentana("/view/singUp_win.fxml", "Reci Inventario - Registro");
    }

    public static void mostrarMain(){
        cargarVentana("/view/main_win.fxml", "Reci Inventario - Principal");
    }

    private static void cargarVentana(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource(fxml));
            Scene scene = new Scene(loader.load());
            aplicarEstilosCSS(scene);
            
            primaryStage.setTitle(titulo);
            primaryStage.setScene(scene);
            agregarIcono(primaryStage);
            
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Error al cargar la ventana");
            a.setContentText("No se pudo cargar la ventana: " + e.getMessage());
            a.setOnShown(ex -> {
                    Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
            a.showAndWait();
        }
    }

    // ===== VENTANAS MODALES =====
    public static void lanzarAjustes() {
        mostrarModalSimple("/view/settings_win.fxml", "Settings");
    }

    public static void lanzarNuevoProducto() {
        mostrarModalSimple("/view/newProducto_win.fxml", "New Producto");
    }

    public static void lanzarNuevoUsuario() {
        mostrarModalSimple("/view/newUser_win.fxml", "New Usuario");
    }

    public static void lanzarNuevaTransaccion() {
        mostrarModalSimple("/view/newTransaccion_win.fxml", "Transaction");
    }

    public static void lanzarEscanear() {
        mostrarModalSimple("/view/escanear_win.fxml", "Escaneo");
    }

    public static void lanzarCambioContraseña() {
        mostrarModalSimple("/view/changePasswd_win.fxml", "Cambiar Contraseña");
    }

    public static void lanzarModTransaccion(){
        mostrarModalSimple("/view/modTransaccion_win.fxml", "Mod");
    }

    public static void lanzarModUser(){
        mostrarModalSimple("/view/modUser_win.fxml", "Mod");
    }

    public static void lanzarModProducto(){
        mostrarModalSimple("/view/modProducto_win.fxml", "Mod");
    }

    // MÉTODO PARA MODALES SIMPLES (sin parámetros especiales)
    private static void mostrarModalSimple(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource(fxml));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle(titulo);
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            Scene scene = new Scene(root);
            aplicarEstilosCSS(scene);
            modalStage.setScene(scene);
            
            agregarIcono(modalStage);
            modalStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            Alert a = new Alert(AlertType.ERROR);
            a.setOnShown(ex -> {
                    Stage stage = (Stage) a.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
            a.showAndWait();
        }
    }
}