package org.n52.v3d.worldviz.dataaccess.importtools;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class WorldSharesComputer {

	/**
	 * Computes the share of each country. It replaces the original data values
	 * of each country-entry with its cmputed share.
	 * 
	 * @param datasetEntries
	 *            must contain an entry with key="World", which represents the
	 *            total amount from which the shares for each country shall be
	 *            computed.
	 * @return
	 */
	public static Map<String, DatasetEntry> computeWorldShares(Map<String, DatasetEntry> datasetEntries) {

		/*
		 * extract entry with key="World".
		 */
		DatasetEntry worldEntry = datasetEntries.get("World");
		String[] attributes = worldEntry.getAttributes();

		/*
		 * remove first attributeHeader from array, since that only is the ISO
		 * country code.
		 */
		String[] attributesHeaders = new String[attributes.length - 1];
		for (int i = 0; i < attributesHeaders.length; i++) {
			attributesHeaders[i] = attributes[i + 1];
		}

		/*
		 * iterate over each entry: for each year: read thematic value compute
		 * the share compared to the total value (from entry "World") set share
		 * as new value!
		 */
		Iterator<Entry<String, DatasetEntry>> iterator = datasetEntries.entrySet().iterator();
		
		DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.GERMAN);
		formatSymbols.setDecimalSeparator('.');

		DecimalFormat decimalFormatter = new DecimalFormat("#0.0000000000", formatSymbols);

		while (iterator.hasNext()) {
			Entry<String, DatasetEntry> nextEntry = iterator.next();

			DatasetEntry country = nextEntry.getValue();

			for (String attributeName : attributesHeaders) {
				String totalValueAsText = worldEntry.getValue(attributeName);
				String countryValueAsText = country.getValue(attributeName);

				if (!countryValueAsText.equals("NODATA")) {
					double totalWorldValue = Double.parseDouble(totalValueAsText);
					double countryValue = Double.parseDouble(countryValueAsText);

					double computedShare = countryValue / totalWorldValue;
					country.setValue(attributeName, decimalFormatter.format(computedShare));
				}
				else{
					/*
					 * no value could be determined due to missing "NODATA" entry.
					 * simply set 0 as share, since we not know better!
					 */
					country.setValue(attributeName, decimalFormatter.format(0));
				}
			}

			datasetEntries.put(country.getKeyAttributeValue(), country);
		}

		return datasetEntries;
	}

}
