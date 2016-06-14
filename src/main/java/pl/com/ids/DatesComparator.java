package pl.com.ids;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DatesComparator {
	private boolean debug = false;

	public boolean between(String lowLimit, String highLimit, Date date) {
		String DATE_FORMAT = "dd-MM-yyyy";

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
				DATE_FORMAT);
		Calendar cll = Calendar.getInstance();
		Calendar chl = Calendar.getInstance();
		Calendar cd = Calendar.getInstance();
		// remember months are zero-based : 0 jan 1 feb ...
		// try
		Date ll = null;
		Date hl = null;
		try {
			if (lowLimit != null)
				ll = sdf.parse(lowLimit);
			if (highLimit != null)
				hl = sdf.parse(highLimit);

		} catch (ParseException pe) {
			System.out.println("Nieprawidlowy format daty");
			return false;
		}
		if (ll == null)
			cll = null;
		else
			cll.setTime(ll);
		if (hl == null)
			chl = null;
		else {
			chl.setTime(hl);
			chl.add(Calendar.DAY_OF_YEAR, 1);	
		}
		
		

		cd.setTime(date);

		if ((cll == null || cll.before(cd)) && ((chl == null) || chl.after(cd))) {
			if (debug)
				System.out.println("ok");
			return true;
		}
		if (debug)
			System.out.println("spoza zakresu");
		return false;
	}

	public static void main(String[] args) {
		DatesComparator cd = new DatesComparator();
		cd.between(args[0], args[1], new Date());
		cd.between(args[0], null, new Date());
		cd.between(null, args[1], new Date());
	}
}
