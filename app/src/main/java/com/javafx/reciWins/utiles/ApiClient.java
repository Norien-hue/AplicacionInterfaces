package com.javafx.reciWins.utiles;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;

/**
 * Cliente HTTP centralizado para comunicarse con la API REST de ReciApp.
 * Sustituye todas las conexiones JDBC directas a MySQL.
 * Usa java.net.http.HttpClient (built-in Java 11+).
 */
public class ApiClient {

    private static ApiClient instance;

    private final HttpClient httpClient;
    private final Gson gson;
    private String baseUrl;
    private String jwtToken;

    private ApiClient() {
        this.gson = new Gson();
        cargarConfiguracion();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    private void cargarConfiguracion() {
        try {
            URL configUrl = getClass().getResource("/configuration.properties");
            if (configUrl != null) {
                InputStream input = configUrl.openStream();
                Properties props = new Properties();
                props.load(input);
                this.baseUrl = props.getProperty("api.url", "http://52.201.91.206:3000");
                input.close();
            } else {
                this.baseUrl = "http://52.201.91.206:3000";
            }
            System.out.println("ApiClient configurado con URL: " + baseUrl);
        } catch (Exception e) {
            this.baseUrl = "http://52.201.91.206:3000";
            System.err.println("Error al cargar configuracion, usando URL por defecto: " + e.getMessage());
        }
    }

    // ===== METODOS AUXILIARES =====

    private HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json");
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        return builder;
    }

    private JsonObject parseResponse(HttpResponse<String> response) throws Exception {
        int status = response.statusCode();
        String body = response.body();

        if (status >= 200 && status < 300) {
            if (body == null || body.isBlank()) {
                return new JsonObject();
            }
            JsonElement element = JsonParser.parseString(body);
            if (element.isJsonObject()) {
                return element.getAsJsonObject();
            }
            // Wrap arrays or primitives
            JsonObject wrapper = new JsonObject();
            wrapper.add("data", element);
            return wrapper;
        } else {
            String errorMsg = "Error del servidor (HTTP " + status + ")";
            try {
                JsonObject errorJson = JsonParser.parseString(body).getAsJsonObject();
                if (errorJson.has("message")) {
                    errorMsg = errorJson.get("message").getAsString();
                } else if (errorJson.has("error")) {
                    errorMsg = errorJson.get("error").getAsString();
                }
            } catch (Exception ignored) {
                if (body != null && !body.isBlank()) {
                    errorMsg = body;
                }
            }
            throw new Exception(errorMsg);
        }
    }

    private JsonArray parseArrayResponse(HttpResponse<String> response) throws Exception {
        int status = response.statusCode();
        String body = response.body();

        if (status >= 200 && status < 300) {
            if (body == null || body.isBlank()) {
                return new JsonArray();
            }
            JsonElement element = JsonParser.parseString(body);
            if (element.isJsonArray()) {
                return element.getAsJsonArray();
            }
            JsonArray arr = new JsonArray();
            arr.add(element);
            return arr;
        } else {
            String errorMsg = "Error del servidor (HTTP " + status + ")";
            try {
                JsonObject errorJson = JsonParser.parseString(body).getAsJsonObject();
                if (errorJson.has("message")) {
                    errorMsg = errorJson.get("message").getAsString();
                } else if (errorJson.has("error")) {
                    errorMsg = errorJson.get("error").getAsString();
                }
            } catch (Exception ignored) {
                if (body != null && !body.isBlank()) {
                    errorMsg = body;
                }
            }
            throw new Exception(errorMsg);
        }
    }

    // ===== AUTENTICACION =====

    /**
     * Inicia sesion y almacena el token JWT.
     * @return JsonObject con id, nombre, permisos
     */
    public JsonObject login(String nombre, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("nombre", nombre);
        body.addProperty("password", password);

        HttpRequest request = requestBuilder("/api/usuarios/login")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject result = parseResponse(response);

        if (result.has("token")) {
            this.jwtToken = result.get("token").getAsString();
        }
        return result;
    }

    /**
     * Registra un nuevo usuario.
     * @return JsonObject con id, nombre
     */
    public JsonObject register(String nombre, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("nombre", nombre);
        body.addProperty("password", password);

        HttpRequest request = requestBuilder("/api/usuarios/register")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseResponse(response);
    }

    // ===== PERFIL DE USUARIO =====

    /**
     * Obtiene el perfil de un usuario por ID.
     */
    public JsonObject getProfile(int userId) throws Exception {
        HttpRequest request = requestBuilder("/api/usuarios/profile/" + userId)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseResponse(response);
    }

    /**
     * Actualiza la contrasena del usuario.
     */
    public void updatePassword(int userId, String oldPassword, String newPassword) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("oldPassword", oldPassword);
        body.addProperty("newPassword", newPassword);

