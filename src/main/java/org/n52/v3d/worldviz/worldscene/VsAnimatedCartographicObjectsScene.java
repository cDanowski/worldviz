package org.n52.v3d.worldviz.worldscene;

import org.n52.v3d.triturus.t3dutil.T3dColor;
import org.n52.v3d.triturus.vgis.VgIndexedTIN;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.n52.v3d.worldviz.extensions.mappers.NamesForAttributes;
import org.n52.v3d.worldviz.extensions.mappers.T3dAttrSymbolInstance;
import org.n52.v3d.worldviz.projections.Wgs84ToSphereCoordsTransform;

public class VsAnimatedCartographicObjectsScene extends VsCartographicSymbolsOnASphereScene {

	private static final String TIME_SENSOR_NAME = "TimeSensor";
	private static final String PROX_SENSOR_NAME = "ProximitySensor";
	private static final String HUD_NAME = "HUD";
	private static final String CURRENT_YEAR_TEXT_NODE = "CurrentYear";
	private static final String CHANGE_YEAR_SCRIPT_NODE = "ChangeYearScript";
	private static final String CHANGE_YEAR_SCRIPT_NODE_TEXT_FIELD = "textValue";
	private static final String ADDITIONAL_TEXT_PER_YEAR_TEXT_NODE = "AdditionalText";
	private static final String ADDITIONAL_TEXT_SCRIPT_NODE = "ChangeAdditionalTextScript";
	private static final String ADDITIONAL_TEXT_SCRIPT_NODE_TEXT_FIELD = "textValue";
	private int firstYear;
	private int lastYear;
	private int interval;
	private int animationDuration = 5;
	private String sceneTitle = "CO2-ppm shares";
	private String[] additionalTextPerYear;

	public VsAnimatedCartographicObjectsScene(String outputfile) {
		super(outputfile);
	}

	public VsAnimatedCartographicObjectsScene(int firstYear, int lastYear, int interval) {
		super();

		this.firstYear = firstYear;
		this.lastYear = lastYear;
		this.interval = interval;
	}

	public int getAnimationDuration() {
		return animationDuration;
	}

	public void setAnimationDuration(int animationDuration) {
		this.animationDuration = animationDuration;
	}

	public String getSceneTitle() {
		return sceneTitle;
	}

	public void setSceneTitle(String sceneTitle) {
		this.sceneTitle = sceneTitle;
	}

	public String[] getAdditionalTextPerYear() {
		return additionalTextPerYear;
	}

	public void setAdditionalTextPerYear(String[] additionalTextPerYear) {
		this.additionalTextPerYear = additionalTextPerYear;
	}

	public int getFirstYear() {
		return firstYear;
	}

	public void setFirstYear(int firstYear) {
		this.firstYear = firstYear;
	}

	public int getLastYear() {
		return lastYear;
	}

