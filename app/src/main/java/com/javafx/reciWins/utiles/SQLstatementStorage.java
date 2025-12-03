package com.javafx.reciWins.utiles;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.javafx.reciWins.start.StartWin;

public class SQLstatementStorage {
    public static ArrayList<String> preparedStatements = new ArrayList<>();
    private static Connection conn = StartWin.conn;

    public static void storeStatement(String statement) {
        preparedStatements.add(statement);
    }

    public static void executeStatements() {
        // Ordenar sentencias para evitar violaciones de FK
        List<String> productos = new ArrayList<>();
        List<String> usuarios = new ArrayList<>();
        List<String> recicla = new ArrayList<>();
        List<String> otros = new ArrayList<>();
        
        // Clasificar las sentencias por tipo
        for (String statement : preparedStatements) {
            String upper = statement.toUpperCase();
            
            if (upper.contains("INSERT INTO PRODUCTOS") || 
                upper.contains("UPDATE PRODUCTOS") || 
                upper.contains("DELETE FROM PRODUCTOS")) {
                productos.add(statement);
            }
            else if (upper.contains("INSERT INTO USUARIOS") || 
                     upper.contains("UPDATE USUARIOS") || 
                     upper.contains("DELETE FROM USUARIOS")) {
                usuarios.add(statement);
            }
            else if (upper.contains("INSERT INTO RECICLA") || 
                     upper.contains("UPDATE RECICLA") || 
                     upper.contains("DELETE FROM RECICLA")) {
                recicla.add(statement);
            }
            else {
                // Otros tipos de sentencias
                otros.add(statement);
            }
        }
        
        // Ejecutar en el orden correcto para mantener integridad referencial
        try {
            // 1. Productos primero (las transacciones dependen de ellos)
            for (String stmt : productos) {
                Statement s = conn.createStatement();
                s.executeUpdate(stmt);
            }
            
            // 2. Usuarios (las transacciones también dependen de ellos)
            for (String stmt : usuarios) {
                Statement s = conn.createStatement();
                s.executeUpdate(stmt);
            }
            
            // 3. Recicla (depende de Productos y Usuarios)
            for (String stmt : recicla) {
                Statement s = conn.createStatement();
                s.executeUpdate(stmt);
            }
            
            // 4. Otros tipos
            for (String stmt : otros) {
                Statement s = conn.createStatement();
                s.executeUpdate(stmt);
            }
            
            // Limpiar la lista después de ejecutar
            preparedStatements.clear();
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error ejecutando sentencias SQL: " + e.getMessage());
        }
    }
}