        HttpRequest request = requestBuilder("/api/usuarios/" + userId + "/password")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        parseResponse(response);
    }

    /**
     * Solicita generar un nuevo TAP para el usuario.
     * @return el nuevo TAP generado
     */
    public int requestTap(int userId) throws Exception {
        HttpRequest request = requestBuilder("/api/usuarios/" + userId + "/tap")
                .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject result = parseResponse(response);
        if (result.has("tap")) {
            return result.get("tap").getAsInt();
        }
        throw new Exception("No se recibio el TAP del servidor");
    }

    // ===== ADMIN - USUARIOS =====

    public JsonArray getAllUsuarios() throws Exception {
        HttpRequest request = requestBuilder("/api/admin/usuarios")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseArrayResponse(response);
    }

    public void createUsuario(String nombre, String password, String permisos) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("nombre", nombre);
        body.addProperty("password", password);
        body.addProperty("permisos", permisos);

        HttpRequest request = requestBuilder("/api/admin/usuarios")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        parseResponse(response);
    }

    public void updateUsuario(int id, String nombre, String permisos, Integer tap, Float emisiones) throws Exception {
        JsonObject body = new JsonObject();
        if (nombre != null) body.addProperty("nombre", nombre);
        if (permisos != null) body.addProperty("permisos", permisos);
        if (tap != null) body.addProperty("tap", tap);
        if (emisiones != null) body.addProperty("emisiones", emisiones);

        HttpRequest request = requestBuilder("/api/admin/usuarios/" + id)
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        parseResponse(response);
    }

    public void deleteUsuario(int id) throws Exception {
        HttpRequest request = requestBuilder("/api/admin/usuarios/" + id)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        parseResponse(response);
    }

    // ===== ADMIN - PRODUCTOS =====

    public JsonArray getAllProductos() throws Exception {
        HttpRequest request = requestBuilder("/api/admin/productos")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseArrayResponse(response);
    }

    public void createProducto(String tipo, long barras, String nombre, float emisiones, String material, String imagen) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("tipo", tipo);
        body.addProperty("numeroBarras", barras);
        body.addProperty("nombre", nombre);
        body.addProperty("emisionesReducibles", emisiones);
        body.addProperty("material", material);
        if (imagen != null) body.addProperty("imagen", imagen);

        HttpRequest request = requestBuilder("/api/admin/productos")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        parseResponse(response);
    }

    public void updateProducto(String tipoOrig, long barrasOrig, String nombre, float emisiones, String material, String imagen) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("nombre", nombre);
        body.addProperty("emisionesReducibles", emisiones);
        body.addProperty("material", material);
        if (imagen != null) body.addProperty("imagen", imagen);

        String path = "/api/admin/productos/" + encodeUri(tipoOrig) + "/" + barrasOrig;
        HttpRequest request = requestBuilder(path)
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        parseResponse(response);
    }

    public void deleteProducto(String tipo, long barras) throws Exception {
        String path = "/api/admin/productos/" + encodeUri(tipo) + "/" + barras;
        HttpRequest request = requestBuilder(path)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        parseResponse(response);
    }

    // ===== ADMIN - TRANSACCIONES =====

    public JsonArray getAllTransacciones() throws Exception {
        HttpRequest request = requestBuilder("/api/admin/transacciones")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseArrayResponse(response);
    }

    public void createTransaccion(int idUsuario, String tipo, long barras, String fecha, String hora) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("idUsuario", idUsuario);
        body.addProperty("tipo", tipo);
        body.addProperty("numeroBarras", barras);
        body.addProperty("fecha", fecha);
        body.addProperty("hora", hora);

        HttpRequest request = requestBuilder("/api/admin/transacciones")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        parseResponse(response);
    }

    public void deleteTransaccion(int idUsuario, String tipo, long barras, String fecha, String hora) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("idUsuario", idUsuario);
        body.addProperty("tipo", tipo);
        body.addProperty("numeroBarras", barras);
        body.addProperty("fecha", fecha);
        body.addProperty("hora", hora);

        HttpRequest request = requestBuilder("/api/admin/transacciones")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        parseResponse(response);
    }

    // ===== INFORMES =====

    /**
     * Descarga un informe PDF generado por la API.
     * @param nombre nombre del informe (informe1, informe2)
     * @param params parametros del informe (ej: NombreUsuario)
     * @return bytes del PDF
     */
    public byte[] downloadReport(String nombre, java.util.Map<String, String> params) throws Exception {
        StringBuilder path = new StringBuilder("/api/admin/reports/" + encodeUri(nombre));
        if (params != null && !params.isEmpty()) {
            path.append("?");
            boolean first = true;
            for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) path.append("&");
                path.append(encodeUri(entry.getKey())).append("=").append(encodeUri(entry.getValue()));
                first = false;
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path.toString()))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", jwtToken != null ? "Bearer " + jwtToken : "")
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            String errorBody = new String(response.body());
            String errorMsg = "Error al generar informe (HTTP " + response.statusCode() + ")";
            try {
                JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();
                if (errorJson.has("message")) {
                    errorMsg = errorJson.get("message").getAsString();
                }
            } catch (Exception ignored) {}
            throw new Exception(errorMsg);
        }
    }

    // ===== HEALTH CHECK =====

    /**
     * Verifica que la API esta accesible.
     */
    public boolean verificarConexion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            // Si /api/health no existe, intentar con la raiz
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() < 500;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    /**
     * Limpia el token (para logout).
     */
    public void logout() {
        this.jwtToken = null;
    }

    public String getToken() {
        return jwtToken;
    }

    // ===== UTILIDADES =====

    private String encodeUri(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
