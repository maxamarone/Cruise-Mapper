package eu.eurostat.trackingships.marinetraffic.job;

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
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.SessionNotCreatedException;
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

public class SearchShipGeoAreaSatelliteJob implements Job
{

   private static final Logger LOGGER = Logger.getLogger(SearchShipGeoAreaSatelliteJob.class.getName());

   private static final String FILE_COLS_SEPARATOR = ";";

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
         String subDir = props.getProperty("subdir_marinetraffic");
         String subDirGeoAreaSatellite = props.getProperty("subdir_marinetraffic_geoareasatellite");
         String fileName = props.getProperty("file_index_marinetraffic_geoareasatellite");

         //set directory name
         String dirName = dirRoot + File.separator + subDir;

         //create directory port & voyage info
         String dirNameGeoAreaSatellite = dirName + File.separator + subDirGeoAreaSatellite;
         File directory = new File(dirNameGeoAreaSatellite);
         if (!directory.exists())
         {
            directory.mkdir();
         }

         //get date time 
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");  
         LocalDateTime now = LocalDateTime.now();  
         String nowFormatted = dtf.format(now);   
         
         //write file output
         File fileOut = new File(dirNameGeoAreaSatellite + File.separator + nowFormatted + "_" + fileName);
         FileOutputStream fs = new FileOutputStream(fileOut);
         OutputStreamWriter ow = new OutputStreamWriter(fs, StandardCharsets.UTF_8.name());
         BufferedWriter bw = new BufferedWriter(ow);

         //init web driver
         WebDriver driver = intiWebClient();
         
         //activate javascript
         JavascriptExecutor js = (JavascriptExecutor) driver;

         /*
         https://www.marinetraffic.com/en/data/
         ?asset_type=vessels
         &columns=flag,shipname,photo,recognized_next_port,reported_eta,reported_destination,current_port,imo,mmsi,ship_type,show_on_live_map,time_of_latest_position,area,area_local,lat_of_latest_position,lon_of_latest_position,status,eni,distance,speed,course,draught,navigational_status,year_of_build,length,width,dwt,current_port_unlocode,current_port_country,callsign
         &time_of_latest_position_between|gte|time_of_latest_position_between=10,525600
         &near_me|coordinates|near_me=51.8,2.7,20
         &ship_type_in|in|Passenger Vessels,Cargo Vessels,Tankers|ship_type_in=6,7,8
         */

         //url root
         final String urlRoot = "https://www.marinetraffic.com/en/data/";
         final String urlParam1 = "?asset_type=vessels";
         final String urlParam2 = "&columns=flag,shipname,photo,recognized_next_port,reported_eta,reported_destination,current_port,imo,mmsi,ship_type,show_on_live_map,time_of_latest_position,area,area_local,lat_of_latest_position,lon_of_latest_position,status,eni,speed,course,draught,navigational_status,year_of_build,length,width,dwt,current_port_unlocode,current_port_country,callsign,distance";
         final String urlParam3 = "&time_of_latest_position_between|gte|time_of_latest_position_between=10,525600";
         final String urlParam4 = "&near_me|coordinates|near_me=51.8,2.7,20";
         final String urlParam5 = "&ship_type_in|in|Passenger Vessels,Cargo Vessels,Tankers|ship_type_in=6,7,8";

         final String urlSearch = urlRoot + urlParam1 + urlParam2 + urlParam3 + urlParam4 + urlParam5;

         //get url 
         driver.get(urlSearch);

         //page
          int page = 1;

         //get reporting div
         WebElement repDiv = driver.findElement(By.id("reporting_ag_grid"));
         //get info
         LOGGER.log(Level.INFO, "URL search {0}", urlSearch);
         getAISVoyageInfo(bw, urlSearch, repDiv);

