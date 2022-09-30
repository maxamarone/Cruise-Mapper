package eu.eurostat.trackingships.webcam;

import eu.eurostat.trackingships.marinetraffic.job.SearchShipEUPortVoyageInfoJob;
import eu.eurostat.trackingships.utility.KillHangDriverProcess;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class CaptureImageFromWebcamAndAISData
{

   private static final Logger LOGGER = Logger.getLogger(CaptureImageFromWebcamAndAISData.class.getName());

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
         String dirDestination = dirRoot + File.separator + "webcam";

         //set filename AIS data
         String fileNameAIS = "AIS_Data_" + nowFormatted + ".txt";

         //write file AIS 
         File fileOut = new File(dirDestination + File.separator + fileNameAIS);
         FileOutputStream fs = new FileOutputStream(fileOut);
         OutputStreamWriter ow = new OutputStreamWriter(fs, StandardCharsets.UTF_8.name());
         BufferedWriter bw = new BufferedWriter(ow);

         //define url and filename array
         String[] urls = new String[24];
         String[] fileNames = new String[24];
         String[] mmsis = new String[24];

         //set url and filename array 
         urls[0] = "https://webcam-binnenvaart.nl/pages/cam-famke.php";
         fileNames[0] = "cam-famke_" + nowFormatted + ".jpeg";
         mmsis[0] = "244630496";
         urls[1] = "https://webcam-binnenvaart.nl/pages/cam-mi-vida.php";
         fileNames[1] = "cam-mi-vida_" + nowFormatted + ".jpeg";
         mmsis[1] = "244750267";
         urls[2] = "https://webcam-binnenvaart.nl/pages/cam-strick.php";
         fileNames[2] = "cam-strick_" + nowFormatted + ".jpeg";
         mmsis[2] = "244710427";
         urls[3] = "https://webcam-binnenvaart.nl/pages/cam-de-zuiderzee.php";
         fileNames[3] = "cam-de-zuiderzee_" + nowFormatted + ".jpeg";
         mmsis[3] = "244740232";
         urls[4] = "https://webcam-binnenvaart.nl/pages/cam-anita.php";
         fileNames[4] = "cam-anita_" + nowFormatted + ".jpeg";
         mmsis[4] = "244219047";
         urls[5] = "https://webcam-binnenvaart.nl/pages/cam-sakura.php";
         fileNames[5] = "cam-sakura_" + nowFormatted + ".jpeg";
         mmsis[5] = "244670607";
         urls[6] = "https://webcam-binnenvaart.nl/pages/cam-everingen.php";
         fileNames[6] = "cam-everingen_" + nowFormatted + ".jpeg";
         mmsis[6] = "244650973";
         urls[7] = "https://webcam-binnenvaart.nl/pages/cam-rhenus-carisma.php";
         fileNames[7] = "cam-rhenus-carisma_" + nowFormatted + ".jpeg";
         mmsis[7] = "244010927";
         urls[8] = "https://webcam-binnenvaart.nl/pages/cam-survivor.php";
         fileNames[8] = "cam-survivor_" + nowFormatted + ".jpeg";
         mmsis[8] = "244670966";
         urls[9] = "https://webcam-binnenvaart.nl/pages/cam-lauwerszee.php";
         fileNames[9] = "cam-lauwerszee_" + nowFormatted + ".jpeg";
         mmsis[9] = "244180341";
         urls[10] = "https://webcam-binnenvaart.nl/pages/cam-red-fox.php";
         fileNames[10] = "cam-red-fox_" + nowFormatted + ".jpeg";
         mmsis[10] = "244700660";
         urls[11] = "https://webcam-binnenvaart.nl/pages/cam-furka.php";
         fileNames[11] = "cam-furka_" + nowFormatted + ".jpeg";
         mmsis[11] = "244750626";
         urls[12] = "https://webcam-binnenvaart.nl/pages/cam-ariel.php";
         fileNames[12] = "cam-ariel_" + nowFormatted + ".jpeg";
         mmsis[12] = "244084316";
         urls[13] = "https://webcam-binnenvaart.nl/pages/cam-anita2.php";
         fileNames[13] = "cam-anita2_" + nowFormatted + ".jpeg";
         mmsis[13] = "244700404";
         urls[14] = "https://webcam-binnenvaart.nl/pages/cam-yaris.php";
         fileNames[14] = "cam-yaris_" + nowFormatted + ".jpeg";
         mmsis[14] = "205263890";
         urls[15] = "https://webcam-binnenvaart.nl/pages/cam-gilla.php";
         mmsis[15] = "264163411";
         fileNames[15] = "cam-gilla_" + nowFormatted + ".jpeg";
         urls[16] = "https://webcam-binnenvaart.nl/pages/cam-zequinsly.php";
         fileNames[16] = "cam-zequinsly_" + nowFormatted + ".jpeg";
         mmsis[16] = "244700451";
         urls[17] = "https://webcam-binnenvaart.nl/pages/cam-guardian.php";
         fileNames[17] = "cam-guardian_" + nowFormatted + ".jpeg";
         mmsis[17] = "244750806";
         urls[18] = "https://webcam-binnenvaart.nl/pages/cam-patriot.php";
         fileNames[18] = "cam-patriot_" + nowFormatted + ".jpeg";
         mmsis[18] = "244110559";
         urls[19] = "https://webcam-binnenvaart.nl/pages/cam-feniks.php";
         fileNames[19] = "cam-feniks_" + nowFormatted + ".jpeg";
         mmsis[19] = "244139567";
         urls[20] = "https://webcam-binnenvaart.nl/pages/cam-salamanca.php";
         fileNames[20] = "cam-salamanca_" + nowFormatted + ".jpeg";
         mmsis[20] = "244630541";
         urls[21] = "https://webcam-binnenvaart.nl/pages/cam-sayonara.php";
         fileNames[21] = "cam-sayonara_" + nowFormatted + ".jpeg";
         mmsis[21] = "244660831";
         urls[22] = "https://webcam-binnenvaart.nl/pages/cam-drift.php";
         fileNames[22] = "cam-drift_" + nowFormatted + ".jpeg";
         mmsis[22] = "244690108";
         urls[23] = "https://webcam-binnenvaart.nl/pages/cam-mastodont.php";
         fileNames[23] = "cam-mastodont" + nowFormatted + ".jpeg";
         mmsis[23] = "244780611";

         //init web driver
         WebDriver driver = intiWebClient();

         //init web driver
         WebDriver driverAIS = intiWebClient();

         //cycle for all urls
         for (int i = 0; i < urls.length; i++)
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
               Thread.sleep(5 * 1000);
            }
            catch (InterruptedException ex)
            {
               LOGGER.log(Level.SEVERE, ex.getMessage());
            }

            //remove google advice
            try
            {
               WebElement advice = driver.findElement(By.id("aswift_3_anchor"));
               JavascriptExecutor js = (JavascriptExecutor) driver;
               WebElement parent = (WebElement) js.executeScript("return arguments[0].parentNode;", advice);
               WebElement parent2 = (WebElement) js.executeScript("return arguments[0].parentNode;", parent);
               js.executeScript("arguments[0].remove();", parent);
               js.executeScript("arguments[0].remove();", parent2);
            }
            catch (NoSuchElementException ex)
            {
               LOGGER.log(Level.WARNING, ex.getMessage());
            }

            //get element screenshot
            byte[] screenshot = element.getScreenshotAs(OutputType.BYTES);

            //save file
            File file = new File(dirDestination, fileName);
            FileOutputStream fileStream = new FileOutputStream(file);
            fileStream.write(screenshot);

            //get AIS data
            getAISData(driverAIS, bw, mmsis[i]);
         }

         //close file output
         bw.close();
         ow.close();
         fs.close();

         //close web driver
         driver.close();

         //close web driver AIS
         driverAIS.close();
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

   private static void getAISData(WebDriver driver, BufferedWriter bw, String mmsi) throws IOException
   {
      //set url
      final String url = "https://www.marinetraffic.com/en/data/?asset_type=vessels&columns=flag,shipname,photo,recognized_next_port,reported_eta,reported_destination,current_port,imo,mmsi,ship_type,show_on_live_map,time_of_latest_position,area,area_local,lat_of_latest_position,lon_of_latest_position,status,eni,speed,course,draught,navigational_status,year_of_build,length,width,dwt,current_port_unlocode,current_port_country,callsign,notes&quicksearch|begins|quicksearch=" + mmsi;

      //get url 
      driver.get(url);

      //get reporting div
      WebElement repDiv = driver.findElement(By.id("reporting_ag_grid"));

      //get cols value
      //Flag as transmitted through AIS or registered
      List<WebElement> flagCols = repDiv.findElements(By.xpath(".//div[@col-id='flag']"));
      //Registered vessel name or AIS reported name
      List<WebElement> shipNameCols = repDiv.findElements(By.xpath(".//div[@col-id='shipname']"));
      //Community vessel photos
      List<WebElement> photoCols = repDiv.findElements(By.xpath(".//div[@col-id='photo']"));
      //Destination port of call as identified by MarineTraffic
      List<WebElement> destPortCols = repDiv.findElements(By.xpath(".//div[@col-id='recognized_next_port']"));
      //Expected time of arrival as reported through AIS
      List<WebElement> ETACols = repDiv.findElements(By.xpath(".//div[@col-id='reported_eta']"));
      //Destination as reported through AIS
      List<WebElement> repDestPortCols = repDiv.findElements(By.xpath(".//div[@col-id='reported_destination']"));
      //Current port the vessel is within port limits
      List<WebElement> currPortCols = repDiv.findElements(By.xpath(".//div[@col-id='current_port']"));
      //IMO vessel identification number
      List<WebElement> IMOCols = repDiv.findElements(By.xpath(".//div[@col-id='imo']"));
      //MMSI as transmitted through AIS
      List<WebElement> MMSICols = repDiv.findElements(By.xpath(".//div[@col-id='mmsi']"));
      //Generic category of vessel type
      List<WebElement> shipTypeGenericCols = repDiv.findElements(By.xpath(".//div[@col-id='ship_type']"));
      //Live Map vessel icon
      List<WebElement> liveMapCols = repDiv.findElements(By.xpath(".//div[@col-id='show_on_live_map']"));
      //Date and time of latest position
      List<WebElement> timeLastPosCols = repDiv.findElements(By.xpath(".//div[@col-id='time_of_latest_position']"));
      //Global Area of the vessel's latest position
      List<WebElement> globalAreaCols = repDiv.findElements(By.xpath(".//div[@col-id='area']"));
      //Local Area of the vessel's latest position
      List<WebElement> localAreaCols = repDiv.findElements(By.xpath(".//div[@col-id='area_local']"));
      //Latitude of latest position
      List<WebElement> latCols = repDiv.findElements(By.xpath(".//div[@col-id='lat_of_latest_position']"));
      //Longitude of latest position
      List<WebElement> lonCols = repDiv.findElements(By.xpath(".//div[@col-id='lon_of_latest_position']"));
      //Whether the vessel is active or dead/scrapped, under repair or laid up
      List<WebElement> statusCols = repDiv.findElements(By.xpath(".//div[@col-id='status']"));
      //Inland vessel identification number
      List<WebElement> eniCols = repDiv.findElements(By.xpath(".//div[@col-id='eni']"));
      //Speed of latest position
      List<WebElement> speedCols = repDiv.findElements(By.xpath(".//div[@col-id='speed']"));
      //Course of latest position
      List<WebElement> courseCols = repDiv.findElements(By.xpath(".//div[@col-id='course']"));
      //Vessel draught as reported through AIS
      List<WebElement> draughtCols = repDiv.findElements(By.xpath(".//div[@col-id='draught']"));
      //As reported through AIS or corrected based on vessel movement patterns
      List<WebElement> navStatusCols = repDiv.findElements(By.xpath(".//div[@col-id='navigational_status']"));
      //Year of build
      List<WebElement> yearBuildCols = repDiv.findElements(By.xpath(".//div[@col-id='year_of_build']"));
      //Overall vessel length
      List<WebElement> lengthCols = repDiv.findElements(By.xpath(".//div[@col-id='length']"));
      //Vessel's width
      List<WebElement> widthCols = repDiv.findElements(By.xpath(".//div[@col-id='width']"));
      //Deadweight capacity
      List<WebElement> deadweightCols = repDiv.findElements(By.xpath(".//div[@col-id='dwt']"));
      //UN location code of current port
      List<WebElement> UNLocPortCols = repDiv.findElements(By.xpath(".//div[@col-id='current_port_unlocode']"));
      //Country of current port
      List<WebElement> countryPortCols = repDiv.findElements(By.xpath(".//div[@col-id='current_port_country']"));
      //Callsign as transmitted through AIS
      List<WebElement> callsignCols = repDiv.findElements(By.xpath(".//div[@col-id='callsign']"));

      //set cols separator char
      final String FILE_COLS_SEPARATOR = ";";

      //cylcle cols
      for (int i = 1; i < Math.min(2, flagCols.size()); i++)
      {
         String line = "";

         //flag
         WebElement flagCol = flagCols.get(i);
         WebElement flagDiv = flagCol.findElement(By.xpath(".//div[@class='flag-icon']"));
         WebElement flagSpan = flagDiv.findElement(By.xpath(".//span"));
         String country = flagDiv.getAttribute("title");
         String countryCode = flagSpan.getAttribute("title");
         line += FILE_COLS_SEPARATOR + country + " (" + countryCode.toUpperCase() + ")";
         //shipname
         WebElement shipNameCol = shipNameCols.get(i);
         WebElement shipNameAnchor = shipNameCol.findElement(By.xpath(".//a"));
         String shipName = shipNameAnchor.getText();
         line += FILE_COLS_SEPARATOR + shipName;
         String shipNameURL = shipNameAnchor.getAttribute("href");
         line += FILE_COLS_SEPARATOR + shipNameURL;
         //photo
         WebElement photoCol = photoCols.get(i);
         WebElement photoAnchor = photoCol.findElement(By.xpath(".//a"));
         String photoURL = photoAnchor.getAttribute("href");
         line += FILE_COLS_SEPARATOR + photoURL;
         //recognized_next_port (destination port name and url)
         WebElement destPortCol = destPortCols.get(i);
         String destPortName = destPortCol.getText();
         if (destPortName.equals("-"))
         {
            line += FILE_COLS_SEPARATOR + destPortName;
            line += FILE_COLS_SEPARATOR + "";
         }
         else
         {
            WebElement destPortAnchor = destPortCol.findElement(By.xpath(".//a"));
            destPortName = destPortAnchor.getText();
            line += FILE_COLS_SEPARATOR + destPortName;
            String destPortURL = destPortAnchor.getAttribute("href");
            line += FILE_COLS_SEPARATOR + destPortURL;
         }
         //reported_eta
         WebElement ETACol = ETACols.get(i);
         String ETA = ETACol.getText();
         line += FILE_COLS_SEPARATOR + ETA.replace("Over 24 hrs - Unlock", "-");
         //reported_destination
         WebElement repDestPortCol = repDestPortCols.get(i);
         String repDestPortName = repDestPortCol.getText();
         line += FILE_COLS_SEPARATOR + repDestPortName;
         //current_port
         WebElement currPortCol = currPortCols.get(i);
         String currPortName = currPortCol.getText();
         if (currPortName.equals("-"))
         {
            line += FILE_COLS_SEPARATOR + currPortName;
            line += FILE_COLS_SEPARATOR + "";
         }
         else
         {
            WebElement currPortAnchor = currPortCol.findElement(By.xpath(".//a"));
            currPortName = currPortAnchor.getText();
            line += FILE_COLS_SEPARATOR + currPortName;
            String currPortURL = currPortAnchor.getAttribute("href");
            line += FILE_COLS_SEPARATOR + currPortURL;
         }
         //imo
         WebElement IMOCol = IMOCols.get(i);
         String IMO = IMOCol.getText();
         line += FILE_COLS_SEPARATOR + IMO;
         //mmsi
         WebElement MMSICol = MMSICols.get(i);
         String MMSI = MMSICol.getText();
         line += FILE_COLS_SEPARATOR + MMSI;
         //ship_type
         WebElement shipTypeGenericCol = shipTypeGenericCols.get(i);
         WebElement shipTypeGenericImg = shipTypeGenericCol.findElement(By.xpath(".//img"));
         String shipTypeGeneric = shipTypeGenericImg.getAttribute("title");
         line += FILE_COLS_SEPARATOR + shipTypeGeneric;
         //live map
         WebElement liveMapCol = liveMapCols.get(i);
         WebElement liveMapAnchor = liveMapCol.findElement(By.xpath(".//a"));
         String liveMapURL = liveMapAnchor.getAttribute("href");
         line += FILE_COLS_SEPARATOR + liveMapURL;
         //extract lat from liveMapURL
         String lanActual = "";
         int start = liveMapURL.indexOf("/centery:");
         if (start != -1)
         {
            start += "/centery:".length();
            int end = liveMapURL.length();
            lanActual = liveMapURL.substring(start, end);
         }
         line += FILE_COLS_SEPARATOR + lanActual;
         //extract lon from liveMapURL
         String lonActual = "";
         start = liveMapURL.indexOf("/centerx:");
         if (start != -1)
         {
            start += "/centerx:".length();
            int end = liveMapURL.indexOf("/centery:");
            if (end != -1)
            {
               lonActual = liveMapURL.substring(start, end);
            }
         }
         line += FILE_COLS_SEPARATOR + lonActual;
         //time_of_latest_position
         WebElement timeLastPosCol = timeLastPosCols.get(i);
         String timeLastPos = timeLastPosCol.getText();
         line += FILE_COLS_SEPARATOR + timeLastPos;
         //global area
         WebElement globalAreaCol = globalAreaCols.get(i);
         String globalArea = globalAreaCol.getText();
         line += FILE_COLS_SEPARATOR + globalArea;
         //area_local
         WebElement localAreaCol = localAreaCols.get(i);
         String localArea = localAreaCol.getText();
         line += FILE_COLS_SEPARATOR + localArea;
         //lat_of_latest_position
         WebElement latCol = latCols.get(i);
         String lat = latCol.getText();
         line += FILE_COLS_SEPARATOR + lat;
         //lon_of_latest_position
         WebElement lonCol = lonCols.get(i);
         String lon = lonCol.getText();
         line += FILE_COLS_SEPARATOR + lon;
         //status
         WebElement statusCol = statusCols.get(i);
         String status = statusCol.getText();
         line += FILE_COLS_SEPARATOR + status;
         //eni
         WebElement eniCol = eniCols.get(i);
         String eni = eniCol.getText();
         line += FILE_COLS_SEPARATOR + eni;
         //speed
         WebElement speedCol = speedCols.get(i);
         String speed = speedCol.getText();
         line += FILE_COLS_SEPARATOR + speed;
         //course
         WebElement courseCol = courseCols.get(i);
         String course = courseCol.getText();
         line += FILE_COLS_SEPARATOR + course;
         //draught
         WebElement draughtCol = draughtCols.get(i);
         String draught = draughtCol.getText();
         line += FILE_COLS_SEPARATOR + draught;
         //navigational_status
         WebElement navStatusCol = navStatusCols.get(i);
         String navStatus = navStatusCol.getText();
         line += FILE_COLS_SEPARATOR + navStatus;
         //year_of_build
         WebElement yearBuildCol = yearBuildCols.get(i);
         String yearBuild = yearBuildCol.getText();
         line += FILE_COLS_SEPARATOR + yearBuild;
         //length
         WebElement lengthCol = lengthCols.get(i);
         String length = lengthCol.getText();
         line += FILE_COLS_SEPARATOR + length;
         //width
         WebElement widthCol = widthCols.get(i);
         String width = widthCol.getText();
         line += FILE_COLS_SEPARATOR + width;
         //dwt
         WebElement deadweightCol = deadweightCols.get(i);
         String deadweight = deadweightCol.getText();
         line += FILE_COLS_SEPARATOR + deadweight;
         //current_port_unlocode
         WebElement UNLocPortCol = UNLocPortCols.get(i);
         String UNLocPort = UNLocPortCol.getText();
         line += FILE_COLS_SEPARATOR + UNLocPort;
         //current_port_country
         WebElement countryPortCol = countryPortCols.get(i);
         String countryPort = countryPortCol.getText();
         line += FILE_COLS_SEPARATOR + countryPort;
         //callsign
         WebElement callsignCol = callsignCols.get(i);
         String callsign = callsignCol.getText();
         line += FILE_COLS_SEPARATOR + callsign;

         //write file output              
         bw.write(url + line);
         bw.newLine();
      }//for ...

      //flush file output
      bw.flush();
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
