package eu.eurostat.trackingships.marinetraffic.job;

import eu.eurostat.trackingships.bean.ShipPortVoyageInfo;
import eu.eurostat.trackingships.dao.marinetraffic.ShipEUPortVoyageInfoImplement;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class LoadShipEUPortVoyageInfoJob implements Job
{
   private static final Logger LOGGER = Logger.getLogger(LoadShipEUPortVoyageInfoJob.class.getName());

   private static final String FILE_COLS_SEPARATOR = ";";
   

   @Override
   public void execute(JobExecutionContext context)
           throws JobExecutionException
  {
      try
      {
         //read properties files for directory path
         InputStream propsFile = LoadShipEUPortVoyageInfoJob.class.getClassLoader().getResourceAsStream("config.properties");
         Properties props = new Properties();
         props.load(propsFile);
         String dirRoot = props.getProperty("root_directory");
         String subDir = props.getProperty("subdir_marinetraffic");
         String subDirPortVoyageInfo = props.getProperty("subdir_marinetraffic_portvoyageinfo");
         String subDirPortVoyageInfoLoaded = props.getProperty("eu_subdir_marinetraffic_portvoyageinfo_loaded");

         //set directory name
         String dirName = dirRoot + File.separator + subDir;
         //set directory port & voyage info
         String dirNamePortVoyageInfo = dirName + File.separator + subDirPortVoyageInfo;
         File directorySearch = new File(dirNamePortVoyageInfo);
         //create directory port & voyage info
         String dirNamePortVoyageInfoLoaded = dirNamePortVoyageInfo + File.separator + subDirPortVoyageInfoLoaded;
         File directoryLoaded = new File(dirNamePortVoyageInfoLoaded);
         if (!directoryLoaded.exists())
         {
            directoryLoaded.mkdir();
         }

         //get yesterday date 
         DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd");  
         LocalDateTime now = LocalDateTime.now();  
         LocalDateTime yesterday = now.minusDays(1);
         String extractionDate = dtf.format(yesterday);  
         LOGGER.log(Level.INFO, "Date of extraction {0}", extractionDate);
         //
         final String fileNameStart = extractionDate;  
         final String fileNameEnd = ".txt";

         //read all files into the directory using a filter
         @SuppressWarnings("Convert2Lambda")
         FilenameFilter filter = new FilenameFilter() 
         {
            @Override
            public boolean accept(File file, String name) 
            {
              boolean accept;
              accept = name.startsWith(fileNameStart) & name.endsWith(fileNameEnd);              
              return accept;
            }
         };  
         File[] files = directorySearch.listFiles(filter);                 
         //order 
         Arrays.sort(files);            
         
         //cycle for all files
         for (File file : files) 
         {
            //extract file name
            String fileName = file.getName();
            LOGGER.log(Level.INFO, "File name {0}", fileName);

            //extract date and hour
            String extractionDateTime = fileName.substring(0, 13);
            LOGGER.log(Level.INFO, "Date and time {0}", extractionDateTime);

            //define array of beans
            ArrayList<ShipPortVoyageInfo> shipBeans = new ArrayList<>();

            //read data file
            Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name());
            //cycle for all lines
            int lines = 0;
            while (scanner.hasNextLine()) 
            {  
               //
               lines++;
               LOGGER.log(Level.INFO, "Line number {0}", lines);
               //get line
               String line = scanner.nextLine();
               String[] lineCols = line.split(FILE_COLS_SEPARATOR, -1); 

               if (lineCols.length == 35) 
               {
                  //set bean
                  ShipPortVoyageInfo shipBean = new ShipPortVoyageInfo();

                  shipBean.setSearchUrl(lineCols[0]);
                  shipBean.setFlag(lineCols[1]);
                  shipBean.setShipName(lineCols[2]);
                  shipBean.setShipNameUrl(lineCols[3]);
                  shipBean.setUrlPhoto(lineCols[4]);
                  shipBean.setDestinationPortName(lineCols[5]);
                  shipBean.setDestinationPortUrl(lineCols[6]);
                  shipBean.setReportedEta(lineCols[7]);
                  shipBean.setReportedDestination(lineCols[8]);
                  shipBean.setCurrentPortName(lineCols[9]);
                  shipBean.setCurrentPortUrl(lineCols[10]);
                  shipBean.setImo(lineCols[11]);
                  shipBean.setMmsi(lineCols[12]);
                  shipBean.setShipType(lineCols[13]);
                  shipBean.setLiveMapUrl(lineCols[14]);
                  shipBean.setCurrentLat(lineCols[15]);
                  shipBean.setCurrentLon(lineCols[16]);
                  shipBean.setTimeOfLatestPosition(lineCols[17]);
                  shipBean.setGlobalArea(lineCols[18]);
                  shipBean.setLocalArea(lineCols[19]);
                  shipBean.setLatOfLatestPosition(lineCols[20]);
                  shipBean.setLonOfLatestPosition(lineCols[21]);
                  shipBean.setStatus(lineCols[22]);
                  shipBean.setEni(lineCols[23]);
                  shipBean.setSpeed(lineCols[24]);
                  shipBean.setCourse(lineCols[25]);
                  shipBean.setDraught(lineCols[26]);
                  shipBean.setNavigationalStatus(lineCols[27]);
                  shipBean.setYearOfBuild(lineCols[28]);
                  shipBean.setLength(lineCols[29]);
                  shipBean.setWidth(lineCols[30]);
                  shipBean.setDwt(lineCols[31]);
                  shipBean.setCurrentPortUnlocode(lineCols[32]);
                  shipBean.setCurrentPortCountry(lineCols[33]);
                  shipBean.setCallsign(lineCols[34]);
                  shipBean.setTrackingDate(extractionDateTime);

                  //set array of beans
                  shipBeans.add(shipBean);
               }
            }//while (scanner.hasNextLine())

            //load into database
            ShipEUPortVoyageInfoImplement dao = new ShipEUPortVoyageInfoImplement();
            int rows = dao.setShip(shipBeans);
            LOGGER.log(Level.INFO, "Rows inserted {0}", rows);

            //close scanner
            scanner.close();

            //move file to another directory
            String fromFile = dirNamePortVoyageInfo + File.separator + fileName;
            String toFile = dirNamePortVoyageInfoLoaded + File.separator + fileName;
            Path source = Paths.get(fromFile);
            Path target = Paths.get(toFile);
            Files.move(source, target);
         }//for (File file : files)
      }
      catch (IOException | SQLException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
   }
   
}