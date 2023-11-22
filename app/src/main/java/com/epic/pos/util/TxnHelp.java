package com.epic.pos.util;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import java.util.Date;
import java.util.Random;



//import com.epic.mpayment.util.SysoutMessage;

public class TxnHelp {

	private static String priviousRRN;
	
	/**
	 * print ISO 8583 packet
	 * @param m
	 * @param msgt
	 * @throws Exception
	 */
	public static void printPacket(ISOMsg m, String msgt) throws Exception {

		StringBuffer msg = new StringBuffer();
		msg.append("\n---------------------------------------------------\n");
		msg.append(msgt + "\n");
		msg.append("---------------------------------------------------\n");
		for (int i = 0; i < 128; i++) {
			if (m.hasField(i)) {
				msg.append("Element [" + i + "]  " + m.getValue(i).toString() + "\n");
				
			}
		}
		msg.append("---------------------------------------------------\n");
		AppLog.i("PACKET ",msg.toString());
	}
	
	/*
	 * get RNN value
	 */
	public static String getRRN()throws Exception {
		Random ran = new Random();
		String ranv = ran.nextInt(100)+"";
		Date d = new Date();
		priviousRRN = (ISODate.getDate(d)+ ISODate.getTime(d) + ISOUtil.zeropad(ranv, 2));
		return priviousRRN;
	}
	
	public static String getRevesalRRN()throws Exception {
		return priviousRRN;
	}

}
