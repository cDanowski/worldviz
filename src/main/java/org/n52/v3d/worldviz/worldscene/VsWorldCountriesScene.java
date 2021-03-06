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
package org.n52.v3d.worldviz.worldscene;

import java.util.ArrayList;
import java.util.List;

import org.n52.v3d.triturus.core.T3dException;
import org.n52.v3d.triturus.core.T3dNotYetImplException;
import org.n52.v3d.triturus.gisimplm.GmPoint;
import org.n52.v3d.triturus.gisimplm.GmSimpleTINGeometry;
import org.n52.v3d.triturus.t3dutil.T3dColor;
import org.n52.v3d.triturus.t3dutil.T3dVector;
import org.n52.v3d.triturus.vgis.VgAttrFeature;
import org.n52.v3d.triturus.vgis.VgGeomObject;
import org.n52.v3d.triturus.vgis.VgIndexedTIN;
import org.n52.v3d.triturus.vgis.VgPoint;
import org.n52.v3d.triturus.vgis.VgTIN;
import org.n52.v3d.worldviz.extensions.VgLinearRing;
import org.n52.v3d.worldviz.extensions.VgMultiPolygon;
import org.n52.v3d.worldviz.extensions.VgPolygon;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ColoredAttrFeature;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ExtrudedAttrFeature;
import org.n52.v3d.worldviz.extensions.mappers.NamesForAttributes;
import org.n52.v3d.worldviz.triangulation.PolygonTriangulator;
import org.n52.v3d.worldviz.worldscene.helper.NormalVectorHelper;

/**
 * This class is meant to produce a scene description in which the world's
 * countries are colored and/or extruded corresponding to a specific thematic
 * attribute. <br/>
 * <br/>
 * Only flat countries that are placed in the XY-plane (surface area in XY,
 * possible height in Z) are accepted!
 * 
 * @author Christian Danowski
 * 
 */
public class VsWorldCountriesScene extends VsAbstractWorldScene {

	protected static final String _COORDINATE_SIDE_WALLS = "_coordinate_sideWalls_";

	protected static final String _MATERIAL_SIDE_WALLS = "_material_sideWalls_";
	protected static final String _MATERIAL_BORDER = "_material_border_";
	protected static final String _COORDINATE_SURFACE = "_coordinate_surface_";
	protected static final String _MATERIAL_SURFACE = "_material_surface_";
	protected static final String _COORDINATE_BORDER = "_coordinate_border_";
	protected static final String COUNTRY_CODE = "Country code";
	protected final double offsetForBorders = 0.001;
	// holds the geometry of the world's countries
	protected List<VgAttrFeature> worldCountries = new ArrayList<VgAttrFeature>();
	private T3dColor defaultCountryBordersColor = new T3dColor(0.f, 0.f, 0.f);

	// choose if the borders of the world's countries shall be drawn
	private boolean drawBorders = true;

	private boolean extrudeCountries = false;

	private final double defaultExtrusionHeight = 0;
	protected List<VgIndexedTIN> currentVgTINs;
	private VgAttrFeature currentFeature;

	/**
	 * Constructor that takes a filePath to which the generated scene
	 * description will be saved.
	 * 
	 * @param filePath
	 */
	public VsWorldCountriesScene(String filePath) {
		super(filePath);

		this.worldCountries.clear();
	}
	
	public VsWorldCountriesScene() {
		super();

		this.worldCountries.clear();
	}

	/**
	 * This parameter defines whether the border of each world country should be
	 * included in the scene description (as a single outline).
	 * 
	 * <br/>
	 * <br/>
	 * The standard value is set to <b>true</b>
	 * 
	 * @return true, if the borders shall be drawn.
	 */
	public boolean isDrawBorders() {
		return drawBorders;
	}

	/**
	 * Choose whether the borders of the world's countries shall be drawn
	 * (included into the generated scene description).
	 * 
	 * @param drawBorders
	 *            true, if the borders shall be drawn
	 */
	public void setDrawBorders(boolean drawBorders) {
		this.drawBorders = drawBorders;
	}

	/**
	 * The default color for visualizing the borders of a country. The default
	 * value is set to black (0, 0, 0) in RGB-model
	 * 
	 * @return
	 */
	public T3dColor getDefaultCountryBordersColor() {
		return defaultCountryBordersColor;
	}

	/**
	 * Sets the color for visualizing the borders of a country. The default
	 * value is set to black (0, 0, 0) in RGB-model
	 * 
	 * @param defaultCountryBordersColor
	 */
	public void setDefaultCountryBordersColor(
			T3dColor defaultCountryBordersColor) {
		this.defaultCountryBordersColor = defaultCountryBordersColor;
	}

	/**
	 * Returns whether the geometry of each country shall be extruded or not. <br/>
	 * <br/>
	 * <b>Important note:</b> If the geometries shall be extruded then the
	 * instances of {@link VgAttrFeature}-objects need to have an extrusion
	 * height value as attribute. This can be achieved via the mapper
	 * {@link MpValue2ExtrudedAttrFeature}.
	 * 
	 * @return
	 */
	public boolean isExtrudeCountries() {
		return extrudeCountries;
	}

