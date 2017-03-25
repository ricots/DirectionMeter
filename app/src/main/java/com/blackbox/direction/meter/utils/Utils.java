package com.blackbox.direction.meter.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.blackbox.direction.meter.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (NullPointerException e) {

        }
    }

    public static float getFormattedDouble(double value) {
        float precision = (float) Math.pow(10, 6);
        return (float) ((int) (precision * value)) / precision;
    }

    public static Typeface setTypeface(Context context, int type) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/ThirstyScriptExtraBoldDemo.otf");

        switch (type) {
            case 0:
                font = Typeface.createFromAsset(context.getAssets(), "fonts/ThirstyScriptExtraBoldDemo.otf");
                break;
            case 1:
                font = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Regular.ttf");
                break;
            case 2:
                font = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Bold.ttf");
                break;
        }

        return font;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    public static boolean isInternetAvailable(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    public static void setHideSoftKeyboard(EditText editText, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public static String arrageDate(String date) {
        String[] months = {"January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October", "November",
                "December"};
        String strDate = "";
        String[] arrDate = date.split("-");
        strDate = months[Integer.parseInt(arrDate[1])] + " " + arrDate[2]
                + ", " + arrDate[0];
        return strDate;
    }

    public static Spanned getColoredSpanned(String text, int color) {
        String input = "<font color='" + color + "'>" + text + "</font>";
        Spanned spannedStrinf = Html.fromHtml(input);
        return spannedStrinf;
    }


    public static void callSwitchLang(String langCode, Context context) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {

        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }


    public static boolean isPackageExisted(Context context, String targetPackage) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage,
                    PackageManager.GET_META_DATA);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }


    public static Boolean createNewDirectory(String directory_name) {
        Boolean if_created = false;


        Log.i(TAG, "Created folder in External storage..");
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + directory_name);
        if (!directory.exists()) {
            if_created = true;
            directory.mkdirs();
        }

        return if_created;
    }

    public static String getStringSizeLengthFile(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMo = sizeKb * sizeKb;
        float sizeGo = sizeMo * sizeKb;
        float sizeTerra = sizeGo * sizeKb;


        if (size < sizeMo)
            return df.format(size / sizeKb) + " KB";
        else if (size < sizeGo)
            return df.format(size / sizeMo) + " MB";
        else if (size < sizeTerra)
            return df.format(size / sizeGo) + " GB";

        return "";
    }

    public static boolean IsExternalStorageAvailable() {
        return
                Environment.MEDIA_MOUNTED
                        .equals(Environment.getExternalStorageState());
    }

    public static String getPackageInfo(Context context) {
        PackageInfo pi = null;
        String pacname = "";
        try {
            pi = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pacname = pi.versionName + "";
    }

    public static Boolean isValidApk(Context context, String path) {
        Boolean isValid = false;
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, 0);

        try {
            Log.i(TAG, "Verify isValidApk: " + info.versionName + " " + info.versionCode);
            if (info.versionName != null) {
                isValid = true;
            }
        } catch (NullPointerException e) {
            isValid = false;
        }

        return isValid;
    }

    public static Boolean isPathValid(String path) {
        Boolean isValid = false;

        if (path.equals("") || path.isEmpty() || path.matches("0") || path.matches("null")) {
            isValid = false;
        } else {
            isValid = true;
        }
        return isValid;
    }

    public static StringBuilder readFromAssets(Context context,String filename) {


        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(filename);
        } catch (IOException e) {
            Log.e("message: ", e.getMessage());
        }

        StringBuilder stg_build = new StringBuilder();
        BufferedReader r = new BufferedReader(
                new InputStreamReader(inputStream));
        String line;
        int i = 0;
        String dateStr = "04/05/2010";
        String versionName = "V 1.0";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0).versionName;
        } catch (PackageManager.NameNotFoundException e1) {

            e1.printStackTrace();
        }
        try {
            while ((line = r.readLine()) != null) {

                if (i == 1) {

                    stg_build.append("V" + "" + versionName);
                    stg_build.append("\n");
                } else if (i == 2) {
                    String date = new SimpleDateFormat("yyyy-MM-dd")
                            .format(new Date());
                    try {
                        ApplicationInfo ai = context.getPackageManager()
                                .getApplicationInfo(context.getPackageName(), 0);
                        ZipFile zf = new ZipFile(ai.sourceDir);
                        ZipEntry ze = zf.getEntry("META-INF/MANIFEST.MF");
                        long time = ze.getTime();
                        date = SimpleDateFormat.getInstance().format(
                                new Date(time));

                    } catch (Exception e) {
                    }
                    stg_build.append(date + "");
                    stg_build.append("\n");
                } else {
                    stg_build.append(line);
                    stg_build.append("\n");
                }
                i++;


            }


        } catch (IOException e) {

            e.printStackTrace();
        }
        return stg_build;
    }

    public static SpannableString extractLinks(String string)
    {
        int startIndex = 0;
        int endIndex = 0;
        SpannableString styledString = new SpannableString(string);
        Pattern p = Pattern.compile(
                "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                        + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                        + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

        String [] parts = string.split("\\s+");

        for( String item : parts ) {
            Matcher m = p.matcher(item);
            if(m.matches()) {
                startIndex = string.indexOf(item);
                endIndex = startIndex+m.end();
                styledString.setSpan(new URLSpan(item), startIndex, endIndex, 0);
                styledString.setSpan(new ForegroundColorSpan(Color.BLUE), startIndex, endIndex, 0);
            }
        }
        return styledString;
    }

    public static Boolean isValidText(String txt) {
        Boolean is_valid = false;

        try {
            if (!txt.equalsIgnoreCase("null") && !txt.equalsIgnoreCase(" ") && !TextUtils.isEmpty(txt) && !txt.equalsIgnoreCase("")) {
                is_valid = true;
            }
        } catch (NullPointerException e) {
            is_valid = false;
        }

        return is_valid;
    }

    public static void shareContent(Context context, String msg_share, String share_url) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
            //String sAux = context.getString(R.string.share_url) + share_url + msg_share + "\n\n" + context.getString(R.string.share_msg);
            i.putExtra(Intent.EXTRA_TEXT, "");
            context.startActivity(Intent.createChooser(i, "Share"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTimeDifference(String date) {

        String time_difference = "";

        long start_time_milisecond = Long.parseLong(date) * 1000L;

        long diff = System.currentTimeMillis() - start_time_milisecond;

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        long diffWeeks = diffDays / 7;
        long diffMonths = diffDays / 31;
        long diffYears = diffMonths / 12;

        //Log.i(TAG, diffYears + " years, " + diffMonths + " months, " + diffWeeks + " weeks, "+ diffDays + " days, " + diffHours + " hours, " + diffMinutes + " minutes, " + diffSeconds + " seconds.");

        if (diffYears > 0) {
            if (diffYears == 1) {
                time_difference = diffYears + " year ago";
            } else {
                time_difference = diffYears + " years ago";
            }
            return time_difference;
        } else if (diffMonths > 0) {
            if (diffMonths == 1) {
                time_difference = diffMonths + " month ago";
            } else {
                time_difference = diffMonths + " months ago";
            }
            return time_difference;
        } else if (diffWeeks > 0) {
            if (diffWeeks == 1) {
                time_difference = diffWeeks + " week ago";
            } else {
                time_difference = diffWeeks + " weeks ago";
            }
            return time_difference;
        } else if (diffDays > 0) {
            if (diffDays == 1) {
                time_difference = diffDays + " day ago";
            } else {
                time_difference = diffDays + " days ago";
            }
            return time_difference;
        } else if (diffHours > 0) {
            if (diffHours == 1) {
                time_difference = diffHours + " hour ago";
            } else {
                time_difference = diffHours + " hours ago";
            }
            return time_difference;
        } else if (diffMinutes > 0) {
            if (diffMinutes == 1) {
                time_difference = diffMinutes + " minute ago";
            } else {
                time_difference = diffMinutes + " minutes ago";
            }
            return time_difference;
        } else if (diffSeconds > 0) {
            if (diffSeconds == 1) {
                time_difference = diffSeconds + " second ago";
            } else {
                time_difference = diffSeconds + " seconds ago";
            }
            return time_difference;
        }

        return time_difference;
    }

     public static String formatTime(String time) {
        String formatted = "";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            format.setTimeZone(timeZone);
            Date newDate = null;
            try {
                newDate = format.parse(time);
            } catch (ParseException e) {
                //   e.printStackTrace();
            }
            format = new SimpleDateFormat("EEE, MMMM d, yyyy hh:mm a");
            formatted = format.format(newDate);
        } catch (NullPointerException e) {
            // e.printStackTrace();
            formatted = time;
        }

        return formatted;
    }

    public static void openURL(String url, Context context) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(i);
    }

    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);
        double resultDegree = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        String coordNames[] = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N"};
        double directionid = Math.round(resultDegree / 22.5);
        // no of array contain 360/16=22.5
        if (directionid < 0) {
            directionid = directionid + 16;
            //no. of contains in array
        }
        String compasLoc = coordNames[(int) directionid];

        return resultDegree;
    }

    public static double angleFromCoordinate(double lat1, double long1, double lat2,
                                       double long2) {

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng;

        return brng;
    }

    public static String distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (round2(dist,2)+" Km");
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static float round2(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = (float) (number * pow);
        return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }

}
