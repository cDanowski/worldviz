package org.n52.v3d.worldviz.worldscene.mapper;

import java.util.List;

import org.n52.v3d.triturus.vgis.VgAttrFeature;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ColoredAttrFeature;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ColoredAttrFeature_forAnimation;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ExtrudedAttrFeature;
import org.n52.v3d.worldviz.extensions.mappers.MpValue2ExtrudedAttrFeature_forAnimation;
import org.n52.v3d.worldviz.worldscene.VsAbstractWorldScene;
import org.n52.v3d.worldviz.worldscene.VsAnimatedWorldCountriesOnASphereScene;
import org.n52.v3d.worldviz.worldscene.VsWorldCountriesOnASphereScene;
import org.n52.v3d.worldviz.worldscene.VsWorldCountriesScene;
import org.n52.v3d.worldviz.worldscene.helper.FindExtrudeAndColorMissingCountriesHelper;
import org.n52.v3d.worldviz.worldscene.helper.FindExtrudeAndColorMissingCountriesHelper_forAnimation;

import de.hsbo.fbg.worldviz.WvizConfigDocument;
import de.hsbo.fbg.worldviz.WvizConfigDocument.WvizConfig.GlobeVisualization.WorldCountries.PolygonVisualizer.Animation;
import de.hsbo.fbg.worldviz.WvizConfigDocument.WvizConfig.GlobeVisualization.WorldCountries.PolygonVisualizer.Animation.Years;

public class MpWorldCountriesAnimationMapper extends MpWorldCountriesMapper {

	private String animationType;
	private int firstYear;
	private int lastYear;
	private int interval;

	public MpWorldCountriesAnimationMapper(WvizConfigDocument wVizConfigFile, String attributeNameForMapping) {
		super(wVizConfigFile, attributeNameForMapping);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected VsWorldCountriesOnASphereScene initializeScene() {
		// TODO Auto-generated method stub
		return new VsAnimatedWorldCountriesOnASphereScene(firstYear, lastYear, interval);
	}

	@Override
	protected void parameterizeScene(VsAbstractWorldScene scene, WvizConfigDocument wVizConfigFile) {
		super.parameterizeScene(scene, wVizConfigFile);

		Animation animationConfig = wVizConfigFile.getWvizConfig().getGlobeVisualization().getWorldCountries()
				.getPolygonVisualizer().getAnimation();

		this.animationType = animationConfig.getType();
		Years yearsConfig = animationConfig.getYears();
		this.firstYear = yearsConfig.getFirstYear();
		this.lastYear = yearsConfig.getLastYear();
		this.interval = yearsConfig.getInterval();
		
		((VsAnimatedWorldCountriesOnASphereScene) scene).setFirstYear(firstYear);
		((VsAnimatedWorldCountriesOnASphereScene) scene).setLastYear(lastYear);
		((VsAnimatedWorldCountriesOnASphereScene) scene).setInterval(interval);
	}

	@Override
	protected MpValue2ColoredAttrFeature createColorMapperInstance() {
		return new MpValue2ColoredAttrFeature_forAnimation();
	}

	@Override
	protected MpValue2ExtrudedAttrFeature createExtrusionMapperInstance() {
		return new MpValue2ExtrudedAttrFeature_forAnimation();
	}

	@Override
	protected List<VgAttrFeature> transformGeoObjectsToSceneObjects(VsWorldCountriesScene worldCountriesScene,
			List<VgAttrFeature> geoObjects) {

		/*
		 * TODO for each year --> perform color and extrusion mapper which
		 * creates a color and extrusion attribute for that specific year
		 * 
		 */
		Animation animationConfig = this.wVizConfigFile.getWvizConfig().getGlobeVisualization().getWorldCountries()
				.getPolygonVisualizer().getAnimation();
		Years yearsConfig = animationConfig.getYears();

		for (int currentYear = firstYear; currentYear <= lastYear; currentYear = currentYear + interval) {

			geoObjects = this.colorMapper.transform(String.valueOf(currentYear), geoObjects);

			geoObjects = this.extrusionMapper.transform(String.valueOf(currentYear), geoObjects);
		}

		List<VgAttrFeature> allSceneObjects = processMissingCountries(geoObjects);

		return allSceneObjects;

	}

	@Override
	protected List<VgAttrFeature> processMissingCountries(List<VgAttrFeature> extrudedColoredSceneObjects) {

		FindExtrudeAndColorMissingCountriesHelper missingCountriesHelper = new FindExtrudeAndColorMissingCountriesHelper_forAnimation(
				this.countryBorderLOD, this.firstYear, this.lastYear, this.interval);

		// missingCountriesHelper.findExtrudeAndColorMissingCountries(alreadyExtrudedAndColoredCountries,
		// neutralColor, neutralExtrusionHeight)
		List<VgAttrFeature> allSceneObjects = missingCountriesHelper.findExtrudeAndColorMissingCountries(
				extrudedColoredSceneObjects, this.colorMapper.getNeutralColor(),
				this.extrusionMapper.getNeutralExtrusionHeight());

		return allSceneObjects;
	}

}
