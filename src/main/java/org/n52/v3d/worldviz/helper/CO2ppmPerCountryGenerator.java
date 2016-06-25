package org.n52.v3d.worldviz.helper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.n52.v3d.triturus.vgis.VgAttrFeature;
import org.n52.v3d.worldviz.dataaccess.importtools.KeyValuePair;

/**
 * helper class for CO2 animation scenes.
 * 
 * @author Christian
 *
 */
public class CO2ppmPerCountryGenerator {

	/**
	 * Generates a map of features, representing the share of ppmConcentration
	 * in the atmosphere!
	 * 
	 * @param co2_ppm_world_pairs
	 * @param countries_shares
	 * @return
	 */
	public static List<VgAttrFeature> generatePpmPerCountry(List<KeyValuePair> co2_ppm_world_pairs,
			List<VgAttrFeature> countries_shares) {
		List<VgAttrFeature> ppmSharesPerCountry = new ArrayList<VgAttrFeature>(countries_shares.size());
		
		DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.GERMAN);
		formatSymbols.setDecimalSeparator('.');

		DecimalFormat decimalFormatter = new DecimalFormat("#0.0000000000", formatSymbols);
		
		for (VgAttrFeature country : countries_shares) {
			/*
			 * for all years: compute the share of the ppm using the share value of co2 emissions.
			 * 
			 * replace co2 emission share with ppm share? Or create a new VgAttrFeature?
			 */
			
			for (KeyValuePair co2_ppm_perYear : co2_ppm_world_pairs) {
				String year = co2_ppm_perYear.getKey();
				double co2_ppm_total = Double.parseDouble(co2_ppm_perYear.getValue());
				
				try{
				double country_co2_emission_share = Double.parseDouble((String)country.getAttributeValue(year));
				
				/*
				 * multiply total ppm value with the co2 emission share of the country.
				 */
				double co2_ppm_country_share = co2_ppm_total * country_co2_emission_share / co2_ppm_total;
				
				/*
				 * set new co2 ppm share at VgAttrFeature and add it to result map
				 */
				country.setAttributeValue(year, decimalFormatter.format(co2_ppm_country_share));
				}
				catch(Exception e){
					//TODO auto-generated catch block
				}
			}

			ppmSharesPerCountry.add(country);
		}
		
		return ppmSharesPerCountry;
		
	}

}
