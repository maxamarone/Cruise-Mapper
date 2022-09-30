package eu.eurostat.trackingships.vesselfinder;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
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

public class SearchShipMainInfo
{
   private static final Logger LOGGER = Logger.getLogger(SearchShipMainInfo.class.getName());
   
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
         String subDir = props.getProperty("subdir_marinetraffic");
         String subDirMainInfo = props.getProperty("subdir_vesselfinder_maininfo");
         String fileNameIn = props.getProperty("file_index_vesselfinder");
         String fileNameOut = props.getProperty("file_index_vesselfinder_maininfo");
         String appendFileOut = props.getProperty("restart_from_last_search_vesselfinder_maininfo");
         boolean appendLastSearch = BooleanUtils.toBoolean(appendFileOut);
         String urlLastSearch = props.getProperty("url_last_search_vesselfinder_maininfo");
         //set directory name
         String dirName = dirRoot + File.separator + subDir;
         
         //read file input
         File fileIn = new File(dirName + File.separator + fileNameIn);
         FileInputStream fis = new FileInputStream(fileIn);
         InputStreamReader ir = new InputStreamReader(fis, StandardCharsets.UTF_8.name());
         BufferedReader br = new BufferedReader(ir);
         
         //create directory main info
         String dirNameMainInfo = dirName + File.separator + subDirMainInfo;
         File directory = new File(dirNameMainInfo);
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
               String lineMainInfo = "";
               
               //get url detail
               int start = line.lastIndexOf("https://");
               int end = line.length();
               String urlSearch = line.substring(start, end);
               //urlSearch = "https://www.vesselfinder.com/vessels/MSC-ISTANBUL-IMO-9606326-MMSI-636017537";
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
                  //skip page not found
                  if (headers == null)
                  {
                     LOGGER.log(Level.WARNING, "Page not found!");
                  } 
                  else
                  {        
                     for (HtmlHeading2 header : headers)
                     {
                        String textHeader = header.asNormalizedText();
                        if (textHeader.equals("Vessel Particulars"))
                        {
                           //get parent node
                           HtmlSection section = (HtmlSection) header.getParentNode();

                           //get main info
                           lineMainInfo = getMainInfo(section); 

                           //manage exit from cycle
                           break;
                        }//if (textHeader.equals("Vessel Particulars"))
                     }//for (HtmlHeading2 header : headers)
                  }//if (headers != null)

                  //compute missing values
                  if (lineMainInfo.isEmpty())
                  {
                     for (int i=0; i<12; i++)
                     {
                        lineMainInfo += FILE_COLS_SEPARATOR;
                     }
                  }
                  //write file output              
                  bw.write(urlSearch + lineMainInfo);
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

   private static String getMainInfo(HtmlSection section) throws IOException
   {
      String line = "";

      //get table
      HtmlTable table = (HtmlTable) section.getFirstByXPath(".//table[@class='tparams']");
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
               case "IMO number":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Vessel Name":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Ship type":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Flag":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Gross Tonnage":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Summer Deadweight (t)":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Length Overall (m)":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Beam (m)":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Year of Built":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Crude":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Grain":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
               case "Bale":
                  line += FILE_COLS_SEPARATOR + value;
                  break;
            }//switch (header)   
         }//for (HtmlTableRow tableRow : tableRows)
         //
         LOGGER.log(Level.INFO, "line {0}", line);
      }//if (table != null)
      
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
