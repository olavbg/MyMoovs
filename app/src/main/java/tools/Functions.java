package tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Functions {

    public static String get_days_since(Date date) {
        //if date2 is more in the future than date1 then the result will be negative
        //if date1 is more in the future than date2 then the result will be positive.

        Date now = new Date();
        int days_between = (int) ((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24l));
        if (days_between == 0)
            return "Today";
        return String.valueOf(days_between) + " days ago";
    }

    public static String getTodaysDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

//    public static boolean isConnected(){
//        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo != null && networkInfo.isConnected())
//            return true;
//        else
//            return false;
//    }
}