	/**
	 * Sets whether the geometry of each country shall be extruded or not. <br/>
	 * <br/>
	 * <b>Important note:</b> If the geometries shall be extruded then the
	 * instances of {@link VgAttrFeature}-objects need to have an extrusion
	 * height value as attribute. This can be achieved via the mapper
	 * {@link MpValue2ExtrudedAttrFeature}.
	 * 
	 * @param extrudeCountries
	 *            TRUE, if the geometries shall be extruded
	 */
	public void setExtrudeCountries(boolean extrudeCountries) {
		this.extrudeCountries = extrudeCountries;
	}

	/**
	 * Simply adds a colored/extruded world country to the scene. Note, that you
	 * are responsible for the consistency of the scene. This method does not
	 * check any duplicates. The attributed feature must be of geometry type
	 * {@link VgPolygon} or {@link VgMultiPolygon}! The color/extrusion of the
	 * feature has to be set via the mapper {@link MpValue2ExtrudedAttrFeature}
	 * {@link MpValue2ColoredAttrFeature}
	 * 
	 * @param worldFeature
	 *            a world country
	 */
	public void addWorldCountry(VgAttrFeature worldFeature) {

		// TODO maybe check whether necessary attributes for color and extrusion
		// are set.

		VgGeomObject geometry = worldFeature.getGeometry();
		if ((geometry instanceof VgMultiPolygon)
				|| (geometry instanceof VgPolygon))
			this.worldCountries.add(worldFeature);

		else
			throw new T3dException(
					"You can only add features of geometry type 'VgPolygon' or 'VgMultiPolygon' "
							+ "to a world countries scene! Instead you tried to add "
							+ "a feature of geometry type: '"
							+ geometry.getClass().toString() + "'!");

	}

	@Override
	protected void generateSceneContentKML() {
		// TODO Auto-generated method stub
		throw new T3dNotYetImplException(
				"This method is not yet implemented! Please use format 'X3D' instead!");
	}

	@Override
	protected void generateSceneContentVRML() {
		// TODO Auto-generated method stub
		throw new T3dNotYetImplException(
				"This method is not yet implemented! Please use format 'X3D' instead!");
	}

	@Override
	protected void generateSceneContentX3D(boolean asX3DOM) {

		// write all scene objects (coloredWorldCountries) into X3D
		int amountOfFeatures = worldCountries.size();

		for (int i = 0; i < amountOfFeatures; i++) {

			if (i % 30 == 0) {
				if (logger.isInfoEnabled()) {
					logger.info("Processed {} / {} features.", i,
							amountOfFeatures);
				}
			}

			VgAttrFeature colAttrFeature = worldCountries.get(i);

			writeAttrFeature(colAttrFeature);
		}

	}

	/**
	 * Takes a {@link VgAttrFeature} and writes it to an X3D-File by delegating
	 * the writing process to another method depending on geometry type. The
	 * color/extrusion information is encoded in a specific attribute of the
	 * feature.
	 * 
	 * @param attrFeature
	 */
	protected void writeAttrFeature(VgAttrFeature attrFeature) {
		
		this.currentFeature = attrFeature;

		/*
		 * Only geometries of type VgPolygon or VgMultiPolygon are allowed. Thus
		 * we only need to generate such content.
		 */

		VgGeomObject geometry = attrFeature.getGeometry();
		Object colorAttributeValue = attrFeature
				.getAttributeValue(NamesForAttributes.attributeNameForColor);

		Object extrusionAttributeValue = this.defaultExtrusionHeight;

		if (this.extrudeCountries)
			extrusionAttributeValue = attrFeature
					.getAttributeValue(NamesForAttributes.attributeNameForExtrusion);

		if (colorAttributeValue instanceof T3dColor) {

			T3dColor color = (T3dColor) colorAttributeValue;

			double extrusionHeight = this.defaultExtrusionHeight;

			if (extrusionAttributeValue instanceof Double)
				extrusionHeight = (Double) extrusionAttributeValue;

			if (geometry instanceof VgMultiPolygon)
				writeMultiPolygon((VgMultiPolygon) geometry, color,
						extrusionHeight);
			else if (geometry instanceof VgPolygon)
				writePolygon((VgPolygon) geometry, color, extrusionHeight);
			else
				throw new T3dException(
						"Tried to write a feature of geometry type "
								+ geometry.getClass().toString()
								+ "'! Only types 'VgPolygon' or 'VgMultiPolygon' "
								+ "are allowed to use though!");

		} else
			throw new T3dException(
					"The feature has no valid attribute entry for the color!"
							+ "Attribute name must be'"
							+ NamesForAttributes.attributeNameForColor + "'!\n"
							+ "Attribute value must be of type '"
							+ T3dColor.class.toString() + "'!");

	}

