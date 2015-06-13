package org.n52.v3d.worldviz.featurenet.scene;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.List;

@XStreamAlias("Fill")
public class Fill{
    @XStreamImplicit
    private List<SvgParameter> svgParameter;
    
    public List getSvgParameter(){
        return svgParameter;
    }
    
}