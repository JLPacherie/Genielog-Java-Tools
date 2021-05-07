package com.genielog.tools;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class collects utility methods that might be used in several places around the code. These utilities are
 * provided as static, Thread-safe methods.
 * 
 * @author pacherie
 *
 */
public class Tools {

	
	public static boolean isUndefined(String str) {
		return str == null || str.isEmpty();
	}
	
	public static <T> boolean isUndefined(Collection<T> collection) {
		return collection == null || collection.isEmpty();
	}

	public static String getExceptionMessages(Throwable e) {
		String result = "";
		if (e != null) {
			result = e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
			if (e.getCause() != null) {
				result += ", because " + e.getClass().getSimpleName() + ": " + e.getCause().getLocalizedMessage();
			}
		}
		return result;
	}
	
	// ******************************************************************************************************************
	// Number to String formatting
	// ******************************************************************************************************************

	/** Format a number to a fixed length string. For example 3435 to 3.4K */
	public static String getStrFrom(double number) {
		String suffix = "";
		double v = number;
		double log = Math.log10(number);
		if (log > 6) {
			suffix = "M";
			v = v / 1000000;
		} else if (log > 3) {
			suffix = "K";
			v = v / 1000;
		}

		String result = "";
		if (((int) v != v) && (v < 10)) {
			v = ((int) (v * 10)) / 10.0;
			result = Double.toString(v);
		} else {
			result = Integer.toString((int) v);
		}
		return result + suffix;
	}