	/**
	 * Writes a geometry of type {@link VgPolygon} to a X3D-File by dividing the
	 * polygon into {@link VgIndexedTIN}-objects.
	 * 
	 * @param polygon
	 *            an instance of {@link VgPolygon}
	 * @param color
	 *            an instance of {@link T3dColor} to color the geometry in the
	 *            created X3D-File
	 * @param extrusionHeight
	 *            the value which shall be used to offset/extrude the geometry
	 */
	private void writePolygon(VgPolygon polygon, T3dColor color,
			double extrusionHeight) {

		List<VgIndexedTIN> vgTINs = new ArrayList<VgIndexedTIN>();

		addTINsForPolygon(vgTINs, polygon);
		
		this.currentVgTINs = vgTINs;

		if (this.extrudeCountries) {

			// countries must be extruded

			List<VgIndexedTIN> extrusionTINs = extrudeTINs(vgTINs,
					extrusionHeight);

			writeSurfaceTINs(color, vgTINs, extrusionTINs);

			writeSideWalls(polygon, color, extrusionHeight, 0);

		}

		else {

			// non extruded
			writeSurfaceTINs(color, vgTINs);

		}

		// write borders of polygon
		if (drawBorders)
			writeBorderForPolygon(polygon, extrusionHeight, 0);
	}

	/**
	 * Writes a geometry of type {@link VgMultiPolygon} to a X3D-File by
	 * dividing the multiPolygon into {@link VgIndexedTIN}-objects.
	 * 
	 * @param multiPolygon
	 *            an instance of {@link VgPolygon}
	 * @param color
	 *            an instance of {@link T3dColor} to color the geometry in the
	 *            created X3D-File
	 * @param extrusionHeight
	 *            the value which shall be used to offset/extrude the geometry
	 */
	private void writeMultiPolygon(VgMultiPolygon multiPolygon, T3dColor color,
			double extrusionHeight) {

		List<VgIndexedTIN> vgTINs = calculateTriangulation(multiPolygon);
		
		this.currentVgTINs = vgTINs;

		if (this.isExtrudeCountries()) {

			List<VgIndexedTIN> extrusionTINs = extrudeTINs(vgTINs,
					extrusionHeight);

			writeSurfaceTINs(color, vgTINs, extrusionTINs);

			writeSideWalls(multiPolygon, color, extrusionHeight);
		}

		else {

			writeSurfaceTINs(color, vgTINs);

		}

		// write borders of each multiPolygon
		if (drawBorders)
			writeBordersForMultiPolygon(multiPolygon, extrusionHeight);

	}

	/**
	 * Writes the so called 'side walls' of an extruded polygon as TriangleSet.
	 * 
	 * @param polygon
	 * @param color
	 * @param extrusionHeight
	 * @param index
	 *            if this method has been called from a MultiPolygon loop, then
	 *            this index will indicate the index of the polygon, which is
	 *            important to create a unique DEF name; if it is called once
	 *            for a polygon it should be set to 0!
	 */
	private void writeSideWalls(VgPolygon polygon, T3dColor color,
			double extrusionHeight, int index) {
		Object countryCode = this.currentFeature.getAttributeValue(COUNTRY_CODE);
		
		wl("      <Shape>");
		wl("        <Appearance>");
		
		String uniqueCountryMaterialName_sideWalls = countryCode + _MATERIAL_SIDE_WALLS + index;

		wl("          <Material DEF=\""+ uniqueCountryMaterialName_sideWalls + "\" diffuseColor=\"" + color.getRed() + " "
				+ color.getGreen() + " " + color.getBlue() + "\" />");
		wl("        </Appearance>");

		// TODO better also as IndexedFaceSet? -> for X3D TriangleSet might be
		// of better performance; -> for X3DOM: no support for TriangleSet

		wl("<TriangleSet ccw=\"true\" solid=\"true\" colorPerVertex=\"false\" normalPerVertex=\"false\">");
		
		String uniqueCountryCoordinateNamePerPolygon_sideWalls = countryCode + _COORDINATE_SIDE_WALLS + index;
		String coordinateString = createCoordinateStringForSideWalls(polygon, extrusionHeight);
		
		wl("<Coordinate DEF=\"" + uniqueCountryCoordinateNamePerPolygon_sideWalls + "\" point=\"" + coordinateString+ "\"/>");

		wl("</TriangleSet>");
		wl("      </Shape>");

	}

	private String createCoordinateStringForSideWalls(VgPolygon polygon, double extrusionHeight) {
		// first process the outer boundary, then all holes
		VgLinearRing outerBoundary = polygon.getOuterBoundary();

		int numberOfHoles = polygon.getNumberOfHoles();

		StringBuffer buffer = new StringBuffer(110);
		// process outer boundary
		appendCoordinateStringForLinearRing(outerBoundary, extrusionHeight, buffer);

		// process holes
		for (int i = 0; i < numberOfHoles; i++) {
			appendCoordinateStringForLinearRing(polygon.getHole(i), extrusionHeight, buffer);
		}
		
		return buffer.toString();
	}

