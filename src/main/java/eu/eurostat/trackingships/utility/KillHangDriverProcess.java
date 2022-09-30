package eu.eurostat.trackingships.utility;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KillHangDriverProcess
{

   private static final Logger LOGGER = Logger.getLogger(KillHangDriverProcess.class.getName());

   public void start()
   {
      try
      {
         String soName = System.getProperty("os.name").toLowerCase();
         boolean isWindows = soName.startsWith("windows");

         ProcessBuilder builder = new ProcessBuilder();

         //geckodriver
         if (isWindows)
         {
            builder.command("taskkill", "/F", "/IM", "geckodriver.exe");
         }
         else
         {
            builder.command("pkill", "-9", "geckodriver");
         }
         
         Process process = builder.start();
         process.waitFor();

         //firefox
         if (isWindows)
         {
            //builder.command("taskkill", "/F", "/IM", "firefox.exe");
         }
         else
         {
            builder.command("pkill", "-9", "firefox");
         }
         
         process = builder.start();
         process.waitFor();
         
         LOGGER.log(Level.INFO, "Hang process killed");
      }
      catch (IOException | InterruptedException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
   }

}