         //manage pagination
         while (true)
         {
            //get next page button
            WebElement nextPageButton = driver.findElement(By.xpath(".//button[@title='Next page']"));
            boolean isEnabled = nextPageButton.isEnabled();
            //
            if (isEnabled)
            {
               page++;
               LOGGER.log(Level.INFO, "Next page {0}", page);
               try
               {
                  //click next button
                  nextPageButton.click();
               }
               catch (ElementClickInterceptedException ex)
               {
                  //manage privacy popup window
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
                        //click next button
                        nextPageButton.click();
                        break;
                     }
                  }
               }

               //get reporting div
               repDiv = driver.findElement(By.id("reporting_ag_grid"));
               //get info
               String urlSearchNext = driver.getCurrentUrl();
               LOGGER.log(Level.INFO, "URL search {0}", urlSearchNext);  
               getAISVoyageInfo(bw, urlSearchNext, repDiv);
            }
            else
            {
               LOGGER.log(Level.INFO, "End next page {0}", page);
               break;
            }
         }//while (true)

         //close web driver
         driver.close();

         //close file output
         bw.close();
         ow.close();
         fs.close();
      }
      catch (SessionNotCreatedException ex)
      {
         try
         {
            //wait 1 minute
            Thread.sleep(5*60*1000);//wait 5 minutes
         }
         catch (InterruptedException exNested)
         {
            LOGGER.log(Level.SEVERE, exNested.getMessage());
         }     
         //restart search
         eu.eurostat.trackingships.marinetraffic.SearchShipGeoAreaSatellite searchShip = new eu.eurostat.trackingships.marinetraffic.SearchShipGeoAreaSatellite();
      }
      catch (IOException | InterruptedException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
      finally
      {
         KillHangDriverProcess killDriverProcess = new KillHangDriverProcess();
         killDriverProcess.start();
      }

   }

   private static void getAISVoyageInfo(BufferedWriter bw, String urlSearch, WebElement repDiv) throws IOException, InterruptedException
   {
      //
      String line = "";
      
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
                 
      //cylcle cols
      for (int i = 0; i < flagCols.size(); i++)
      {
         //skip header
         if (i > 0)
         {
            boolean enableWrite = false;

            line = "";

            //flag
            WebElement flagCol = flagCols.get(i);
            try 
            {
               WebElement flagDiv = flagCol.findElement(By.xpath(".//div[@class='flag-icon']"));
               WebElement flagSpan = flagDiv.findElement(By.xpath(".//span"));
               String country = flagDiv.getAttribute("title");
               String countryCode = flagSpan.getAttribute("title");
               line += FILE_COLS_SEPARATOR + country + " (" + countryCode.toUpperCase() + ")";
            }
            catch (NoSuchElementException ex)
            {
               line += FILE_COLS_SEPARATOR + "-";
            }

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
            try 
            {
               WebElement shipTypeGenericCol = shipTypeGenericCols.get(i);
               WebElement shipTypeGenericImg = shipTypeGenericCol.findElement(By.xpath(".//img"));
               String shipTypeGeneric = shipTypeGenericImg.getAttribute("title");
               line += FILE_COLS_SEPARATOR + shipTypeGeneric;

               if (shipTypeGeneric.equals("Cargo") || shipTypeGeneric.equals("Tanker") || shipTypeGeneric.equals("Passenger"))
               {
                  enableWrite = true;
               }
            }
            catch (NoSuchElementException ex)
            {
               line += FILE_COLS_SEPARATOR + "-";
               enableWrite = false;
            }
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
                   
            if (enableWrite)
            {
               //write file output              
               bw.write(urlSearch + line);
               bw.newLine();              
            }
         }//if (i > 0)
      }//for (int i=0; i<flagDivs.size(); i++)
      
      //flush file output
      bw.flush();  

      try
      {
         //wait 5 seconds
         Thread.sleep(5 * 1000);
      }
      catch (InterruptedException ex)
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
      timeouts.pageLoadTimeout(Duration.ofMinutes(2));
      timeouts.scriptTimeout(Duration.ofMinutes(2));
      timeouts.implicitlyWait(Duration.ofSeconds(60));

      //change window dimension
      WebDriver.Window window = options.window();
      window.maximize();

      return driver;
   }
}
