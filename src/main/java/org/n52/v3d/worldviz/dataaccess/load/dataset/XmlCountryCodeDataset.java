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
package org.n52.v3d.worldviz.dataaccess.load.dataset;

import java.io.IOException;
import java.util.List;

import noNamespace.DatasetDocument;
import noNamespace.DatasetDocument.Dataset.TableStructure.Property;

import org.n52.v3d.triturus.gisimplm.GmAttrFeature;
import org.n52.v3d.triturus.vgis.VgAttrFeature;
import org.n52.v3d.worldviz.dataaccess.load.dataset.helper.GeometryConverter;
import org.n52.v3d.worldviz.dataaccess.load.dataset.helper.JTSHelper;
import org.n52.v3d.worldviz.exception.ShapeGeometryNotFoundException;
import org.n52.v3d.worldviz.exception.UnknownCountryCodeCrsException;
import org.n52.v3d.worldviz.extensions.VgMultiPolygon;
import org.n52.v3d.worldviz.worldscene.helper.CountryBordersLODEnum;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Specialization of XmlDataset that is used for CountryCode-datasets (datasets
 * that contain information concerning a whole country and use a specific
 * country-code as coordinate reference system). The geometry of each country is
 * extracted from a shape file.
 * 
 * @author Christian Danowski
 * 
 */
public class XmlCountryCodeDataset extends AbstractXmlDataset {

	protected String joinHeader = "";

	// possible values for the CRS-element of the GeoReference-element of the
	// ENE-dataset
	private final static String CRS_ISO_ALPHA_2 = "ENE:CountriesWorldwide:iso3166-2";
	private final static String CRS_ISO_ALPHA_3 = "ENE:CountriesWorldwide:iso3166-3";

	// HEADERS of shape file, that contains the world borders
	// These headers are used to join the geometry to the feature

	// information for shapefile with detailed borders
	private final static String HEADER_FOR_ISO_ALPHA_2_DETAILED = "ISO2";
	private final static String HEADER_FOR_ISO_ALPHA_3_DETAILED = "ISO3";

	// information for shapefile with simplified borders (scale 1:50m and scale
	// 1:110m)
	private final static String HEADER_FOR_ISO_ALPHA_2_SIMPLIFIED_50m_AND_110m = "iso_a2";
	private final static String HEADER_FOR_ISO_ALPHA_3_SIMPLIFIED_50m_AND_110m = "iso_a3";

	public XmlCountryCodeDataset(DatasetDocument doc,
			CountryBordersLODEnum countryBordersLOD) throws Exception {
		super(doc, countryBordersLOD);

	}

	@Override
	protected void setGeometry(VgAttrFeature newFeature, Property property,
			String entryValue) throws UnknownCountryCodeCrsException,
			IOException, ShapeGeometryNotFoundException {

		if (logger.isDebugEnabled())
			logger.debug(
					"Creating a MultiPolygon-geometry from the feature property '{}' with value '{}'.",
					property.getTitle().getStringValue(), entryValue);

		determineJoinHeader(property);

		List<SimpleFeature> jtsSimpleFeatures = this.shapeReader
				.getSimpleFeatureCollection();

		MultiPolygon jtsMultiPolygon = JTSHelper.getMultiPolygonForAttribute(
				joinHeader, entryValue, jtsSimpleFeatures);

		VgMultiPolygon triturusMultiPolygon = null;

		if (jtsMultiPolygon != null) {
			triturusMultiPolygon = GeometryConverter
					.convertJtsMultiPolygon2TriturusMultiPolygon(jtsMultiPolygon);
		}

		// if triturusMultiPolygon is still =null, then we catch that in super
		// class AbstractXmlDataset (methods processProperties() und
		// generateFeatures())
		((GmAttrFeature) newFeature).setGeometry(triturusMultiPolygon);
	}

	private void determineJoinHeader(Property property)
			throws UnknownCountryCodeCrsException {
		String crs = property.getGeoReference().getCRS().toString();

		if (crs.equals(CRS_ISO_ALPHA_2))
			switch (this.countryBordersLOD) {
			case DETAILED:
				joinHeader = HEADER_FOR_ISO_ALPHA_2_DETAILED;
				break;

			case SIMPLIFIED_50m:
				joinHeader = HEADER_FOR_ISO_ALPHA_2_SIMPLIFIED_50m_AND_110m;
				break;

			case SIMPLIFIED_110m:
				joinHeader = HEADER_FOR_ISO_ALPHA_2_SIMPLIFIED_50m_AND_110m;
				break;

			default:
				joinHeader = HEADER_FOR_ISO_ALPHA_2_DETAILED;
				break;
			}

		else if (crs.equals(CRS_ISO_ALPHA_3))
			switch (this.countryBordersLOD) {
			case DETAILED:
				joinHeader = HEADER_FOR_ISO_ALPHA_3_DETAILED;
				break;

			case SIMPLIFIED_50m:
				joinHeader = HEADER_FOR_ISO_ALPHA_3_SIMPLIFIED_50m_AND_110m;
				break;

			case SIMPLIFIED_110m:
				joinHeader = HEADER_FOR_ISO_ALPHA_3_SIMPLIFIED_50m_AND_110m;
				break;

			default:
				joinHeader = HEADER_FOR_ISO_ALPHA_3_DETAILED;
				break;
			}

		else {
			// no valid country code value value be found
			throw new UnknownCountryCodeCrsException(
					"The GeoReference-element of the ENE-CountryCode-dataset contains the "
							+ "coordinate reference system '"
							+ crs
							+ "', that is unknown to the ENE processing logic and cannot be interpreted!");
		}
	}

}
