/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dato;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author drobles
 */
public class Producto implements Serializable{
    String sku;
    String nombre;
    String marca;
    int precio;
    String categoria;
    String link;
    int id_tienda;
    int variacion;
    Date fecha_actualizacion;
    int precio_anterior;
    String imagen;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getId_tienda() {
        return id_tienda;
    }

    public void setId_tienda(int id_tienda) {
        this.id_tienda = id_tienda;
    }

    public int getVariacion() {
        return variacion;
    }

    public void setVariacion(int variacion) {
        this.variacion = variacion;
    }

    public Date getFecha_actualizacion() {
        return fecha_actualizacion;
    }

    public void setFecha_actualizacion(Date fecha_actualizacion) {
        this.fecha_actualizacion = fecha_actualizacion;
    }

    public int getPrecio_anterior() {
        return precio_anterior;
    }

    public void setPrecio_anterior(int precio_anterior) {
        this.precio_anterior = precio_anterior;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    @Override
    public String toString() {
        return "Producto{" + "sku=" + sku + ", nombre=" + nombre + ", marca=" + marca + ", precio=" + precio + ", categoria=" + categoria + ", link=" + link + ", id_tienda=" + id_tienda + ", variacion=" + variacion + ", fecha_actualizacion=" + fecha_actualizacion + ", precio_anterior=" + precio_anterior + ", imagen=" + imagen + '}';
    }
   
}
