/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dao;

import cl.drobles.conexion.Conexion;
import cl.drobles.dato.CategoriaTienda;
import cl.drobles.dato.VistaProductoCategoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel
 */
public class VistaProductoCategoriaDAO {
     public List<VistaProductoCategoria> obtCategoriasPorUsuario(String categoria) {
        Conexion bd = new Conexion();
        List<VistaProductoCategoria> prod_categorias = new ArrayList();
        Connection conn = bd.getConection();
        PreparedStatement stmt;
        ResultSet rs;
        try {
            String query = "SELECT COUNT(*) FROM (SELECT sku,\n" +
                            "         MIN(precio) AS historico,        \n" +
                            "         fecha_act AS fec_historico\n" +
                            "       FROM PRODUCTO\n" +
                            "       GROUP BY sku) h, (SELECT fecha_act AS fec_ult_precio,\n" +
                            "           precio AS ult_precio,\n" +
                            "            sku AS sku,\n" +
                            "           categoria AS categoria\n" +
                            "         FROM PRODUCTO\n" +
                            "         WHERE id IN  (SELECT MAX(id) FROM PRODUCTO WHERE categoria = ? GROUP BY sku) ) l, PRODUCTO p\n" +
                            " WHERE l.sku = h.sku\n" +
                            " AND p.sku = l.sku\n" +
                            " AND p.categoria = ?"; 
            stmt = conn.prepareStatement(query);
            stmt.setString(1, categoria);
            stmt.setString(2, categoria);
            rs = stmt.executeQuery();
            while (rs.next()) {
                VistaProductoCategoria producto = new VistaProductoCategoria();
                producto.setSku(rs.getString("sku"));
                producto.setNombre(rs.getString("nombre"));
                producto.setLink(rs.getString("link"));
                producto.setPrecio_his(rs.getInt("precio_his"));
                producto.setFec_historico(rs.getDate("fec_historico"));
                producto.setUlt_precio(rs.getInt("ult_precio"));
                producto.setFec_ult_precio(rs.getDate("fec_ult_precio"));
                producto.setCategoria(rs.getString("categoria"));                
                prod_categorias.add(producto);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(VistaProductoCategoriaDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(VistaProductoCategoriaDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return prod_categorias;
    }
}
