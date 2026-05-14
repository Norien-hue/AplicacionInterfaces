package com.javafx.reciWins.controllers;

import com.javafx.model.Producto;
import com.javafx.model.Transaccion;
import com.javafx.model.Usuario;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.ApiClient;
import com.javafx.reciWins.utiles.StorageSharer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.axes.spi.format.SimpleFormatter;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.dataset.spi.DefaultErrorDataSet;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Random;

import javafx.scene.web.WebView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

public class MainController implements Initializable {

    public static int id_user;
    private boolean esAdministrador = false;

    private Map<String, Object> parametrosInforme = new HashMap<>();

    private void configurarInformes() {
    if (webViewInforme != null) {
        webViewInforme.getEngine().setJavaScriptEnabled(true);
    }

    if (chk_todosUsuarios != null && txt_filtroUsuario != null) {
        chk_todosUsuarios.selectedProperty().addListener((obs, oldVal, newVal) -> {
            txt_filtroUsuario.setDisable(newVal);
            if (newVal) {
                txt_filtroUsuario.setText("");
            }
        });
        txt_filtroUsuario.setDisable(true);
    }

    // Informes se generan solo cuando el usuario pulsa el boton
}

    @FXML
    void generarInforme1(ActionEvent event) {
        generarInforme1Local(false);
    }

    @FXML
    void exportarInforme1PDF(ActionEvent event) {
        generarInforme1Local(true);
    }

