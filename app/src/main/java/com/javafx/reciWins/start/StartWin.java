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
import java.sql.SQLException;
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
    private static Properties dbProps;

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
        
        // Cargar icono primero
        cargarIcono();
        
        // Cargar propiedades de la BD
        cargarPropiedadesBD();
        
        // Establecer conexión a BD
        if (!establecerConexionBD()) {
            // Si falla la conexión, mostrar diálogo y esperar decisión del usuario
            return;
        }
        
        // Mostrar ventana de login
        mostrarLogin();
    }

    private void cargarPropiedadesBD() {
        try {
            dbProps = new Properties();
            URL configUrl = getClass().getResource("/configuration.properties");
            InputStream input = configUrl.openStream();
            dbProps.load(input);
            System.out.println("Propiedades de BD cargadas correctamente");
        } catch (Exception e) {
            System.err.println("Error al cargar propiedades de BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean establecerConexionBD() {
        try {
            if (dbProps == null) {
                throw new Exception("No se han cargado las propiedades de la base de datos");
            }
            
            String url = dbProps.getProperty("db.url");
            String user = dbProps.getProperty("db.username");
            String password = dbProps.getProperty("db.password");
            
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión a BD establecida correctamente");
            return true;
        } catch (Exception e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
            mostrarDialogoErrorConexion(e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si la conexión a la BD está activa
     * @return true si la conexión es válida, false si está cerrada o es nula
     */
    public static boolean verificarConexion() {
        try {
            if (conn == null || conn.isClosed()) {
                return false;
            }
            // Hacer un test simple de conexión
            conn.isValid(2); // timeout de 2 segundos
            return true;
        } catch (SQLException e) {
            System.err.println("Error al verificar conexión: " + e.getMessage());
            return false;
        }
    }

    /**
     * Maneja la pérdida de conexión mostrando un diálogo con opciones
     * Este método debe ser llamado cuando se detecte un SQLException
     * @param errorDetalle Mensaje de error detallado (opcional)
     */
    public static void manejarPerdidaConexion(String errorDetalle) {
        System.err.println("Conexión perdida con la base de datos");
        
        // Crear botones personalizados
        ButtonType btnReintentar = new ButtonType("Reintentar");
        ButtonType btnSalir = new ButtonType("Salir");
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText("Se ha perdido la conexión con la base de datos");
        
        String contenido = "No se puede conectar con el servidor de base de datos.\n\n";
        if (errorDetalle != null && !errorDetalle.isEmpty()) {
            contenido += "Detalle: " + errorDetalle + "\n\n";
        }
        contenido += "¿Qué deseas hacer?";
        alert.setContentText(contenido);
        
        // Configurar el icono del alert
        alert.setOnShown(ex -> {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            if (icon != null) {
                stage.getIcons().add(icon);
            }
        });
        
        // Agregar los botones personalizados
        alert.getButtonTypes().setAll(btnReintentar, btnSalir);
        
        // Mostrar el diálogo y esperar respuesta
        Optional<ButtonType> resultado = alert.showAndWait();
        
        if (resultado.isPresent()) {
            if (resultado.get() == btnReintentar) {
                // Reintentar la conexión
                System.out.println("Reintentando conexión a la base de datos...");
                if (intentarReconexion()) {
                    // Conexión restaurada exitosamente
                    Alert exito = new Alert(AlertType.INFORMATION);
                    exito.setTitle("Conexión Restaurada");
                    exito.setHeaderText("Conexión exitosa");
                    exito.setContentText("La conexión con la base de datos se ha restaurado correctamente.");
                    exito.setOnShown(ex -> {
                        Stage stage = (Stage) exito.getDialogPane().getScene().getWindow();
                        if (icon != null) {
                            stage.getIcons().add(icon);
                        }
                    });
                    exito.showAndWait();
                }
                // Si falla, se volverá a mostrar el diálogo de error recursivamente
            } else if (resultado.get() == btnSalir) {
                // Salir de la aplicación
                System.out.println("Cerrando aplicación...");
                cerrarAplicacion();
            }
        } else {
            // Si el usuario cierra el diálogo sin seleccionar nada, salir
            System.out.println("Diálogo cerrado sin selección. Cerrando aplicación...");
            cerrarAplicacion();
        }
    }

    /**
     * Intenta reconectar a la base de datos
     * @return true si la reconexión fue exitosa, false en caso contrario
     */
    private static boolean intentarReconexion() {
        try {
            // Cerrar la conexión anterior si existe
            if (conn != null && !conn.isClosed()) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar conexión anterior: " + e.getMessage());
                }
            }
            
            // Intentar nueva conexión
            if (dbProps == null) {
                throw new Exception("No se han cargado las propiedades de la base de datos");
            }
            
            String url = dbProps.getProperty("db.url");
            String user = dbProps.getProperty("db.username");
            String password = dbProps.getProperty("db.password");
            
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Reconexión exitosa a la base de datos");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error al reconectar: " + e.getMessage());
            manejarPerdidaConexion(e.getMessage());
            return false;
        }
    }

    /**
     * Cierra la aplicación de forma segura
     */
    private static void cerrarAplicacion() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Conexión a BD cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
        System.exit(0);
    }

    private void mostrarDialogoErrorConexion(String mensajeError) {
        // Crear botones personalizados
        ButtonType btnReintentar = new ButtonType("Reintentar");
        ButtonType btnSalir = new ButtonType("Salir");
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText("No se pudo conectar con la base de datos");
        alert.setContentText("Error: " + mensajeError + "\n\n¿Qué deseas hacer?");
        
        // Configurar el icono del alert
        alert.setOnShown(ex -> {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            if (icon != null) {
                stage.getIcons().add(icon);
            }
        });
        
        // Agregar los botones personalizados
        alert.getButtonTypes().setAll(btnReintentar, btnSalir);
        
        // Mostrar el diálogo y esperar respuesta
        Optional<ButtonType> resultado = alert.showAndWait();
        
        if (resultado.isPresent()) {
            if (resultado.get() == btnReintentar) {
                // Reintentar la conexión
                System.out.println("Reintentando conexión a la base de datos...");
                if (establecerConexionBD()) {
                    // Si la conexión fue exitosa, mostrar el login
                    mostrarLogin();
                }
                // Si falla, se volverá a mostrar el diálogo de error recursivamente
            } else if (resultado.get() == btnSalir) {
                // Salir de la aplicación
                System.out.println("Cerrando aplicación...");
                cerrarAplicacion();
            }
        } else {
            // Si el usuario cierra el diálogo sin seleccionar nada, salir
            System.out.println("Diálogo cerrado sin selección. Cerrando aplicación...");
            cerrarAplicacion();
        }
    }

    private void cargarIcono() {
        try {
            icon = new Image(getClass().getResourceAsStream("/img/logo.png"));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
            icon = null;
        }
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