package eu.eurostat.trackingships.marinetraffic;

import eu.eurostat.trackingships.marinetraffic.job.SearchShipEUPortVoyageInfoJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchShipDetailFromURL
{
   private static final Logger LOGGER = Logger.getLogger(SearchShipDetailFromURL.class.getName());

   private static final String FILE_COLS_SEPARATOR = ";";

   private static boolean privacyVesselDetails = false;
   
   public static void main(String[] args) throws InterruptedException
   {
      try
      {
         //read properties files for directory path
         InputStream propsFile = SearchShipEUPortVoyageInfoJob.class.getClassLoader().getResourceAsStream("config.properties");
         Properties props = new Properties();
         props.load(propsFile);
         String dirRoot = props.getProperty("root_directory");
         String subDir = props.getProperty("subdir_marinetraffic");
         String subDirShipDetail = props.getProperty("subdir_marinetraffic_shipdetailfromurl");
         String fileNameIn = props.getProperty("file_marinetraffic_ship_url");
         String fileNameOut = props.getProperty("file_marinetraffic_ship_detail");
         //set directory name
         String dirName = dirRoot + File.separator + subDir + File.separator + subDirShipDetail;

         //read file input
         File fileIn = new File(dirName + File.separator + fileNameIn);
         FileInputStream fis = new FileInputStream(fileIn);
         InputStreamReader ir = new InputStreamReader(fis, StandardCharsets.UTF_8.name());
         BufferedReader br = new BufferedReader(ir);

         //get date time 
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH");  
         LocalDateTime now = LocalDateTime.now();  
         String nowFormatted = dtf.format(now);   
         
         //write file output
         File fileOut = new File(dirName + File.separator + nowFormatted + "_" + fileNameOut);
         FileOutputStream fos = new FileOutputStream(fileOut);
         OutputStreamWriter ow = new OutputStreamWriter(fos, StandardCharsets.UTF_8.name());
         BufferedWriter bw = new BufferedWriter(ow);

         //reduce HtmlUnit log verbosity
         LogFactory logFactory = LogFactory.getFactory();
         logFactory.setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

         Logger loggerHTMLUnit = Logger.getLogger("com.gargoylesoftware.htmlunit");
         loggerHTMLUnit.setLevel(Level.OFF);
         Logger loggerHTTPClient = Logger.getLogger("org.apache.commons.httpclient");
         loggerHTTPClient.setLevel(Level.OFF);
         Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
         Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

         //init web driver
         WebDriver driver = intiWebClient();
         
         //activate javascript
         JavascriptExecutor js = (JavascriptExecutor) driver;

         //cycle file input lines
         while (br.ready()) 
         {
            String line = br.readLine();
            //LOGGER.log(Level.INFO, "line {0}", line);

            String urlSearch = line.substring(0, line.length());
            LOGGER.log(Level.INFO, "URL Search {0}", urlSearch);

            //get url 
            driver.get(urlSearch);

            if (!privacyVesselDetails)
            {
               privacyVesselDetails = true;

               //manage privacy popup window
               WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
               wait.until(presenceOfElementLocated(By.id("qc-cmp2-ui")));
               WebElement privFirstDiv = driver.findElement(By.id("qc-cmp2-ui"));
               WebElement privSecDiv = privFirstDiv.findElement(By.xpath(".//div[@class='qc-cmp2-summary-buttons']"));
               List<WebElement> privButts = privSecDiv.findElements(By.xpath(".//button[@mode='secondary']"));
               for (WebElement privButt : privButts)
               {
                  String textButt = privButt.getText();
                  if (textButt.contains("AGREE"))
                  {
                     //click privacy button agree
                     privButt.click();
                     break;
                  }
               }

               //"MuiDialog-root"
            }

            //scroll screen to activate section
            for (int i=0; i<20; i++)
            {
               int yScroll = (i+1) * 50;
               js.executeScript("window.scrollBy(0," + yScroll + ")");
               Thread.sleep(1000);//wait 1 seconds
            }

            //ship_type generic
            WebElement divTypeGeneric = driver.findElement(By.id("shipType"));
            String shipTypeGeneric = divTypeGeneric.getText();
            shipTypeGeneric = shipTypeGeneric.replace("Vessel Type - Generic: ", "");
            line += FILE_COLS_SEPARATOR + shipTypeGeneric; 

            //ship_type specific
            WebElement divTypeDetailed = driver.findElement(By.id("shipTypeSpecific"));
            String shipTypeDetailed = divTypeDetailed.getText();
            shipTypeDetailed = shipTypeDetailed.replace("Vessel Type - Detailed: ", "");

            line += FILE_COLS_SEPARATOR + shipTypeDetailed; 

            //gross tonnage
            WebElement divGT = driver.findElement(By.id("grossTonnage"));
            String shipGT = divGT.getText().trim();
            shipGT = shipGT.replace("Gross Tonnage: ", "");
            line += FILE_COLS_SEPARATOR + shipGT;

            //write file output              
            bw.write(urlSearch + line);
            bw.newLine(); 

            //flush file output
            bw.flush();
         }//while 
                  
         //close web client
         driver.close();

         //close file input
         br.close();
         ir.close();
         fis.close();                  
         
         //close file output
         bw.close();
         ow.close();
         fos.close(); 
      }
      catch (IOException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
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
      timeouts.implicitlyWait(Duration.ofMinutes(1));

      //change window dimension
      WebDriver.Window window = options.window();
      window.maximize();

      return driver;
   }
}
