/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.scrappers;

import cl.drobles.dao.ProductoEasyDAO;
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
public class Easy {

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
        ProductoEasyDAO dao = new ProductoEasyDAO();
        int contador = 0;
        Date inicio = new Date();
        while (categorias.size() > contador) {
            String urlPage = "https://www.easy.cl/tienda/categoria/" + categorias.get(contador).getCategoria().trim();
            driver.getWebdriver().get(urlPage + "?cur_pos=0&cur_page=1&cur_view=grid");
            String items = "0";
            if (!driver.getWebdriver().getCurrentUrl().equals("https://www.easy.cl/tienda/categoria/")) {
                List<VistaProductoCategoria> listaCategorias = dao.obtProductosPorCategoria(categorias.get(contador).getCategoria());
                if (listaCategorias != null) {
                    System.out.println("==== Cargando: " + categorias.get(contador).getCategoria() + " ====");
                    String curr = urlPage + "?cur_pos=" + items + "&cur_page=1&cur_view=grid";
                    try {
                        driver.getWebdriver().get(curr);
                        try {
                            WebDriverWait wait = new WebDriverWait(driver.getWebdriver(), 10);
                            wait.until(ExpectedConditions.elementToBeClickable(By.className("search-display__products")));

                        } catch (Exception ex) {

                        }
                        WebElement itemsTotales = driver.getWebdriver().findElement(By.className("load-more-products-div"));
                        items = itemsTotales.getText().split(" ")[3];
                        if (!items.equals("0")) {

                            driver.getWebdriver().get(urlPage + "?cur_pos=" + items + "&cur_page=1&cur_view=grid");
                            WebDriverWait wait = new WebDriverWait(driver.getWebdriver(), 10);
                            wait.until(ExpectedConditions.elementToBeClickable(By.className("search-display__products")));
                            List<WebElement> resultsList = driver.getWebdriver().findElements(By.className("product"));
                            productos.clear();
                            if (!resultsList.isEmpty()) {
                                for (WebElement ele : resultsList) {
                                    Producto producto = new Producto();
                                    producto.setNombre(ele.findElement(By.className("product_name")).getText());
                                    producto.setLink(ele.findElement(By.className("product_name")).getAttribute("href"));
                                    String[] sku_aux = producto.getLink().split("-");
                                    if (sku_aux.length == 1) {
                                        String[] sku_aux2 = producto.getLink().split("/");
                                        producto.setSku(sku_aux2[sku_aux2.length - 1]);
                                    } else {
                                        producto.setSku(sku_aux[sku_aux.length - 1]);
                                    }
                                    producto.setCategoria(categorias.get(contador).getCategoria());
                                    producto.setMarca("");
                                    producto.setId_tienda(7);
                                    if(producto.getSku().endsWith("p"))
                                    {
                                        producto.setImagen("https://media.easy.cl/is/image/EasySA/"+producto.getSku().substring(0, producto.getSku().length()-1));
                                    }
                                    else{
                                        producto.setImagen("https://media.easy.cl/is/image/EasySA/"+producto.getSku());
                                    }
                                    
                                    try {
                                        
                                        int tam = ele.findElement(By.className("product_price")).findElements(By.className("internet_rojo")).size();
                                        String precio = "";
                                        if(tam > 0)
                                        {
                                            precio = ele.findElement(By.className("product_price")).findElement(By.className("internet_rojo")).getText().replace("Internet: ", "").replace("$", "").replace(".", "");
                                        }
                                        else{
                                            precio = ele.findElement(By.className("product_price")).findElement(By.className("pricevisible")).getText().replace("Internet: ", "").replace("$", "").replace(".", "");
                                        }
                                        producto.setPrecio(Integer.parseInt(precio));
                                        productos.add(producto);
                                    } catch (Exception ex) {
                                        System.out.println(ex);
                                    }
                                }

                            }
                            InsProductoThread t1 = new InsProductoThread(dao, productos, listaCategorias, categorias.get(contador).getSensibilidad());
                            t1.start();
                        }
                    } catch (Exception ex) {
                        driver.getWebdriver().close();
                        driver.getWebdriver().quit();
                        if (driverDesc.equals("chrome")) {
                            driver.setWebdriver(fun.getDriverChrome());
                        } else {
                            driver.setWebdriver(fun.getDriverFirefox(optFirefox));
                        }
                        System.out.println("ex " + ex + " link: " + curr);
                    }
                }
                try {
                    driver.getWebdriver().close();
                    driver.getWebdriver().quit();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                if (driverDesc.equals("chrome")) {
                    //driver.quit();
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