	private StringBuffer appendCoordinateStringForLinearRing(VgLinearRing linearRing, double extrusionHeight, StringBuffer buffer) {
		
		// for each point of the polygon (except the last)
				// calculate extruded version of this and the next point!
				// mesh bottom and top (extruded) points ccw

				int lastIndex = linearRing.getNumberOfVertices() - 1;

				for (int i = 0; i < lastIndex; i++) {

					// calculate offset versions
					VgPoint bottomPoint = calculateOffsetBorderPoint(linearRing, i,
							-offsetForBorders);
					VgPoint extrudedPoint = calculateOffsetBorderPoint(linearRing, i,
							extrusionHeight);

					VgPoint bottomNextPoint = calculateOffsetBorderPoint(linearRing,
							i + 1, -offsetForBorders);
					VgPoint extrudedNextPoint = calculateOffsetBorderPoint(linearRing,
							i + 1, extrusionHeight);

					// write mesh of points as two triangles

					// first triangle
					appendCoordinateStringForTriangle(bottomPoint, extrudedPoint, bottomNextPoint, buffer);

					// second triangle
					appendCoordinateStringForTriangle(bottomNextPoint, extrudedPoint, extrudedNextPoint, buffer);

				}

				// process last point like above!

				VgPoint lastBottomPoint = calculateOffsetBorderPoint(linearRing,
						lastIndex, -offsetForBorders);
				VgPoint lastExtrudedPoint = calculateOffsetBorderPoint(linearRing,
						lastIndex, extrusionHeight);

				VgPoint firstBottomPoint = calculateOffsetBorderPoint(linearRing, 0,
						-offsetForBorders);
				VgPoint firstExtrudedPoint = calculateOffsetBorderPoint(linearRing, 0,
						extrusionHeight);

				appendCoordinateStringForTriangle(lastBottomPoint, firstBottomPoint, lastExtrudedPoint, buffer);

				appendCoordinateStringForTriangle(firstBottomPoint, lastExtrudedPoint,
						firstExtrudedPoint, buffer);
				
				return buffer;
	}

	private StringBuffer appendCoordinateStringForTriangle(VgPoint p1, VgPoint p2, VgPoint p3, StringBuffer buffer) {
		appendPoint(p1, buffer).append(", ");
		appendPoint(p2, buffer).append(", ");
		appendPoint(p3, buffer).append(", ");
		
		return buffer;
	}

	/**
	 * Method called by {@link #writeSideWalls(VgPolygon, T3dColor, double)}
	 * that for each point of the linearRing-geometry first calculates the
	 * extrusion and then writes the triangular mesh of all neighboring points
	 * counterclockwise.
	 * 
	 * @param linearRing
	 * @param extrusionHeight
	 */
	private void writeSideWalls(VgLinearRing linearRing, double extrusionHeight) {

		// for each point of the polygon (except the last)
		// calculate extruded version of this and the next point!
		// mesh bottom and top (extruded) points ccw

		int lastIndex = linearRing.getNumberOfVertices() - 1;

		for (int i = 0; i < lastIndex; i++) {

			// calculate offset versions
			VgPoint bottomPoint = calculateOffsetBorderPoint(linearRing, i,
					-offsetForBorders);
			VgPoint extrudedPoint = calculateOffsetBorderPoint(linearRing, i,
					extrusionHeight);

			VgPoint bottomNextPoint = calculateOffsetBorderPoint(linearRing,
					i + 1, -offsetForBorders);
			VgPoint extrudedNextPoint = calculateOffsetBorderPoint(linearRing,
					i + 1, extrusionHeight);

			// write mesh of points as two triangles

			// first triangle
			writeTriangleCCW(bottomPoint, extrudedPoint, bottomNextPoint);

			wl();

			// second triangle
			writeTriangleCCW(bottomNextPoint, extrudedPoint, extrudedNextPoint);

		}

		// process last point like above!

		VgPoint lastBottomPoint = calculateOffsetBorderPoint(linearRing,
				lastIndex, -offsetForBorders);
		VgPoint lastExtrudedPoint = calculateOffsetBorderPoint(linearRing,
				lastIndex, extrusionHeight);

		VgPoint firstBottomPoint = calculateOffsetBorderPoint(linearRing, 0,
				-offsetForBorders);
		VgPoint firstExtrudedPoint = calculateOffsetBorderPoint(linearRing, 0,
				extrusionHeight);

		writeTriangleCCW(lastBottomPoint, firstBottomPoint, lastExtrudedPoint);

		wl();

		writeTriangleCCW(firstBottomPoint, lastExtrudedPoint,
				firstExtrudedPoint);

	}

	/**
	 * Writes the side walls of a multipolygon by delegating each polygon to
	 * method {@link #writeSideWalls(VgPolygon, T3dColor, double)}.
	 * 
	 * @param multiPolygon
	 * @param color
	 * @param extrusionHeight
	 */
	private void writeSideWalls(VgMultiPolygon multiPolygon, T3dColor color,
			double extrusionHeight) {

		int numberOfPolygons = multiPolygon.getNumberOfGeometries();

		for (int i = 0; i < numberOfPolygons; i++) {

			VgPolygon polygon = (VgPolygon) multiPolygon.getGeometry(i);

			writeSideWalls(polygon, color, extrusionHeight, i);
		}

	}

