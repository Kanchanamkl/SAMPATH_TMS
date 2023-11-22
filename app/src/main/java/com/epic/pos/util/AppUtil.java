package com.epic.pos.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.epic.pos.BuildConfig;
import com.epic.pos.common.Const;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AppUtil {

    /**
     * Int value to invoice number
     *
     * @param val
     * @return
     */
    public static String toInvoiceNumber(int val) {
        return Utility.padLeftZeros(String.valueOf(val), 6);
    }

    public static String toTraceNumber(int val) {
        return Utility.padLeftZeros(String.valueOf(val), 6);
    }

    public static String toTransactionDate(Date date) {
        return new SimpleDateFormat(Const.TXN_DATE).format(date);
    }

    public static String toTransactionTime(Date date) {
        return new SimpleDateFormat(Const.TXN_TIME).format(date);
    }


    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public static String getAmountIgnoreCents(String totalAmount) {
        try {
            DecimalFormat formatter = new DecimalFormat("#,###");
            if (totalAmount.contains(".")) {
                return formatter.format(Long.parseLong(totalAmount.split("\\.")[0]));
            } else {
                return formatter.format(Long.parseLong(totalAmount));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public static boolean isTmsClientAppExists(Context context) {
        return isAppExists(context, "com.epic.eatmca");
    }

    public static boolean isAppExists(Context context, String packageName) {
        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isNexgoBuild(){
        return BuildConfig.FLAVOR.equals("nexgodev") || BuildConfig.FLAVOR.equals("nexgolive");
    }

    public static String getCentsIgnoreAmount(String totalAmount) {
        try {
            if (totalAmount.contains(".")) {
                String total = totalAmount.split("\\.")[1];
                if (total.length() == 1) {
                    return totalAmount.split("\\.")[1] + "0";
                } else {
                    return totalAmount.split("\\.")[1];
                }
            } else {
                return "00";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "00";
    }

    public static String getFormattedAmount(String amount) {
        return getAmountIgnoreCents(amount) + "." + getCentsIgnoreAmount(amount);
    }
}
