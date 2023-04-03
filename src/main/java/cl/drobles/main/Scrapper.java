/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.main;

import cl.drobles.dao.CategoriaTiendaDAO;
import cl.drobles.dao.ParametroDAO;
import cl.drobles.dao.ProductoFalabellaDAO;
import cl.drobles.dao.UsuarioDAO;
import cl.drobles.dato.CategoriaTienda;
import cl.drobles.dato.Parametro;
import cl.drobles.dato.Usuario;
import cl.drobles.scrappers.Easy;
import cl.drobles.scrappers.Falabella;
import cl.drobles.scrappers.Funciones;
import cl.drobles.scrappers.LaPolar;
import cl.drobles.scrappers.Lider;
import cl.drobles.scrappers.Linio;
import cl.drobles.scrappers.Paris;
import cl.drobles.scrappers.Ripley;
import cl.drobles.scrappers.Sodimac;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Daniel
 */
public class Scrapper {

    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String VERSION = "3.3";
    private static boolean cierreAutomatico = true;
    static Properties prop = new Properties();

    public static void main(String[] args) throws Exception {
        ParametroDAO daoParam = new ParametroDAO();
        CategoriaTiendaDAO daoCategorias = new CategoriaTiendaDAO();
        UsuarioDAO daoUser = new UsuarioDAO();
        Parametro versionOficial = daoParam.obtParametroPorNombre("VERSION");
        if (Integer.parseInt(VERSION.replace(".", "")) < Integer.parseInt(versionOficial.getValor1().replace(".", ""))) {
            System.out.println("Esta versión esta desactualizada.\nVersión local: " + VERSION + "\nVersión actual: " + versionOficial.getValor1() +"\n Ejecute el Launcher");
            return;
        }
        InputStream input = new FileInputStream("config.properties");
        prop.load(input);
        String nom_usr = prop.getProperty("usuario");
        String pass_usr = prop.getProperty("password");
        String autocerrado = prop.getProperty("autocerrado");
        if (autocerrado != null && autocerrado.equals("false")) {

            cierreAutomatico = false;

        }
        Logger.getLogger(Scrapper.class.getName()).log(Level.INFO, "cierre automatico de navegador: " + cierreAutomatico, "cierre automatico de navegador: " + cierreAutomatico);
        Usuario usuario = daoUser.obtUsuario(nom_usr, pass_usr);
        if (usuario.getId() == 0) {
            Logger.getLogger(ProductoFalabellaDAO.class.getName()).log(Level.INFO, "Usuario o password no validos", "Usuario o password no validos");
            return;
        } else {
            //LLama al scrapper
            List<CategoriaTienda> listaCategorias = daoCategorias.obtCategoriasPorUsuario(usuario.getId());
            if (!listaCategorias.isEmpty()) {
                System.out.println("Usuario " + usuario.getNombre() + " ultima vez conectado " + usuario.getFec_act());
                String tienda = "Falabella";
                if (listaCategorias.get(0).getTienda() == 2) {
                    tienda = "Sodimac";
                }
                else if(listaCategorias.get(0).getTienda() == 3){
                    tienda = "Linio";
                }
                else if(listaCategorias.get(0).getTienda() == 4){
                    tienda = "Paris";
                }
                else if(listaCategorias.get(0).getTienda() == 5){
                    tienda = "Ripley";
                }
                else if(listaCategorias.get(0).getTienda() == 6){
                    tienda = "Lider";
                }
                else if(listaCategorias.get(0).getTienda() == 7){
                    tienda = "Easy";
                }
                else if(listaCategorias.get(0).getTienda() == 8){
                    tienda = "La Polar";
                }
                System.out.println("Tienda: " + tienda);
                System.out.println("categorias asignadas: ");
                for (CategoriaTienda cat : listaCategorias) {
                    System.out.println(cat.getNombre());
                }
            } else {
                System.out.println("Sin categorias asignadas");
                return;
            }

            usuario.setActivo(true);
            daoUser.actUsrLogin(usuario);
            Funciones fun = new Funciones(OS, prop.getProperty("plataforma"));
            if (listaCategorias.get(0).getTienda() == 1) //Falabella
            {
                Falabella falabella = new Falabella();
                falabella.initScrapper(prop.getProperty("driver"), listaCategorias, cierreAutomatico, fun);
            } else if(listaCategorias.get(0).getTienda() == 2) {
                Sodimac sod = new Sodimac();
                sod.initScrapper(prop.getProperty("driver"), listaCategorias, cierreAutomatico, fun);
            }
            else if(listaCategorias.get(0).getTienda() == 3) {
                Linio linio = new Linio();
                linio.initScrapper(prop.getProperty("driver"), listaCategorias, cierreAutomatico, fun);
            }
            else if(listaCategorias.get(0).getTienda() == 4) {
                Paris paris = new Paris();
                paris.initScrapper(prop.getProperty("driver"), listaCategorias, cierreAutomatico, fun);
            }
            else if(listaCategorias.get(0).getTienda() == 5) {
                Ripley ripley = new Ripley();
                ripley.initScrapper(prop.getProperty("driver"), listaCategorias, cierreAutomatico, fun);
            }
            else if(listaCategorias.get(0).getTienda() == 6){
                    Lider lider = new Lider();
                    lider.initScrapper(prop.getProperty("driver"), listaCategorias, cierreAutomatico, fun);
                
            }
             else if(listaCategorias.get(0).getTienda() == 7){
                    Easy easy = new Easy();
                    easy.initScrapper(prop.getProperty("driver"), listaCategorias, cierreAutomatico, fun);
                
            }
            else if(listaCategorias.get(0).getTienda() == 8){
                LaPolar laPolar = new LaPolar();
                laPolar.initScrapper(prop.getProperty("driver"), listaCategorias, cierreAutomatico, fun);
                
            }
            input.close();
            usuario.setActivo(false);
            daoUser.actUsrLogin(usuario);
        }

    }


}
