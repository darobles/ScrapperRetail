/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dao;

import cl.drobles.conexion.Conexion;
import cl.drobles.dato.CategoriaTienda;
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
public class CategoriaTiendaDAO {
     public List<CategoriaTienda> obtCategoriasPorUsuario(int id_usuario) {
        Conexion bd = new Conexion();
        List<CategoriaTienda> categorias = new ArrayList();
        Connection conn = bd.getConection();
        PreparedStatement stmt;
        ResultSet rs;
        try {
            String query = "SELECT id,categoria,nombre,tienda,sensibilidad,id_usuario, prefijo FROM CATEGORIA_TIENDA WHERE id_usuario = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, id_usuario);
            rs = stmt.executeQuery();
            while (rs.next()) {
                CategoriaTienda categoria = new CategoriaTienda();
                categoria.setId(rs.getInt("id"));
                categoria.setCategoria(rs.getString("categoria"));
                categoria.setNombre(rs.getString("nombre"));                
                categoria.setTienda(rs.getInt("tienda"));
                categoria.setSensibilidad(rs.getInt("sensibilidad"));
                categoria.setId_usuario(id_usuario);
                categoria.setPrefijo(rs.getString("prefijo"));
                categorias.add(categoria);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(UsuarioDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return categorias;
    }
}
