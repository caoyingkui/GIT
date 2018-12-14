package analyzer.histories;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDate {
    private long time;
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
            Date date = format.parse(format.format(time));
            result = date.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
