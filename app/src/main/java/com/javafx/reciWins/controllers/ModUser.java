package com.javafx.reciWins.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import com.javafx.model.Transaccion;
import com.javafx.model.Usuario;
import com.javafx.reciWins.start.StartWin;
import com.javafx.reciWins.utiles.SQLstatementStorage;
import com.javafx.reciWins.utiles.StorageSharer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class ModUser implements Initializable{

    @FXML
    private RadioButton adminRoleUsuarioMod;

    @FXML
    private Button btn_cancelar;

    @FXML
    private TextField emisionesModUsuario;

    @FXML
    private TextField idModUsuario;

    @FXML
    private Button modUsuarioConfirmar;

    @FXML
    private TextField nombreModUsuario;

    @FXML
    private TextField tapModUsuario;

    @FXML
    private RadioButton userRoleUsuarioMod;

    @FXML
    void kill(ActionEvent event) {
        StorageSharer.itemStorage.forEach(e -> System.out.println(e) ); //TODO : eliminar esto, es para debug solo
        StorageSharer.itemStorage.clear();
        StorageSharer.itemStorage.forEach(e -> System.out.println(e) ); //TODO : eliminar esto, es para debug solo
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idModUsuario.setText(StorageSharer.itemStorage.get(0));
        nombreModUsuario.setText(StorageSharer.itemStorage.get(1));

        String role  = StorageSharer.itemStorage.get(2);
        if(role.equals("cliente")){
            userRoleUsuarioMod.selectedProperty().set(true);
        }else{
            adminRoleUsuarioMod.selectedProperty().set(true);
        }
        tapModUsuario.setText(StorageSharer.itemStorage.get(3));
        emisionesModUsuario.setText(StorageSharer.itemStorage.get(4)); //TODO: cambiar esto por que es un valor calculado en realidad y por ende debo hacer que la bd se encargue de eso
        Platform.runLater(() -> {
            ((Stage)btn_cancelar.getScene().getWindow()).getIcons().add(StartWin.icon);
        });
    }

    @FXML
    void modUsuarioPulsado(ActionEvent event) {
        launchAlertsModUser(); //TODO: Imp if para poner todo si esto devuelve false si y si no error
        String role = userRoleUsuarioMod.isSelected() ? "cliente" : "admin";
        SQLstatementStorage.storeStatement("UPDATE Usuarios SET Nombre = '"+ nombreModUsuario.getText() +"', Permisos = '"+ role +"', TAP = '"+ tapModUsuario.getText() +"', Emisiones_Reducidas = '"+ emisionesModUsuario.getText() +"' WHERE Id_Usuario = '"+ idModUsuario.getText() +"'");
        //TODO: eliminar este debug todo su explendor
        if(StorageSharer.itemPre instanceof Usuario){
            System.out.println(StorageSharer.itemPre);
            System.out.println((Usuario)StorageSharer.itemPre);
        }
        if(StorageSharer.itemToMod instanceof Usuario){
            System.out.println(StorageSharer.itemToMod);
            System.out.println((Usuario)StorageSharer.itemToMod);
        }
        MainController.modItem();
        StorageSharer.itemToMod = null;
        StorageSharer.itemStorage.clear();
        ((Stage)btn_cancelar.getScene().getWindow()).close();
    }

    private static boolean checkAlert = false; 

    private void checkForAlertModUser(){

        ArrayList<TextField> camposTexto = new ArrayList<>();

        camposTexto.add(idModUsuario);
        camposTexto.add(nombreModUsuario);
        camposTexto.add(tapModUsuario);
        camposTexto.add(emisionesModUsuario);

        camposTexto.forEach((e) -> {
            if(e.getText().contains("@") || e.getText().contains("?") || e.getText().contains("=") || e.getText().contains("'") || e.getText().contains("\"") || e.getText().contains("|") || e.getText().contains("&") || e.getText().contains("*") || e.getText().contains("+") || e.getText().contains("\\") || e.getText().strip().equals("")){
                checkAlert = true;
            }
        });
    }

    private boolean launchAlertsModUser(){
        checkForAlertModUser();

        boolean ret = false; 

        if(checkAlert){
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText("Campo invalido");
            a.setContentText("O se introdujeron solo espacios o se han usado algunos de los siguentes caracteres en algun campo: @, ?, =, ', \", |, *, &, |, *, + or \\");
            a.showAndWait();
            ret = true;
        }        

        checkAlert = false;

        return ret;
    }
}
