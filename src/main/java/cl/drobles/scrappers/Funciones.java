/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.drobles.scrappers;

import cl.drobles.main.Scrapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 *
 * @author drobles
 */
public class Funciones {

    String OS = "win";
    String plataforma = "";

    public Funciones(String OS, String plataforma) {
        this.OS = OS;
        this.plataforma = plataforma;
    }

    public WebDriver getDriverChrome() {

        ChromeOptions optChrome = new ChromeOptions();
        optChrome.addArguments("headless", "--disable-gpu", "--blink-settings=imagesEnabled=false");
        //optChrome.addExtensions(new File ("BlockImage.crx"));
        optChrome.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        if (OS.contains("win")) {
            System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        } else {
            if (plataforma != null && plataforma.equals("heroku")) {
                // optChrome.setBinary("/app/.apt/usr/bin/google-chrome");
                System.setProperty("webdriver.chrome.driver", "/app/.chromedriver/bin/chromedriver");
            } else {
                System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
            }
        }
        System.setProperty("webdriver.chrome.silentOutput", "true");
        ChromeDriverService chromeDriverService = ChromeDriverService.createDefaultService();
        WebDriver driver = new ChromeDriver(chromeDriverService, optChrome);
        int port = chromeDriverService.getUrl().getPort();
        try {
            System.out.println("PID " + GetChromeDriverProcessID(port));
        } catch (IOException ex) {
            Logger.getLogger(Funciones.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Funciones.class.getName()).log(Level.SEVERE, null, ex);
        }

        return driver;
    }

    public WebDriver getDriverFirefox(FirefoxOptions options) {
        System.setProperty("webdriver.gecko.driver", "Scrapper" + File.separator +"geckodriver.exe");
        WebDriver driver = new FirefoxDriver(options);
        WebElement html = driver.findElement(By.tagName("html"));
        html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
        return driver;
    }
    

    public void endFirefoxTask() {
        System.out.println("OS " + OS);
        try {
            if (OS.contains("win")) {

                Runtime.getRuntime().exec("taskkill /F /IM geckodriver.exe");
                Runtime.getRuntime().exec("taskkill /F /IM firefox.exe");

            } else {
                Runtime.getRuntime().exec("pkill geckodriver");
                Runtime.getRuntime().exec("pkill Firefox");
            }
        } catch (IOException ex) {
            Logger.getLogger(Scrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void endChromeAllTask() {
        try {
            if (OS.contains("win")) {

                Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
                Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");

            } else {
                Runtime.getRuntime().exec("pkill chromedriver");
                Runtime.getRuntime().exec("pkill Chrome");
            }
        } catch (IOException ex) {
            Logger.getLogger(Scrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void endChromedrive(int PID) {
        try {
            if (OS.contains("win")) {

                Runtime.getRuntime().exec("taskkill /F /PID " + PID);
            } else {
                Runtime.getRuntime().exec("kill -9 " + PID);
            }
        } catch (IOException ex) {
            Logger.getLogger(Scrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    private int GetChromeDriverProcessID(int aPort) throws IOException, InterruptedException {
        String[] commandArray = new String[3];

        if (OS.contains("win")) {

            commandArray[0] = "cmd";
            commandArray[1] = "/c";
            commandArray[2] = "netstat -aon | findstr LISTENING | findstr " + aPort;
        } else {
            commandArray[0] = "/bin/sh";
            commandArray[1] = "-c";
            commandArray[2] = "netstat -anp | grep LISTEN | grep " + aPort;
        }

        Process p = Runtime.getRuntime().exec(commandArray);
        p.waitFor();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }

        String result = sb.toString().trim();

        return !OS.contains("win") ? ParseChromeDriverLinux(result) : ParseChromeDriverWindows(result);
    }

    private int ParseChromeLinux(String result) {
        String[] pieces = result.split("\\s+");
        // root 20780 20772 20759 15980  9 11:04 pts/1    00:00:00 /opt/google/chrome/google-chrome.........
        // the second one is the chrome process id
        return Integer.parseInt(pieces[1]);
    }

    private int ParseChromeWindows(String result) {
        String[] pieces = result.split("\\s+");
        // C:\Program Files (x86)\Google\Chrome\Application\chrome.exe 14304 19960
        return Integer.parseInt(pieces[pieces.length - 1]);
    }

    private int ParseChromeDriverLinux(String netstatResult) {
        String[] pieces = netstatResult.split("\\s+");
        String last = pieces[pieces.length - 1];
        // tcp 0 0 127.0.0.1:2391 0.0.0.0:* LISTEN 3333/chromedriver
        return Integer.parseInt(last.substring(0, last.indexOf('/')));
    }

    private int ParseChromeDriverWindows(String netstatResult) {
        String[] pieces = netstatResult.split("\\s+");
        // TCP 127.0.0.1:26599 0.0.0.0:0 LISTENING 22828
        return Integer.parseInt(pieces[pieces.length - 1]);
    }
}
