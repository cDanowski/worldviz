package org.n52.v3d.worldviz.featurenet.scene;

import org.n52.v3d.triturus.vgis.VgPoint;

/**
 * This interface provides necessary information to place a symbol as a
 * connection between two points. These points represent a from-to-vector
 * relationship. The implementing class computes the necessary rotation (as
 * angles for x-, y- and z-axis) and translation (as mid-point between the two
 * given points) parameters for a connection between those points.<br/>
 * <br/>
 * Please note that any symbol in a Virtual Reality scene description like X3D
 * is directed with respect to the y-axis of the scene coordinate system
 * (meaning that a height-parameter of a symbol like a cylinder will extrude the
 * cylinder in y-direction!).
 * 
 * @author Christian Danowski
 *
 */
public interface SceneSymbolTransformer {

	/**
	 * Returns the length of the vector between the points 'from' and 'to'
	 * 
	 * @return
	 */
	public double getLengthFromTo();

	/**
	 * Returns the rotation of a symbol with respect to the x-axis (pointing to
	 * the right) of the scene-coordinate system.
	 * 
	 * @return
	 */
	public double getAngleX();

	/**
	 * Returns the rotation of a symbol with respect to the y-axis (pointing
	 * upwards; equivalent to the height axis) of the scene-coordinate system.
	 * 
	 * @return
	 */
	public double getAngleY();

	/**
	 * Returns the rotation of a symbol with respect to the z-axis (pointing
	 * towards the user outside of the screen) of the scene-coordinate system.
	 * 
	 * @return
	 */
	public double getAngleZ();

	/**
	 * Returns the mid-point between the points 'from' and 'to' defined in the
	 * constructor.
	 * 
	 * @return
	 */
	public VgPoint getMidPoint();

}
