package eu.eurostat.trackingships;

import eu.eurostat.trackingships.marinetraffic.job.SearchShipGeoAreaSatelliteJob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;

public class StartJobSearchShipGeoAreaSatellite
{

   private static final Logger LOGGER = Logger.getLogger(StartJobSearchShipGeoAreaSatellite.class.getName());

   public static void main(String[] args)
   {
      try
      {
         /*
         job scheduling by list
         */

         /*  
         B 	59 	06/04/2022 	17:41:31 	ASC
         C 	110 	10/04/2022 	05:58:35 	DESC
         A 	161 	13/04/2022 	17:33:24 	ASC
         D 	8 	   15/04/2022 	06:06:45 	DESC
         B 	59 	18/04/2022 	17:41:31 	ASC
         C 	110 	22/04/2022 	05:58:35 	DESC
         A 	161 	25/04/2022 	17:33:24 	ASC
         D 	8 	   27/04/2022 	06:06:45 	DESC
         */
         ArrayList<String> dates = new ArrayList<>();
         dates.add("10-04-2022 05:58:35");
         dates.add("13-04-2022 17:33:24");
         dates.add("15-04-2022 06:06:45");
         dates.add("18-04-2022 17:41:31");
         dates.add("22-04-2022 05:58:35");
         dates.add("25-04-2022 17:33:24");
         dates.add("27-04-2022 06:06:45");

         String pattern = "dd-MM-yyyy HH:mm:ss";
         SimpleDateFormat sdf = new SimpleDateFormat(pattern);

         int jobID = 0;
         for (String stringDate : dates)
         {
            jobID++;

            String stringStartDate = stringDate;
            Date startDate = sdf.parse(stringStartDate);
            long timeInSecs = startDate.getTime();
            long timeShiftInSecs = 60 * 60 * 1000;//60 minutes
            long timeStartInSecs = timeInSecs - timeShiftInSecs;//start before a shift time from satellite 
            startDate = new Date(timeStartInSecs);
            long timeEndInSecs = timeInSecs + timeShiftInSecs;//stop after a shift time from satellite
            Date endDate = new Date(timeEndInSecs);

            //get scheduler instance from the factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            //start scheduler
            scheduler.start();

            //define the job and tie the class
            JobDetail job = newJob(SearchShipGeoAreaSatelliteJob.class)
               .withIdentity("jobStartJobListOfDate"+jobID, "groupStartJobListOfDate"+jobID)
               .build();

            //trigger the job to run
            Trigger trigger = newTrigger()
               .withIdentity("triggerStartJobListOfDate"+jobID, "groupStartJobListOfDate"+jobID)
               .startAt(startDate)
               .endAt(endDate)
               //.withSchedule(cronSchedule("0 0/5 * * * ?"))//every 5 minutes
               .withSchedule(cronSchedule("0 0 0/1 * * ?"))//every hour
               .build();

            //set quartz to schedule the job using trigger
            scheduler.scheduleJob(job, trigger);

            //stop scheduler
            //scheduler.shutdown();
         }
      }
      catch (SchedulerException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
      catch (ParseException ex)
      {
         Logger.getLogger(StartJobSearchShipGeoAreaSatellite.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

}
