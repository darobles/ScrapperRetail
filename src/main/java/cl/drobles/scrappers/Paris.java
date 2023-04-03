/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.scrappers;

import cl.drobles.dao.ProductoParisDAO;
import cl.drobles.dato.CategoriaTienda;
import cl.drobles.dato.Driver;
import cl.drobles.dato.Producto;
import cl.drobles.dato.VistaProductoCategoria;
import cl.drobles.threads.InsProductoThread;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author drobles
 */
public class Paris {

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
            System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
            driver.setWebdriver(fun.getDriverFirefox(optFirefox));

        } else {
            if (cierreAutomatico) {
                fun.endChromeAllTask();
            }
            driver.setWebdriver(fun.getDriverChrome());

        }

        List<Producto> productos = new ArrayList();
        ProductoParisDAO dao = new ProductoParisDAO();
        int pagina = 1;
        int contador = 0;
        Date inicio = new Date();
        while (categorias.size() > contador) {
            String urlPage = "https://www.paris.cl/" + categorias.get(contador).getCategoria().trim();
            driver.getWebdriver().get(urlPage);
            if (!driver.getWebdriver().getCurrentUrl().equals("https://www.paris.cl/")) {
                List<VistaProductoCategoria> listaCategorias = dao.obtProductosPorCategoria(categorias.get(contador).getCategoria());
                if (listaCategorias != null) {
                    System.out.println("==== Cargando: " + categorias.get(contador).getCategoria() + " ====");
                    while (true) {
                        String curr = urlPage + "?start=" + 40 * pagina + "&sz=40";
                        try {
                            driver.getWebdriver().get(curr);
                            List<WebElement> resultsList = driver.getWebdriver().findElements(By.className("product-tile"));
                            productos.clear();
                            if (!resultsList.isEmpty() && driver.getWebdriver().findElements(By.className("empty-search")).isEmpty()) {
                                String dim2 = resultsList.get(0).getAttribute("data-product").split("dimension2\":\"")[1].split(",")[0].replace("\"","");
                                for (WebElement ele : resultsList) {
                                    Producto producto = new Producto();
                                    WebElement priceCont = ele.findElement(By.className("box-desc-product"));
                                    String jsonParis = ele.getAttribute("data-product").replaceAll("'", "").replaceAll("\"", "'");
                                    JsonObject o = new JsonParser().parse(jsonParis).getAsJsonObject();
                                    producto.setNombre(ele.findElement(By.className("ellipsis_text")).getText().replaceAll("'", "\""));
                                    producto.setSku(o.get("variant").getAsString());
                                    producto.setLink(priceCont.findElement(By.className("js-product-layer")).getAttribute("href"));
                                    producto.setCategoria(categorias.get(contador).getCategoria());
                                    producto.setMarca(o.get("brand").getAsString());
                                    producto.setId_tienda(4);   
                                    producto.setImagen(ele.findElements(By.className("img-prod")).get(0).getAttribute("data-src").split("\\?")[0]);
                                    try {
                                        producto.setPrecio(o.get("price").getAsInt());
                                        if(producto.getPrecio() > 0)
                                        {
                                            productos.add(producto);
                                        }                                        
                                    } catch (NumberFormatException ex) {
                                        System.out.println(ex);
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

        Date fin = new Date();
        Logger.getLogger(Sodimac.class.getName()).log(Level.INFO, "\nInicio: " + inicio + "\nFin: " + fin + "\nDuración: " + fun.getDateDiff(inicio, fin, TimeUnit.MINUTES), "");

    }

}
