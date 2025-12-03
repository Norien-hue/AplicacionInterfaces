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
                otros.add(statement);
            }
        }
        
        try {
            for (String stmt : productos) {
                Statement s = conn.createStatement();
                s.executeUpdate(stmt);
            }
            
            for (String stmt : usuarios) {
                Statement s = conn.createStatement();
                s.executeUpdate(stmt);
            }
            
            for (String stmt : recicla) {
                Statement s = conn.createStatement();
                s.executeUpdate(stmt);
            }
            
            for (String stmt : otros) {
                Statement s = conn.createStatement();
                s.executeUpdate(stmt);
            }
            
            preparedStatements.clear();
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error ejecutando sentencias SQL: " + e.getMessage());
        }
    }
}