/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dao;

import cl.drobles.conexion.Conexion;
import cl.drobles.dato.Parametro;
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
public class ParametroDAO {

    public Parametro obtParametroPorNombre(String nombre) {
        Conexion bd = new Conexion();
        Parametro parametro = new Parametro();
        Connection conn = bd.getConection();
        PreparedStatement stmt;
        ResultSet rs;
        try {
            String query = "SELECT id,nombre, valor1,valor2 FROM PARAMETRO WHERE nombre = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, nombre);
            rs = stmt.executeQuery();
            while (rs.next()) {
                parametro.setId(rs.getInt("id"));
                parametro.setNombre(rs.getString("nombre"));
                parametro.setValor1(rs.getString("valor1"));
                parametro.setValor2(rs.getString("valor2"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(ProductoFalabellaDAO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ProductoFalabellaDAO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return parametro;
    }
}
