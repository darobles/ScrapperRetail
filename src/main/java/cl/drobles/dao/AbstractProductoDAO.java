/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dao;

import cl.drobles.dato.Producto;
import cl.drobles.dato.VistaProductoCategoria;
import java.util.List;

/**
 *
 * @author drobles
 */
public abstract class AbstractProductoDAO {

    public abstract List<VistaProductoCategoria> obtProductosPorCategoria(String categoria);

    public abstract void insertarProductos(List<Producto> productos);

}
