package org.n52.v3d.worldviz.helper;

import java.util.ArrayList;
import java.util.List;

import org.n52.v3d.triturus.vgis.VgAttrFeature;

public class CountryShareHelper {

	public static VgAttrFeature computeNewShareBasedOnLastXyears(VgAttrFeature country, String currentYear,
			int numberOfLastYears) {

		List<Double> sharesFromLastXyears = null;

		try {
			sharesFromLastXyears = extractSharesForPassedYears(country, currentYear, numberOfLastYears);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * compute average
		 */
		double raisedShareForNewYear = computeAverageRaise(sharesFromLastXyears);

		/*
		 * add computed raise to last years value!
		 */
		double newShare = computeNewShare(country, currentYear, raisedShareForNewYear);

		/*
		 * add averageChange as new attribute for the currentYear!
		 */
		country.addAttribute(currentYear, Double.class.toString());
		country.setAttributeValue(currentYear, String.valueOf(newShare));

		return country;

	}

	private static double computeNewShare(VgAttrFeature country, String currentYear, double raisedShareForNewYear) {
		int targetYear = Integer.parseInt(currentYear);

		double previousYearShare = Double
				.parseDouble((String) country.getAttributeValue(String.valueOf(targetYear - 1)));
		
		double newShare = previousYearShare + raisedShareForNewYear;
		
		if(newShare < 0)
			newShare = 0.0;
		
		return newShare;

	}

	private static double computeAverageRaise(List<Double> sharesFromLastXyears) {
		double sum = 0;

		for (int i = 1; i < sharesFromLastXyears.size(); i++) {

			Double currentShare = sharesFromLastXyears.get(i);
			Double previousShare = sharesFromLastXyears.get(i - 1);
			double raisedShare = currentShare - previousShare;

			sum += raisedShare;
		}

		double average = sum / (sharesFromLastXyears.size() - 1);

		return average;
	}

	private static List<Double> extractSharesForPassedYears(VgAttrFeature country, String currentYear,
			int numberOfLastYears) {
		List<Double> shares = new ArrayList<Double>(numberOfLastYears);

		int targetYear = Integer.parseInt(currentYear);

		for (int i = numberOfLastYears; i > 0; i--) {
			int yearToExtract = targetYear - i;

			Double shareForYear = Double.parseDouble((String) country.getAttributeValue(String.valueOf(yearToExtract)));

			shares.add(shareForYear);
		}

		return shares;

	}

}
