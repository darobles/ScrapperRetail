/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author drobles
 */
public class Conexion {

    public Connection getConection() {
        Connection con = null;
        try {
            // Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://URL/odum";
            con = DriverManager.getConnection(url, "username", "password");
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }

        return con;
    }
}
