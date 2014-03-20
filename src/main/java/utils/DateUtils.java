package utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtils {
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    public SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);

    public String now() {
	Calendar cal = Calendar.getInstance();
	return sdf.format(cal.getTime());
    }

    public String diff(String begin, String end) throws ParseException {
	Date beg = sdf.parse(begin);
	Date en = sdf.parse(end);
	return diff(beg, en);

    }

    public static String diff(Date beg, Date en) {
	long l = en.getTime() - beg.getTime();
	long day = l / (24 * 60 * 60 * 1000);
	long hour = (l / (60 * 60 * 1000) - day * 24);
	long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
	long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
	String result = "" + day + "d" + hour + "h" + min + "m" + s + "s";
	return result;
    }

    public static void main(String arg[]) throws ParseException {
	int c = 0;
	Random random = new Random(System.currentTimeMillis());
	double ranNum1 = random.nextDouble();
	for (int i = 0; i < 10000000; i++) {
	    random = new Random(System.currentTimeMillis());
	    double ranNum2 = random.nextDouble();
	    if (Math.abs(ranNum2 - ranNum1) > 0.1) {
		c++;
		System.out.println(i);
		System.out.println(ranNum2);
	    }
	    ranNum1 = ranNum2;
	}
	System.out.println(c);

	/*
	 * long now = System.currentTimeMillis(); String begin =
	 * DateUtils.now(); for(int i=0; i<100000; i++){ long next =
	 * System.currentTimeMillis(); String end = DateUtils.now(); if(next >
	 * now){ System.out.println(i + "=" + now); String diff = diff(begin,
	 * end); System.out.println(diff); break; } now = next; begin = end; }
	 */

    }

}