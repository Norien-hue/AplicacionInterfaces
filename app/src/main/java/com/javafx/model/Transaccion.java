package com.javafx.model;

import java.sql.Date;
import java.sql.Time;

public class Transaccion {
    private int idUsuario;
    private String tipo;
    private long numeroBarras;
    private Date fecha;
    private Time hora;

    public Transaccion(int idUsuario, String tipo, long numeroBarras, Date fecha, Time hora) {
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.numeroBarras = numeroBarras;
        this.fecha = fecha;
        this.hora = hora;
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Time getHora() {
        return hora;
    }

    public void setHora(Time hora) {
        this.hora = hora;
    }
}
