package eu.eurostat.trackingships;


import eu.eurostat.trackingships.webcam.job.CaptureImageFromWebcamAISDataJob;
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

public class StartJobCaptureImageFromWebcamAISData
{
   private static final Logger LOGGER = Logger.getLogger(StartJobCaptureImageFromWebcamAISData.class.getName());

   public static void main(String[] args)
   {
      try
      {
         /*
         Screenshot image from webcam
         */

         //get scheduler instance from the factory
         Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

         //start scheduler
         scheduler.start();

         //define the job and tie the class
         JobDetail job = newJob(CaptureImageFromWebcamAISDataJob.class)
                 .withIdentity("jobCaptureImageFromWebcamAISDataJob", "groupCaptureImageFromWebcamAISDataJob")
                 .build();

         //trigger the job to run
         Trigger trigger = newTrigger()
                 .withIdentity("triggerCaptureImageFromWebcamAISDataJob", "groupCaptureImageFromWebcamAISDataJob")
                 .startNow()
                 .withSchedule(cronSchedule("0 0 0/4 * * ?"))//six times a day at 0:00, 4:00, 8:00, 12:00, 16:00 and 20:00
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