	/**
	 * Writes a triangle as string "p1, p2, p3". The point syntax can bee seen
	 * at method {@link #writePoint(VgPoint)}.
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	private void writeTriangleCCW(VgPoint p1, VgPoint p2, VgPoint p3) {
		writePoint(p1);
		w(", ");
		writePoint(p2);
		w(", ");
		writePoint(p3);
		w(", ");
	}

	/**
	 * Writes a point as string "x z -y ". <br/>
	 * This might be unusual. But the coordinate system of 3D-viewers often has
	 * a different layout than a geo coordinate system. Hence the geo
	 * coordinates must be mapped properly to the coordinate system of the
	 * viewer by switching z and y axis and make y-values negative.
	 * 
	 * @param point
	 *            a point with GEO(!!!) coordinates
	 */
	private void writePoint(VgPoint point) {
		w("" + this.decimalFormatter.format(point.getX()) + " "
				+ this.decimalFormatter.format(point.getZ()) + " "
				+ this.decimalFormatter.format(-point.getY()) + " ");
	}

	/**
	 * creates an offset version of all {@link VgIndexedTIN}-objects in
	 * height-direction (usually z coordinate of a geo object).
	 * 
	 * @param vgTINs
	 * @param extrusionHeight
	 *            the height value according to which the offset will be
	 *            calculated.
	 * @return a list of the same {@link VgIndexedTIN}-objects with an
	 *         additional offset in height direction.
	 */
	protected List<VgIndexedTIN> extrudeTINs(List<VgIndexedTIN> vgTINs,
			double extrusionHeight) {

		// normal vector determines the height direction
		T3dVector normalVector = NormalVectorHelper
				.calculateNormalVectorFromThreePoints(vgTINs.get(0));

		List<VgIndexedTIN> extrusionTINs = new ArrayList<VgIndexedTIN>(
				vgTINs.size());

		for (VgIndexedTIN vgIndexedTIN : vgTINs) {

			// set up new TIN-object
			GmSimpleTINGeometry extrusionTIN = new GmSimpleTINGeometry(
					vgIndexedTIN.numberOfPoints(),
					vgIndexedTIN.numberOfTriangles());

			extrusionTIN.setSRS(vgIndexedTIN.getSRS());

			// add offset/extruded points
			for (int i = 0; i < vgIndexedTIN.numberOfPoints(); i++) {
				VgPoint point = vgIndexedTIN.getPoint(i);

				// use extrusionHeight to offset the new point!
				VgPoint extrusionPoint = extrudePoint(point, normalVector,
						extrusionHeight);

				extrusionTIN.setPoint(i, extrusionPoint);
			}

			// add triangles
			for (int t = 0; t < vgIndexedTIN.numberOfTriangles(); t++) {
				int[] triangleVertexIndices = vgIndexedTIN
						.getTriangleVertexIndices(t);

				extrusionTIN.setTriangle(t, triangleVertexIndices[0],
						triangleVertexIndices[1], triangleVertexIndices[2]);
			}

			extrusionTINs.add(extrusionTIN);
		}
		return extrusionTINs;
	}

	/**
	 * Helper method to calculate the offset of a point according to the
	 * extrusion height
	 * 
	 * @param point
	 *            the point to be offset
	 * @param normalVector
	 *            the normal vector pointing to the height direction
	 * @param extrusionHeight
	 *            the value for the offset
	 * @return the offset/extruded point
	 */
	protected VgPoint extrudePoint(VgPoint point, T3dVector normalVector,
			double extrusionHeight) {

		// use extrusionHeight to offset the new point!
		VgPoint extrusionPoint = addOffsetInNormalDirection(normalVector,
				point, 0, extrusionHeight);

		return extrusionPoint;
	}

	/**
	 * Writes the borders for each polygon of the multiPolygon by calling
	 * {@link VsWorldCountriesScene#writeBorderForPolygon(VgPolygon)}
	 * 
	 * @param multiPolygon
	 * @param extrusionHeight
	 *            the value which shall be used to offset/extrude the geometry
	 *            (here needed to draw the border on top of the extruded
	 *            geometry!)
	 */
	private void writeBordersForMultiPolygon(VgMultiPolygon multiPolygon,
			double extrusionHeight) {
		for (int k = 0; k < multiPolygon.getNumberOfGeometries(); k++) {

			VgPolygon polygon = (VgPolygon) multiPolygon.getGeometry(k);

			writeBorderForPolygon(polygon, extrusionHeight, k);
		}
	}

