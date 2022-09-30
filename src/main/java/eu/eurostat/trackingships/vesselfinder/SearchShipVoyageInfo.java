package eu.eurostat.trackingships.vesselfinder;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSection;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.LogFactory;

public class SearchShipVoyageInfo
{
   private static final Logger LOGGER = Logger.getLogger(SearchShipVoyageInfo.class.getName());
   
   private static final String FILE_COLS_SEPARATOR = ";";

   @SuppressWarnings("SleepWhileInLoop")
   public static void main(String[] args) throws InterruptedException
   {
      try
      {
         //read properties files for directory path
         InputStream propsFile = SearchShipPhoto.class.getClassLoader().getResourceAsStream("config.properties");
         Properties props = new Properties();
         props.load(propsFile);
         String dirRoot = props.getProperty("root_directory");
         String subDir = props.getProperty("subdir_vesselfinder");
         String subDirVoyageInfo = props.getProperty("subdir_vesselfinder_voyageinfo");
         String fileNameIn = props.getProperty("file_index_vesselfinder");
         String fileNameOut = props.getProperty("file_index_vesselfinder_voyageinfo");
         String appendFileOut = props.getProperty("restart_from_last_search_vesselfinder_voayageinfo");
         boolean appendLastSearch = BooleanUtils.toBoolean(appendFileOut);
         String urlLastSearch = props.getProperty("url_last_search_vesselfinder_voyageinfo");
         //set directory name
         String dirName = dirRoot + File.separator + subDir;
         
         //read file input
         File fileIn = new File(dirName + File.separator + fileNameIn);
         FileInputStream fis = new FileInputStream(fileIn);
         InputStreamReader ir = new InputStreamReader(fis, StandardCharsets.UTF_8.name());
         BufferedReader br = new BufferedReader(ir);
         
         //create directory voyage info
         String dirNameVoyageInfo = dirName + File.separator + subDirVoyageInfo;
         File directory = new File(dirNameVoyageInfo);
         if (!directory.exists())
         {
            directory.mkdir();
         }    
         
         //write file output
         File fileOut = new File(dirName + File.separator + fileNameOut);
         FileOutputStream fos = new FileOutputStream(fileOut, appendLastSearch);
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

         //init web client
         WebClient client = intiWebClient();
         
         //cycle file input lines
         while (br.ready()) 
         {
            String line = br.readLine();
            //LOGGER.log(Level.INFO, "line {0}", line);
            
            //manage restart from last url search
            if (urlLastSearch.isEmpty())
            {   
               //
               String lineVoyageInfo = "";
               String lineLocationInfo = "";
               
               //get url detail
               int start = line.lastIndexOf("https://");
               int end = line.length();
               String urlSearch = line.substring(start, end);
               //urlSearch = "https://www.vesselfinder.com/vessels/MSC-ISTANBUL-IMO-9606326-MMSI-636017537";
               //urlSearch = "https://www.vesselfinder.com/vessels/A.GLO-MARIMAR-H-E-U-IMO-0-MMSI-501973031";
               LOGGER.log(Level.INFO, "URL Search {0}", urlSearch);   

               //get search page
               HtmlPage page = null;
               while (true)
               {
                  try
                  {
                     page = client.getPage(urlSearch);
                     break;
                  }
                  catch (SocketException ex)
                  {
                     LOGGER.log(Level.SEVERE, ex.getMessage());
                     //sleep 60 seconds
                     LOGGER.log(Level.WARNING, "Sleep 60 sec");
                     Thread.sleep(60*000);               
                  }  
                  catch (FailingHttpStatusCodeException ex)
                  {
                     LOGGER.log(Level.SEVERE, ex.getMessage());
                     //check page not found
                     if (ex.getMessage().contains("404"))
                     {
                        break;
                     }
                     else
                     {
                        //sleep 60 seconds
                        LOGGER.log(Level.WARNING, "Sleep 60 sec");
                        Thread.sleep(60*000);               
                     }                  
                  }                  
               }//while (true)                
               
               //check page retrieved
               if (page != null)
               {   
                  //get headers
                  List<HtmlHeading2> headers = page.getByXPath(".//h2[@class='bar']");
                  if (headers != null)
                  {    
                     boolean voyageInfoFound = false;
                     boolean locationInfoFound = false;
                     for (HtmlHeading2 header : headers)
                     {
                        String textHeader = header.asNormalizedText();
                        if (textHeader.equals("Position & Voyage Data"))
                        {
                           //get parent node
                           HtmlSection section = (HtmlSection) header.getParentNode();

                           //get voyage info
                           lineVoyageInfo = getVoyageInfo(section); 

                           //set found
                           voyageInfoFound = true;
                        }//if (textHeader.equals("Position & Voyage Data"))
                        else if (textHeader.equals("Map position & Weather"))
                        { 
                           //get parent node
                           HtmlDivision division = (HtmlDivision) header.getParentNode();

                           //get location info
                           lineLocationInfo = getLocationInfo(division); 
                           //set found
                           locationInfoFound = true;
                        }//else if (textHeader.equals("Map position & Weather"))

                        //manage exit from cycle
                        if (voyageInfoFound && locationInfoFound)
                        {
                           break;
                        }
                     }//for (HtmlHeading2 header : headers)
                  }//if (headers != null)

                  //compute missing values for voyage data
                  if (lineVoyageInfo.isEmpty())
                  {
                     for (int i=0; i<18; i++)
                     {
                        lineVoyageInfo += FILE_COLS_SEPARATOR;
                     }
                  }
                  //compute missing values for location data
                  if (lineLocationInfo.isEmpty())
                  {
                     for (int i=0; i<6; i++)
                     {
                        lineLocationInfo += FILE_COLS_SEPARATOR;
                     }
                  }                  
                  //write file output              
                  bw.write(urlSearch + lineVoyageInfo + lineLocationInfo);
                  bw.newLine();
                  //flush file output
                  bw.flush();                                
               }//if (page != null)                    
            }//if (urlLastSearch.isEmpty())
            
            //manage restart from last url search
            if (!urlLastSearch.isEmpty())
            {
               if (line.contains(urlLastSearch))
               {
                  urlLastSearch = "";
               }
            }            
         }//while 
                  
         //close web client
         client.close();

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

   private static String getVoyageInfo(HtmlSection section) throws IOException
   {
      String line = "";
      
      //get time and port of ETA, ATA info
      String ETATime = "-";
      String ATATime = "-";
      String ETAPort = "-";
      String ATAPort = "-";
      //
      HtmlDivision divTA = section.getFirstByXPath(".//div[@class='vi__r1 vi__sbt']");
      if (divTA != null)
      {
         HtmlDivision divTAValue = divTA.getFirstByXPath(".//div[@class='_value']");
         if (divTAValue != null)
         {
            String value = divTAValue.asNormalizedText();
            if (value.contains("ETA:"))
            {
               //get time
               ETATime = value.replace("ETA: ", "");
               //remove div value
               divTAValue.remove();
               //get port
               ETAPort = divTA.asNormalizedText();
            }
            else if (value.contains("ATA:"))
            {
               //get time
               ETATime = value.replace("ATA: ", "");
               //remove div value
               divTAValue.remove();
               //get port
               ETAPort = divTA.asNormalizedText();
            }
            else
            {
               LOGGER.log(Level.INFO, "TA Time not expected {0}", value);
            }            
         }//if (divTAValue != null)
      }//if (divTA != null)
      LOGGER.log(Level.INFO, "ETATime {0}", ETATime);
      line += FILE_COLS_SEPARATOR + ETATime;
      LOGGER.log(Level.INFO, "ATATime {0}", ATATime);
      line += FILE_COLS_SEPARATOR + ETATime;
      LOGGER.log(Level.INFO, "ETAPort {0}", ETAPort);
      line += FILE_COLS_SEPARATOR + ETAPort;
      LOGGER.log(Level.INFO, "ATAPort {0}", ATAPort);
      line += FILE_COLS_SEPARATOR + ATAPort;

      //get time and port of ETD, ATD info
      String ETDTime = "-";
      String ATDTime = "-";      
      String ETDPort = "-";
      String ATDPort = "-";
      //
      HtmlDivision divTD = section.getFirstByXPath(".//div[@class='vi__r1 vi__stp']");
      if (divTD != null)
      {
         HtmlDivision divTDValue = divTD.getFirstByXPath(".//div[@class='_value']");
         if (divTDValue != null)
         {
            String value = divTDValue.asNormalizedText();
            if (value.contains("ETD:"))
            {
               //get time
               ETDTime = value.replace("ETD: ", "");
               //remove div value
               divTDValue.remove();
               //get port
               ETDPort = divTD.asNormalizedText();
            }
            else if (value.contains("ATD:"))
            {
               //get time
               ETDTime = value.replace("ATD: ", "");
               //remove div value
               divTDValue.remove();
               //get port
               ETDPort = divTD.asNormalizedText();
            }
            else
            {
               LOGGER.log(Level.INFO, "TD time not expected {0}", value);
            }            
         }//if (divTDValue != null)
      }//if (divTD != null)
      LOGGER.log(Level.INFO, "ETDTime {0}", ETDTime);
      line += FILE_COLS_SEPARATOR + ETDTime;
      LOGGER.log(Level.INFO, "ATDTime {0}", ATDTime);
      line += FILE_COLS_SEPARATOR + ETDTime;
      LOGGER.log(Level.INFO, "ETDPort {0}", ETDPort);
      line += FILE_COLS_SEPARATOR + ETDPort;
      LOGGER.log(Level.INFO, "ATDPort {0}", ATDPort);
      line += FILE_COLS_SEPARATOR + ATDPort;
      
      //get table
      HtmlTable table = (HtmlTable) section.getFirstByXPath(".//table[@class='aparams']");
      if (table != null)
      {
         //get table rows
         List<HtmlTableRow> rows = table.getRows();
         //cycle for table rows
         for (HtmlTableRow row : rows)
         {
            //get first header
            HtmlTableCell cellHeader = row.getCell(0);
            String header = cellHeader.asNormalizedText();
            //LOGGER.log(Level.INFO, "header {0}", header);
            //get second value
            HtmlTableCell cellValue = row.getCell(1);
            String value = cellValue.asNormalizedText();
            //LOGGER.log(Level.INFO, "value {0}", value);

            //compose line
            switch (header) 
            {
               case "Predicted ETA":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Distance / Time":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Course / Speed":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Current draught":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Navigation Status":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Position received":
                  value = cellValue.getAttribute("data-title");
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "IMO / MMSI":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Callsign":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Flag":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Length / Beam":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
            }//switch (header)   
         }//for (HtmlTableRow tableRow : tableRows)
         //
         LOGGER.log(Level.INFO, "line {0}", line);
      }//if (table != null)
      
      return line;      
   }

   private static String getLocationInfo(HtmlDivision division) throws IOException
   {
      String line = "";
      
      //get lat info
      List<HtmlDivision> divisionsLat = division.getByXPath(".//div[@class='coordinate lat']");
      HtmlDivision divisionLat = divisionsLat.get(0);
      String lat = divisionLat.asNormalizedText();
      LOGGER.log(Level.INFO, "lat {0}", lat);
      line += FILE_COLS_SEPARATOR + lat;
      //get lon info
      List<HtmlDivision> divisionsLon = division.getByXPath(".//div[@class='coordinate lon']");
      HtmlDivision divisionLon = divisionsLon.get(0);
      String lon = divisionLon.asNormalizedText();
      LOGGER.log(Level.INFO, "lon {0}", lon);
      line += FILE_COLS_SEPARATOR + lon;

      //get weather info
      HtmlDivision divisionWeather = division.getFirstByXPath(".//div[@id='weather-inner']");
      List<HtmlDivision> divisionsM = divisionWeather.getByXPath(".//div[@class='wtw-m']");
      List<HtmlDivision> divisionsA = divisionWeather.getByXPath(".//div[@class='wtw-a']");
      HtmlDivision divisionWind = divisionWeather.getFirstByXPath(".//div[@class='flx wtw-c wtw-x2']");
      String temperature = divisionsM.get(0).asNormalizedText();
      LOGGER.log(Level.INFO, "temperature {0}", temperature);
      line += FILE_COLS_SEPARATOR + temperature;
      String windSpeed = divisionsA.get(1).asNormalizedText();
      LOGGER.log(Level.INFO, "windSpeed {0}", windSpeed);
      line += FILE_COLS_SEPARATOR + windSpeed;
      String windDirection = divisionWind.getAttribute("title");
      windDirection = windDirection.replace("Wind direction: ", "");
      LOGGER.log(Level.INFO, "windDirection {0}", windDirection);
      line += FILE_COLS_SEPARATOR + windDirection;
      String seaWaveHeight = divisionsM.get(2).asNormalizedText();
      LOGGER.log(Level.INFO, "seaWaveHeight {0}", seaWaveHeight);
      line += FILE_COLS_SEPARATOR + seaWaveHeight;
      
      return line;      
   }
   
   private static WebClient intiWebClient() throws IOException
   {
      //init web client
      WebClient client = new WebClient(BrowserVersion.FIREFOX);

      //client settings
      client.setJavaScriptTimeout(120000);
      client.waitForBackgroundJavaScript(120000);
      client.waitForBackgroundJavaScriptStartingBefore(3000);
      NicelyResynchronizingAjaxController nrac = new NicelyResynchronizingAjaxController();
      client.setAjaxController(nrac);

      //get options
      WebClientOptions options = client.getOptions();
      
      //read properties files for proxy
      InputStream propsFile = CreateURLIndexSearch.class.getClassLoader().getResourceAsStream("proxy.properties");
      Properties props = new Properties();
      props.load(propsFile);
      boolean enabled = BooleanUtils.toBoolean(props.getProperty("proxy_enabled"));
      if (enabled)
      {
         String host = props.getProperty("proxy_host");
         String port = props.getProperty("proxy_port");      
         ProxyConfig proxy = new ProxyConfig();
         proxy.setProxyHost(host);
         proxy.setProxyPort(Integer.parseInt(port));       
         //set options proxy
         options.setProxyConfig(proxy);
      }
      
      //set options
      options.setUseInsecureSSL(true);
      options.setJavaScriptEnabled(true);
      options.setThrowExceptionOnScriptError(false);
      options.setCssEnabled(false);
      options.setTimeout(120000);//120 seconds (time in milliseconds)     
      //options.setTimeout(0);//zero for an infinite wait

      return client;
   }
}
