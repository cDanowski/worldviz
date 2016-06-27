package org.n52.v3d.worldviz.worldscene.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.n52.v3d.triturus.t3dutil.T3dColor;
import org.n52.v3d.triturus.vgis.VgAttrFeature;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ColoredAttrFeature;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ColoredAttrFeature_forAnimation;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ExtrudedAttrFeature;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ExtrudedAttrFeature_forAnimation;

public class FindExtrudeAndColorMissingCountriesHelper_forAnimation extends FindExtrudeAndColorMissingCountriesHelper {

	private int firstYear;
	private int lastYear;
	private int interval;

	public FindExtrudeAndColorMissingCountriesHelper_forAnimation(CountryBordersLODEnum worldBordersLOD, int firstYear,
			int lastYear, int interval) {
		super(worldBordersLOD);

		this.firstYear = firstYear;
		this.lastYear = lastYear;
		this.interval = interval;
	}

	@Override
	protected List<VgAttrFeature> extrudeAndColorRemainingCountries(Map<String, VgAttrFeature> remainingCountriesMap,
			T3dColor neutralColor, double neutralExtrusionHeight) {

		Set<Entry<String, VgAttrFeature>> remainingCountries = remainingCountriesMap.entrySet();

		List<VgAttrFeature> extrudedColoredRemainingCountries = new ArrayList<VgAttrFeature>(remainingCountries.size());

		// color mapper to color the remaining countries with neutral color
		MpValue2ColoredAttrFeature_forAnimation colorMapper = new MpValue2ColoredAttrFeature_forAnimation();
		if (neutralColor != null) {
			colorMapper.setNeutralColor(neutralColor);
		}

		// extrusion mapper to extrude the remaining countries with neutral
		// extrusionHeight
		MpValue2ExtrudedAttrFeature_forAnimation extrusionMapper = new MpValue2ExtrudedAttrFeature_forAnimation();
		extrusionMapper.setNeutralExtrusionHeight(neutralExtrusionHeight);

		for (Entry<String, VgAttrFeature> remainingCountry : remainingCountries) {

			VgAttrFeature neutralFeature = remainingCountry.getValue();

			for (int currentYear = this.firstYear; currentYear <= this.lastYear; currentYear += this.interval) {

				neutralFeature = colorMapper.colorWithNeutralColor(String.valueOf(currentYear), neutralFeature);

				neutralFeature = extrusionMapper.extrudeWithNeutralHeight(String.valueOf(currentYear), neutralFeature);
			}
			
			/*
			 * set the "Country code" attribute with the iso3166-2 value!
			 */
			addISO3166CountryCodeAttribute(neutralFeature, remainingCountry);

			extrudedColoredRemainingCountries.add(neutralFeature);
		}

		return extrudedColoredRemainingCountries;

	}

	@Override
	protected List<VgAttrFeature> extrudeAndColorRemainingCountries(Map<String, VgAttrFeature> remainingCountriesMap) {

		Set<Entry<String, VgAttrFeature>> remainingCountries = remainingCountriesMap.entrySet();

		List<VgAttrFeature> extrudedColoredRemainingCountries = new ArrayList<VgAttrFeature>(remainingCountries.size());

		MpValue2ColoredAttrFeature_forAnimation colorMapper = new MpValue2ColoredAttrFeature_forAnimation();
		MpValue2ExtrudedAttrFeature_forAnimation extrusionMapper = new MpValue2ExtrudedAttrFeature_forAnimation();

		for (Entry<String, VgAttrFeature> remainingCountry : remainingCountries) {

			VgAttrFeature neutralFeature = remainingCountry.getValue();

			for (int currentYear = this.firstYear; currentYear <= this.lastYear; currentYear += this.interval) {

				neutralFeature = colorMapper.colorWithNeutralColor(String.valueOf(currentYear), neutralFeature);

				neutralFeature = extrusionMapper.extrudeWithNeutralHeight(String.valueOf(currentYear), neutralFeature);
			}

			/*
			 * set the "Country code" attribute with the iso3166-2 value!
			 */
			addISO3166CountryCodeAttribute(neutralFeature, remainingCountry);
			
			extrudedColoredRemainingCountries.add(neutralFeature);
		}

		return extrudedColoredRemainingCountries;
	}

}
