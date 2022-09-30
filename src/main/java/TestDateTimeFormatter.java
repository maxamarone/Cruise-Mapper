
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestDateTimeFormatter
{

   public static void main(String[] args)
   {
      //get date time 
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
      LocalDateTime now = LocalDateTime.now();
      String nowFormatted = dtf.format(now);
      System.out.println(nowFormatted);
   }

}
