package org.n52.v3d.worldviz.demoapplications.ene.earth;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.t3dutil.T3dColor;
import org.n52.v3d.triturus.t3dutil.T3dVector;
import org.n52.v3d.triturus.vgis.VgAttrFeature;
import org.n52.v3d.worldviz.dataaccess.importtools.KeyValuePair;
import org.n52.v3d.worldviz.dataaccess.load.DatasetLoader;
import org.n52.v3d.worldviz.dataaccess.load.SimpleTextFileLoader;
import org.n52.v3d.worldviz.dataaccess.load.dataset.XmlDataset;
import org.n52.v3d.worldviz.extensions.mappers.MpAttrFeature2AttrSymbol;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ColoredAttrFeature_forAnimation;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ExtrudedAttrFeature_forAnimation;
import org.n52.v3d.worldviz.extensions.mappers.NamesForAttributes;
import org.n52.v3d.worldviz.extensions.mappers.T3dAttrSymbolInstance;
import org.n52.v3d.worldviz.helper.CO2ppmPerCountryGenerator;
import org.n52.v3d.worldviz.helper.RelativePaths;
import org.n52.v3d.worldviz.projections.AxisSwitchTransform;
import org.n52.v3d.worldviz.worldscene.VsAnimatedCartographicObjectsScene;
import org.n52.v3d.worldviz.worldscene.VsCartographicSymbolsOnASphereScene;
import org.n52.v3d.worldviz.worldscene.helper.CountryBordersLODEnum;
import org.n52.v3d.worldviz.worldscene.helper.FindExtrudeAndColorMissingCountriesHelper;
import org.n52.v3d.worldviz.worldscene.helper.FindExtrudeAndColorMissingCountriesHelper_forAnimation;

public class CO2_Animation_test {

	private static final String COUNTRY_CODE = "Country code";
	private static final CountryBordersLODEnum SIMPLIFIED110M = CountryBordersLODEnum.SIMPLIFIED_110m;
	private static final String outputFile = "test/animation/AnimatedSymbols_CO2ppm.x3d";
	private static int firstYear = 1960;
	private static int lastYear = 2014;
	private static int interval = 1;

	private static String latAttr = "Latitude";
	private static String lonAttr = "Longitude";

	private MpValue2ColoredAttrFeature_forAnimation colorMapper_forAnimation;
	private MpValue2ExtrudedAttrFeature_forAnimation extrusionMapper_forAnimation;

	public CO2_Animation_test() {

	}

