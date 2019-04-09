package analyzer.histories;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EventDate {
    public long time;
    private static SimpleDateFormat format ;
    static {
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }

    // 2018-09-10T15:45:50+0000
    public EventDate(String s) {
        try {
            s = s.substring(0, s.indexOf("+")).replace("T", " ");
            Date d = format.parse(s);

            time = d.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EventDate(long date) {
        this.time = date;
    }

    public static int compare(EventDate date1, EventDate date2) {
        return date1.time < date2.time ? -1 : (date1.time == date2.time ? 0 : 1);
    }

    public String toString() {
        String result = "";
        try {
            //Date date = format.parse(format.format(time - 8 * 60 * 60 * 1000));
            //result = date.toString();
            Date date = new Date(time);
            return format.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            String s = "2018-12-16T13:14:30+0000";
            s = s.substring(0, s.indexOf("+")).replace("T", " ");
            Date d = format.parse(s);
            long time = d.getTime();

            long time2 = new Date().getTime();
            System.out.println(time + " " + time2);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