	/**
	 * Generates X3D-code to display the border of the polygon as an
	 * IndexedLineSet. The color of the line is taken from the class-attribute
	 * {@link VsWorldCountriesScene#defaultCountryBordersColor}
	 * 
	 * @param polygon
	 * @param extrusionHeight
	 *            the value which shall be used to offset/extrude the geometry
	 *            (here needed to draw the border on top of the extruded
	 *            geometry!)
	 * @param index
	 *            if this method has been called from a MultiPolygon loop, then
	 *            this index will indicate the index of the polygon, which is
	 *            important to create a unique DEF name; if it is called once
	 *            for a polygon it should be set to 0!
	 */
	private void writeBorderForPolygon(VgPolygon polygon, double extrusionHeight, int index) {

		Object countryCode = this.currentFeature.getAttributeValue(COUNTRY_CODE);
		
		wl("      <Shape>");
		wl("        <Appearance>");
		
		String uniqueCountryMaterialNamePerPolygon_border = countryCode + _MATERIAL_BORDER + index;
		
		wl("          <Material DEF=\"" + uniqueCountryMaterialNamePerPolygon_border + "\" diffuseColor=\""
				+ defaultCountryBordersColor.getRed() + " "
				+ defaultCountryBordersColor.getGreen() + " "
				+ defaultCountryBordersColor.getBlue() + "\"></Material>");
		wl("        </Appearance>");

		w("<IndexedLineSet coordIndex=\"");

		for (int l = 0; l < polygon.getOuterBoundary().getNumberOfVertices(); l++) {
			w("" + l + " ");
		}

		wl("\">");

		String uniqueCountryCoordinateNamePerPolygon_border = countryCode + _COORDINATE_BORDER + index;
		String coordinateString = createCoordinateStringForPolygon_forBorder(polygon, extrusionHeight);
		
		w("<Coordinate DEF=\"" + uniqueCountryCoordinateNamePerPolygon_border + "\" point=\"" + coordinateString + "\"/>");

		wl("</IndexedLineSet>");

		wl("      </Shape>");
	}

	private String createCoordinateStringForPolygon_forBorder(VgPolygon polygon, double extrusionHeight) {
		StringBuffer buffer = new StringBuffer(110);
		
		for (int i = 0; i < polygon.getOuterBoundary().getNumberOfVertices(); i++) {

			// calculate a slightly offset point so that the border will be
			// displayed 'over' the surface of the polygon.
			VgPoint calculatedBorderPoint = calculateOffsetBorderPoint(polygon,
					i, extrusionHeight);

			appendPoint(calculatedBorderPoint, buffer);
		}
		return buffer.toString();
	}

	/**
	 * This method is necessary to solve the following problem:
	 * <p>
	 * Imagine you want to create a 3D-Scene in which you create a polygonal
	 * surface (let's say in the XZ-pane) and on top of this surface you want to
	 * additionally draw the border of this polygon. If you draw the border on
	 * the same height (same Y-value) as the surface, the border line might be
	 * (partially) hidden by the surface. <br/>
	 * So the border of the polygon has to be drawn "on top" of the surface
	 * </p>
	 * 
	 * 
	 * @param polygon
	 * @param pointIndex
	 *            the index of the point for which the corresponding point of
	 *            the offset border is calculated
	 * @param extrusionHeight
	 *            the value which shall be used to offset/extrude the geometry
	 *            (here needed to draw the border on top of the extruded
	 *            geometry!)
	 * @return
	 */
	protected VgPoint calculateOffsetBorderPoint(VgPolygon polygon,
			int pointIndex, double extrusionHeight) {

		// calculate a slightly offset point by using the normal-vector so
		// that the border will be displayed 'over' the surface of the polygon.

		VgLinearRing outerBoundary = polygon.getOuterBoundary();

		VgPoint calculatedBorderPoint = calculateOffsetBorderPoint(
				outerBoundary, pointIndex, extrusionHeight);

		return calculatedBorderPoint;
	}

	/**
	 * same as {@link #calculateOffsetBorderPoint(VgPolygon, int, double)} for a
	 * {@link VgLinearRing}-geometry
	 * 
	 * @param linearRing
	 * @param pointIndex
	 *            the index of the point for which the corresponding point of
	 *            the offset border is calculated
	 * @param extrusionHeight
	 *            the value which shall be used to offset/extrude the geometry
	 *            (here needed to draw the border on top of the extruded
	 *            geometry!)
	 * @return
	 */
	protected VgPoint calculateOffsetBorderPoint(VgLinearRing linearRing,
			int pointIndex, double extrusionHeight) {
		/*
		 * a universal way would be:
		 * 
		 * To solve this problem this method uses three neighbor points (indices
		 * (i-1), i, (i+1)) and calculates the normal-vector of these three
		 * points. Thus, the direction in which the border must be offset is
		 * known. Each axis-component of this direction is then multiplied with
		 * the class-attribute {@link VsWorldCountriesScene#offsetForBorders},
		 * added to the XYZ-values of the i-th point and finally returned as the
		 * new borderPoint.
		 */

		// T3dVector normalVector = calculateNormalVectorFromThreePoints(
		// linearRing, pointIndex);

		/*
		 * as the countries are flat in this case, a simple normal vector (0, 0,
		 * 1) which points to the positive z-axix suffices.
		 */
		T3dVector normalVector = new T3dVector(0, 0, 1);

		// actual Point!
		VgPoint actualPoint = linearRing.getVertex(pointIndex);

		// calculate the borderpoint by adding a small offset in
		// the direction of the normal-vector
		VgPoint calculatedBorderPoint = addOffsetInNormalDirection(
				normalVector, actualPoint, this.offsetForBorders,
				extrusionHeight);

		return calculatedBorderPoint;

	}

