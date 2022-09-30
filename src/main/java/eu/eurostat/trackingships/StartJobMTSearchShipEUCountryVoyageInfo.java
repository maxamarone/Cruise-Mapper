package eu.eurostat.trackingships;


import eu.eurostat.trackingships.marinetraffic.job.SearchShipEUCountryVoyageInfoJob;
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

public class StartJobMTSearchShipEUCountryVoyageInfo
{
   private static final Logger LOGGER = Logger.getLogger(StartJobMTSearchShipEUCountryVoyageInfo.class.getName());

   public static void main(String[] args)
   {
      try
      {
         /*
         Marine Traffic (MT)
         */

         //get scheduler instance from the factory
         Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

         //start scheduler
         scheduler.start();

         //define the job and tie the class
         JobDetail job = newJob(SearchShipEUCountryVoyageInfoJob.class)
                 .withIdentity("jobSearchShipEUCountryVoyageInfo", "groupSearchShipEUCountryVoyageInfo")
                 .build();

         //trigger the job to run
         Trigger trigger = newTrigger()
                 .withIdentity("triggerSearchShipEUCountryVoyageInfo", "groupSearchShipEUCountryVoyageInfo")
                 .startNow()
                 //.withSchedule(cronSchedule("0/30 * * * * ?"))//every 30 seconds
                 //.withSchedule(cronSchedule("0 0 6 ? * *"))//every day at 6 
                 .withSchedule(cronSchedule("0 0 0/6 * * ?"))//four time a day at 0:00, 6:00, 12:00 and 18:00
                 .build();

         //set quartz to schedule the job using trigger
         scheduler.scheduleJob(job, trigger);

         //stop scheduler
         //scheduler.shutdown();
      }
      catch (SchedulerException ex)
      {
         LOGGER.log(Level.SEVERE, ex.getMessage());
      }
   }
}
