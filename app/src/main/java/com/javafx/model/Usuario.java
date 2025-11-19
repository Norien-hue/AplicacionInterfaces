package com.javafx.model;

public class Usuario {
    private int idUsuario;
    private float emisionesReducidas;
    private String permisos;
    private String nombre;
    private int tap;

    public Usuario(int idUsuario, float emisionesReducidas, String permisos, String nombre, int tap) {
        this.idUsuario = idUsuario;
        this.emisionesReducidas = emisionesReducidas;
        this.permisos = permisos;
        this.nombre = nombre;
        this.tap = tap;
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public float getEmisionesReducidas() {
        return emisionesReducidas;
    }

    public void setEmisionesReducidas(float emisionesReducidas) {
        this.emisionesReducidas = emisionesReducidas;
    }

    public String getPermisos() {
        return permisos;
    }

    public void setPermisos(String permisos) {
        this.permisos = permisos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTap() {
        return tap;
    }

    public void setTap(int tap) {
        this.tap = tap;
    }
}
