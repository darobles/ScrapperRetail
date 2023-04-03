/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.scrappers;

import cl.drobles.dao.ProductoFalabellaDAO;
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
 * @author drobles
 */
public class Falabella {

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
        ProductoFalabellaDAO dao = new ProductoFalabellaDAO();
        int pagina = 1;
        int contador = 0;
        Date inicio = new Date();
        while (categorias.size() > contador) {
            String urlPage = "https://www.falabella.com/falabella-cl/category/" + categorias.get(contador).getPrefijo() + categorias.get(contador).getCategoria() + "/";
            driver.getWebdriver().get(urlPage + "?isPLP=1");
            if (!driver.getWebdriver().getCurrentUrl().equals("https://www.falabella.com/falabella-cl/?isPLP=1")) {
                List<VistaProductoCategoria> listaCategorias = dao.obtProductosPorCategoria(categorias.get(contador).getCategoria());
                if (listaCategorias != null) {
                    System.out.println("==== Cargando: " + categorias.get(contador).getNombre() + " " + categorias.get(contador).getCategoria() + " ====");
                    String context = driver.getWebdriver().getCurrentUrl().replace(urlPage, "");
                    while (true) {
                        String curr = urlPage + context + "&page=" + pagina;
                        try {
                            driver.getWebdriver().get(curr);
                            System.out.println("curr " + curr);
                            List<WebElement> resultsList = driver.getWebdriver().findElements(By.xpath("//*[@data-pod='catalyst-pod']"));
                            //List<WebElement> resultsList = driver.getWebdriver().findElements(By.className("pod-4_GRID"));
                            if(resultsList.size() > 48)
                            {
                                resultsList.remove(0);
                                resultsList.remove(0);
                            }
                            productos.clear();
                            if (!resultsList.isEmpty() && !driver.getWebdriver().getCurrentUrl().equals("https://www.falabella.com/falabella-cl/?isPLP=1")) {
                                for (WebElement ele : resultsList) {
                                    Producto producto = new Producto();
                                    WebElement header = ele.findElement(By.className("pod-link"));
                                    producto.setNombre(header.findElement(By.className("pod-subTitle")).getText());
                                    producto.setSku(ele.getAttribute("data-key").trim());
                                    producto.setLink(header.getAttribute("href"));
                                    producto.setCategoria(categorias.get(contador).getCategoria());
                                    producto.setMarca(header.findElement(By.className("pod-title")).getText());
                                    producto.setImagen("https://falabella.scene7.com/is/image/Falabella/"+producto.getSku());
                                    producto.setId_tienda(1);
                                    //System.out.println("Vendedor: " + header.findElement(By.className("pod-sellerText")).getText()); 
                                    WebElement precio = null;
                                    try {
                                        precio = ele.findElement(By.className("price-0")); //"fa--prices" price-0
                                    } catch (NoSuchElementException ex) {
                                        System.out.println("Error");
                                        try {
                                            precio = ele.findElement(By.className("pod-summary"));
                                        } catch (Exception e) {
                                            System.out.println("Error 3");
                                        }
                                    }
                                    if (precio != null) {
                                        String pr = precio.findElement(By.className("primary")).getText().replace("$", "").replace(".", "").replace("(Oferta)", "").trim();
                                        if (pr.contains("-")) {
                                            pr = pr.split("-")[0].trim();
                                        }
                                        if (!pr.equals("")) {
                                            producto.setPrecio(Integer.parseInt(pr));
                                           // System.out.println(producto.toString());
                                            productos.add(producto);
                                        }
                                    }
                                    
                                }
                            } else {
                                break;
                            }
                            InsProductoThread t1 = new InsProductoThread(dao, productos, listaCategorias, categorias.get(contador).getSensibilidad());
                            t1.start();
                            pagina++;
                            //break;
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
                            break;
                        }
                    }
                }
                pagina = 1;
                driver.getWebdriver().close();
                driver.getWebdriver().quit();
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
