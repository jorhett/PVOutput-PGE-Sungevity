package com.droidbytes.pge;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import com.droidbytes.beans.EnergyForDay;
import com.droidbytes.beans.EnergyUnit;

public class PGEDataParser {

	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	public static LinkedHashMap<String,EnergyForDay> parseFile(String fileName)
			throws IOException, NumberFormatException, ParseException {
		
		LinkedHashMap<String, EnergyForDay> energyMap = new LinkedHashMap<String, EnergyForDay>();
		//EnergyForDay pgeDataForAday = new EnergyForDay();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;

		String currentDate = "";
		EnergyForDay pgeDataForAday = new EnergyForDay();
		
		while ((line = br.readLine()) != null) {
			List<String> items = Arrays.asList(line.split("\\s*,\\s*"));
			if ("Electric usage".equals(items.get(0))) {
				// executed very first time
				if (currentDate.equals("")) {
					currentDate = items.get(1); // current date
				}
				// executed whenever date switches
				// if read date is not same as current date then
				if (!currentDate.equals(items.get(1))) {
					// store current date and energy for day in map
					energyMap.put(currentDate, pgeDataForAday.clone());
					// clear current object
					pgeDataForAday = new EnergyForDay();
					// switch current date to new date
					currentDate = items.get(1);
				}
				// read rest of line
				EnergyUnit su = new EnergyUnit(
						dateFormatter.parse(items.get(1) + " " + items.get(2)),
						0, Math.round(Double.parseDouble(items.get(4)) * 1000));
				pgeDataForAday.addSolarProductionUnit(su);
			}
			if ("There is no available usage data for this service at this time. Please check back at a later date to download usage data.".equals(items.get(0))) {
				br.close();
				throw new ParseException("No data in file", 0);
			}
		}
		energyMap.put(currentDate, pgeDataForAday.clone());
		br.close();
		
		return energyMap;

	}
}
