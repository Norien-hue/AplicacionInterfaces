package com.javafx.model;

public class Producto {
    private String tipo;
    private long numeroBarras;
    private String nombre;
    private float emisionesReducibles;
    private String material;

    public Producto(String tipo, long numeroBarras, String nombre, float emisionesReducibles, String material) {
        this.tipo = tipo;
        this.numeroBarras = numeroBarras;
        this.nombre = nombre;
        this.emisionesReducibles = emisionesReducibles;
        this.material = material;
    }

    // Getters y Setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getNumeroBarras() {
        return numeroBarras;
    }

    public void setNumeroBarras(long numeroBarras) {
        this.numeroBarras = numeroBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getEmisionesReducibles() {
        return emisionesReducibles;
    }

    public void setEmisionesReducibles(float emisionesReducibles) {
        this.emisionesReducibles = emisionesReducibles;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }
}
