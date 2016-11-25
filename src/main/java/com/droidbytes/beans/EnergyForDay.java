package com.droidbytes.beans;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class EnergyForDay {
	HashMap<String, EnergyUnit> dateTimeToUnitMap = new HashMap<String, EnergyUnit>();
	
	public void addSolarProductionUnit(EnergyUnit solarProdUnit) {
		String hourOfDay = String.format("%1$tY%1$tm%1$td%1$tH", solarProdUnit.getOutputDate());
		dateTimeToUnitMap.put(hourOfDay, solarProdUnit);
	}
	
	public Collection<EnergyUnit> getSolarProdUnits() {
		Map<String, EnergyUnit> treeMap = new TreeMap<String, EnergyUnit>(dateTimeToUnitMap);
		return treeMap.values();
	}
	
	public long totalConsumptionForDay() {
		long totalWh = 0;
		for (Iterator<EnergyUnit> unitIter = getSolarProdUnits().iterator(); unitIter.hasNext();) {
			EnergyUnit solarProductionUnit = (EnergyUnit) unitIter.next();
			totalWh +=solarProductionUnit.getWattHourConsumed();
		}
		return totalWh;
	}

	public void setSolarProdUnits(Collection<EnergyUnit> solarProdUnits) {
		for (Iterator<EnergyUnit> unitCollection = solarProdUnits.iterator(); unitCollection.hasNext();) {
			EnergyUnit solarProductionUnit = (EnergyUnit) unitCollection
					.next();
			addSolarProductionUnit(solarProductionUnit);
		}
	}
	
	public EnergyUnit getSolarProdUnitForHour(Date dateToSearch) {
		return dateTimeToUnitMap.get(String.format("%1$tY%1$tm%1$td%1$tH", dateToSearch));
	}

	public String getGenerationDataForDay() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<EnergyUnit> unitIter = getSolarProdUnits().iterator(); unitIter.hasNext();) {
			EnergyUnit solarProductionUnit = (EnergyUnit) unitIter
					.next();
			sb.append(solarProductionUnit.getGenerationDataString()).append(";");
		}
		
		return sb.toString();
	}

	public String getConsumptionDataForDay() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<EnergyUnit> unitIter = getSolarProdUnits().iterator(); unitIter.hasNext();) {
			EnergyUnit solarProductionUnit = (EnergyUnit) unitIter
					.next();
			sb.append(solarProductionUnit.getConsumptionDataString()).append(";");
		}
		
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<EnergyUnit> unitIter = getSolarProdUnits().iterator(); unitIter.hasNext();) {
			EnergyUnit solarProductionUnit = (EnergyUnit) unitIter
					.next();
			sb.append(solarProductionUnit.toString()).append(";");
		}
		
		return sb.toString();
	}
	
	public EnergyForDay clone() {
		EnergyForDay retVal = new EnergyForDay();
		retVal.dateTimeToUnitMap.putAll(dateTimeToUnitMap);
		return retVal;
	}
	
}
