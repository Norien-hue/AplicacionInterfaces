package com.javafx.reciWins.utiles;

import java.sql.SQLException;
import java.util.ArrayList;
import com.javafx.reciWins.start.StartWin;



public class SQLstatementStorage {
    private static ArrayList<String> preparedStatements = new ArrayList<>();

    public static boolean executeStatements(){

        for(int i = 0; i<preparedStatements.size(); i++){
            String currentSentence = preparedStatements.get(i);
        
            try {
                StartWin.conn.createStatement().execute(currentSentence);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        preparedStatements.clear();

        return true;
    }

    public static void storeStatement(String statement){
        preparedStatements.add(statement);
    }

    public static void clearStorage(){
        preparedStatements.clear();
    }
}