    private void generarInforme1Local(boolean exportar) {
        try {
            ApiClient api = ApiClient.getInstance();
            JsonArray productos = api.getAllProductos();

            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset='UTF-8'/><style>");
            html.append("body{font-family:Arial,sans-serif;margin:20px;background:#f9f9f9;}");
            html.append("h1{color:#16a34a;text-align:center;}");
            html.append("p.fecha{text-align:center;color:#888;font-size:12px;}");
            html.append("table{width:100%;border-collapse:collapse;margin-top:15px;}");
            html.append("th{background:#16a34a;color:white;padding:8px;font-size:12px;}");
            html.append("td{padding:6px;text-align:center;font-size:11px;border-bottom:1px solid #ddd;}");
            html.append("tr:nth-child(even){background:#dcfce7;}");
            html.append(".total{font-weight:bold;margin-top:10px;font-size:13px;}");
            html.append("</style></head><body>");
            html.append("<h1>ReciApp - Catalogo de Productos</h1>");
            html.append("<p class='fecha'>Generado: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</p>");
            html.append("<table><tr><th>Tipo</th><th>Codigo Barras</th><th>Nombre</th><th>Material</th><th>CO2 (kg)</th></tr>");

            for (JsonElement elem : productos) {
                JsonObject p = elem.getAsJsonObject();
                String tipo = p.has("tipo") && !p.get("tipo").isJsonNull() ? p.get("tipo").getAsString() : "";
                String barras = p.has("numeroBarras") ? String.valueOf(p.get("numeroBarras").getAsLong()) : "";
                String nombre = p.has("nombre") && !p.get("nombre").isJsonNull() ? p.get("nombre").getAsString() : "";
                String material = p.has("material") && !p.get("material").isJsonNull() ? p.get("material").getAsString() : "";
                String co2 = p.has("emisionesReducibles") && !p.get("emisionesReducibles").isJsonNull() ? String.format("%.3f", p.get("emisionesReducibles").getAsFloat()) : "0";
                html.append("<tr><td>").append(tipo).append("</td><td>").append(barras).append("</td><td>").append(nombre).append("</td><td>").append(material).append("</td><td>").append(co2).append("</td></tr>");
            }

            html.append("</table>");
            html.append("<p class='total'>Total de productos: ").append(productos.size()).append("</p>");
            html.append("</body></html>");

            if (exportar) {
                guardarHtmlYAbrir("informe1", html.toString());
            } else if (webViewInforme != null) {
                webViewInforme.getEngine().loadContent(html.toString());
            }
        } catch (Exception e) {
            mostrarError("Error al generar informe", "No se pudo generar el informe", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void generarInforme2(ActionEvent event) {
        generarInforme2Local(false);
    }

    @FXML
    void exportarInforme2PDF(ActionEvent event) {
        generarInforme2Local(true);
    }

    private void generarInforme2Local(boolean exportar) {
        try {
            ApiClient api = ApiClient.getInstance();
            JsonArray usuarios = api.getAllUsuarios();
            JsonArray transacciones = api.getAllTransacciones();

            String filtro = null;
            if (chk_todosUsuarios == null || !chk_todosUsuarios.isSelected()) {
                if (txt_filtroUsuario != null && !txt_filtroUsuario.getText().trim().isEmpty()) {
                    filtro = txt_filtroUsuario.getText().trim().toLowerCase();
                }
            }

            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta charset='UTF-8'/><style>");
            html.append("body{font-family:Arial,sans-serif;margin:20px;background:#f9f9f9;}");
            html.append("h1{color:#16a34a;text-align:center;}");
            html.append("h2{color:#16a34a;margin-top:25px;}");
            html.append("p.fecha{text-align:center;color:#888;font-size:12px;}");
            html.append("p.info{font-size:11px;color:#555;}");
            html.append("p.empty{font-style:italic;color:#888;font-size:11px;}");
            html.append("table{width:100%;border-collapse:collapse;margin-top:8px;margin-bottom:5px;}");
            html.append("th{background:#16a34a;color:white;padding:6px;font-size:11px;}");
            html.append("td{padding:5px;text-align:center;font-size:10px;border-bottom:1px solid #ddd;}");
            html.append("tr:nth-child(even){background:#dcfce7;}");
            html.append(".count{font-style:italic;font-size:10px;}");
            html.append("</style></head><body>");
            html.append("<h1>ReciApp - Historico de Reciclaje por Usuario</h1>");
            html.append("<p class='fecha'>Generado: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</p>");

            for (JsonElement uElem : usuarios) {
                JsonObject u = uElem.getAsJsonObject();
                String nombre = u.has("nombre") && !u.get("nombre").isJsonNull() ? u.get("nombre").getAsString() : "";
                int userId = u.has("id") ? u.get("id").getAsInt() : 0;

                if (filtro != null && !nombre.toLowerCase().contains(filtro)) continue;

                String permisos = u.has("permisos") && !u.get("permisos").isJsonNull() ? u.get("permisos").getAsString() : "cliente";
                float emis = 0;
                if (u.has("emisionesReducidas") && !u.get("emisionesReducidas").isJsonNull()) emis = u.get("emisionesReducidas").getAsFloat();
                else if (u.has("emisiones") && !u.get("emisiones").isJsonNull()) emis = u.get("emisiones").getAsFloat();

                html.append("<h2>Usuario: ").append(nombre).append("</h2>");
                html.append("<p class='info'>Rol: ").append(permisos).append(" | Emisiones reducidas: ").append(String.format("%.3f", emis)).append(" kg CO2</p>");

                int count = 0;
                StringBuilder rows = new StringBuilder();
                for (JsonElement tElem : transacciones) {
                    JsonObject t = tElem.getAsJsonObject();
                    int tUser = t.has("idUsuario") ? t.get("idUsuario").getAsInt() : -1;
                    if (tUser != userId) continue;
                    String tTipo = t.has("tipo") && !t.get("tipo").isJsonNull() ? t.get("tipo").getAsString() : "";
                    String tBarras = t.has("numeroBarras") ? String.valueOf(t.get("numeroBarras").getAsLong()) : "";
                    String tFecha = t.has("fecha") && !t.get("fecha").isJsonNull() ? t.get("fecha").getAsString() : "";
                    String tHora = t.has("hora") && !t.get("hora").isJsonNull() ? t.get("hora").getAsString() : "";
                    rows.append("<tr><td>").append(tTipo).append("</td><td>").append(tBarras).append("</td><td>").append(tFecha).append("</td><td>").append(tHora).append("</td></tr>");
                    count++;
                }

                if (count == 0) {
                    html.append("<p class='empty'>Sin transacciones registradas.</p>");
                } else {
                    html.append("<table><tr><th>Tipo</th><th>Codigo Barras</th><th>Fecha</th><th>Hora</th></tr>");
                    html.append(rows);
                    html.append("</table>");
                    html.append("<p class='count'>Total transacciones: ").append(count).append("</p>");
                }
            }

            html.append("</body></html>");

            if (exportar) {
                guardarHtmlYAbrir("informe2", html.toString());
            } else if (webViewInforme != null) {
                webViewInforme.getEngine().loadContent(html.toString());
            }
        } catch (Exception e) {
            mostrarError("Error al generar informe", "No se pudo generar el informe", e.getMessage());
            e.printStackTrace();
        }
    }

    private void guardarHtmlYAbrir(String nombre, String html) {
        try {
            File carpeta = new File("informes");
            if (!carpeta.exists()) carpeta.mkdirs();
            File archivo = new File("informes/" + nombre + ".html");
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                fos.write(html.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivo);
            }
        } catch (Exception e) {
            mostrarError("Error", "No se pudo exportar el informe", e.getMessage());
        }
    }

    private String obtenerTituloInforme(String nombreArchivo) {
        switch (nombreArchivo) {
            case "informe1":
                return "Catalogo de Productos";
            case "informe2":
                return "Historico de Reciclaje por Usuario";
            default:
                return nombreArchivo;
        }
    }

    @FXML
    void tab_informes(ActionEvent event) {
    }

    private void mostrarError(String titulo, String header, String contenido) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setOnShown(e -> {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            if (StartWin.icon != null) {
                stage.getIcons().add(StartWin.icon);
            }
        });
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        alert.showAndWait();
    }


    @FXML private WebView webViewInforme;
    @FXML private Button btn_generarInforme1;
    @FXML private Button btn_generarInforme2;
    @FXML private Button btn_exportarPDF1;
    @FXML private Button btn_exportarPDF2;
    @FXML private TextField txt_filtroUsuario;
    @FXML private CheckBox chk_todosUsuarios;

    @FXML private AnchorPane tab_graph_content;
    @FXML private ScrollPane scrollPaneGraph;
    @FXML private Button btn_graph;
    @FXML private Button btn_informes;
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
    @FXML private VBox rootVBox;

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
    private TableColumn<Producto, String> colImagenProducto;

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

    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        aplicarAnimacionEntrada();

        cargarDatosUsuarioActual();

        configurarColumnasProductos();
        configurarColumnasTransacciones();
        configurarColumnasUsuarios();

        cargarDatosProductos();
        cargarDatosTransacciones();
        cargarDatosUsuarios();

        configurarPermisosSegunRol();

        configurarTooltips();

        configurarAnimacionesTab();

        // Grafico e Informes ocultos
        if (tabMain.getTabs().size() > 3) tabMain.getTabs().remove(3);
        if (tabMain.getTabs().size() > 2) tabMain.getTabs().remove(2);
        if (btn_graph != null) { btn_graph.setVisible(false); btn_graph.setManaged(false); }
        if (btn_informes != null) { btn_informes.setVisible(false); btn_informes.setManaged(false); }
    }

