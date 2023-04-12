package com.example.carwhasmovil.modelos;

public class Historial {
    private String numero;
    private String vehiculo;
    private String servicio;
    private String ubicacion;
    private String fecha;
    private String estado;

    public Historial(){}

    public Historial(String vehiculo, String servicio, String ubicacion, String fecha, String estado) {
        this.vehiculo = vehiculo;
        this.servicio = servicio;
        this.ubicacion = ubicacion;
        this.fecha = fecha;
        this.estado = estado;
    }


    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(String vehiculo) {
        this.vehiculo = vehiculo;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
