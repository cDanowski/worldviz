package org.n52.v3d.worldviz.extensions.mappers;

import org.n52.v3d.triturus.vgis.VgAttrFeature;

/**
 * Expects input feature to have multiple year attributes with name=year and
 * value=(thematic value for that year).
 * 
 * The mapper will then iterate over all year attributes and create a new
 * year-specific attribute holding the computed extrusion value. Per convention
 * the standard attributeName for the extrusion attribute
 * ({@link NamesForAttributes#attributeNameForExtrusion}) will be extended by
 * "_(year)", where (year) is replaced by the actual year.
 * 
 * @author Christian Danowski
 *
 */
public class MpValue2ExtrudedAttrFeature_forAnimation extends MpValue2ExtrudedAttrFeature {

	public MpValue2ExtrudedAttrFeature_forAnimation() {
		super();
	}

	/**
	 * Adds the extrusion height attribute to the feature. The extrusion height
	 * value is stored in an additional attribute of the {@link VgAttrFeature}
	 * -object: <br/>
	 * <br/>
	 * <b>Attribute name = the value stored at
	 * {@link NamesForAttributes#attributeNameForExtrusion} extended by
	 * "_(year)", where (year) is replaced by the actual year <br/>
	 * Attribute value = a double value representing the height of the
	 * object</b>
	 * 
	 * @param feature
	 * @param extrusionHeight
	 */
	@Override
	protected void addExtrusionAttribute(VgAttrFeature feature, double extrusionHeight) {
		String attributeName = NamesForAttributes.attributeNameForExtrusion + "_" + this.attrName;
		feature.addAttribute(attributeName, Double.class.toString());
		feature.setAttributeValue(attributeName, extrusionHeight);
	}

	public VgAttrFeature extrudeWithNeutralHeight(String attrName, VgAttrFeature feature) {
		this.attrName = attrName;
		this.addExtrusionAttribute(feature, this.neutralExtrusionHeight);
		return feature;
	}

}
