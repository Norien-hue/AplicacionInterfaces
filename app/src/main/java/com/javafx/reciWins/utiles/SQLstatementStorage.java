package com.javafx.reciWins.utiles;

import java.sql.SQLException;
import java.util.ArrayList;
import com.javafx.reciWins.start.StartWin;



public class SQLstatementStorage {
    private static ArrayList<String> preparedStatements = new ArrayList<>();

    private static boolean executeStatement(){
        String currentSentence = preparedStatements.removeFirst();
        
        try {
            StartWin.conn.createStatement().execute(currentSentence);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static void storeStatement(String statement){
        preparedStatements.add(statement);
    }

    public static void clearStorage(){
        preparedStatements.clear();
    }

    public static void executeEverything(){
        preparedStatements.forEach((e) -> executeStatement());;
    }
}
