/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.threads;

import cl.drobles.dao.AbstractProductoDAO;
import cl.drobles.dato.Producto;
import cl.drobles.dato.VistaProductoCategoria;
import cl.drobles.telegram.TelegramNotifier;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author drobles
 */
public class InsProductoThread extends Thread {
    
    AbstractProductoDAO dao;
    List<Producto> productos;
    String key = "1280030807:AAHbuYksY8KuZoswRHYk8rBAkmRSUNbm0dM";
    List<VistaProductoCategoria> listaProductosCategoria;
    int sensibilidad;
    
    public InsProductoThread(AbstractProductoDAO dao, List<Producto> productos, List<VistaProductoCategoria> listaProductosCategoria, int sensibilidad) {
        this.dao = dao;
        this.productos = new ArrayList<>(productos);
        this.listaProductosCategoria = listaProductosCategoria;
        this.sensibilidad = sensibilidad;
    }    
    
    
    @Override
    public void run() {
        boolean existe = false;
        List<Producto> listaInsertar = new ArrayList();
        TelegramNotifier tele = new TelegramNotifier(-1001271651698L, "1280030807:AAHbuYksY8KuZoswRHYk8rBAkmRSUNbm0dM");
        for(Producto producto: productos)
        {
            existe = false;
            for(VistaProductoCategoria vista: listaProductosCategoria)
            {
                if(vista.getSku().equals(producto.getSku()))
                {
                    //System.out.println(vista.toString());
                    existe = true;
                    if(vista.getUlt_precio() > 0)
                    {
                        int variacion = 100 - producto.getPrecio()*100/vista.getUlt_precio();
                        if(producto.getPrecio() != vista.getUlt_precio())
                        {
                            
                            if((variacion > sensibilidad && producto.getPrecio() < vista.getPrecio_his()) || variacion > 80 )  //Notificar
                            {
                                tele.sendMsg(variacion +"% - " + producto.getNombre() + " *$"+ producto.getPrecio() +"* " + producto.getLink());
                            }
                            System.out.println("Producto " + producto.getSku() + " " + producto.getImagen());
                            listaInsertar.add(producto);
                        }
                    }
                    else{
                        vista.getUlt_precio();
                    }
                    break;
                }
            }
            if(!existe)
            {
                listaInsertar.add(producto);
            }
            
        }
        if(!listaInsertar.isEmpty())
        {
            listaInsertar.forEach(producto -> {
                String newNombre  = Normalizer.normalize(producto.getNombre(), Normalizer.Form.NFD);
                newNombre = newNombre.replaceAll("[^\\p{ASCII}]", "");
                    producto.setNombre(newNombre);
                            });
            dao.insertarProductos(listaInsertar);
        }
   
    }
}