    private void aplicarAnimacionEntrada() {
        if (rootVBox != null) {
            rootVBox.setOpacity(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), rootVBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setCycleCount(1);

            fadeIn.setDelay(Duration.millis(200));

            fadeIn.play();
        }
    }

    private void configurarAnimacionesTab() {
        tabMain.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (oldTab != null && newTab != null) {
                Node oldContent = oldTab.getContent();
                Node newContent = newTab.getContent();

                animarCambioDeTab(oldContent, newContent);
            }
        });
    }

    private void animarCambioDeTab(Node nodoSaliente, Node nodoEntrante) {
        if (nodoSaliente == null || nodoEntrante == null) return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), nodoSaliente);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), nodoEntrante);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        nodoEntrante.setOpacity(0);

        SequentialTransition sequentialTransition = new SequentialTransition(fadeOut, fadeIn);
        sequentialTransition.play();
    }


    private void animarCambioDeVistaTablas(Node vistaActual, Node nuevaVista) {
        if (vistaActual == null || nuevaVista == null) return;

        FadeTransition fadeOutActual = new FadeTransition(Duration.millis(250), vistaActual);
        fadeOutActual.setFromValue(1.0);
        fadeOutActual.setToValue(0.0);

        FadeTransition fadeInNueva = new FadeTransition(Duration.millis(250), nuevaVista);
        fadeInNueva.setFromValue(0.0);
        fadeInNueva.setToValue(1.0);

        nuevaVista.setOpacity(0);
        nuevaVista.setVisible(true);

        SequentialTransition sequentialTransition = new SequentialTransition(fadeOutActual, fadeInNueva);

        sequentialTransition.setOnFinished(event -> {
            vistaActual.setVisible(false);
            vistaActual.setOpacity(1);
        });

        sequentialTransition.play();
    }

    private void inicializarGrafico() {
        try {
            tab_graph_content.getChildren().clear();

            XYChart chart = new XYChart();
            chart.setPrefSize(800, 400);

            DefaultNumericAxis xAxis = new DefaultNumericAxis("Usuarios", 0, 1, 1);
            DefaultNumericAxis yAxis = new DefaultNumericAxis("Emisiones Reducidas (kg CO2)", 0, 1, 1);

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

    public void actualizarTodasLasVistas() {
        cargarDatosUsuarioActual();
        cargarDatosProductos();
        cargarDatosTransacciones();
        cargarDatosUsuarios();
    }

    public static void actualizarVistasDesdeExterno() {
        if (instance != null) {
            Platform.runLater(() -> {
                instance.actualizarTodasLasVistas();
            });
        }
    }

    @FXML
    void tab_graph(ActionEvent event) {
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
        btn_graph.setTooltip(new Tooltip("Grafico de Emisiones"));
        btn_settings.setTooltip(new Tooltip("Ajustes"));
        btn_generarTap.setTooltip(new Tooltip("Generar un nuevo numero TAP"));
        btn_exit.setTooltip(new Tooltip("Cerrar sesion"));
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

        colImagenProducto.setCellValueFactory(cellData -> {
            boolean tiene = cellData.getValue().tieneImagen();
            return new javafx.beans.property.SimpleStringProperty(tiene ? "V" : "X");
        });
        colImagenProducto.setCellFactory(col -> new javafx.scene.control.TableCell<Producto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: "
                        + ("V".equals(item) ? "green" : "red") + ";");
                }
            }
        });
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
            ApiClient api = ApiClient.getInstance();
            JsonObject user = api.getProfile(id_user);

            String nombre = user.has("nombre") && !user.get("nombre").isJsonNull() ? user.get("nombre").getAsString() : "N/A";
            nombreBD.setText(nombre);

            float emisiones = 0.0f;
            if (user.has("emisionesReducidas") && !user.get("emisionesReducidas").isJsonNull()) {
                emisiones = user.get("emisionesReducidas").getAsFloat();
            } else if (user.has("emisiones") && !user.get("emisiones").isJsonNull()) {
                emisiones = user.get("emisiones").getAsFloat();
            }
            saldoBD.setText(String.format("%.1f", emisiones) + " kg CO2");

            String permisos = user.has("permisos") && !user.get("permisos").isJsonNull() ? user.get("permisos").getAsString() : "cliente";
            esAdministrador = permisos != null && permisos.equalsIgnoreCase("administrador");
            rolBD.setText(permisos);

        } catch (Exception e) {
            e.printStackTrace();
            nombreBD.setText("Error");
            saldoBD.setText("0.0 kg CO2");
            rolBD.setText("N/A");
        }
    }

    private void cargarDatosProductos() {
        try {
            ApiClient api = ApiClient.getInstance();
            JsonArray productos = api.getAllProductos();

            tablaProductosObservable = FXCollections.observableArrayList();

            for (JsonElement elem : productos) {
                JsonObject p = elem.getAsJsonObject();
                String tipo = p.has("tipo") ? p.get("tipo").getAsString() : "";
                long barras = p.has("numeroBarras") ? p.get("numeroBarras").getAsLong() : 0;
                String nombre = p.has("nombre") ? p.get("nombre").getAsString() : "";
                float emis = p.has("emisionesReducibles") ? p.get("emisionesReducibles").getAsFloat() : 0;
                String material = p.has("material") ? p.get("material").getAsString() : "";
                String imagen = p.has("imagen") && !p.get("imagen").isJsonNull() ? p.get("imagen").getAsString() : null;

                Producto producto = new Producto(tipo, barras, nombre, emis, material, imagen);
                tablaProductosObservable.add(producto);
            }

            if (tablaProductos != null) {
                tablaProductos.setItems(tablaProductosObservable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarDatosTransacciones() {
        try {
            ApiClient api = ApiClient.getInstance();
            JsonArray transacciones = api.getAllTransacciones();

            tablaTransaccionesObservable = FXCollections.observableArrayList();

            for (JsonElement elem : transacciones) {
                JsonObject t = elem.getAsJsonObject();
                int idUsuario = t.has("idUsuario") ? t.get("idUsuario").getAsInt() : 0;
                String tipo = t.has("tipo") ? t.get("tipo").getAsString() : "";
                long barras = t.has("numeroBarras") ? t.get("numeroBarras").getAsLong() : 0;
                String fechaStr = t.has("fecha") ? t.get("fecha").getAsString() : "2000-01-01";
                String horaStr = t.has("hora") ? t.get("hora").getAsString() : "00:00:00";

                Date fecha = Date.valueOf(fechaStr);
                Time hora = Time.valueOf(horaStr.length() == 5 ? horaStr + ":00" : horaStr);

                Transaccion transaccion = new Transaccion(idUsuario, tipo, barras, fecha, hora);
                tablaTransaccionesObservable.add(transaccion);
            }

            if (tablaTransacciones != null) {
                tablaTransacciones.setItems(tablaTransaccionesObservable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarDatosUsuarios() {
        try {
            ApiClient api = ApiClient.getInstance();
            JsonArray usuarios = api.getAllUsuarios();

            tablaUsuarioObservable = FXCollections.observableArrayList();

            for (JsonElement elem : usuarios) {
                JsonObject u = elem.getAsJsonObject();
                int id = u.has("id") ? u.get("id").getAsInt() : 0;
                float emisiones = 0;
                if (u.has("emisionesReducidas") && !u.get("emisionesReducidas").isJsonNull()) {
                    emisiones = u.get("emisionesReducidas").getAsFloat();
                } else if (u.has("emisiones") && !u.get("emisiones").isJsonNull()) {
                    emisiones = u.get("emisiones").getAsFloat();
                }
                String permisos = u.has("permisos") && !u.get("permisos").isJsonNull() ? u.get("permisos").getAsString() : "cliente";
                String nombre = u.has("nombre") && !u.get("nombre").isJsonNull() ? u.get("nombre").getAsString() : "";
                int tap = u.has("tap") && !u.get("tap").isJsonNull() ? u.get("tap").getAsInt() : 0;

                Usuario usuario = new Usuario(id, emisiones, permisos, nombre, tap);
                tablaUsuarioObservable.add(usuario);
            }

            if (tablaUsuario != null) {
                tablaUsuario.setItems(tablaUsuarioObservable);
            }
        } catch (Exception e) {
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
            alerta.setOnShown(e -> {
                Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                stage.getIcons().add(StartWin.icon);
            });
            alerta.setHeaderText("Error de seleccion");
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

        ApiClient.getInstance().logout();
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

        if (tab_transaccion.isVisible() || tab_usuario.isVisible()) {
            Node vistaActual = null;
            if (tab_transaccion.isVisible()) vistaActual = tab_transaccion;
            if (tab_usuario.isVisible()) vistaActual = tab_usuario;

            animarCambioDeVistaTablas(vistaActual, tab_product);
        } else {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), tab_product);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            tab_product.setOpacity(0);
            tab_product.setVisible(true);
            fadeIn.play();
        }
    }

    @FXML
    void tab_transactions(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }

        deseleccionarTodos();
        tabMain.getSelectionModel().select(1);

        if (tab_product.isVisible() || tab_usuario.isVisible()) {
            Node vistaActual = null;
            if (tab_product.isVisible()) vistaActual = tab_product;
            if (tab_usuario.isVisible()) vistaActual = tab_usuario;

            animarCambioDeVistaTablas(vistaActual, tab_transaccion);
        } else {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), tab_transaccion);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            tab_transaccion.setOpacity(0);
            tab_transaccion.setVisible(true);
            fadeIn.play();
        }
    }

    @FXML
    void tab_users(ActionEvent event) {
        if (!esAdministrador) {
            mostrarErrorAcceso();
            return;
        }

        deseleccionarTodos();
        tabMain.getSelectionModel().select(1);

        if (tab_product.isVisible() || tab_transaccion.isVisible()) {
            Node vistaActual = null;
            if (tab_product.isVisible()) vistaActual = tab_product;
            if (tab_transaccion.isVisible()) vistaActual = tab_transaccion;

            animarCambioDeVistaTablas(vistaActual, tab_usuario);
        } else {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), tab_usuario);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            tab_usuario.setOpacity(0);
            tab_usuario.setVisible(true);
            fadeIn.play();
        }
    }

    @FXML
    void launch_newProducto(ActionEvent event) {
        if (!esAdministrador) { mostrarErrorAcceso(); return; }
        deseleccionarTodos();
        StartWin.lanzarNuevoProducto();
    }

    @FXML
    void launch_newTransaccion(ActionEvent event) {
        if (!esAdministrador) { mostrarErrorAcceso(); return; }
        deseleccionarTodos();
        StartWin.lanzarNuevaTransaccion();
    }

    @FXML
    void launch_newUser(ActionEvent event) {
        if (!esAdministrador) { mostrarErrorAcceso(); return; }
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
        if (!esAdministrador) { mostrarErrorAcceso(); return; }

        Producto producto = tablaProductos.getSelectionModel().getSelectedItem();
        if(producto != null){
            try {
                ApiClient api = ApiClient.getInstance();
                api.deleteProducto(producto.getTipo(), producto.getNumeroBarras());

                actualizarTodasLasVistas();
                deseleccionarTodos();

                Alert alerta = new Alert(AlertType.INFORMATION);
                alerta.setOnShown(e -> {
                    Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                alerta.setHeaderText("Producto eliminado");
                alerta.setContentText("El producto y todas sus transacciones asociadas han sido eliminados.\nLas emisiones de los usuarios afectados se han actualizado.");
                alerta.showAndWait();

            } catch (Exception ex) {
                Alert alerta = new Alert(AlertType.ERROR);
                alerta.setOnShown(e -> {
                    Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                alerta.setHeaderText("Error al eliminar");
                alerta.setContentText("No se pudo eliminar el producto: " + ex.getMessage());
                alerta.showAndWait();
                ex.printStackTrace();
            }
        } else {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setOnShown(e -> {
                Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                stage.getIcons().add(StartWin.icon);
            });
            alerta.setHeaderText("Error de seleccion");
            alerta.setContentText("Selecciona un producto para eliminar");
            alerta.showAndWait();
        }
    }

    @FXML
    void borrarTransaccion(ActionEvent event) {
        if (!esAdministrador) { mostrarErrorAcceso(); return; }

        Transaccion e = tablaTransacciones.getSelectionModel().getSelectedItem();
        if(e!=null){
            try {
                ApiClient api = ApiClient.getInstance();
                api.deleteTransaccion(e.getIdUsuario(), e.getTipo(), e.getNumeroBarras(),
                        e.getFecha().toString(), e.getHora().toString());

                actualizarTodasLasVistas();
                deseleccionarTodos();
            } catch (Exception ex) {
                Alert alerta = new Alert(AlertType.ERROR);
                alerta.setOnShown(ea -> {
                    Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                alerta.setHeaderText("Error al eliminar");
                alerta.setContentText("No se pudo eliminar la transaccion: " + ex.getMessage());
                alerta.showAndWait();
                ex.printStackTrace();
            }
        }else{
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setOnShown(ex -> {
                Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                stage.getIcons().add(StartWin.icon);
            });
            alerta.setHeaderText("Error de seleccion");
            alerta.setContentText("Selecciona un elemento");
            alerta.showAndWait();
        }
    }

    public static void actualizarEmisionesUsuarioObservable(int idUsuario, float cambio) {
        if (tablaUsuarioObservable == null) return;
        for (Usuario usuario : tablaUsuarioObservable) {
            if (usuario.getIdUsuario() == idUsuario) {
                float nuevasEmisiones = usuario.getEmisionesReducidas() + cambio;
                usuario.setEmisionesReducidas(nuevasEmisiones);
                break;
            }
        }
    }

    @FXML
    void borrarUsuario(ActionEvent event) {
        if (!esAdministrador) { mostrarErrorAcceso(); return; }

        Usuario usuario = tablaUsuario.getSelectionModel().getSelectedItem();
        if(usuario != null){
            try {
                ApiClient api = ApiClient.getInstance();
                api.deleteUsuario(usuario.getIdUsuario());

                actualizarTodasLasVistas();
                deseleccionarTodos();

                Alert alerta = new Alert(AlertType.INFORMATION);
                alerta.setOnShown(e -> {
                    Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                alerta.setHeaderText("Usuario eliminado");
                alerta.setContentText("El usuario y todas sus transacciones han sido eliminados.");
                alerta.showAndWait();

            } catch (Exception ex) {
                Alert alerta = new Alert(AlertType.ERROR);
                alerta.setOnShown(e -> {
                    Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(StartWin.icon);
                });
                alerta.setHeaderText("Error al eliminar");
                alerta.setContentText("No se pudo eliminar el usuario: " + ex.getMessage());
                alerta.showAndWait();
                ex.printStackTrace();
            }
        } else {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setOnShown(e -> {
                Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                stage.getIcons().add(StartWin.icon);
            });
            alerta.setHeaderText("Error de seleccion");
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
        MainController controller = getInstance();
        if (controller != null) {
            controller.actualizarTodasLasVistas();
        }
    }

    public static void actualizarVistaPersonal() {
        MainController controller = getInstance();
        if (controller != null) {
            controller.cargarDatosUsuarioActual();
        }
    }

    @FXML
    void launch_modProducto(ActionEvent event) {
        if (!esAdministrador) { mostrarErrorAcceso(); return; }

        Producto m = tablaProductos.getSelectionModel().getSelectedItem();
        if(m != null) {
            StorageSharer.itemStorage.clear();

            StorageSharer.itemStorage.add(m.getTipo());
            StorageSharer.itemStorage.add(m.getNumeroBarras() + "");
            StorageSharer.itemStorage.add(m.getNombre());
            StorageSharer.itemStorage.add(m.getEmisionesReducibles() + "");
            StorageSharer.itemStorage.add(m.getMaterial());
            StorageSharer.itemStorage.add(m.getImagenBase64() == null ? "" : m.getImagenBase64());
            StorageSharer.itemToMod = m;
            StorageSharer.itemPre = m;

            StartWin.lanzarModProducto();
        } else {
            Alert alerta = new Alert(AlertType.WARNING);
            alerta.setOnShown(e -> {
                Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                stage.getIcons().add(StartWin.icon);
            });
            alerta.setHeaderText("Error de seleccion");
            alerta.setContentText("Selecciona un producto");
            alerta.showAndWait();
        }
    }

    @FXML
    void launch_modTransaccion(ActionEvent event) {
        if (!esAdministrador) { mostrarErrorAcceso(); return; }

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
            alerta.setOnShown(e -> {
                Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                stage.getIcons().add(StartWin.icon);
            });
            alerta.setHeaderText("Error de seleccion");
            alerta.setContentText("Selecciona una transaccion");
            alerta.showAndWait();
        }
    }

    @FXML
    void generarTap(ActionEvent event) {
        try {
            ApiClient api = ApiClient.getInstance();
            int nuevoTap = api.requestTap(id_user);

            actualizarTodasLasVistas();

            Alert alerta = new Alert(AlertType.INFORMATION);
            alerta.setOnShown(e -> {
                Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                stage.getIcons().add(StartWin.icon);
            });
            alerta.setHeaderText("Nuevo TAP generado");
            alerta.setContentText("Tu nuevo numero TAP es: " + nuevoTap);
            alerta.showAndWait();

        } catch (Exception e) {
            Alert alerta = new Alert(AlertType.ERROR);
            alerta.setOnShown(xe -> {
                Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                stage.getIcons().add(StartWin.icon);
            });
            alerta.setHeaderText("Error al generar TAP");
            alerta.setContentText("No se pudo generar el nuevo TAP: " + e.getMessage());
            alerta.showAndWait();
            e.printStackTrace();
        }
    }

    private void mostrarErrorAcceso() {
        Alert alerta = new Alert(AlertType.WARNING);
        alerta.setOnShown(e -> {
                Stage stage = (Stage) alerta.getDialogPane().getScene().getWindow();
                stage.getIcons().add(StartWin.icon);
            });
        alerta.setHeaderText("Acceso denegado");
        alerta.setContentText("Esta funcion solo esta disponible para administradores.");
        alerta.showAndWait();
    }
}
