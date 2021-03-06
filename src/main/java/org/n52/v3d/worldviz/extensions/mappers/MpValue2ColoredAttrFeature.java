/**
 * Copyright (C) 2015-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *  - Apache License, version 2.0
 *  - Apache Software License, version 1.0
 *  - GNU Lesser General Public License, version 3
 *  - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *  - Common Development and Distribution License (CDDL), version 1.0.
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.v3d.worldviz.extensions.mappers;

import java.util.List;

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.t3dutil.MpHypsometricColor;
import org.n52.v3d.triturus.t3dutil.MpSimpleHypsometricColor;
import org.n52.v3d.triturus.t3dutil.MpValue2Symbol;
import org.n52.v3d.triturus.t3dutil.T3dColor;
import org.n52.v3d.triturus.vgis.VgAttrFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapper that maps a color to an attributed feature. The color is interpolated
 * by using the {@link MpHypsometricColor}-mapper of the Triturus-framework
 * 
 * <br/>
 * By calling the {@link #setPalette(double[], T3dColor[])}-method you define,
 * how any attribute value shall be mapped to a color. It is advisable to call
 * this method before calling any <i>transform</i>-method to define a meaningful
 * mapping.
 * 
 * @author Christian Danowski
 * 
 */
public class MpValue2ColoredAttrFeature extends MpValue2Symbol {

	private MpSimpleHypsometricColor colorMapper;
	protected T3dColor neutralColor = new T3dColor(0.4f, 0.4f, 0.4f);
	private Logger logger = LoggerFactory.getLogger(getClass());
	protected String attrName;

	/**
	 * Constructor. Note that you should also call
	 * {@link #setPalette(double[], T3dColor[])} to specify the colorMapping.
	 */
	public MpValue2ColoredAttrFeature() {
		colorMapper = new MpSimpleHypsometricColor();
	}

	/**
	 * sets the hypsometric color palette. Here, the value <i>pValues[i]</i>
	 * will be mapped to the color <i>pColors[i]</i>. Any other value will be
	 * interpolated between colors in HSV color space (hue/saturation/value) if
	 * the 'interpolate'-parameter is set to TRUE.
	 * 
	 * @param pValues
	 * @param pColors
	 * @param interpolate
	 *            if set to TRUE, then any value will be linearly interpolated
	 * 
	 * @see MpSimpleHypsometricColor#setPalette(double[], T3dColor[], boolean)
	 */
	public void setPalette(double[] pValues, T3dColor[] pColors,
			boolean interpolate) {
		this.colorMapper.setPalette(pValues, pColors, interpolate);
	}

	/**
	 * This is the default color for any attribute's value, which cannot be
	 * parsed as a double value. This might happen, when the attribute's value
	 * simply is no real double value and is thus symbolized through any String
	 * like '-'. The default color is set to grey (0.4, 0.4, 0.4) in RGB-model
	 * 
	 * @return
	 */
	public T3dColor getNeutralColor() {
		return neutralColor;
	}

	/**
	 * Sets the neutralColor. This is the default color for any attribute's
	 * value, which cannot be parsed as a double value. This might happen, when
	 * the attribute's value simply is no real double value and is thus
	 * symbolized through any String like '-'. The default color is set to grey
	 * (0.4, 0.4, 0.4) in RGB-model
	 * 
	 * @param neutralColor
	 */
	public void setNeutralColor(T3dColor neutralColor) {
		this.neutralColor = neutralColor;
	}

	/**
	 * Creates a colored version of the attributed feature by interpolating a
	 * color based on the <i>value</i>-parameter. The color value is stored in
	 * an additional attribute of the {@link VgAttrFeature}-object: <br/>
	 * <br/>
	 * <b>Attribute name = the value stored at
	 * {@link NamesForAttributes#attributeNameForColor} <br/>
	 * Attribute value = a {@link T3dColor}-instance</b>
	 * 
	 * @param value
	 *            the value of the attribute which shall be used to color the
	 *            feature
	 * @param feature
	 *            the feature
	 * @return the feature-instance that holds the interpolated color for the
	 *         feature in a new attribute (see information above)
	 */
	private VgAttrFeature transform(double value, VgAttrFeature feature) {

		T3dColor interpolatedColor = this.colorMapper.transform(value);

		if (logger.isDebugEnabled())
			logger.debug("Attribute value: '{}';    interpolated color: '{}'",
					value, interpolatedColor);

		addColorAttribute(feature, interpolatedColor);

		return feature;

	}

