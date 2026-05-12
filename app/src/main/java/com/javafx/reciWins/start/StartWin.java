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

import com.javafx.reciWins.utiles.ApiClient;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Aaron Sanchez Martin
 */
public class StartWin extends Application {

    private static Stage primaryStage;
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
        "Carton",
        "Acero",
        "Cobre",
        "Bronce",
        "Laton",
        "Plastico mixto",
        "Organico",
        "Electronico",
        "Textil",
        "Madera"
    );

    @Override
    public void start(Stage primeraEscena) throws Exception {
        primaryStage = primeraEscena;

        // Cargar icono primero
        cargarIcono();

        // Verificar conexion con la API
        if (!verificarConexionAPI()) {
            // Si falla la conexion, mostrar dialogo y esperar decision del usuario
            return;
        }

        // Mostrar ventana de login
        mostrarLogin();
    }

    private boolean verificarConexionAPI() {
        try {
            ApiClient api = ApiClient.getInstance();
            if (api.verificarConexion()) {
                System.out.println("Conexion con la API establecida correctamente");
                return true;
            } else {
                mostrarDialogoErrorConexion("No se pudo conectar con el servidor API");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error al conectar con la API: " + e.getMessage());
            mostrarDialogoErrorConexion(e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si la conexion a la API esta activa
     * @return true si la conexion es valida
     */
    public static boolean verificarConexion() {
        try {
            return ApiClient.getInstance().verificarConexion();
        } catch (Exception e) {
            System.err.println("Error al verificar conexion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Maneja la perdida de conexion mostrando un dialogo con opciones
     * @param errorDetalle Mensaje de error detallado (opcional)
     */
    public static void manejarPerdidaConexion(String errorDetalle) {
        System.err.println("Conexion perdida con la API");

        // Crear botones personalizados
        ButtonType btnReintentar = new ButtonType("Reintentar");
        ButtonType btnSalir = new ButtonType("Salir");

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error de Conexion");
        alert.setHeaderText("Se ha perdido la conexion con el servidor");

        String contenido = "No se puede conectar con el servidor API.\n\n";
        if (errorDetalle != null && !errorDetalle.isEmpty()) {
            contenido += "Detalle: " + errorDetalle + "\n\n";
        }
        contenido += "Que deseas hacer?";
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

        // Mostrar el dialogo y esperar respuesta
        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent()) {
            if (resultado.get() == btnReintentar) {
                // Reintentar la conexion
                System.out.println("Reintentando conexion con la API...");
                if (intentarReconexion()) {
                    // Conexion restaurada exitosamente
                    Alert exito = new Alert(AlertType.INFORMATION);
                    exito.setTitle("Conexion Restaurada");
                    exito.setHeaderText("Conexion exitosa");
                    exito.setContentText("La conexion con el servidor se ha restaurado correctamente.");
                    exito.setOnShown(ex -> {
                        Stage stage = (Stage) exito.getDialogPane().getScene().getWindow();
                        if (icon != null) {
                            stage.getIcons().add(icon);
                        }
                    });
                    exito.showAndWait();
                }
                // Si falla, se volvera a mostrar el dialogo de error recursivamente
            } else if (resultado.get() == btnSalir) {
                // Salir de la aplicacion
                System.out.println("Cerrando aplicacion...");
                cerrarAplicacion();
            }
        } else {
            // Si el usuario cierra el dialogo sin seleccionar nada, salir
            System.out.println("Dialogo cerrado sin seleccion. Cerrando aplicacion...");
            cerrarAplicacion();
        }
    }

    /**
     * Intenta reconectar a la API
     * @return true si la reconexion fue exitosa, false en caso contrario
     */
    private static boolean intentarReconexion() {
        try {
            boolean ok = ApiClient.getInstance().verificarConexion();
            if (ok) {
                System.out.println("Reconexion exitosa con la API");
                return true;
            } else {
                manejarPerdidaConexion("El servidor no responde");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al reconectar: " + e.getMessage());
            manejarPerdidaConexion(e.getMessage());
            return false;
        }
    }

    /**
     * Cierra la aplicacion de forma segura
     */
    private static void cerrarAplicacion() {
        System.exit(0);
    }

    private void mostrarDialogoErrorConexion(String mensajeError) {
        // Crear botones personalizados
        ButtonType btnReintentar = new ButtonType("Reintentar");
        ButtonType btnSalir = new ButtonType("Salir");

        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error de Conexion");
        alert.setHeaderText("No se pudo conectar con el servidor API");
        alert.setContentText("Error: " + mensajeError + "\n\nQue deseas hacer?");

        // Configurar el icono del alert
        alert.setOnShown(ex -> {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            if (icon != null) {
                stage.getIcons().add(icon);
            }
        });

        // Agregar los botones personalizados
        alert.getButtonTypes().setAll(btnReintentar, btnSalir);

        // Mostrar el dialogo y esperar respuesta
        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent()) {
            if (resultado.get() == btnReintentar) {
                // Reintentar la conexion
                System.out.println("Reintentando conexion con la API...");
                if (verificarConexionAPI()) {
                    // Si la conexion fue exitosa, mostrar el login
                    mostrarLogin();
                }
                // Si falla, se volvera a mostrar el dialogo de error recursivamente
            } else if (resultado.get() == btnSalir) {
                // Salir de la aplicacion
                System.out.println("Cerrando aplicacion...");
                cerrarAplicacion();
            }
        } else {
            // Si el usuario cierra el dialogo sin seleccionar nada, salir
            System.out.println("Dialogo cerrado sin seleccion. Cerrando aplicacion...");
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

    // ===== METODOS PARA CSS =====
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

    // ===== METODOS PARA ICONOS =====
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

    public static void lanzarCambioContrasena() {
        mostrarModalSimple("/view/changePasswd_win.fxml", "Cambiar Contrasena");
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

    // METODO PARA MODALES SIMPLES (sin parametros especiales)
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