	private void run() {
		SimpleTextFileLoader loader = new SimpleTextFileLoader();

		/*
		 * pairs of (year, ppm-value)
		 */
		List<KeyValuePair> co2_ppm_world_pairs = loader
				.extractKeyValuePairs(new File("data/CO2_ppm_world_1960_2014.dat"), 1);

		/*
		 * TODO load a dataset with CO2 emissions of all countries
		 * 
		 * for each year of co2_ppm_world_pairs: split the ppm value --> assign
		 * to each country!
		 * 
		 * new Map<year, Map<withAllCountries> <-- Map contains ppm Value and
		 * corresponding country as VgAttrFeature!
		 * 
		 * --> auf deren Basis dann eine Szene/Animation bauen --> für jedes
		 * Jahr jedes Land animieren!
		 */
		DatasetLoader xmlDatasetLoader = new DatasetLoader(RelativePaths.CARBON_EMISSIONS_SHARES_1960_2014_XML);
		xmlDatasetLoader.setCountryBordersLOD(SIMPLIFIED110M);

		XmlDataset carbonEmissionsShares_1960_2014 = xmlDatasetLoader.loadDataset();
		
		

		DatasetLoader xmlDatasetLoader2 = new DatasetLoader(RelativePaths.COUNTRIES_POINT_XML);
		xmlDatasetLoader2.setCountryBordersLOD(SIMPLIFIED110M);

		XmlDataset countriesPoint = xmlDatasetLoader2.loadDataset();

		/*
		 * how to extract the "World"-entry which represents the total value of
		 * CO2 emissions of all countries for each year?
		 */
		List<VgAttrFeature> countries_shares = carbonEmissionsShares_1960_2014.getFeatures();
		
		double[] test = this.setValuesForAttribute(countries_shares);
		
		List<VgAttrFeature> countryPointFeatures = countriesPoint.getFeatures();
		Map<String, VgAttrFeature> countryPointMap = createCountryCodeMap(countryPointFeatures);

		/*
		 * what about future years > 2014? Shall the simulation values
		 * explicitly be stored in the dataset file --> hence, would be loaded
		 * by now and can be accessed as all others? --> multiple files for
		 * multiple scenarios!
		 * 
		 * or should they be computed on the fly?
		 */

		/*
		 * now use the CO2 emissions per country to split the global ppm value
		 * into appropriate shares for each country
		 */

		List<VgAttrFeature> co2_ppm_perCountry = CO2ppmPerCountryGenerator.generatePpmPerCountry(co2_ppm_world_pairs,
				countries_shares);

		/*
		 * TODO create a new scene to which the co2_ppm_perCountry and
		 * co2_ppm_world_pairs are added as data sources!
		 * 
		 * Better: pass them into a mapper that transforms those input files
		 * into an animated scene!
		 * 
		 * VsAnimatedCo2PpmPerCountryScene scene = new
		 * VsAnimatedCo2PpmPerCountryScene(co2_ppm_world_pairs,
		 * co2_ppm_perCountry); scene.createAnimation();
		 * 
		 * in addition, a basic globe should be generated as well!
		 * 
		 * animation idea: smoke bubbles for each year, that rise into the
		 * atmosphere and maybe expand? and dissappear after time (disappearance
		 * slower than creation!)
		 * 
		 * ------> USE THIS IDEA FIRST: or just create an animation of the
		 * extruded colored world countries? where extrusion and color will
		 * change due to the yearly development? could be easier for first try!
		 * Since we could use the existing Vs...Scene classes and create an
		 * animation around them!
		 */

		/*
		 * TODO it would conceptually be best, if the scene inputs are already
		 * computed with the parameters (color and extrusion) for each country
		 * and each year. E.g. via a mapper. In the scene just create the visual
		 * output.
		 */
		// WvizConfigDocument wVizConfigFile = null;
		// try {
		//// URL wVizConfig_path =
		// getClass().getClassLoader().getResource("WvizConfig.xml");
		// File wVizConfig_path = new
		// File("C:\\Users\\Christian\\Documents\\git\\worldviz\\data\\WvizConfig.xml");
		// wVizConfigFile = WvizConfigDocument.Factory.parse(wVizConfig_path);
		// } catch (XmlException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// MpWorldCountriesAnimationMapper mapper = new
		// MpWorldCountriesAnimationMapper(wVizConfigFile, null);

		VsAnimatedCartographicObjectsScene animatedScene = createAnimatedScene(co2_ppm_perCountry, countryPointMap);

		animatedScene.setOutputFile(
				new File("C:\\Users\\Christian\\Documents\\git\\worldviz\\test\\animation\\CO2_Animation.x3d"));

		animatedScene.generateScene();

		System.out.println("Finished");
	}

	private Map<String, VgAttrFeature> createCountryCodeMap(List<VgAttrFeature> countryPointFeatures) {
		Map<String, VgAttrFeature> map = new HashMap<String, VgAttrFeature>(countryPointFeatures.size());

		for (VgAttrFeature vgAttrFeature : countryPointFeatures) {
			String countryCode = (String) vgAttrFeature.getAttributeValue(COUNTRY_CODE);

			map.put(countryCode, vgAttrFeature);
		}
		return map;
	}

