package org.redfrog404.mobycraft.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.Vec3i;

public class Utils {

	// Used for byte conversions
	static Map<Integer, String> byteSuffixNumbers = new HashMap<Integer, String>();
	
	static {
		byteSuffixNumbers.put(0, "B");
		byteSuffixNumbers.put(1, "KB");
		byteSuffixNumbers.put(2, "MB");
		byteSuffixNumbers.put(3, "GB");
	}

	public static <T extends Comparable<? super T>> List<T> asSortedList(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	public static boolean checkIfArgIsNull(String[] args, int argNumber) {
		try {
			if (args[argNumber].equals(null)) {
				return true;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return true;
		}
		return false;
	}
	
	public static String imageSizeConversion(double bytes) {
		int suffixNumber = 0;

		/*
		 * Docker seems to convert bytes using 1000 instead of 1024
		 */
		while (bytes / 1000 > 1) {
			bytes /= 1000;
			suffixNumber++;
		}

		NumberFormat formatter = new DecimalFormat("#0.0");
		String byteString = formatter.format(bytes) + " "
				+ byteSuffixNumbers.get(suffixNumber);
		if (byteString.contains(".0")) {
			byteString = byteString.replace(".0", "");
		}

		return byteString;
	}
}
