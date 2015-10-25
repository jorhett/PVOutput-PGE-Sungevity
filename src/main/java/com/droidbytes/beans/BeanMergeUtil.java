package com.droidbytes.beans;

import java.util.Collection;
import java.util.Iterator;

public class BeanMergeUtil {

	public static EnergyForDay mergeData(EnergyForDay consumption, EnergyForDay production) {
		EnergyForDay result = new EnergyForDay();
		Collection<EnergyUnit> consumptionUnits = consumption.getSolarProdUnits();
		long totalGeneration = 0;
		long totalConsumption = 0;

		for (Iterator<EnergyUnit> consumptionIter  = consumptionUnits.iterator(); consumptionIter.hasNext();) {
			EnergyUnit consumptionUnit = (EnergyUnit)consumptionIter.next();
			EnergyUnit productionUnit = production.getSolarProdUnitForHour(consumptionUnit.getOutputDate());
			if (productionUnit !=null ) {
				totalGeneration = totalGeneration + productionUnit.getWattHourGenerated();
				totalConsumption = totalConsumption + (consumptionUnit.getWattHourConsumed() + productionUnit.getWattHourGenerated());
				EnergyUnit consolidatedUnit = new EnergyUnit();
				consolidatedUnit.setOutputDate(productionUnit.getOutputDate());
				consolidatedUnit.setWattHourConsumed(totalConsumption);
				consolidatedUnit.setWattHourGenerated(totalGeneration);
				result.addSolarProductionUnit(consolidatedUnit);
			}
			
		}
		
		return result;
	}
}
