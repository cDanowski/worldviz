package org.n52.v3d.worldviz.extensions.mappers;

import org.n52.v3d.triturus.t3dutil.T3dColor;
import org.n52.v3d.triturus.vgis.VgAttrFeature;

/**
 * Expects input feature to have multiple year attributes with name=year and
 * value=(thematic value for that year).
 * 
 * The mapper will then iterate over all year attributes and create a new
 * year-specific attribute holding the computed color. Per convention the
 * standard attributeName for the color attribute
 * ({@link NamesForAttributes#attributeNameForColor}) will be extended by
 * "_(year)", where (year) is replaced by the actual year.
 * 
 * @author Christian Danowski
 *
 */
public class MpValue2ColoredAttrFeature_forAnimation extends MpValue2ColoredAttrFeature {

	public MpValue2ColoredAttrFeature_forAnimation() {
		super();
	}

	/**
	 * Adds the color attribute to the feature. The color value is stored in an
	 * additional attribute of the {@link VgAttrFeature}-object: <br/>
	 * <br/>
	 * <b>Attribute name = the value stored at
	 * {@link NamesForAttributes#attributeNameForColor} extended by "_(year)",
	 * where (year) is replaced by the actual year<br/>
	 * Attribute value = a {@link T3dColor}-instance</b>
	 * 
	 * @param feature
	 * @param interpolatedColor
	 */
	@Override
	protected void addColorAttribute(VgAttrFeature feature, T3dColor interpolatedColor) {
		String attributeName = NamesForAttributes.attributeNameForColor + "_" + this.attrName;
		feature.addAttribute(attributeName, T3dColor.class.toString());
		feature.setAttributeValue(attributeName, interpolatedColor);
	}

	public VgAttrFeature colorWithNeutralColor(String attrName, VgAttrFeature feature) {
		this.attrName = attrName;
		
		addColorAttribute(feature, this.neutralColor);

		return feature;
	}

}
