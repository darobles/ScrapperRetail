package cl.drobles.scrappers;

import cl.drobles.dao.ProductoSodimacDAO;
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author drobles
 */
public class Sodimac {

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
        ProductoSodimacDAO dao = new ProductoSodimacDAO();
        int pagina = 1;
        int contador = 0;
        Date inicio = new Date();
        while (categorias.size() > contador) {
            String urlPage = "https://www.sodimac.cl/sodimac-cl/category/" + categorias.get(contador).getPrefijo() + categorias.get(contador).getCategoria().trim() + "?isPLP=1";
            driver.getWebdriver().get(urlPage);
            if (!driver.getWebdriver().getCurrentUrl().equals("https://www.sodimac.cl/sodimac-cl/no-search-result?Ntt=")) {
                List<VistaProductoCategoria> listaCategorias = dao.obtProductosPorCategoria(categorias.get(contador).getCategoria());
                
                if (listaCategorias != null) {
                    System.out.println("==== Cargando: " + driver.getWebdriver().findElement(By.className("category-title")).getText() + " ====");
                   // String context = driver.getWebdriver().getCurrentUrl().replace(urlPage, "");
                    while (true) {
                        String curr = urlPage + "&currentpage=" + pagina;
                        try {
                            driver.getWebdriver().get(curr);
                            List<WebElement> resultsList = driver.getWebdriver().findElements(By.xpath("//*[@data-category]"));
                            productos.clear();
                            if (!resultsList.isEmpty() && !driver.getWebdriver().getCurrentUrl().equals("https://www.sodimac.cl/sodimac-cl/no-search-result?Ntt=")) {
                                for (WebElement ele : resultsList) {
                                    Producto producto = new Producto();
                                    //WebElement header = ele.findElement(By.className("product-wrapper"));
                                    producto.setNombre(ele.findElement(By.className("product-title")).getText());
                                    producto.setSku(ele.getAttribute("data-key").trim());
                                    producto.setLink(ele.findElement(By.className("link-primary")).getAttribute("href"));
                                    producto.setCategoria(categorias.get(contador).getCategoria());
                                    producto.setMarca(ele.findElement(By.className("product-brand")).getText());
                                    producto.setId_tienda(2);
                                    producto.setImagen("https://sodimac.scene7.com/is/image//SodimacCL/"+producto.getSku());
                                    //System.out.println("Vendedor: " + header.findElement(By.className("pod-sellerText")).getText()); 
                                    WebElement precio = null;
                                    try {
                                        precio = ele.findElement(By.className("desktop-price-cart-btn")).findElement(By.className("product-price-and-logo")).findElement(By.className("price")); //"fa--prices" price-0
                                    } catch (NoSuchElementException ex) {
                                        System.out.println("Error");
                                        try {
                                            precio = ele.findElement(By.className("pod-summary"));
                                        } catch (Exception e) {
                                            System.out.println("Error 3");
                                        }
                                    }
                                    if (precio != null) {
                                        String precioInt = precio.getText().replace("$", "").replace(".", "").trim();

                                        producto.setId_tienda(2);
                                        try {
                                            producto.setPrecio(Integer.parseInt(precioInt));
                                        } catch (NumberFormatException ex) {
                                            //System.out.println("Ex " + ex + " " + curr);
                                            precioInt = precioInt.replace("(Oferta)", "").replace("C/U", "").replace("ML", "").replace("PAR", "").replace("CAJA", "").replace("PACK", "");
                                            if (precioInt.contains("-")) {
                                                precioInt = precioInt.split("-")[0].trim();
                                            }
                                            producto.setPrecio(Integer.parseInt(precioInt));
                                        }
                                        //break;
                                    }
                                    productos.add(producto);
                                    //System.out.println("Precio: " + ele.findElement(By.className("pod-summary")).findElement(By.className("pod-prices")).getText());
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
                            System.out.println("Exception: " + ex + " en " + curr);
                            pagina++;

                        }
                    }
                }
                pagina = 1;
                driver.getWebdriver().close();
                driver.getWebdriver().quit();
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
