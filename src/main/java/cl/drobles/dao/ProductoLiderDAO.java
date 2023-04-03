/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dao;

import cl.drobles.conexion.Conexion;
import cl.drobles.dato.Producto;
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
public class ProductoLiderDAO extends AbstractProductoDAO {

    @Override
    public void insertarProductos(List<Producto> productos) {
        Connection conn = null;
        Conexion bd = new Conexion();
        try {
            conn = bd.getConection();
            PreparedStatement stmt = null;
            conn.setAutoCommit(false);
            try {
                stmt = conn.prepareStatement("INSERT INTO PRODUCTO_LIDER(sku,nombre,marca,categoria,precio,link,tienda,imagen,fecha_act) \n"
                        + "VALUES(?,?,?,?,?,?,?,?,NOW() - INTERVAL 4 HOUR)");
                for (Producto producto : productos) {
                    stmt.setString(1, producto.getSku());
                    stmt.setString(2, producto.getNombre());
                    stmt.setString(3, producto.getMarca());
                    stmt.setString(4, producto.getCategoria());
                    stmt.setInt(5, producto.getPrecio());
                    stmt.setString(6, producto.getLink());
                    stmt.setInt(7, producto.getId_tienda());
                    stmt.setString(8, producto.getImagen());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                stmt.clearBatch();
                conn.commit();

            } catch (SQLException e) {
                Logger.getLogger(ProductoLiderDAO.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(ProductoLiderDAO.class.getName()).log(Level.SEVERE, null, e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ProductoLiderDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ProductoLiderDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public List<VistaProductoCategoria> obtProductosPorCategoria(String categoria) {
        Conexion bd = new Conexion();
        int flag = 0;
        List<VistaProductoCategoria> prod_categorias = new ArrayList();
        Connection conn = bd.getConection();
        PreparedStatement stmt;
        ResultSet rs;
        try {
            String query = "SELECT p.sku,nombre,link,historico,fec_historico,ult_precio,fec_ult_precio FROM (SELECT sku,\n"
                    + "         MIN(precio) AS historico,        \n"
                    + "         fecha_act AS fec_historico\n"
                    + "       FROM PRODUCTO_LIDER\n"
                    + "       GROUP BY sku) h, (SELECT fecha_act AS fec_ult_precio,\n"
                    + "           precio AS ult_precio,\n"
                    + "            sku AS sku,\n"
                    + "           categoria AS categoria\n"
                    + "         FROM PRODUCTO_LIDER\n"
                    + "         WHERE id IN  (SELECT MAX(id) FROM PRODUCTO_LIDER WHERE categoria = ? GROUP BY sku) ) l, PRODUCTO_LIDER p\n"
                    + " WHERE l.sku = h.sku\n"
                    + " AND p.sku = l.sku\n"
                    + " AND p.categoria = ?\n"
                    + " GROUP BY sku";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, categoria);
            stmt.setString(2, categoria);
            rs = stmt.executeQuery();
            while (rs.next()) {
                VistaProductoCategoria producto = new VistaProductoCategoria();
                producto.setSku(rs.getString("sku"));
                producto.setNombre(rs.getString("nombre"));
                producto.setLink(rs.getString("link"));
                producto.setPrecio_his(rs.getInt("historico"));
                producto.setFec_historico(rs.getDate("fec_historico"));
                producto.setUlt_precio(rs.getInt("ult_precio"));
                producto.setFec_ult_precio(rs.getDate("fec_ult_precio"));
                producto.setCategoria(categoria);
                prod_categorias.add(producto);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ProductoLiderDAO.class.getName()).log(Level.SEVERE, null, ex);
            flag = 1;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ProductoLiderDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (flag > 0) {
            return null;
        }
        return prod_categorias;
    }

}