	public void setLastYear(int lastYear) {
		this.lastYear = lastYear;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	@Override
	protected void generateSceneContentX3D(boolean asX3DOM) {

		super.generateSceneContentX3D(asX3DOM);

		/*
		 * create global time sensor
		 */
		writeGlobalTimeSensor();

		int amountOfFeatures = cartographicSymbols.size();

		String keyValueForInterpolators = createKeyAttributeValue();
		
		/*
		 * HUD
		 */
		writeHUD(keyValueForInterpolators);

		for (int i = 0; i < amountOfFeatures; i++) {

			if (i % 1 == 0) {
				if (logger.isInfoEnabled()) {
					logger.info("Processed {} / {} features.", i, amountOfFeatures);
				}
			}

			T3dAttrSymbolInstance symbol = cartographicSymbols.get(i);

			// super.writeT3dAttrSymbolInstance(symbol);

			/*
			 * writeAnimations
			 * 
			 * use the scale transform node of each feature to alter the
			 * extrusion
			 */

			createInterpolatorsForColorAndExtrusion(keyValueForInterpolators, symbol);
		}

	}

	

	private void createInterpolatorsForColorAndExtrusion(String keyValueForInterpolators,
			T3dAttrSymbolInstance symbol) {
		/*
		 * for each year compute color and extrusion and create the
		 * interpolators keyValue string.
		 */
		String uniqueIdentifier = createUniqueIdentifierPerSymbol(symbol);
		String idScale = uniqueIdentifier + "_scale";
		String idMaterial = uniqueIdentifier + "_material";
		String idTranslation = uniqueIdentifier + "_translation";

		/*
		 * for each year --> extrudeTINs and create the coordinateString +
		 * extract the color value from the year-dependent color attribute
		 */

		StringBuffer scaleInterpolatorValueBuffer = new StringBuffer(110);
		StringBuffer colorInterpolatorValueBuffer = new StringBuffer(110);
		StringBuffer positionInterpolatorValueBuffer = new StringBuffer(110);

		for (int year = this.firstYear; year <= this.lastYear; year += interval) {
			/*
			 * INFO the corresponding color/extrusion mappers have set
			 * year-dependent attributes
			 */
			appendScalesForYear(symbol, scaleInterpolatorValueBuffer, year);

			appendColorStringForYear(symbol, colorInterpolatorValueBuffer, year);

			appendPositionStringForYear(symbol, positionInterpolatorValueBuffer, year);

		}
		String scaleInterpolatorValue = scaleInterpolatorValueBuffer.toString();
		String colorInterpolatorValue = colorInterpolatorValueBuffer.toString();
		String positionInterpolatorValue = positionInterpolatorValueBuffer.toString();

		String uniqueScaleInterpolatorName = uniqueIdentifier + "_scaleInterpolator";

		this.wl("<PositionInterpolator DEF=\"" + uniqueScaleInterpolatorName + "\" key=\"" + keyValueForInterpolators
				+ "\" keyValue=\"" + scaleInterpolatorValue + "\" />");

		String uniqueColorInterpolatorName = uniqueIdentifier + "_colorInterpolator";
		this.wl("<ColorInterpolator DEF=\"" + uniqueColorInterpolatorName + "\" key=\"" + keyValueForInterpolators
				+ "\" keyValue=\"" + colorInterpolatorValue + "\" />");

		String uniquePositionInterpolatorName = uniqueIdentifier + "_positionInterpolator";

		this.wl("<PositionInterpolator DEF=\"" + uniquePositionInterpolatorName + "\" key=\"" + keyValueForInterpolators
				+ "\" keyValue=\"" + positionInterpolatorValue + "\" />");

		/*
		 * ROUTE
		 */

		this.writeRoute(TIME_SENSOR_NAME, "fraction_changed", uniqueScaleInterpolatorName, "set_fraction");
		this.writeRoute(uniqueScaleInterpolatorName, "value_changed", idScale, "set_scale");

		this.writeRoute(TIME_SENSOR_NAME, "fraction_changed", uniqueColorInterpolatorName, "set_fraction");
		this.writeRoute(uniqueColorInterpolatorName, "value_changed", idMaterial, "diffuseColor");

		this.writeRoute(TIME_SENSOR_NAME, "fraction_changed", uniquePositionInterpolatorName, "set_fraction");
		this.writeRoute(uniquePositionInterpolatorName, "value_changed", idTranslation, "set_translation");

	}

	private StringBuffer appendPositionStringForYear(T3dAttrSymbolInstance symbol,
			StringBuffer positionInterpolatorValueBuffer, int year) {

		String extrusionHeightAttribute_year = NamesForAttributes.attributeNameForExtrusion + "_" + year;
		double yScaleValue = (double) symbol.getAttributeValue(extrusionHeightAttribute_year);

		resetPosition(symbol, yScaleValue);

		positionInterpolatorValueBuffer.append(createPositionString(symbol) + " ");

		return positionInterpolatorValueBuffer;

	}

	private String createPositionString(T3dAttrSymbolInstance symbol) {
		VgPoint position = symbol.getPosition();
		String positionString = this.decimalFormatter.format(position.getX()) + " "
				+ this.decimalFormatter.format(position.getY()) + " " + this.decimalFormatter.format(position.getZ());

		return positionString;
	}

	private void resetPosition(T3dAttrSymbolInstance symbol, double yScaleValue) {

		VgPoint position_sphere = symbol.getPosition();
		VgPoint position_wgs84 = Wgs84ToSphereCoordsTransform.sphereToWgs84(position_sphere, getRadius());

		/*
		 * now change the height (z-value)
		 */
		double currentHeight = position_wgs84.getZ();

		currentHeight = yScaleValue / 2;

		position_wgs84.setZ(currentHeight);

		/*
		 * re-transform to sphere coords
		 */
		position_sphere = Wgs84ToSphereCoordsTransform.wgs84ToSphere(position_wgs84, getRadius());

		symbol.setPosition(position_sphere);

	}

	private StringBuffer appendColorStringForYear(T3dAttrSymbolInstance symbol,
			StringBuffer colorInterpolatorValueBuffer, int year) {
		String colorAttribute_year = NamesForAttributes.attributeNameForColor + "_" + year;
		T3dColor color_year = (T3dColor) symbol.getAttributeValue(colorAttribute_year);

		colorInterpolatorValueBuffer.append(color_year.getRed()).append(" ").append(color_year.getGreen()).append(" ")
				.append(color_year.getBlue()).append(" ");

		return colorInterpolatorValueBuffer;

	}

	private StringBuffer appendScalesForYear(T3dAttrSymbolInstance symbol, StringBuffer scaleInterpolatorValueBuffer,
			int year) {

		String extrusionHeightAttribute_year = NamesForAttributes.attributeNameForExtrusion + "_" + year;
		double yScaleValue = (double) symbol.getAttributeValue(extrusionHeightAttribute_year);

		resetScale(symbol, yScaleValue);

		scaleInterpolatorValueBuffer.append(createScaleString(symbol) + " ");

		return scaleInterpolatorValueBuffer;

	}

	private String createScaleString(T3dAttrSymbolInstance symbol) {
		String scaleString = this.decimalFormatter.format(symbol.getxScale()) + " "
				+ this.decimalFormatter.format(symbol.getyScale()) + " "
				+ this.decimalFormatter.format(symbol.getzScale());

		return scaleString;
	}

	private void resetScale(T3dAttrSymbolInstance symbol, double yScaleValue) {
		symbol.setyScale(yScaleValue);
		symbol.setxScale(yScaleValue * 0.5);
		symbol.setzScale(yScaleValue * 0.5);
	}

	private String createKeyAttributeValue() {
		/*
		 * key attribute value for interpolators takes multiple values between
		 * 0.0 - 1.0 (animationStart - animationEnd). Represents the time steps
		 * for coordinate definition
		 */
		/*
		 * compute the number of animated steps from firstYear - lastYear using
		 * interval.
		 * 
		 * then divide 1.0 by that number --> these are the steps in between.
		 */
		int steps = 0;
		for (int i = this.firstYear; i <= this.lastYear; i = i + this.interval) {
			steps++;
		}

		double keyStep = 1.0 / steps;
		double currentKeyValue = 0.0;

		// example: String keyAttributeValue = "0.0 0.25 0.5 0.75 1.0";
		StringBuffer output = new StringBuffer(110);
		String keyAttributeValue = "";
		while (currentKeyValue < 1.0) {
			output.append(decimalFormatter.format(currentKeyValue)).append(" ");

			currentKeyValue += keyStep;
		}
		// append 1.0
		keyAttributeValue = output.append(decimalFormatter.format(1.0)).toString();

		return keyAttributeValue;
	}

	private void writeGlobalTimeSensor() {
		this.wl("<TimeSensor DEF='" + TIME_SENSOR_NAME + "' cycleInterval='" + this.animationDuration
				+ "' loop='true'/>");

	}

	private void writeHUD(String keyValueForInterpolators) {
		// use proximitry sensor and create ROUTE statements
		this.wl("<ProximitySensor DEF='" + PROX_SENSOR_NAME + "' center='0 0 0' size='1000 1000 1000'/>");

		this.wl("<Transform DEF='" + HUD_NAME + "' translation='0 0 0'>");
		this.wl("	<Transform DEF='HUDpos' translation='-2.7 1.7 -5.0'>");

		this.wl("	<Shape>");
		this.wl("		<Text string='\"" + this.sceneTitle + "\" \"" + this.firstYear + "-" + this.lastYear + "\"'>");
		this.wl("			<FontStyle size='.1' justify='\"MIDDLE\" \"MIDDLE\"'/>");
		this.wl("		</Text>");
		this.wl("		<Appearance>");
		this.wl("			<Material diffuseColor='0.8 0.8 0.8'/>");
		this.wl("		</Appearance>");
		this.wl("	</Shape>");

		this.wl("	</Transform>");
		
		/*
		 * changing textNode showing the current year
		 */
		this.wl("	<Transform translation='-2.7 1.3 -5.0'>");

		this.wl("	<Shape>");
		this.wl("		<Text DEF='" + CURRENT_YEAR_TEXT_NODE + "' string='" + this.firstYear + "'>");
		this.wl("			<FontStyle size='.1' justify='\"MIDDLE\" \"MIDDLE\"'/>");
		this.wl("		</Text>");
		this.wl("		<Appearance>");
		this.wl("			<Material diffuseColor='0.8 0.8 0.8'/>");
		this.wl("		</Appearance>");
		this.wl("	</Shape>");

		this.wl("	</Transform>");
		
		/*
		 * write script node for changing the text, which shows the current year!
		 */
		writeYearTextScriptNode(keyValueForInterpolators);
		
		/*
		 * changing textNode showing additional text from array
		 */
		if (this.additionalTextPerYear != null && this.additionalTextPerYear.length > 0) {
			this.wl("	<Transform translation='-2.7 0.9 -5.0'>");

			this.wl("	<Shape>");
			this.wl("		<Text DEF='" + ADDITIONAL_TEXT_PER_YEAR_TEXT_NODE + "' string='" + this.additionalTextPerYear[0] + "'>");
			this.wl("			<FontStyle size='.1' justify='\"MIDDLE\" \"MIDDLE\"'/>");
			this.wl("		</Text>");
			this.wl("		<Appearance>");
			this.wl("			<Material diffuseColor='0.8 0.8 0.8'/>");
			this.wl("		</Appearance>");
			this.wl("	</Shape>");

			this.wl("	</Transform>");
			
			/*
			 * write script node for changing the text, which shows the current year!
			 */
			writeAdditionalTextScriptNode(keyValueForInterpolators);
		}
		
		this.wl("</Transform>");

		/*
		 * ROUTE for HUD position and orientation
		 */
		writeRoute(PROX_SENSOR_NAME, "position_changed", HUD_NAME, "translation");
		writeRoute(PROX_SENSOR_NAME, "orientation_changed", HUD_NAME, "rotation");

	}
	
	private void writeAdditionalTextScriptNode(String keyValueForInterpolators) {
		/*
		 * use the value of the textNode as input for script node
		 * 
		 * then have an array of all possible values
		 * 
		 * identify which value is currently used --> use the subsequent array element as output
		 */
		
		this.wl("<Script DEF='" + ADDITIONAL_TEXT_SCRIPT_NODE+ "' directOutput='true'>");
		this.wl("	<field name='" + ADDITIONAL_TEXT_SCRIPT_NODE_TEXT_FIELD + "' accessType='inputOnly' type='SFInt32'/>");
		this.wl("	<field name='additionalTextPerYear' accessType='initializeOnly' type='SFNode'>");
		this.wl("		<Text USE='" + ADDITIONAL_TEXT_PER_YEAR_TEXT_NODE + "' />");
		this.wl("	</field>");
		
		this.wl("	<![CDATA[ecmascript:");
		
		/*
		 * initializeFunction
		 */
		this.wl("		function initialize() {");
		this.wl("			additionalTextPerYear.string = new MFString (" + this.additionalTextPerYear[0] + ");");
		this.wl("		}"); //end initialize function
		
		/*
		 * change text function
		 */
		this.wl("		function " + ADDITIONAL_TEXT_SCRIPT_NODE_TEXT_FIELD + "(index) {");
		
		this.wl("			switch (index) {");
		
		for(int i=0; i<this.additionalTextPerYear.length; i++){
			this.wl("				case " + i + ":");
			
			this.wl("					additionalTextPerYear.string = new MFString(" + this.additionalTextPerYear[i] + ");");
			
			this.wl("					break;");
		}
		/*
		 * default case
		 */
		this.wl("				default:");
		
		this.wl("					additionalTextPerYear.string = new MfString(" + this.additionalTextPerYear[0] + ");");

		this.wl("			}"); // end switch
		
		this.wl("		}"); // end change text function
		
		this.wl("	]]>");
		
		this.wl("</Script>");
		
		/*
		 * create an integer sequencer that will trigger the changeText event
		 */
		String uniqueTextChangerSequencerName = "AdditionalTextChangeSequencer";
		String textChangerSequencerValue = createAdditionalTextChangerSequencerValue();
		this.wl("<IntegerSequencer DEF=\"" + uniqueTextChangerSequencerName + "\" key=\"" + keyValueForInterpolators
				+ "\" keyValue=\"" + textChangerSequencerValue + "\" />");
		
		this.writeRoute(TIME_SENSOR_NAME, "fraction_changed", uniqueTextChangerSequencerName, "set_fraction");
		this.writeRoute(uniqueTextChangerSequencerName, "value_changed", ADDITIONAL_TEXT_SCRIPT_NODE, ADDITIONAL_TEXT_SCRIPT_NODE_TEXT_FIELD);
		
		
	}

	private String createAdditionalTextChangerSequencerValue() {
		/*
		 * use sequencer to hold array indices! which are then used to access the array
		 */
		
		StringBuffer buffer = new StringBuffer(110);

		for (int index = 0; index < this.additionalTextPerYear.length; index ++) {
			buffer.append(index + " ");
		}

		return buffer.toString();
	}

	private void writeYearTextScriptNode(String keyValueForInterpolators) {
		
		this.wl("<Script DEF='" + CHANGE_YEAR_SCRIPT_NODE+ "' directOutput='true'>");
		this.wl("	<field name='" + CHANGE_YEAR_SCRIPT_NODE_TEXT_FIELD+ "' accessType='inputOnly' type='SFInt32'/>");
		this.wl("	<field name='currentYearText' accessType='initializeOnly' type='SFNode'>");
		this.wl("		<Text USE='" + CURRENT_YEAR_TEXT_NODE + "' />");
		this.wl("	</field>");
		
		this.wl("	<![CDATA[ecmascript:");
		
		/*
		 * initializeFunction
		 */
		this.wl("		function initialize() {");
		this.wl("			currentYearText.string = new MFString (\"current year:\", \"" + this.firstYear + "\");");
		this.wl("		}"); //end initialize function
		
		/*
		 * change text function
		 */
		this.wl("		function " + CHANGE_YEAR_SCRIPT_NODE_TEXT_FIELD + "(value) {");
		
		this.wl("			var yearValue = \"current year: \" + value;");
		
		this.wl("			currentYearText.string = new MFString(yearValue);");
		
		this.wl("		}"); // end change text function
		
		this.wl("	]]>");
		
		this.wl("</Script>");
		
		/*
		 * create an integer sequencer that will trigger the changeText event
		 */
		String uniqueTextChangerSequencerName = "CurrentYearChangeSequencer";
		String textChangerSequencerValue = createCurrentYearChangerSequencerValue();
		this.wl("<IntegerSequencer DEF=\"" + uniqueTextChangerSequencerName + "\" key=\"" + keyValueForInterpolators
				+ "\" keyValue=\"" + textChangerSequencerValue + "\" />");
		
		this.writeRoute(TIME_SENSOR_NAME, "fraction_changed", uniqueTextChangerSequencerName, "set_fraction");
		this.writeRoute(uniqueTextChangerSequencerName, "value_changed", CHANGE_YEAR_SCRIPT_NODE, CHANGE_YEAR_SCRIPT_NODE_TEXT_FIELD);
		
	}

	/*
	 * just creates a string with placeholder values, which are actually never used.
	 * 
	 * But they are necessary for animating the text changer!
	 */
	private String createCurrentYearChangerSequencerValue() {
		StringBuffer buffer = new StringBuffer(110);
		
		int year = this.firstYear;
		
		for(int i=this.firstYear; i<= this.lastYear; i++){
			buffer.append(year + " ");
			year += this.interval;
		}
		
		return buffer.toString();
	}

	private void writeRoute(String fromNode, String fromField, String toNode, String toField) {

		this.wl("<ROUTE fromNode='" + fromNode + "' fromField='" + fromField + "' toNode='" + toNode + "' toField='"
				+ toField + "'/>");

	}

}
