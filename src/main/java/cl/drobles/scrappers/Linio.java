/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.scrappers;

import cl.drobles.dao.ProductoLinioDAO;
import cl.drobles.dato.CategoriaTienda;
import cl.drobles.dato.Driver;
import cl.drobles.dato.Producto;
import cl.drobles.dato.VistaProductoCategoria;
import cl.drobles.threads.InsProductoThread;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 *
 * @author Daniel
 */
public class Linio {

    public void initScrapper(String driverProp, List<CategoriaTienda> categorias, boolean cierreAutomatico, Funciones fun) throws FileNotFoundException, IOException {
        String driverDesc = "chrome";
        FirefoxOptions optFirefox = null;
        Driver driver = new Driver();
        if (driverProp.equals("firefox")) {
            if (cierreAutomatico) {
                fun.endFirefoxTask();
            }
            driverDesc = "firefox";
            optFirefox = new FirefoxOptions()
                    .addPreference("permissions.default.image", 2);
            //   .addArguments("--headless");
            optFirefox.setHeadless(true);
            optFirefox.setPageLoadStrategy(PageLoadStrategy.NORMAL);
            // optFirefox.addPreference("javascript.enabled", false);
            driver.setWebdriver(fun.getDriverFirefox(optFirefox));

        } else {
            if (cierreAutomatico) {
                fun.endChromeAllTask();
            }
            driver.setWebdriver(fun.getDriverChrome());
        }
        List<Producto> productos = new ArrayList();
        ProductoLinioDAO dao = new ProductoLinioDAO();
        int pagina = 1;
        int contador = 0;
        String urlBase = "https://www.linio.cl/c/";
        while (categorias.size() > contador) {
            String urlPage = urlBase + categorias.get(contador).getPrefijo() + categorias.get(contador).getCategoria() + "?is_international=0";
            driver.getWebdriver().get(urlPage);
            if (!driver.getWebdriver().getCurrentUrl().equals("https://www.linio.cl/c/")) {
                List<VistaProductoCategoria> listaCategorias = dao.obtProductosPorCategoria(categorias.get(contador).getCategoria());
                if (listaCategorias != null) {
                    System.out.println("==== Cargando: " + categorias.get(contador).getCategoria() + " ====");
                    while (true) {
                        String curr = urlPage + "&page=" + pagina;
                        try {
                            driver.getWebdriver().get(curr);
                            List<WebElement> resultsList = driver.getWebdriver().findElements(By.className("catalogue-product"));
                            productos.clear();
                            if (!resultsList.isEmpty() && driver.getWebdriver().findElements(By.className("empty-search")).isEmpty()) {
                                for (WebElement ele : resultsList) {
                                    Producto producto = new Producto();
                                    WebElement priceCont = ele.findElement(By.className("price-section"));
                                    producto.setNombre(ele.findElement(By.cssSelector("meta[itemprop='name']")).getAttribute("content"));
                                    producto.setSku(ele.findElement(By.cssSelector("meta[itemprop='sku']")).getAttribute("content"));
                                    producto.setLink("https://www.linio.cl" + priceCont.findElement(By.cssSelector("meta[itemprop='url']")).getAttribute("content"));
                                    producto.setCategoria(categorias.get(contador).getCategoria());
                                    producto.setMarca(ele.findElement(By.cssSelector("meta[itemprop='brand']")).getAttribute("content"));
                                    producto.setId_tienda(3);
                                    producto.setImagen("https:" +ele.findElement(By.cssSelector("meta[itemprop='image']")).getAttribute("content"));
                                    List<WebElement> precios = null;

                                    String precioStr = "";
                                    try {
                                        precios = priceCont.findElements(By.className("lowest-price"));
                                        if (precios.size() == 1) {
                                            precioStr = precios.get(0).findElement(By.className("price-main-md")).getText();
                                        } else if (precios.size() == 2) {
                                            precioStr = precios.get(1).findElement(By.className("price-promotional")).getText();
                                        }
                                    } catch (NoSuchElementException ex) {
                                        System.out.println("Error de conversion");
                                    }
                                    if (!precioStr.equals("")) {
                                        try {
                                            producto.setPrecio(Integer.parseInt(precioStr.replace(".", "").replace("$", "")));
                                            productos.add(producto);
                                        } catch (NumberFormatException ex) {
                                            System.out.println(ex);
                                        }

                                    }
                                }
                            } else {
                                break;
                            }
                            InsProductoThread t1 = new InsProductoThread(dao, productos, listaCategorias, categorias.get(contador).getSensibilidad());
                            t1.start();
                            pagina++;
                        } catch (Exception ex) {
                            driver.getWebdriver().close();
                            driver.getWebdriver().quit();
                            if (driverDesc.equals("chrome")) {
                                driver.setWebdriver(fun.getDriverChrome());
                            } else {
                                driver.setWebdriver(fun.getDriverFirefox(optFirefox));
                            }
                            System.out.println("ex " + ex + " link: " + curr);
                            pagina++;
                        }
                    }
                }
                pagina = 1;
                try {
                    driver.getWebdriver().close();
                    driver.getWebdriver().quit();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                if (driverDesc.equals("chrome")) {                    
                    driver.setWebdriver(fun.getDriverChrome());
                } else {
                    driver.setWebdriver(fun.getDriverFirefox(optFirefox));

                }
            }
            contador++;
        }
        driver.getWebdriver().close();
        driver.getWebdriver().quit();
        if (cierreAutomatico) {
            if (driverDesc.equals("chrome")) {
                fun.endChromeAllTask();
            } else {
                fun.endFirefoxTask();
            }
        }

    }

}
