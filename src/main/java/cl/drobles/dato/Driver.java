/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.dato;

import cl.drobles.scrappers.Funciones;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author Daniel
 */
public class Driver {
    WebDriver webdriver;
    int port;
    int PID;
    Funciones fun;
    String browser;
    
    public void close(){
        try{
            webdriver.close();
            webdriver.quit();
        }
        catch(Exception ex)
        {
        
        }
        if(browser.equals("chrome"))
        {
            fun.endChromedrive(PID);
        }
        else{
            fun.endFirefoxTask();
        }
        
        
    }

    public WebDriver getWebdriver() {
        return webdriver;
    }

    public void setWebdriver(WebDriver webdriver) {
        this.webdriver = webdriver;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }
    
    
    
}