	public static String readableFileSize(long size, String unit) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "", "K", "M", "G", "T" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups] + unit;
	}

	/** Cut a given string to maximum lenght by removing either prefix or suffix. Add 's' to notify the string was cut */
	public static String getCuttedString(String src, int maxLength, String s, boolean prefix) {
		if ((src == null) || (src.length() < maxLength)) {
			return src;
		}

		if (prefix) {
			return s + src.substring(s.length() - maxLength);
		} else {
			return src.substring(0, maxLength) + s;
		}
	}


	public static String fmt(double d, int nbDec) {
		if (d == (long) d)
			return String.format("%d", (long) d);
		else
			return String.format("%." + nbDec + "f", d);
	}

	//
	// ******************************************************************************************************************
	//

	public static boolean search(String value, String reFilter) {
		boolean result = false;
		if ((value != null) && (reFilter != null)) {
			// Try string matching
			result = value.toLowerCase(Locale.ENGLISH).contains(reFilter);
			// Or pattern matching
			if (!result) {
				try {
					Pattern p = Pattern.compile(reFilter);
					result = p.matcher(value).find();
				} catch (Exception e) {
					result = false;
				}
			}
		}
		return result;
	}

	//
	// ******************************************************************************************************************
	//

	public static boolean search(Stream<String> stream, String filter) {
		return stream.anyMatch(s -> Tools.search(s, filter));
	}

	//
	// ******************************************************************************************************************
	//

	public static <T> Map<T, Long> createHistogram(Stream<T> stream) {
		HashMap<T, Long> result = new HashMap<>();
		stream.forEach(item -> {
			Long v = result.get(item);
			if (v == null) {
				result.put(item, Long.valueOf(1));
			} else {
				result.put(item, v + 1L);
			}
		});
		return result;
	}

	//
	// ******************************************************************************************************************
	//

	public static <T> Map<T, Double> updateHistogram(Map<T, Double> src, Map<T, Double> dest) {
		for (Map.Entry<T, Double> entry : src.entrySet()) {
			Double destValue = dest.get(entry.getKey());
			if (destValue == null) {
				dest.put(entry.getKey(), entry.getValue());
			} else {
				dest.put(entry.getKey(), entry.getValue() + destValue);
			}
		}
		return dest;
	}

	public static String strReplaceAll(//
																			final String strSource, //
																			final String strFrom, //
																			final String strTo, //
																			final String quote) //
	{

		StringBuilder result = new StringBuilder(strSource);
		if (!strFrom.isEmpty()) {
			int pos = result.indexOf(strFrom, 0);
			while (pos != -1) {
				if (pos > quote.length()) {
					int startPos = pos - quote.length();
					String prefix = result.substring(startPos, pos);
					if (prefix.equals(quote)) {
						// Remplacement ignore car le modele est prefixe par le quote
					} else {
						result.replace(pos, pos + strFrom.length(), strTo);
					}
				} else {
					result.replace(pos, pos + strFrom.length(), strTo);
				}
				pos = result.indexOf(strFrom, pos + strTo.length());
			}
		}
		return result.toString();
	}


	//
	// ******************************************************************************************************************
	// Date & Time conversions
	// ******************************************************************************************************************
	//
	public static final String SICM_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
	public static final DateTimeFormatter dateFormater = DateTimeFormatter.ofPattern(SICM_DATE_FORMAT);

	public static LocalDateTime parseLocalDate(String strDate, LocalDateTime dflt) {
		try {
			if (strDate.isEmpty()) {
				return null;
			}
			return LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern(SICM_DATE_FORMAT));
		} catch (Exception e) {
			return dflt;
		}
	}
	
	/** Returns a string date in the SICM format from a string date in fmt format. */
	public static String convertStrDate(String date, String fmt) {
		try {
			LocalDateTime defectDate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(fmt));
			return defectDate.format(dateFormater);
		} catch (Exception e) {
			return date;
		}
	}
	
	public static String getFormatedDate(long epoch, String dateFormat) {
		Date date = new Date(epoch * 1000L);
		DateFormat format = new SimpleDateFormat(dateFormat);
		format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		return format.format(date);
	}

	public static long getEpochOfDate(String strDate, String dateFormat) {
		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		Date date;
		try {
			date = df.parse(strDate);
			Long epochMilli = date.getTime();
			return epochMilli / 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static long getDaysBetween(long epochFrom, long epochTo) {
		Instant now = Instant.ofEpochSecond(epochFrom);
		Instant creation = Instant.ofEpochSecond(epochTo);
		Duration duration = Duration.between(creation, now);
		return duration.toDays();
	}

	/** Returns true if [lower,upper] denotes a valid date range. */
	public static boolean isValidDateRange(LocalDateTime lower, LocalDateTime upper) {
		
		if ((lower == null) && (upper == null)) {
			return true; // Empty range, that's possible
		} 
		
		// ] - Inf ; upper ]
		if ((lower == null) && (upper != null)) {
		  return true;
		} 
		
		// [ lower ; + Inf [
		if ((lower != null) && (upper == null) ) {
			return true;
		}
		
		
		return lower.isEqual(upper) || lower.isBefore(upper);
		
	}
	//
	// ******************************************************************************************************************
	// Stream manipulations
	// ******************************************************************************************************************
	//

	/** Produce a sorted list of strings extracted from a list of objects. */
	public static <T> String toStringList(Stream<T> s, Function<T, String> getLabel, String separator) {
		List<String> l = s.map(t -> getLabel.apply(t)).sorted().collect(Collectors.toList());
		StringBuilder buffer = new StringBuilder();
		if (!l.isEmpty()) {
			buffer.append(l.get(0));
			int i = 1;
			while (i < l.size()) {
				buffer.append(separator);
				buffer.append(l.get(i));
				i++;
			}
		}
		return buffer.toString();
	}

	public static <T> int compare(Stream<T> s1, Stream<T> s2, Function<T, String> getLabel) {
		// TODO How to test two sequences when one or both can be null ?
		int c = -1;
		if ((s1 != null) && (s2 != null)) {
			List<String> l1 = s1.map(t -> getLabel.apply(t)).sorted().collect(Collectors.toList());
			List<String> l2 = s2.map(t -> getLabel.apply(t)).sorted().collect(Collectors.toList());
			int index = 0;
			while ((index < l1.size()) && (index < l2.size())) {
				c = l1.get(index).compareTo(l2.get(index));
				if (c != 0)
					return c;
				index++;
			}

			if (index < l2.size()) {
				return 1;
			}
			if (index < l1.size()) {
				return -1;
			}
		}
		return c;
	}
	
	// ******************************************************************************************************************
	
	public static <T> T search(Stream<T> s, T i, BiPredicate<T,T> equals) {
		return s.filter( is -> equals.test(i,is)).findAny().orElse(null);
	}
	
	public static <T> Collection<T> intersect (Stream<T> s1, Supplier<Stream<T>> ss2, BiPredicate<T,T> equals) {
		List<T> result = new ArrayList<>();
		s1.forEach( i1 -> {
			T i = Tools.search(ss2.get(),i1,equals);
			if (i != null) {
				result.add(i);
			}
		});
		return result;
	}

	/** Merge in c1 all items from c2 that are not already there. */
	public static <T> void union (Collection<T> c1, Stream<T> s2, BiPredicate<T,T> equals) {
		s2.forEach( i2 -> {
			T i = Tools.search(c1.stream(),i2,equals);
			if (i == null) {
				c1.add(i2);
			}
		});
	}
	
}
