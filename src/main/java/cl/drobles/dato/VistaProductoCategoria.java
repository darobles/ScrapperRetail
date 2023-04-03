/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dato;

import java.util.Date;

/**
 *
 * @author Daniel
 */
public class VistaProductoCategoria {
    String sku;
    String nombre;
    String link;
    int precio_his;
    Date fec_historico;
    int ult_precio;
    Date fec_ult_precio;
    String categoria;

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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getPrecio_his() {
        return precio_his;
    }

    public void setPrecio_his(int precio_his) {
        this.precio_his = precio_his;
    }

    public Date getFec_historico() {
        return fec_historico;
    }

    public void setFec_historico(Date fec_historico) {
        this.fec_historico = fec_historico;
    }

    public int getUlt_precio() {
        return ult_precio;
    }

    public void setUlt_precio(int ult_precio) {
        this.ult_precio = ult_precio;
    }

    public Date getFec_ult_precio() {
        return fec_ult_precio;
    }

    public void setFec_ult_precio(Date fec_ult_precio) {
        this.fec_ult_precio = fec_ult_precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "VistaProductoCategoria{" + "sku=" + sku + ", nombre=" + nombre + ", link=" + link + ", precio_his=" + precio_his + ", fec_historico=" + fec_historico + ", ult_precio=" + ult_precio + ", fec_ult_precio=" + fec_ult_precio + ", categoria=" + categoria + '}';
    }
    
    
    
}
