package eu.eurostat.trackingships.vesselfinder;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.LogFactory;

public class CreateURLIndexSearch
{
   private static final Logger LOGGER = Logger.getLogger(CreateURLIndexSearch.class.getName());
   
   private static final String FILE_COLS_SEPARATOR = ";";

   @SuppressWarnings("SleepWhileInLoop")
   public static void main(String[] args)
   {
      try
      {
         //read properties files for directory path
         InputStream propsFile = CreateURLIndexSearch.class.getClassLoader().getResourceAsStream("config.properties");
         Properties props = new Properties();
         props.load(propsFile);
         String dirRoot = props.getProperty("root_directory");
         String subDir = props.getProperty("subdir_vesselfinder");
         String fileName = props.getProperty("file_index_vesselfinder");
         String appendFileOut = props.getProperty("append_to_last_search_vesselfinder");
         boolean appendLastSearch = BooleanUtils.toBoolean(appendFileOut);
         String urlLastSearch = props.getProperty("url_last_search_vesselfinder");
         //set directory name
         String dirName = dirRoot + File.separator + subDir;
         
         //write file output
         File fileOut = new File(dirName + File.separator + fileName);
         FileOutputStream fs = new FileOutputStream(fileOut, appendLastSearch);
         OutputStreamWriter ow = new OutputStreamWriter(fs, StandardCharsets.UTF_8.name());
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

         //set url search
         String urlSearch = urlRoot + "/vessels";
         LOGGER.log(Level.INFO, "URL Search {0}", urlSearch);
         
         //manage restart from last url search index         
         String lastParamMinGT = null;
         String lastParamMaxGT = null;
         String lastParamType = null;
         String lastParamFlag = null;
         int index;
         int indexStart;
         int indexEnd;
         if (appendLastSearch && !urlLastSearch.isEmpty())
         {
            LOGGER.log(Level.WARNING, "Restart from last url search index {0}", urlLastSearch);
            //https://www.vesselfinder.com/vessels?page=4&minGT=201&type=901&flag=cy
            //https://www.vesselfinder.com/vessels?page=139&maxGT=200&type=1&flag=al
            index = urlLastSearch.indexOf("minGT=201");
            if (index != -1)
            {
               lastParamMinGT = "minGT=201";
            }
            index = urlLastSearch.indexOf("maxGT=200");
            if (index != -1)
            {
               lastParamMaxGT = "maxGT=200";
            }            
            index = urlLastSearch.indexOf("type=");
            if (index != -1)
            {
               indexStart = index + "type=".length();
               indexEnd = urlLastSearch.substring(index).indexOf("&") + index;
               lastParamType = urlLastSearch.substring(indexStart, indexEnd);
            }         
            index = urlLastSearch.indexOf("flag=");
            if (index != -1)
            {
               indexStart = index + "flag=".length();
               indexEnd = indexStart + 2;
               lastParamFlag = urlLastSearch.substring(indexStart, indexEnd);
            } 
         }

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
         }//while (true)

         //check page retrieved
         if (searchPage != null)
         {
            //get form
            HtmlForm searchForm = searchPage.getElementByName("form");
            //get flags
            HtmlSelect flagSelect = searchForm.getSelectByName("flag");
            List<HtmlOption> flagOptions = flagSelect.getOptions();
            //get  types
            HtmlSelect typeSelect = searchForm.getSelectByName("type");
            List<HtmlOption> typeOptions = typeSelect.getOptions();
            //set gross tonnage (GT) parameters
            String gtParameters[] = {"maxGT=200", "minGT=201"};
            //cycle for flags
            for (HtmlOption flagOption : flagOptions)
            {
               //get flag value 
               String flagValue = flagOption.getValueAttribute();
               //get flag text 
               String flagText = flagOption.getText();
               //skip first option: any flag
               if (!flagValue.equals("-"))
               {
                  //manage restart from last value
                  if (lastParamFlag == null || lastParamFlag != null && lastParamFlag.equals(flagValue))
                  {
                     //reset last value
                     lastParamFlag = null;
                     //cycle for types
                     for (HtmlOption typeOption : typeOptions)
                     {
                        //get type value 
                        String typeValue = typeOption.getValueAttribute();
                        //get type text 
                        String typeText = typeOption.getText();
                        //skip first option: any type
                        if (!typeValue.equals("-1"))
                        {
                           //manage restart from last value
                           if (lastParamType == null || lastParamType != null && lastParamType.equals(typeValue))
                           {
                              //reset last value
                              lastParamType = null;                           
                              //cycle for GT parameters
                              for (int i=0; i<gtParameters.length; i++)
                              {
                                 //manage restart from last value
                                 if (lastParamMinGT == null && lastParamMaxGT == null || 
                                     lastParamMinGT != null && lastParamMinGT.equals(gtParameters[i]) || 
                                     lastParamMaxGT != null && lastParamMaxGT.equals(gtParameters[i]))
                                 {
                                    //reset last value
                                    lastParamMinGT = null;    
                                    lastParamMaxGT = null;

                                    //set url parameters 
                                    LOGGER.log(Level.INFO, "Filter by Flag[{0}], Type[{1}], GT[{2}]", new Object[] {flagText, typeText, gtParameters[i]});
                                    //compose url search with parameters
                                    String urlSearchList = urlSearch + "?type=" + typeValue + "&flag=" + flagValue + "&" + gtParameters[i];
                                    LOGGER.log(Level.INFO, "URL Search List {0}", urlSearchList); 

                                    //get result page
                                    getResultPage(bw, urlRoot, urlSearchList);                                 
                                 }//if (lastParamMinGT != null && lastParamMinGT.equals(gtParameters[i]) || lastParamMaxGT != null && lastParamMaxGT.equals(gtParameters[i]))
                              }//for (int i=0; i<gtParameters.length; i++)
                           }//if (lastParamType != null && lastParamType.equals(typeValue))
                        }//if (!typeValue.equals("-1"))
                     }//for (HtmlOption typeOption : typeOptions)
                  }//if (lastParamFlag != null && lastParamFlag.equals(flagValue))
               }//if (!flagValue.equals("-"))
            }//for (HtmlOption flagOption : flagOptions)
         }//if (searchPage != null)

         //close web client
         client.close();

         //close file output
         bw.close();
         ow.close();
         fs.close();
      }
      catch (IOException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
      catch (InterruptedException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
   }

   private static void getResultPage(BufferedWriter bw, String urlRoot, String urlSearchList) throws IOException, InterruptedException
   {
      //init web client
      WebClient client = intiWebClient();
      
      //get search list page
      HtmlPage searchListPage = null;
      while (true)
      {
         try
         {
            searchListPage = client.getPage(urlSearchList);
            break;
         }
         catch (SocketException ex)
         {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            //sleep 60 seconds
            LOGGER.log(Level.WARNING, "Sleep 60 sec");
            Thread.sleep(60*000);               
         }            
      }//while (true)      

      //check page retrieved
      if (searchListPage != null)
      {
         //get table results
         DomNodeList<DomElement> tables = searchListPage.getElementsByTagName("table");
         if (tables.size() == 1)
         {
            try
            {
               //get total items found
               HtmlDivision totalDiv = searchListPage.getFirstByXPath(".//div[@class='pagination-totals']");
               String total = totalDiv.getTextContent();
               LOGGER.log(Level.INFO, "Total items {0}", total);
            }
            catch (RuntimeException ex)
            {
               LOGGER.log(Level.WARNING, ex.getMessage());
               // wait for javascript
               Thread.sleep(10*1000);//10 sec 
               //retry to get result page
               getResultPage(bw, urlRoot, urlSearchList);            
            }
            //get table
            HtmlTable table = (HtmlTable) tables.get(0);
            //get table rows
            List<HtmlTableRow> tableRows = table.getRows();
            //cycle for table rows
            boolean tableBody = false;
            for (HtmlTableRow tableRow : tableRows)
            {
               //skip table head
               if (tableBody)
               {
                  //get first cell
                  HtmlTableCell tableCell = tableRow.getCell(0);
                  //get anchor
                  DomNodeList<HtmlElement> cellAnchors = tableCell.getElementsByTagName("a");
                  HtmlAnchor cellAnchor = (HtmlAnchor) cellAnchors.get(0);
                  //get anchor link
                  String link = cellAnchor.getHrefAttribute();
                  //set urlDetailPage
                  String urlDetail = urlRoot + link;
                  LOGGER.log(Level.INFO, "URL item {0}", urlDetail);
                  //write file output
                  bw.write(urlSearchList + FILE_COLS_SEPARATOR + urlDetail);
                  bw.newLine();
               }
               else
               {
                  tableBody = true;
               }
            }//for (HtmlTableRow tableRow : tableRows)

            //flush file output
            bw.flush();

            //manage page next
            try
            {
               HtmlAnchor pageNextAnchor = searchListPage.getFirstByXPath(".//a[@class='pagination-next']");
               //get anchor link
               String link = pageNextAnchor.getHrefAttribute();
               //check next page link
               if (!link.isEmpty())
               {
                  //compose url search next
                  String urlSearchListNext = urlRoot + link;
                  LOGGER.log(Level.INFO, "URL Search List Next {0}", urlSearchListNext); 

                  //get result page
                  getResultPage(bw, urlRoot, urlSearchListNext);         
               }               
            }
            catch (RuntimeException ex)
            {
               LOGGER.log(Level.WARNING, ex.getMessage());
               // wait for javascript
               Thread.sleep(10*1000);//10 sec 
               //retry to get result page
               getResultPage(bw, urlRoot, urlSearchList);            
            }         
         }//if (resultTables.size() == 1)
         else
         {
            LOGGER.log(Level.WARNING, "No data found for this search criteria!");
         }//if (resultTables.size() == 1)
      }//if (searchListPage != null)
      
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
