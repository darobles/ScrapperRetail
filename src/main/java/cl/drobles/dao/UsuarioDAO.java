/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dao;

import cl.drobles.conexion.Conexion;
import cl.drobles.dato.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel
 */
public class UsuarioDAO {
     public Usuario obtUsuario(String nombre, String password) {
        Conexion bd = new Conexion();
        Usuario usuario = new Usuario();
        Connection conn = bd.getConection();
        PreparedStatement stmt;
        ResultSet rs;
        try {
            String query = "SELECT id,nombre, password,activo,fecha_act FROM USUARIO WHERE UPPER(nombre) = UPPER(?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, nombre);
            rs = stmt.executeQuery();
            while (rs.next()) {
                if(password.equals(rs.getString("password")))
                {
                    usuario.setId(rs.getInt("id"));
                    usuario.setNombre(rs.getString("nombre"));
                    usuario.setActivo(rs.getBoolean("activo"));
                    usuario.setFec_act(rs.getDate("fecha_act"));
                }
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
        return usuario;
    }
     
    public void actUsrLogin(Usuario usuario) {
        Conexion bd = new Conexion();
        Connection conn = bd.getConection();
        PreparedStatement stmt;
        try {
            String query = "UPDATE USUARIO SET activo = ?, fecha_act = NOW() - INTERVAL 4 HOUR WHERE UPPER(nombre) = UPPER(?)";
            stmt = conn.prepareStatement(query);
            stmt.setBoolean(1, usuario.isActivo());
            stmt.setString(2, usuario.getNombre());
            stmt.executeUpdate();
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
    }
}