	/**
	 * adds offset/extrusion to the given point by follwing formula: <br/>
	 * <br/>
	 * double offsetPointX = point.getX() + normalVector.getX() offsetFactor +
	 * normalVector.getX() * extrusionHeight; <br/>
	 * <br/>
	 * 
	 * double offsetPointY = point.getY() + normalVector.getY() offsetFactor +
	 * normalVector.getY() * extrusionHeight; <br/>
	 * <br/>
	 * 
	 * double offsetPointZ = point.getZ() + normalVector.getZ() offsetFactor +
	 * normalVector.getZ() * extrusionHeight;
	 * 
	 * @param normalVector
	 * @param point
	 * @param offsetFactor
	 * @param extrusionHeight
	 * @return
	 */
	protected VgPoint addOffsetInNormalDirection(T3dVector normalVector,
			VgPoint point, double offsetFactor, double extrusionHeight) {
		double offsetPointX = point.getX() + normalVector.getX() * offsetFactor
				+ normalVector.getX() * extrusionHeight;
		double offsetPointY = point.getY() + normalVector.getY() * offsetFactor
				+ normalVector.getY() * extrusionHeight;
		double offsetPointZ = point.getZ() + normalVector.getZ() * offsetFactor
				+ normalVector.getZ() * extrusionHeight;

		VgPoint offsetPoint = new GmPoint(offsetPointX, offsetPointY,
				offsetPointZ);

		offsetPoint.setSRS(point.getSRS());

		return offsetPoint;
	}

	/**
	 * Generates X3D-code to display the list of {@link VgTIN}-objects as a
	 * single IndexedFaceSet per VgIndexedTIN.
	 * 
	 * @param color
	 *            an instance of {@link T3dColor} to color the IndexedFaceSet
	 * @param vgTINs
	 *            a list of {@link VgTIN}-objects that form the surface of the
	 *            country
	 */
	private void writeSurfaceTINs(T3dColor color, List<VgIndexedTIN> vgTINs) {
		Object countryCode = this.currentFeature.getAttributeValue(COUNTRY_CODE);
		
		for (VgIndexedTIN vgIndexedTIN : vgTINs) {

			wl("      <Shape>");
			wl("        <Appearance>");

			String uniqueCountryMaterialName = countryCode + "_material";
			wl("          <Material DEF=\"" + uniqueCountryMaterialName + "\" diffuseColor=\"" + color.getRed() + " "
					+ color.getGreen() + " " + color.getBlue() + "\"/>");
			wl("        </Appearance>");

			w("<IndexedFaceSet colorPerVertex=\"false\" creaseAngle=\"0.5\" coordIndex=\"");

			for (int i = 0; i < vgIndexedTIN.numberOfTriangles(); i++) {
				int[] triangleVertexIndices = vgIndexedTIN
						.getTriangleVertexIndices(i);
				w("" + triangleVertexIndices[0] + " "
						+ triangleVertexIndices[1] + " "
						+ triangleVertexIndices[2] + "-1" + " ");
			}

			wl("\">");
			// we do not need to specify a color for each vertex, as the whole
			// IndexedFaceSet is colored in the same color. Then we can simply
			// define this color in the <Material>-Node

			// w("<Color color=\"");
			// for (int i = 0; i < vgIndexedTIN.numberOfTriangles(); i++) {
			//
			// w("" + color.getRed() + " " + color.getGreen() + " "
			// + color.getBlue() + " ");
			// }
			// wl("\"/>");

			String uniqueCountrySurfaceCoordinateNamePerTIN = countryCode + "_coordinate";
			w("<Coordinate DEF=\"" + uniqueCountrySurfaceCoordinateNamePerTIN + "\" point=\"");
			for (int i = 0; i < vgIndexedTIN.numberOfPoints(); i++) {
				VgPoint point = vgIndexedTIN.getPoint(i);
				writePoint(point);
			}
			wl("\"/>");

			wl("</IndexedFaceSet>");
			wl("      </Shape>");
		}
	}