	private VsAnimatedCartographicObjectsScene createAnimatedScene(List<VgAttrFeature> co2_ppm_perCountry,
			Map<String, VgAttrFeature> countryPointMap) {
		String latAttr = "Latitude";
		String lonAttr = "Longitude";

		/*
		 * create cartographic objects with color and extrusion/scale attributes
		 * for every year.
		 */
		List<VgAttrFeature> attributedCO2ppm_allCountries = createCompleteAttributedCO2ppmPerCountry(co2_ppm_perCountry,
				countryPointMap);

		MpAttrFeature2AttrSymbol symbolMapper = new MpAttrFeature2AttrSymbol();
		List<T3dAttrSymbolInstance> attrSymbols = new ArrayList<T3dAttrSymbolInstance>(
				attributedCO2ppm_allCountries.size());

		transformToAttrSymbols(attributedCO2ppm_allCountries, symbolMapper, attrSymbols);

		VsAnimatedCartographicObjectsScene scene = new VsAnimatedCartographicObjectsScene(outputFile);
		scene.setFirstYear(firstYear);
		scene.setLastYear(lastYear);
		scene.setInterval(interval);

		for (T3dAttrSymbolInstance attrSymbol : attrSymbols) {
			
			/*
			 * create uniqueId using the ISO3166-2 code of the country
			 */
			Object uniqueIDValue = attrSymbol.getAttributeValue(COUNTRY_CODE);
			attrSymbol.addAttributeValuePair(NamesForAttributes.attributeNameForIdentifier, uniqueIDValue);

			scene.addCartographicSymbol(attrSymbol);
		}

		scene.setRadius(30);

		return scene;

	}

	private void transformToAttrSymbols(List<VgAttrFeature> attributedCO2ppm_allCountries,
			MpAttrFeature2AttrSymbol symbolMapper, List<T3dAttrSymbolInstance> attrSymbols) {
		AxisSwitchTransform axisSwitch = new AxisSwitchTransform();

		/*
		 * colorMapper and scaleMapper!
		 */

		for (VgAttrFeature vgAttrFeature : attributedCO2ppm_allCountries) {
			// the geometry in THIS case is a point, thus we may cast it
			// normally an instanceOf-check must be done

			double latitude = (double) vgAttrFeature.getAttributeValue(latAttr);
			double longitude = (double) vgAttrFeature.getAttributeValue(lonAttr);
			GmPoint gmPoint = new GmPoint(longitude, latitude, 0);

			// point in virtual world (axes need to be switched.)
			T3dVector virtualPoint = axisSwitch.transform(gmPoint);

			T3dAttrSymbolInstance boxSymbol = symbolMapper.createBoxSymbol(vgAttrFeature,
					new GmPoint(virtualPoint.getX(), virtualPoint.getY(), virtualPoint.getZ()));

			transferAllAttributes(vgAttrFeature, boxSymbol);

			/*
			 * set color value with first year value
			 */
			boxSymbol.setColor((T3dColor) vgAttrFeature
					.getAttributeValue(NamesForAttributes.attributeNameForColor + "_" + firstYear));

			// grundrissebene kleiner machen!
			boxSymbol.setxScale(0.25);
			boxSymbol.setzScale(0.25);
			boxSymbol.setyScale((double) vgAttrFeature
					.getAttributeValue(NamesForAttributes.attributeNameForExtrusion + "_" + firstYear));

			attrSymbols.add(boxSymbol);
		}
	}

	private void transferAllAttributes(VgAttrFeature vgAttrFeature, T3dAttrSymbolInstance boxSymbol) {

		String[] attributeNames = vgAttrFeature.getAttributeNames();

		for (String attributeName : attributeNames) {
			boxSymbol.addAttributeValuePair(attributeName, vgAttrFeature.getAttributeValue(attributeName));
		}

	}

	private List<VgAttrFeature> createCompleteAttributedCO2ppmPerCountry(List<VgAttrFeature> co2_ppm_perCountry,
			Map<String, VgAttrFeature> countryPointMap) {
		initiateMappers_forAnimation(co2_ppm_perCountry);

		for (int currentYear = firstYear; currentYear <= lastYear; currentYear = currentYear + interval) {

			co2_ppm_perCountry = this.colorMapper_forAnimation.transform(String.valueOf(currentYear),
					co2_ppm_perCountry);

			co2_ppm_perCountry = this.extrusionMapper_forAnimation.transform(String.valueOf(currentYear),
					co2_ppm_perCountry);
		}

		List<VgAttrFeature> allSceneObjects = processMissingCountries(co2_ppm_perCountry);

		/*
		 * add attributes "latitude" and "longitude" to the c02Countries.
		 */
		for (VgAttrFeature vgAttrFeature : allSceneObjects) {

			String countryCode = (String) vgAttrFeature.getAttributeValue(COUNTRY_CODE);

			VgAttrFeature pointFeature = countryPointMap.get(countryCode);

			double longitude = Double.parseDouble((String) pointFeature.getAttributeValue(lonAttr));
			double latitude = Double.parseDouble((String) pointFeature.getAttributeValue(latAttr));

			vgAttrFeature.addAttribute(lonAttr, Double.class.toString());
			vgAttrFeature.setAttributeValue(lonAttr, longitude);

			vgAttrFeature.addAttribute(latAttr, Double.class.toString());
			vgAttrFeature.setAttributeValue(latAttr, latitude);

		}

		return allSceneObjects;
	}

