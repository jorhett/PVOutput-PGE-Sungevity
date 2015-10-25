package com.droidbytes.pge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import com.droidbytes.beans.EnergyForDay;
import com.droidbytes.beans.EnergyUnit;

public class PGEDataParser {

	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	public static EnergyForDay parseFile(String fileName)
			throws IOException, NumberFormatException, ParseException {
		
		EnergyForDay pgeData = new EnergyForDay();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		while ((line = br.readLine()) != null) {
			List<String> items = Arrays.asList(line.split("\\s*,\\s*"));
			if ("Electric usage".equals(items.get(0))) {
				EnergyUnit su = new EnergyUnit(
						dateFormatter.parse(items.get(1) + " " + items.get(2)),
						0, Math.round(Double.parseDouble(items.get(4)) * 1000));
				pgeData.addSolarProductionUnit(su);
			}
		}
		br.close();
		
		return pgeData;

	}
}
