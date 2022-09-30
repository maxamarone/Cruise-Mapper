package eu.eurostat.trackingships.marinetraffic;

import eu.eurostat.trackingships.marinetraffic.job.SearchShipEUPortVoyageInfoJob;
import eu.eurostat.trackingships.utility.KillHangDriverProcess;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.BooleanUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;

public class CaptureImageFromMapAIS
{
   private static final Logger LOGGER = Logger.getLogger(CaptureImageFromMapAIS.class.getName());

   public static void main(String[] args)
   {
      try
      {
         //read properties files for directory path
         InputStream propsFile = SearchShipEUPortVoyageInfoJob.class.getClassLoader().getResourceAsStream("config.properties");
         Properties props = new Properties();
         props.load(propsFile);
         String dirRoot = props.getProperty("root_directory");

         //get date time 
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH");  
         LocalDateTime now = LocalDateTime.now();  
         String nowFormatted = dtf.format(now);  

         //set destination directory
         String dirDestination = dirRoot + File.separator + "screenshot";

         //set url and filename array 
         String url = "https://www.marinetraffic.com/en/ais/home/centerx:2.7/centery:51.8/zoom:10";
         String fileName = "AIS_Image_" + nowFormatted + ".jpeg";

         //init web driver
         WebDriver driver = intiWebClient();

         //get page from url
         driver.get(url);
            
         //get element by id
         WebElement element = driver.findElement(By.id("map_area"));

         //accept privacy policy
         WebElement divPrivacy = driver.findElement(By.id("qc-cmp2-ui")); 
         WebElement divButtons = divPrivacy.findElement( By.xpath(".//div[@class='qc-cmp2-footer qc-cmp2-footer-overlay qc-cmp2-footer-scrolled']")); 
         List<WebElement> buttons = divButtons.findElements(By.xpath(".//button"));
         for (WebElement button : buttons)
         {
            String text = button.getText();
            if (text.contains("AGREE"))
            {
               //click privacy button agree
               button.click();
               break;
            }
         }

         /*
         try
         {
            //wait 5 seconds
            Thread.sleep(5*1000);
         }
         catch (InterruptedException ex)
         {
            LOGGER.log(Level.SEVERE, ex.getMessage());
         } 

         //remove google advice
         try 
         {
            WebElement advice = driver.findElement(By.id("google_image_div"));
            JavascriptExecutor js = (JavascriptExecutor) driver; 
            WebElement parent = (WebElement) js.executeScript("return arguments[0].parentNode;", advice);
            js.executeScript("arguments[0].remove();", parent);
         }
         catch (NoSuchElementException ex)
         {
            LOGGER.log(Level.WARNING, ex.getMessage());
         }
         */ 

         //get element screenshot
         byte[] screenshot = element.getScreenshotAs(OutputType.BYTES);

         //save file
         File file = new File(dirDestination, fileName);
         FileOutputStream fileStream = new FileOutputStream(file);
         fileStream.write(screenshot);

         //close web driver
         driver.close();
      }
      catch (IOException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
      finally
      {
         KillHangDriverProcess killDriverProcess = new KillHangDriverProcess();
         killDriverProcess.start();
      }
   }

   private static WebDriver intiWebClient() throws IOException
   {
      WebDriver driver;

      //read properties files for web driver
      InputStream propsFile = SearchShipEUPortVoyageInfoJob.class.getClassLoader().getResourceAsStream("driver.properties");
      Properties props = new Properties();
      props.load(propsFile);
      String driver_file = props.getProperty("web_driver_file");

      //setting gecko driver file
      System.setProperty("webdriver.gecko.driver", driver_file);
      System.setProperty("webdriver.firefox.marionette", "true");

      //set firefox Javascript error level logs
      LoggingPreferences logPrefs = new LoggingPreferences();
      logPrefs.enable(LogType.BROWSER, Level.SEVERE);
      FirefoxOptions firefoxOptions = new FirefoxOptions();
      firefoxOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

      //read properties files for proxy
      propsFile = SearchShipEUPortVoyageInfoJob.class.getClassLoader().getResourceAsStream("proxy.properties");
      props = new Properties();
      props.load(propsFile);
      boolean enabled = BooleanUtils.toBoolean(props.getProperty("proxy_enabled"));
      if (enabled)
      {
         String host = props.getProperty("proxy_host");
         String port = props.getProperty("proxy_port");
         String hostAndPort = host + ":" + port;
         //setting firefox proxy
         Proxy proxy = new Proxy();
         proxy.setHttpProxy(hostAndPort);
         proxy.setSslProxy(hostAndPort);
         firefoxOptions.setCapability(CapabilityType.PROXY, proxy);
      }

      //open driver
      driver = new FirefoxDriver(firefoxOptions);

      //set driver options
      WebDriver.Options options = driver.manage();
      WebDriver.Timeouts timeouts = options.timeouts();
      timeouts.pageLoadTimeout(Duration.ofMinutes(1));
      timeouts.scriptTimeout(Duration.ofMinutes(1));
      timeouts.implicitlyWait(Duration.ofSeconds(10));

      //change window dimension
      WebDriver.Window window = options.window();
      window.maximize();

      return driver;
   }
}
