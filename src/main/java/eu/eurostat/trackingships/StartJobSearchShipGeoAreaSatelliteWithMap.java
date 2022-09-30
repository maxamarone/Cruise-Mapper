package eu.eurostat.trackingships;

import eu.eurostat.trackingships.marinetraffic.job.SearchShipGeoAreaSatelliteWithMapJob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;

public class StartJobSearchShipGeoAreaSatelliteWithMap
{

   private static final Logger LOGGER = Logger.getLogger(StartJobSearchShipGeoAreaSatelliteWithMap.class.getName());

   public static void main(String[] args)
   {
      try
      {
         /*
         job scheduling by list
         */

         /*  
            B	59	30/04/22	17:41:31	ASC	19:41:31
            C	110	04/05/22	05:58:35	DESC	07:58:35
            A	161	07/05/22	17:33:24	ASC	19:33:24
            D	8	09/05/22	06:06:45	DESC	08:06:45
            B	59	12/05/22	17:41:31	ASC	19:41:31
            C	110	16/05/22	05:58:35	DESC	07:58:35
            A	161	19/05/22	17:33:24	ASC	19:33:24
            D	8	21/05/22	06:06:45	DESC	08:06:45
            B	59	24/05/22	17:41:31	ASC	19:41:31
            C	110	28/05/22	05:58:35	DESC	07:58:35
            A	161	31/05/22	17:33:24	ASC	19:33:24
            D	8	02/06/22	06:06:45	DESC	08:06:45
            B	59	05/06/22	17:41:31	ASC	19:41:31
            C	110	09/06/22	05:58:35	DESC	07:58:35
            A	161	12/06/22	17:33:24	ASC	19:33:24
            D	8	14/06/22	06:06:45	DESC	08:06:45
            B	59	17/06/22	17:41:31	ASC	19:41:31
            C	110	21/06/22	05:58:35	DESC	07:58:35
            A	161	24/06/22	17:33:24	ASC	19:33:24
            D	8	26/06/22	06:06:45	DESC	08:06:45
            B	59	29/06/22	17:41:31	ASC	19:41:31
            C	110	03/07/22	05:58:35	DESC	07:58:35
            A	161	06/07/22	17:33:24	ASC	19:33:24
            D	8	08/07/22	06:06:45	DESC	08:06:45
            B	59	11/07/22	17:41:31	ASC	19:41:31
            C	110	15/07/22	05:58:35	DESC	07:58:35
            A	161	18/07/22	17:33:24	ASC	19:33:24
            D	8	20/07/22	06:06:45	DESC	08:06:45
            B	59	23/07/22	17:41:31	ASC	19:41:31
            C	110	27/07/22	05:58:35	DESC	07:58:35
            A	161	30/07/22	17:33:24	ASC	19:33:24
            D	8	01/08/22	06:06:45	DESC	08:06:45
            B	59	04/08/22	17:41:31	ASC	19:41:31
            C	110	08/08/22	05:58:35	DESC	07:58:35
            A	161	11/08/22	17:33:24	ASC	19:33:24
            D	8	13/08/22	06:06:45	DESC	08:06:45
            B	59	16/08/22	17:41:31	ASC	19:41:31
            C	110	20/08/22	05:58:35	DESC	07:58:35
            A	161	23/08/22	17:33:24	ASC	19:33:24
            D	8	25/08/22	06:06:45	DESC	08:06:45
            B	59	28/08/22	17:41:31	ASC	19:41:31
            C	110	01/09/22	05:58:35	DESC	07:58:35
            A	161	04/09/22	17:33:24	ASC	19:33:24
            D	8	06/09/22	06:06:45	DESC	08:06:45
            B	59	09/09/22	17:41:31	ASC	19:41:31
            C	110	13/09/22	05:58:35	DESC	07:58:35
            A	161	16/09/22	17:33:24	ASC	19:33:24
            D	8	18/09/22	06:06:45	DESC	08:06:45
            B	59	21/09/22	17:41:31	ASC	19:41:31
            C	110	25/09/22	05:58:35	DESC	07:58:35
            A	161	28/09/22	17:33:24	ASC	19:33:24
            D	8	30/09/22	06:06:45	DESC	08:06:45
         */
         
         ArrayList<String> dates = new ArrayList<>();

         //dates.add("04-05-2022 05:58:35");
         //dates.add("07-05-2022 17:33:24");
         //dates.add("09-05-2022 06:06:45");
         //dates.add("12-05-2022 17:41:31");

         //dates.add("16-05-2022 05:58:35");
         //dates.add("19-05-2022 17:33:24");
         //dates.add("21-05-2022 06:06:45");
         //dates.add("24-05-2022 17:41:31");

         //dates.add("28-05-2022 05:58:35");
         //dates.add("31-05-2022 17:33:24");
         //dates.add("02-06-2022 06:06:45");
         //dates.add("05-06-2022 17:41:31");

         //dates.add("09-06-2022 05:58:35");
         //dates.add("12-06-2022 17:33:24");
         //dates.add("14-06-2022 06:06:45");
         //dates.add("17-06-2022 17:41:31");

         //dates.add("21-06-2022 05:58:35");
         //dates.add("24-06-2022 17:33:24");
         //dates.add("26-06-2022 06:06:45");
         //dates.add("29-06-2022 17:41:31");

         //dates.add("03-07-2022 05:58:35");
         //dates.add("06-07-2022 17:33:24");
         //dates.add("08-07-2022 06:06:45");
         //dates.add("11-07-2022 17:41:31");

         //dates.add("15-07-2022 05:58:35");
         //dates.add("18-07-2022 17:33:24");
         //dates.add("20-07-2022 06:06:45");
         //dates.add("23-07-2022 17:41:31");

         //dates.add("27-07-2022 05:58:35");
         //dates.add("30-07-2022 17:33:24");
         //dates.add("01-08-2022 06:06:45");
         //dates.add("04-08-2022 17:41:31");

         dates.add("20-07-2022 08:06:00");

         String pattern = "dd-MM-yyyy HH:mm:ss";
         SimpleDateFormat sdf = new SimpleDateFormat(pattern);

         int jobID = 0;
         for (String stringDate : dates)
         {
            jobID++;

            String stringStartDate = stringDate;
            Date startDate = sdf.parse(stringStartDate);
            long timeInSecs = startDate.getTime();
            startDate = new Date(timeInSecs);
            //long timeShiftInSecs = 120*60*1000 + 6*60*1000;//2 hours and 6 minutes
            //long timeStartInSecs = timeInSecs + timeShiftInSecs;//start after shift time from satellite 
            //startDate = new Date(timeStartInSecs);

            //get scheduler instance from the factory
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            //start scheduler
            scheduler.start();

            //define the job and tie the class
            JobDetail job = newJob(SearchShipGeoAreaSatelliteWithMapJob.class)
               .withIdentity("jobStartJobListOfDate"+jobID, "groupStartJobListOfDate"+jobID)
               .build();

            //trigger the job to run
            Trigger trigger = newTrigger()
               .withIdentity("triggerStartJobListOfDate"+jobID, "groupStartJobListOfDate"+jobID)
               .startAt(startDate)
               .withSchedule(simpleSchedule()
                  .withIntervalInSeconds(180)
                  .withRepeatCount(1))
               .withPriority(1)
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
         Logger.getLogger(StartJobSearchShipGeoAreaSatelliteWithMap.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

}