	protected List<VgAttrFeature> processMissingCountries(List<VgAttrFeature> extrudedColoredSceneObjects) {

		FindExtrudeAndColorMissingCountriesHelper missingCountriesHelper = new FindExtrudeAndColorMissingCountriesHelper_forAnimation(
				SIMPLIFIED110M, firstYear, lastYear, interval);

		// missingCountriesHelper.findExtrudeAndColorMissingCountries(alreadyExtrudedAndColoredCountries,
		// neutralColor, neutralExtrusionHeight)
		List<VgAttrFeature> allSceneObjects = missingCountriesHelper.findExtrudeAndColorMissingCountries(
				extrudedColoredSceneObjects, this.colorMapper_forAnimation.getNeutralColor(),
				this.extrusionMapper_forAnimation.getNeutralExtrusionHeight());

		return allSceneObjects;
	}

	private void initiateMappers_forAnimation(List<VgAttrFeature> co2_ppm_perCountry) {
		/*
		 * initiate color and extrusion mappers
		 */
		this.colorMapper_forAnimation = new MpValue2ColoredAttrFeature_forAnimation();

		/*
		 * TODO iterate once over co2_ppm_values to determine min and max values
		 * to better reflect them into value categories
		 */
		double[] values = setValuesForAttribute(co2_ppm_perCountry); // values are the
															// actual PPM
															// values! not
															// shares
		T3dColor green = new T3dColor(0.0f, 1.0f, 0.0f);
		T3dColor yellow = new T3dColor(0.5f, 0.5f, 0.0f);
		T3dColor red = new T3dColor(1.0f, 0.0f, 0.0f);

//		T3dColor[] colors = new T3dColor[] { green, yellow, red };
		
		T3dColor[] colors = new T3dColor[] {
				new T3dColor(72 / 255f, 239 / 255f, 0f),
				new T3dColor(175 / 255f, 239 / 255f, 0f),
				new T3dColor(239 / 255f, 239 / 255f, 0f),
				new T3dColor(239 / 255f, 127 / 255f, 0f),
				new T3dColor(239 / 255f, 0f, 0f) };

		this.colorMapper_forAnimation.setPalette(values, colors, true);

		/*
		 * extrusion
		 */
		this.extrusionMapper_forAnimation = new MpValue2ExtrudedAttrFeature_forAnimation();

//		double[] extents = new double[] { 0.2, 5, 10 };
		double[] extents = new double[] { 1, 5, 10, 14, 20 };

		this.extrusionMapper_forAnimation.setPalette(values, extents, true);
		this.extrusionMapper_forAnimation.setNeutralExtrusionHeight(1);

	}
	
	private double[] setValuesForAttribute(List<VgAttrFeature> features) {
		double locMin = -1;
		double locMax = -1;

		for (VgAttrFeature vgAttrFeature : features) {

			for (int i = firstYear; i <= lastYear; i = i + interval) {

				String attributeValue_string = (String) vgAttrFeature.getAttributeValue(String.valueOf(i));

				double value = Double.valueOf(attributeValue_string);

				if (locMin == -1 && locMax == -1) {
					// erster Schritt
					locMin = value;
					locMax = value;
				} else {
					// jeder weitere Schritt
					if (value < locMin)
						locMin = value;
					else if (value > locMax)
						locMax = value;
				}
			}

		}

		double maxValue = locMax;
		double minValue = locMin;

		double halfValue = locMax * 0.5;
		
		double quarterValue = locMax * 0.25;
		double thirtyFiveValue = locMax * 0.35;

		return new double[] { minValue, quarterValue, halfValue, thirtyFiveValue, maxValue };

	}

	public static void main(String args[]) {

		CO2_Animation_test app = new CO2_Animation_test();

		app.run();

	}

}
