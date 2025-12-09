package com.javafx.reciWins.start;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
        Parent root = FXMLLoader.load(this.getClass().getResource("/view/loginStart_win.fxml"));

        Scene scene = new Scene(root);
        primeraEscena.setScene(scene);
        primeraEscena.setTitle("Reci Inventario");
        primeraEscena.show();

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
        }

        icon = new Image(this.getClass().getResourceAsStream("/img/logo.png"));
        primeraEscena.getIcons().add(icon);
    }
    //dios todo esto era mucho mas facil de lo que parecia solo que da error por alguna razon si intento cargarlo de golpe en ugar de dividirlo entre new FXML y luego load?¿


    public static void mostrarRegistro(){
        try {
            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/singUp_win.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mostrarMain(){
        try {
            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/main_win.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mostrarLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/loginStart_win.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lanzarAjustes() {
        try {
            Stage modal = new Stage();
            modal.setTitle("Settings");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);

            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/settings_win.fxml"));
            Scene scene = new Scene(loader.load());
            modal.setScene(scene);

            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lanzarNuevoProducto() {
        try {
            Stage modal = new Stage();
            modal.setTitle("New Producto");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);

            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/newProducto_win.fxml"));
            Scene scene = new Scene(loader.load());
            modal.setScene(scene);

            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lanzarNuevoUsuario() {
        try {
            Stage modal = new Stage();
            modal.setTitle("New Usuario");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);

            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/newUser_win.fxml"));
            Scene scene = new Scene(loader.load());
            modal.setScene(scene);

            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lanzarNuevaTransaccion() {
        try {
            Stage modal = new Stage();
            modal.setTitle("Transaction");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);

            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/newTransaccion_win.fxml"));
            Scene scene = new Scene(loader.load());
            modal.setScene(scene);

            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lanzarEscanear() {
        try {
            Stage modal = new Stage();
            modal.setTitle("Escaneo");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);

            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/escanear_win.fxml"));
            Scene scene = new Scene(loader.load());
            modal.setScene(scene);

            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lanzarCambioContraseña() {
        try {
            Stage modal = new Stage();
            modal.setTitle("Escaneo");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);

            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/changePasswd_win.fxml"));
            Scene scene = new Scene(loader.load());
            modal.setScene(scene);

            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lanzarModTransaccion(){
        try {
            Stage modal = new Stage();
            modal.setTitle("Mod");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);

            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/modTransaccion_win.fxml"));
            Scene scene = new Scene(loader.load());
            modal.setScene(scene);

            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lanzarModUser(){
        try {
            Stage modal = new Stage();
            modal.setTitle("Mod");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);

            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/modUser_win.fxml"));
            Scene scene = new Scene(loader.load());
            modal.setScene(scene);

            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void lanzarModProducto(){
        try {
            Stage modal = new Stage();
            modal.setTitle("Mod");
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(primaryStage);

            FXMLLoader loader = new FXMLLoader(StartWin.class.getResource("/view/modProducto_win.fxml"));
            Scene scene = new Scene(loader.load());
            modal.setScene(scene);

            modal.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
