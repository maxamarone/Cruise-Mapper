package eu.eurostat.trackingships;


import eu.eurostat.trackingships.marinetraffic.job.SearchShipEUPortVoyageInfoJob;
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

public class StartJobMTSearchShipEUPortVoyageInfo
{
   private static final Logger LOGGER = Logger.getLogger(StartJobMTSearchShipEUPortVoyageInfo.class.getName());

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
         JobDetail job = newJob(SearchShipEUPortVoyageInfoJob.class)
                 .withIdentity("jobSearchShipEUPortVoyageInfo", "groupSearchShipEUPortVoyageInfo")
                 .build();

         //trigger the job to run
         Trigger trigger = newTrigger()
                 .withIdentity("triggerSearchShipEUPortVoyageInfo", "groupSearchShipEUPortVoyageInfo")
                 .startNow()
                 .withSchedule(cronSchedule("0 0 0/1 * * ?"))//every hour
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
