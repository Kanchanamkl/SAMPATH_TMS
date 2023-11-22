package com.epic.pos.util;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

	public static final SimpleDateFormat DD_MM_YYYY_HH_MM_SLASH   = new SimpleDateFormat("dd/MM/yyyy kk:mm", Locale.ENGLISH);
	public static final SimpleDateFormat YYYY_MM_DD_HH_MM_SS      = new SimpleDateFormat("yyyyMMddkkmmss", Locale.ENGLISH);
	public static final SimpleDateFormat HH_MM_ss                 = new SimpleDateFormat("HHmmss", Locale.ENGLISH);
	public static final SimpleDateFormat MM_DD                    = new SimpleDateFormat("MMdd", Locale.ENGLISH);
	public static final SimpleDateFormat YYYYMMDD                 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
	public static final SimpleDateFormat DD_MMM_YYYY_DASH         = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
	public static final SimpleDateFormat DD_MM_YYYY_SLASH         = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

	/**
	 * @return yyyyMMddHHmmss formate date as string
	 */
	public static String getCurrentTimeStamp() {
		return YYYY_MM_DD_HH_MM_SS.format(new Date());
	}

	/**
	 * @return yyyy/MM/dd hh:mm:ss a formate date as string
	 */
	public static String getReceiptDateTime() {
		return DD_MM_YYYY_HH_MM_SLASH.format(new Date());
	}


	public static String isoDate() {
		Calendar c 	= Calendar.getInstance();
		Date day 	= c.getTime();
		String date = MM_DD.format(day);
		return date;
	}

	@SuppressLint("SimpleDateFormat")
	public static String isoTime() {
		Calendar c 	= Calendar.getInstance();
		Date day 	= c.getTime();
		String date = HH_MM_ss.format(day);
		return date;

	}


	public static String convertDateTimeFormat(String date, SimpleDateFormat currentFormat, SimpleDateFormat  newFormat) {
		if (date == null) {
			return "";
		} else {
			try {
				return newFormat.format(currentFormat.parse(date));
			} catch (ParseException e) {
				return date;
			}
		}
	}

	public static String displayDateTimeFormat(String date) {
		if (date == null) {
			return "";
		} else {
			try {
				return DD_MM_YYYY_SLASH.format(YYYYMMDD.parse(date));
			} catch (ParseException e) {
				return "";
			}
		}
	}


	public static String getRequestDateFormat(String date) {
		if (date == null) {
			return "";
		} else {
			try {
				return YYYYMMDD.format(DD_MMM_YYYY_DASH.parse(date));
			} catch (ParseException e) {
				return "";
			}
		}
	}
}
