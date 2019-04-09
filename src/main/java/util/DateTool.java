package util;

import org.eclipse.jgit.revwalk.RevCommit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTool {
    public static String toString(int date) {
        // lucene时间戳算出来之后快了6个小时
        try {
            long nowTimeLong = new Long(date - 6 * 60 * 60).longValue() * 1000;
            DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowTimeStr = ymdhmsFormat.format(nowTimeLong);
            Date nowTimeDate = ymdhmsFormat.parse(nowTimeStr);
            return nowTimeDate.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static long getGMTTime(RevCommit commit) {
        long time = commit.getAuthorIdent().getWhen().getTime();
        return time ;
    }

    public static Date toLocalTime(long time) {
        time -= 8 * 60 * 60 * 1000;
        return new Date(time);
    }

    public static void main(String[] args) {
        int a = 2;
    }
}