	/**
	 * Generates X3D-code to display the list of {@link VgTIN}-objects as a
	 * single IndexedFaceSet per VgIndexedTIN. This method writes extruded
	 * versions of the objects (result displays the extruded top geometries and
	 * their side walls to the bottom)
	 * 
	 * @param color
	 *            an instance of {@link T3dColor} to color the IndexedFaceSet
	 * @param vgTINs
	 *            the 'bottom' objects, meaning non-extruded
	 * @param extrusionTINs
	 *            the 'top' objects, meaning extruded. They form the surface of
	 *            the country
	 */
	private void writeSurfaceTINs(T3dColor color, List<VgIndexedTIN> vgTINs,
			List<VgIndexedTIN> extrusionTINs) {

		/*
		 * to create an extruded version, the vgTINs and extrudedTINs must be
		 * meshed
		 */
		
		Object countryCode = this.currentFeature.getAttributeValue(COUNTRY_CODE);

		for (int t = 0; t < extrusionTINs.size(); t++) {

			VgIndexedTIN topTIN = extrusionTINs.get(t);

			wl("      <Shape>");
			wl("        <Appearance>");

			// TODO check transparency
			

			String uniqueCountryMaterialNamePerTIN_surface = countryCode + _MATERIAL_SURFACE + t;
			wl("          <Material DEF=\"" + uniqueCountryMaterialNamePerTIN_surface + "\" diffuseColor=\"" + color.getRed() + " "
					+ color.getGreen() + " " + color.getBlue() + "\" "

					// + "transparency=\"0.3\""

					+ "/>");
			wl("        </Appearance>");

			w("<IndexedFaceSet ccw=\"true\" solid=\"true\" colorPerVertex=\"false\" creaseAngle=\"0.2\" coordIndex=\"");

			// top triangles

			// int numberOfBottomPoints = bottomTIN.numberOfPoints();

			for (int i = 0; i < topTIN.numberOfTriangles(); i++) {
				int[] triangleVertexIndices = topTIN
						.getTriangleVertexIndices(i);

				// the number of bottom points must be skipped to have the
				// correct mesh for the top TINs

				// int firstIndex = numberOfBottomPoints
				// + triangleVertexIndices[0];
				// int secondIndex = numberOfBottomPoints
				// + triangleVertexIndices[1];
				// int thirdIndex = numberOfBottomPoints
				// + triangleVertexIndices[2];

				int firstIndex = triangleVertexIndices[0];
				int secondIndex = triangleVertexIndices[1];
				int thirdIndex = triangleVertexIndices[2];

				w("" + firstIndex + " " + secondIndex + " " + thirdIndex
						+ " -1" + " ");
			}

			wl("\">");
			// we do not need to specify a color for each vertex, as the whole
			// IndexedFaceSet is colored in the same color. Then we can simply
			// define this color in the <Material>-Node

			/*
			 * Here, the points for the mesh are ordered as follows: 1. bottom
			 * TIN 2. top TIN
			 */

			// // bottom points
			// w("<Coordinate point=\"");
			// for (int j = 0; j < bottomTIN.numberOfPoints(); j++) {
			// VgPoint point = bottomTIN.getPoint(j);
			// writePoint(point);
			// }

			// top points
			
			
			String uniqueCountryCoordinateNamePerTIN_surface = countryCode + _COORDINATE_SURFACE + t;
			
			String coordinateString = createCoordinateString(topTIN);
			
			w("<Coordinate DEF=\"" + uniqueCountryCoordinateNamePerTIN_surface + "\" point=\"" + coordinateString +"\"/>");

			wl("</IndexedFaceSet>");
			wl("      </Shape>");
			
		}

	}

	protected String createCoordinateString(VgIndexedTIN topTIN) {
		
		StringBuffer buffer = new StringBuffer(110);
		for (int k = 0; k < topTIN.numberOfPoints(); k++) {
			VgPoint point = topTIN.getPoint(k);
			appendPoint(point, buffer);
		}
		return buffer.toString();
	}

	private StringBuffer appendPoint(VgPoint point, StringBuffer buffer) {
		return buffer.append(this.decimalFormatter.format(point.getX())).append(" ").append(this.decimalFormatter.format(point.getZ())).append(" ").append(this.decimalFormatter.format(-point.getY())).append(" ");
	}

	/**
	 * Triangulates the multiPolygon.
	 * 
	 * @param multiPolygon
	 * @return a list of {@link VgIndexedTIN} as the result of the
	 *         triangulation.
	 */
	protected List<VgIndexedTIN> calculateTriangulation(
			VgMultiPolygon multiPolygon) {
		int numberOfGeometries = multiPolygon.getNumberOfGeometries();
		List<VgIndexedTIN> vgTINs = new ArrayList<VgIndexedTIN>(
				numberOfGeometries);
		for (int i = 0; i < numberOfGeometries; i++) {

			VgPolygon vgPolygon = (VgPolygon) multiPolygon.getGeometry(i);

			addTINsForPolygon(vgTINs, vgPolygon);
		}
		return vgTINs;
	}

	/**
	 * Triangulates the polygon and adds the resulting triangles to the list of
	 * {@link VgTIN}-objects
	 * 
	 * @param vgTINs
	 * @param vgPolygon
	 */
	protected void addTINsForPolygon(List<VgIndexedTIN> vgTINs,
			VgPolygon vgPolygon) {
		// for planar polygons, there is no need to use so called Steiner points
		// to create smaller triangles

		// if (isGenerateAdditionalInnerPolygonPoints()) {
		// List<VgPoint> innerPoints = InnerPointsForPolygonClass
		// .getInnerPointsForPolygonUsingRaster(vgPolygon,
		// this.xRasterWidth, this.yRasterWidth);
		//
		// vgTINs.add(PolygonTriangulator.triangulatePolygon(vgPolygon,
		// innerPoints));
		// } else {
		vgTINs.add(PolygonTriangulator.triangulatePolygon(vgPolygon));
		// }
	}
}
