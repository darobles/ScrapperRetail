/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dato;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Daniel
 */
public class Usuario {
    int id;
    String nombre;
    String password;
    List<CategoriaTienda> categorias;
    boolean activo;
    Date fec_act;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<CategoriaTienda> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaTienda> categorias) {
        this.categorias = categorias;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Date getFec_act() {
        return fec_act;
    }

    public void setFec_act(Date fec_act) {
        this.fec_act = fec_act;
    }

    @Override
    public String toString() {
        return "Usuario{" + "id=" + id + ", nombre=" + nombre + ", password=" + password + ", categorias=" + categorias + ", activo=" + activo + ", fec_act=" + fec_act + '}';
    }
    
}
