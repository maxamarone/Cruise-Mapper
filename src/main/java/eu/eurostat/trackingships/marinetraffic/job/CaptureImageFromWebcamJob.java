package eu.eurostat.trackingships.marinetraffic.job;

import eu.eurostat.trackingships.utility.KillHangDriverProcess;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.BooleanUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CaptureImageFromWebcamJob implements Job
{
   private static final Logger LOGGER = Logger.getLogger(CaptureImageFromWebcamJob.class.getName());

   @Override
   public void execute(JobExecutionContext context)
           throws JobExecutionException
  {
      try
      {
         //read properties files for directory path
         InputStream propsFile = SearchShipEUPortVoyageInfoJob.class.getClassLoader().getResourceAsStream("config.properties");
         Properties props = new Properties();
         props.load(propsFile);
         String dirRoot = props.getProperty("root_directory");

         //get date time 
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd");  
         LocalDateTime now = LocalDateTime.now();  
         String nowFormatted = dtf.format(now);  

         //set destination directory
         String dirDestination = dirRoot + File.separator + "webcam";

         //define url and filename array
         String[] urls = new String[24];
         String[] fileNames = new String[24];
         
         //set url and filename array 
         urls[0] = "https://webcam-binnenvaart.nl/pages/cam-famke.php";
         fileNames[0] = "cam-famke_" + nowFormatted + ".jpeg";
         urls[1] = "https://webcam-binnenvaart.nl/pages/cam-mi-vida.php";
         fileNames[1] = "cam-mi-vida_" + nowFormatted + ".jpeg";
         urls[2] = "https://webcam-binnenvaart.nl/pages/cam-strick.php";
         fileNames[2] = "cam-strick_" + nowFormatted + ".jpeg";
         urls[3] = "https://webcam-binnenvaart.nl/pages/cam-de-zuiderzee.php";
         fileNames[3] = "cam-de-zuiderzee_" + nowFormatted + ".jpeg";
         urls[4] = "https://webcam-binnenvaart.nl/pages/cam-anita.php";
         fileNames[4] = "cam-anita_" + nowFormatted + ".jpeg";
         urls[5] = "https://webcam-binnenvaart.nl/pages/cam-sakura.php";
         fileNames[5] = "cam-sakura_" + nowFormatted + ".jpeg";
         urls[6] = "https://webcam-binnenvaart.nl/pages/cam-everingen.php";
         fileNames[6] = "cam-everingen_" + nowFormatted + ".jpeg";
         urls[7] = "https://webcam-binnenvaart.nl/pages/cam-rhenus-carisma.php";
         fileNames[7] = "cam-rhenus-carisma_" + nowFormatted + ".jpeg";
         urls[8] = "https://webcam-binnenvaart.nl/pages/cam-survivor.php";
         fileNames[8] = "cam-survivor_" + nowFormatted + ".jpeg";
         urls[9] = "https://webcam-binnenvaart.nl/pages/cam-lauwerszee.php";
         fileNames[9] = "cam-lauwerszee_" + nowFormatted + ".jpeg";
         urls[10] = "https://webcam-binnenvaart.nl/pages/cam-red-fox.php";
         fileNames[10] = "cam-red-fox_" + nowFormatted + ".jpeg";
         urls[11] = "https://webcam-binnenvaart.nl/pages/cam-furka.php";
         fileNames[11] = "cam-furka_" + nowFormatted + ".jpeg";
         urls[12] = "https://webcam-binnenvaart.nl/pages/cam-ariel.php";
         fileNames[12] = "cam-ariel_" + nowFormatted + ".jpeg";
         urls[13] = "https://webcam-binnenvaart.nl/pages/cam-anita2.php";
         fileNames[13] = "cam-anita2_" + nowFormatted + ".jpeg";
         urls[14] = "https://webcam-binnenvaart.nl/pages/cam-yaris.php";
         fileNames[14] = "cam-yaris_" + nowFormatted + ".jpeg";
         urls[15] = "https://webcam-binnenvaart.nl/pages/cam-gilla.php";
         fileNames[15] = "cam-gilla_" + nowFormatted + ".jpeg";
         urls[16] = "https://webcam-binnenvaart.nl/pages/cam-zequinsly.php";
         fileNames[16] = "cam-zequinsly_" + nowFormatted + ".jpeg";
         urls[17] = "https://webcam-binnenvaart.nl/pages/cam-guardian.php";
         fileNames[17] = "cam-guardian_" + nowFormatted + ".jpeg";
         urls[18] = "https://webcam-binnenvaart.nl/pages/cam-patriot.php";
         fileNames[18] = "cam-patriot_" + nowFormatted + ".jpeg";
         urls[19] = "https://webcam-binnenvaart.nl/pages/cam-feniks.php";
         fileNames[19] = "cam-feniks_" + nowFormatted + ".jpeg";
         urls[20] = "https://webcam-binnenvaart.nl/pages/cam-salamanca.php";
         fileNames[20] = "cam-salamanca_" + nowFormatted + ".jpeg";
         urls[21] = "https://webcam-binnenvaart.nl/pages/cam-sayonara.php";
         fileNames[21] = "cam-sayonara_" + nowFormatted + ".jpeg";
         urls[22] = "https://webcam-binnenvaart.nl/pages/cam-drift.php";
         fileNames[22] = "cam-drift_" + nowFormatted + ".jpeg";
         urls[23] = "https://webcam-binnenvaart.nl/pages/cam-mastodont.php";
         fileNames[23] = "cam-mastodont" + nowFormatted + ".jpeg";

         //init web driver
         WebDriver driver = intiWebClient();

         //cycle for all urls
         for (int i=0; i<urls.length; i++)
         {
            //set url 
            String url = urls[i];
            //set filename       
            String fileName = fileNames[i];

            //get page from url
            driver.get(url);

            //get element by id
            WebElement element = driver.findElement(By.id("video_html5_api"));

            /*
            //move to element 
            ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView(true);", element);
            */
            
            try
            {
               //wait 5 seconds
               Thread.sleep(3*1000);
            }
            catch (InterruptedException ex)
            {
               LOGGER.log(Level.SEVERE, ex.getMessage());
            } 

            //get element screenshot
            byte[] screenshot = element.getScreenshotAs(OutputType.BYTES);

            //save file
            File file = new File(dirDestination, fileName);
            FileOutputStream fileStream = new FileOutputStream(file);
            fileStream.write(screenshot);
         }

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