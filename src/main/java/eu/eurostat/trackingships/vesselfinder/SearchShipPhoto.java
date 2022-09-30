package eu.eurostat.trackingships.vesselfinder;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSection;
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

public class SearchShipPhoto
{
   private static final Logger LOGGER = Logger.getLogger(SearchShipPhoto.class.getName());
   
   private static final String FILE_IMAGE_EXTENSIOIN = ".jpg";

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
         String subDirPhoto = props.getProperty("subdir_vesselfinder_photo");
         String fileNameIn = props.getProperty("file_index_vesselfinder");
         String fileNameOut = props.getProperty("file_index_vesselfinder_photo");
         String appendFileOut = props.getProperty("restart_from_last_search_vesselfinder_photo");
         boolean appendLastSearch = BooleanUtils.toBoolean(appendFileOut);
         String urlLastSearch = props.getProperty("url_last_search_vesselfinder_photo");
         //set directory name
         String dirName = dirRoot + File.separator + subDir;
         
         //read file input
         File fileIn = new File(dirName + File.separator + fileNameIn);
         FileInputStream fis = new FileInputStream(fileIn);
         InputStreamReader ir = new InputStreamReader(fis, StandardCharsets.UTF_8.name());
         BufferedReader br = new BufferedReader(ir);
         
         //create directory image
         String dirNamePhoto = dirName + File.separator + subDirPhoto;
         File directory = new File(dirNamePhoto);
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
         
         //url root
         final String urlRoot = "https://www.vesselfinder.com";         
         
         //cycle file input lines
         while (br.ready()) 
         {
            String line = br.readLine();
            //LOGGER.log(Level.INFO, "line {0}", line);
            
            //manage restart from last url search
            if (urlLastSearch.isEmpty())
            {            
               //get url detail
               int start = line.lastIndexOf("https://");
               int end = line.length();
               String urlSearch = line.substring(start, end);
               //urlSearch = "https://www.vesselfinder.com/vessels/NEREIDA-IMO-7519294-MMSI-0";
               LOGGER.log(Level.INFO, "URL Search {0}", urlSearch);   

               //get search page
               HtmlPage searchPage = null;
               while (true)
               {
                  try
                  {
                     searchPage = client.getPage(urlSearch);
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
               if (searchPage != null)
               {
                  //get section 
                  HtmlSection shipSection = searchPage.getFirstByXPath(".//section[@class='column ship-section']");
                  //skip page not found
                  if (shipSection == null)
                  {
                     LOGGER.log(Level.WARNING, "Page not found!");
                  }
                  else                  
                  {
                     //get image
                     HtmlImage image = shipSection.getFirstByXPath(".//img[@class='main-photo']"); 
                     //check photo available
                     if (image == null)
                     {
                        LOGGER.log(Level.INFO, "No photo!");
                     }
                     else
                     {
                        //get anchor
                        DomNodeList<HtmlElement> anchors = shipSection.getElementsByTagName("a");
                        HtmlAnchor anchor = (HtmlAnchor) anchors.get(0);
                        //get anchor link
                        String link = anchor.getHrefAttribute();
                        //set urlDetail
                        String urlPhoto = urlRoot + link;
                        LOGGER.log(Level.INFO, "URL photo {0}", urlPhoto);  
                        //get photo page
                        getPhotoPage(dirNamePhoto, urlPhoto);              
                     }               
                  }//if (shipSection != null)               

                  //write file output
                  bw.write(line);
                  bw.newLine();
                  //flush file output
                  bw.flush();                                
               }//if (searchPage != null)               
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

   private static void getPhotoPage(String dirNamePhoto, String urlPhoto) throws IOException, InterruptedException
   {
      //init web client
      WebClient client = intiWebClient();
      
      //get search list page
      HtmlPage photoPage = null;
      while (true)
      {
         try
         {
            photoPage = client.getPage(urlPhoto);
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
      if (photoPage != null)
      {
         //get headers
         List<HtmlHeading2> headers = photoPage.getByXPath(".//h2");
         if (headers.size() > 0)
         {         
            //get first header
            HtmlHeading2 header = headers.get(0);
            //get text
            String headerText = header.asNormalizedText();
            LOGGER.log(Level.INFO, "URL photo {0}", headerText); 
            //compose photo file name (IMO + "_" MMSI + counter images)
            String fileName = "";
            //find IMO
            String keyIMO = "IMO: ";
            int startIMO = headerText.indexOf(keyIMO);
            if (startIMO != -1)
            {
               startIMO += keyIMO.length();
               int endIMO = headerText.lastIndexOf(", ");
               if (endIMO == -1)
               {
                  endIMO = headerText.length();
               }  
               else
               {
                  if (startIMO > endIMO)
                  {
                     endIMO = headerText.length();
                  }
               }
               fileName += "IMO-" + headerText.substring(startIMO, endIMO);
            }
            //find MMSI
            String keyMMSI = "MMSI: ";
            int startMMSI = headerText.indexOf(keyMMSI);
            if (startMMSI != -1)
            {
               startMMSI += keyMMSI.length();
               int endMMSI = headerText.length();
               //separate IMO and MMSI
               if (!fileName.isEmpty())
               {
                  fileName += "_";
               }
               fileName += "MMSI-" + headerText.substring(startMMSI, endMMSI);
            }
            //LOGGER.log(Level.INFO, "fileName {0}", fileName); 

            //get dive images
            HtmlDivision division = photoPage.getHtmlElementById("thumbs-container");
            //get images
            List<HtmlImage> images = division.getByXPath(".//img"); 
            //cycle all images
            int idImage = 0;
            for (HtmlImage image : images)
            {
               idImage++;
               String src = image.getSrcAttribute();
               LOGGER.log(Level.INFO, "src {0}", src); 
               //set file name
               String fileNamePhoto = fileName + "_" + idImage + FILE_IMAGE_EXTENSIOIN;
               LOGGER.log(Level.INFO, "fileName {0}", fileName); 
               //save photo to file
               File imageFile = new File(dirNamePhoto, fileNamePhoto);
               image.saveAs(imageFile);            
            }
         }//if (headers2.size() == 1)
         
      }//if (photoPage != null)
            
      //close web client
      client.close();
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