	/**
	 * Adds the color attribute to the feature. The color value is stored in an
	 * additional attribute of the {@link VgAttrFeature}-object: <br/>
	 * <br/>
	 * <b>Attribute name = the value stored at
	 * {@link NamesForAttributes#attributeNameForColor} <br/>
	 * Attribute value = a {@link T3dColor}-instance</b>
	 * 
	 * @param feature
	 * @param interpolatedColor
	 */
	protected void addColorAttribute(VgAttrFeature feature,
			T3dColor interpolatedColor) {
		feature.addAttribute(NamesForAttributes.attributeNameForColor,
				T3dColor.class.toString());
		feature.setAttributeValue(NamesForAttributes.attributeNameForColor,
				interpolatedColor);
	}

	/**
	 * Creates a colored version of the attributed feature by interpolating a
	 * color based on the double-value of the <i>attrName</i>-parameter. Note
	 * that the attrName must correspond to one of the features attributes and
	 * it's value must be a double-value (floating-point number). <br/>
	 * <br/>
	 * The color value is stored in an additional attribute of the
	 * {@link VgAttrFeature}-object: <br/>
	 * <br/>
	 * <b>Attribute name = the value stored at
	 * {@link NamesForAttributes#attributeNameForColor} <br/>
	 * Attribute value = a {@link T3dColor}-instance</b>
	 * 
	 * @param attrName
	 *            the name of the attribute which shall be used to color the
	 *            feature
	 * @param feature
	 *            the feature
	 * @return the feature-instance that holds the interpolated color for the
	 *         feature in a new attribute (see information above)
	 */
	public VgAttrFeature transform(String attrName, VgAttrFeature feature) {

		this.attrName = attrName;
		
		// check if that object is either a double or a String value!
		Object attributeValue = feature.getAttributeValue(attrName);
		double doubleValue = 0;

		if (logger.isDebugEnabled())
			logger.debug(
					"Mapping the value '{}' of the attribute '{}' to a color.",
					attributeValue, attrName);

		if (attributeValue == null)
			throw new T3dException("The feature " + feature
					+ " does not contain any attribute like '" + attrName
					+ "'!");

		if (attributeValue instanceof String) {
			String attributeValueString = (String) attributeValue;
			try {
				doubleValue = Double.parseDouble(attributeValueString);
			} catch (Exception e) {

				if (logger.isWarnEnabled())
					logger.warn(
							"WARNING: The attributeValue '{}' of the attribute '{}' cannot be parsed as a double-value! Thus the defaultColorForNonDoubleValue will be used!! ({})",
							attributeValue, attrName, this.neutralColor);

				addColorAttribute(feature, neutralColor);

				return feature;
			}

		}

		else if (attributeValue instanceof Double
				|| attributeValue instanceof Float)
			doubleValue = (Double) attributeValue;

		return transform(doubleValue, feature);
	}

	/**
	 * Creates colored versions of all attributed features by interpolating a
	 * color based on the value of the <i>attrName</i>-parameter. Note that the
	 * attrName must correspond to one of each features attributes it's value
	 * must be a double-value (floating-point number).<br/>
	 * <br/>
	 * 
	 * The color value is stored in an additional attribute of the
	 * {@link VgAttrFeature}-object: <br/>
	 * <br/>
	 * <b>Attribute name = the value stored at
	 * {@link NamesForAttributes#attributeNameForColor} <br/>
	 * Attribute value = a {@link T3dColor}-instance</b>
	 * 
	 * @param attrName
	 *            the name of the attribute which shall be used to color each
	 *            feature
	 * @param features
	 *            the features
	 * @return the list of feature-instances that hold the interpolated color
	 *         for the feature in a new attribute (see information above)
	 */
	public List<VgAttrFeature> transform(String attrName,
			List<VgAttrFeature> features) {

		for (VgAttrFeature feature : features) {
			feature = transform(attrName, feature);
		}

		return features;
	}

	/**
	 * Colors the feature with the neutralColor.
	 * {@link MpValue2ColoredAttrFeature#setNeutralColor(T3dColor)}
	 * 
	 * The color value is stored in an additional attribute of the
	 * {@link VgAttrFeature}-object: <br/>
	 * <br/>
	 * <b>Attribute name = the value stored at
	 * {@link NamesForAttributes#attributeNameForColor} <br/>
	 * Attribute value = a {@link T3dColor}-instance</b>
	 *
	 * @param feature
	 *            the feature
	 * @return the feature-instance that holds the interpolated color for the
	 *         feature in a new attribute (see information above)
	 */
	public VgAttrFeature colorWithNeutralColor(VgAttrFeature feature) {
		addColorAttribute(feature, neutralColor);

		return feature;
	}

	@Override
	public String log() {
		// TODO Auto-generated method stub
		return null;
	}

}
