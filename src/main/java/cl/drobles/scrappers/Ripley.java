/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.scrappers;

import cl.drobles.dao.ProductoRipleyDAO;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
public class Ripley {

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
        ProductoRipleyDAO dao = new ProductoRipleyDAO();
        int pagina = 1;
        int contador = 0;
        Date inicio = new Date();
        while (categorias.size() > contador) {
            String urlPage = "https://simple.ripley.cl/" + categorias.get(contador).getCategoria().trim();
            driver.getWebdriver().get(urlPage);
            if (!driver.getWebdriver().getCurrentUrl().equals("https://simple.ripley.cl/")) {
                List<VistaProductoCategoria> listaCategorias = dao.obtProductosPorCategoria(categorias.get(contador).getCategoria());
                if (listaCategorias != null) {
                    System.out.println("==== Cargando: " + categorias.get(contador).getCategoria() + " ====");
                    while (true) {
                        String curr = urlPage + "?page=" + pagina;
                        System.out.println("curr " + curr);
                        List<Producto> listaAux = new ArrayList();
                        try {
                            driver.getWebdriver().get(curr);
                            List<WebElement> resultsList = driver.getWebdriver().findElements(By.className("ProductItem"));
                            String jsonTest = "";
                            try{
                                jsonTest = driver.getWebdriver().findElement(By.cssSelector("script[type='application/ld+json']")).getAttribute("innerHTML");
                            }
                            catch(Exception ex)
                            {
                                //end
                            }
                            JSONParser parser = new JSONParser();
                            try {
                                WebDriverWait wait = new WebDriverWait(driver.getWebdriver(), 5);
                                wait.until(ExpectedConditions.elementToBeClickable(By.className("catalog-container")));

                                } catch (Exception ex) {
                                    System.out.println("err1");
                                    //break;
                                }                           
                            
                            try {
                                WebDriverWait wait = new WebDriverWait(driver.getWebdriver(), 5);                               
                                wait.until(ExpectedConditions.elementToBeClickable(By.className("catalog-container")));
                            } catch (Exception ex) {
                                System.out.println("err2 " + ex);
                                //break;
                            }
                            if(!jsonTest.equals(""))
                            {
                                JSONObject json = (JSONObject) parser.parse(jsonTest);
                                JSONArray ja_data = (JSONArray)json.get("itemListElement");
                                for(int i = 0; i < ja_data.size(); i++)
                                {
                                    Producto producto = new Producto();
                                    JSONObject obj = (JSONObject)ja_data.get(i);
                                    JSONObject obj2 = (JSONObject)obj.get("item");
                                   // JSONObject obj3 = (JSONObject)obj2.get("offers");
                                    producto.setSku(obj2.get("sku").toString());
                                    producto.setImagen(obj2.get("image").toString());
                                    listaAux.add(producto);
                                } 
                            }
                            List<WebElement> resultsList2 = driver.getWebdriver().findElements(By.className("catalog-product-item"));
                            productos.clear();
                            if (!resultsList.isEmpty()) {
                                for (WebElement ele : resultsList) {
                                    Producto producto = new Producto();
                                    producto.setNombre(ele.findElement(By.className("ProductItem__Name")).getText());
                                    producto.setLink(ele.findElement(By.className("ProductItem__Name")).findElement(By.className("ProductItem__Name")).getAttribute("href"));
                                    String[] sku_aux = producto.getLink().split("-");
                                    producto.setSku(sku_aux[sku_aux.length - 1].replace("p", ""));
                                    producto.setCategoria(categorias.get(contador).getCategoria());
                                    producto.setMarca(ele.findElement(By.cssSelector("div.brand-logo > span")).getText());
                                    producto.setId_tienda(5);
                                    try {
                                        if (!ele.findElements(By.className("ProductItem__RightPrice")).isEmpty() && !ele.findElements(By.className("ProductItem__RightPrice")).get(0).getText().equals("")) {
                                            List<WebElement> precios = ele.findElement(By.className("catalog-prices__list")).findElements(By.tagName("li"));
                                            if (!precios.isEmpty()) {
                                                producto.setPrecio(Integer.parseInt(precios.get(precios.size() - 1).getAttribute("innerHTML").split("<")[0].replace("$", "").replace(".", "").trim()));
                                            }
                                            productos.add(producto);
                                        }
                                    } catch (Exception ex) {
                                        System.out.println(ex);
                                    }
                                }

                            } else if (!resultsList2.isEmpty()) {
                                int i = 0;
                                for (WebElement ele : resultsList2) {
                                    Producto producto = new Producto();
                                    producto.setNombre(ele.findElement(By.className("catalog-product-details__name")).getText());
                                    producto.setLink(ele.getAttribute("href"));
                                    String[] sku_aux = producto.getLink().split("-");
                                    producto.setSku(sku_aux[sku_aux.length - 1]);
                                    producto.setCategoria(categorias.get(contador).getCategoria());
                                    producto.setMarca(ele.findElement(By.cssSelector("div.brand-logo > span")).getText());
                                    producto.setId_tienda(5);
                                    String skuComp = producto.getSku();
                                    if(listaAux.get(i).getSku().endsWith("p"))
                                    {
                                        skuComp = producto.getSku() + "p";
                                    }
                                    if(listaAux.get(i).getSku().toLowerCase().equals(skuComp))
                                    {
                                         producto.setImagen("https:" + listaAux.get(i).getImagen());
                                    }
                                    else{
                                        for(Producto pr: listaAux)
                                        {
                                            if(pr.getSku().toLowerCase().equals(skuComp))
                                            {
                                                 producto.setImagen("https:" + pr.getImagen());
                                                 break;
                                            }
                                        }
                                    }
                                    try {
                                        if (ele.findElements(By.className("catalog-prices__offer-price")).isEmpty() || ele.findElements(By.className("catalog-prices__offer-price")).get(0).getText().equals("")) {
                                            System.out.println("Agotado " + producto.getNombre());
                                        } else {
                                            List<WebElement> precios = ele.findElement(By.className("catalog-prices__list")).findElements(By.tagName("li"));
                                            if (!precios.isEmpty()) {
                                                producto.setPrecio(Integer.parseInt(precios.get(precios.size() - 1).getAttribute("innerHTML").split("<")[0].replace("$", "").replace(".", "").trim()));
                                            }
                                            if(!producto.getSku().equals("mm00011842494"))
                                                productos.add(producto);
                                        }
                                    } catch (Exception ex) {
                                        System.out.println(ex);
                                    }
                                    i++;
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
                            //System.out.println("ex " + ex + " link: " + curr);
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

        Date fin = new Date();
        Logger.getLogger(Sodimac.class.getName()).log(Level.INFO, "\nInicio: " + inicio + "\nFin: " + fin + "\nDuración: " + fun.getDateDiff(inicio, fin, TimeUnit.MINUTES), "");

    }

}
