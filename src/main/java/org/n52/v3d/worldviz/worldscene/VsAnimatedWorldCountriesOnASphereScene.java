package org.n52.v3d.worldviz.worldscene;

public class VsAnimatedWorldCountriesOnASphereScene extends VsWorldCountriesOnASphereScene {

	public VsAnimatedWorldCountriesOnASphereScene() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void generateSceneContentKML() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void generateSceneContentVRML() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void generateSceneContentX3D(boolean asX3DOM) {
		/*
		 * TODO basically I could reuse most code from VsWorldCountriesScene!
		 * Since that triangulates the polygon and calculates the extrusion
		 * coordinates.
		 * 
		 * So we might extend that class instead and make needed methods
		 * protected to access them from here?
		 */

		/*
		 * for each country: only triangulate once! but then assign different
		 * colors and extrusions to the object for each year, which is then
		 * written for the animation
		 * 
		 */

		/*
		 * TODO it would conceptually be best, if the scene inputs are already
		 * computed with the parameters (color and extrusion) for each country
		 * and each year. E.g. via a mapper. In the scene just create the visual
		 * output.
		 */

	}

}
