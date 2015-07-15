package org.n52.v3d.worldviz.featurenet.scene;

import java.util.ArrayList;
import org.n52.v3d.triturus.vgis.VgFeature;
import org.n52.v3d.worldviz.featurenet.VgFeatureNet;
import org.n52.v3d.worldviz.featurenet.VgRelation;
import org.n52.v3d.worldviz.featurenet.impl.WvizConnection;
import org.n52.v3d.worldviz.featurenet.xstream.WvizConfig;

/**
 * Abstract scene model for 3-d visualizations of connection-maps.
 *
 * Note: This abstract model is kept simple. For more sophisticated models,
 * further implementations could be added later.
 *
 * Basically, the visualization pipeline will be realized as follows: In a first
 * step, feature-nets (which are modelled as geo-objects here) wil be mapped to
 * an abstract scene, e.g. a
 * <tt>WvizVirtualConnectionMapScene</tt>. This abstract ("virtual") scene does
 * not know anything about its final realization (X3D, KML, etc.). In a second
 * transformation step, this abstract scene will be mapped to the desired target
 * environment, e.g. X3D. Inside 52N Triturus WorldViz, these mappers are
 * implemented as explicite entities ("transformation processes" as Java
 * objects).
 *
 * @author Benno Schmidt, Adhitya Kamakshidasan, Christian Danowski
 */


//Adhitya: Is this Class doing what it is meant to be?
public class WvizVirtualConnectionMapScene {

    public ArrayList<VgFeature> vertices = null;
    public ArrayList<VgRelation> relations = null;

    public ArrayList<VgRelation> edges = new ArrayList<VgRelation>();
    public ArrayList<VgRelation> arcs = new ArrayList<VgRelation>();
    
    public WvizConfig style;

    public WvizVirtualConnectionMapScene(VgFeatureNet net, WvizConfig style){
        
        vertices = (ArrayList<VgFeature>) net.getFeatures();
        relations = (ArrayList<VgRelation>) net.getRelations();

        /*Adhitya: This is one of the ways, we can differentiate between an Arc and Edge.
         Let me know if you have a better way of doing it. Can this be shifted to VgRelation? */
        
        for (VgRelation r : relations) {
            if (r instanceof WvizConnection) {
                edges.add(r);
            }
            else {
                arcs.add(r);
            }
        }
        
        
        this.style = style;

    }
    
    public ArrayList<VgFeature> getVertices(){
        return vertices;
    }
    
    public ArrayList<VgRelation> getEdges(){
        return edges;
    }
    
    public ArrayList<VgRelation> getArcs(){
        return arcs;
    }
    
    public WvizConfig getStyle(){
        return style;
    }

}
