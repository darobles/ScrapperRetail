/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.scrappers;

import cl.drobles.dao.ProductoLiderDAO;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author Daniel
 */
public class Lider {
    
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
        ProductoLiderDAO dao = new ProductoLiderDAO();
        int pagina = 1;
        int contador = 0;
        Date inicio = new Date();
        String urlBase = "https://www.lider.cl/catalogo/category/";
        while (categorias.size() > contador) {
            String urlPage = urlBase + categorias.get(contador).getPrefijo() + categorias.get(contador).getCategoria();
            driver.getWebdriver().get(urlPage);
            if (!driver.getWebdriver().getCurrentUrl().equals("https://www.lider.cl/catalogo/category/")) {
                List<VistaProductoCategoria> listaCategorias = dao.obtProductosPorCategoria(categorias.get(contador).getCategoria());
                if (listaCategorias != null) {
                    System.out.println("==== Cargando: " + categorias.get(contador).getCategoria() + " ====");
                    while (true) {
                        String curr = urlPage + "?page=" + pagina;
                        try {      
                            
                            driver.getWebdriver().get(curr);
                            try{
                                WebDriverWait wait = new WebDriverWait(driver.getWebdriver(), 10);
                                wait.until(ExpectedConditions.elementToBeClickable(By.className("ais-Hits-item")));
                            }
                            catch(Exception ex)
                            {
                                break;
                            }
                            //driver.getWebdriver().manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
                            List<WebElement> resultsList = driver.getWebdriver().findElements(By.className("ais-Hits-item"));
                           // System.out.println("resultsList " + resultsList.size());
                            productos.clear();
                            if (!resultsList.isEmpty() && driver.getWebdriver().findElements(By.className("empty-search")).isEmpty()) {
                                for (WebElement ele : resultsList) {
                                    WebElement boton = ele.findElement(By.className("walmart-add-cart-button-container"));
                                    List<WebElement> agotado = boton.findElements(By.className("walmart-add-cart-button-out-of-stock"));
                                    if(agotado.isEmpty())
                                    {
                                        Producto producto = new Producto();
                                        WebElement priceCont = ele.findElement(By.className("walmart-sales-price"));
                                        List<WebElement> nombreMarca = ele.findElements(By.cssSelector("div.product-description > div > span"));
                                        producto.setNombre(nombreMarca.get(1).getText());
                                        producto.setSku(ele.findElement(By.cssSelector("div.product-block > a > div")).getAttribute("id"));
                                        producto.setLink(ele.findElement(By.cssSelector("div.product-block > a")).getAttribute("href"));
                                        producto.setCategoria(categorias.get(contador).getCategoria());
                                        producto.setMarca(nombreMarca.get(0).getText());
                                        producto.setId_tienda(6);
                                        producto.setImagen("https://images.lider.cl/wmtcl?source=url[file:/productos/"+producto.getSku()+"a.jpg]&scale=size[300x300]&sink");
                                        String precioStr = "";
                                        try {
                                            precioStr = priceCont.getText().split("\n")[0];
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

        System.out.println("Inicio: " + inicio);
        System.out.println("Fin " + new Date());
        System.out.println("Duración " + fun.getDateDiff(inicio, new Date(), TimeUnit.MINUTES) + "min");
    }

}
