package org.n52.v3d.worldviz.test;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.n52.v3d.worldviz.dataaccess.importtools.KeyValuePair;
import org.n52.v3d.worldviz.dataaccess.load.SimpleTextFileLoader;

import junit.framework.Assert;

public class SimpleTextReader_Test {

	@Test
	public void test() {
		SimpleTextFileLoader loader = new SimpleTextFileLoader();
		
		List<KeyValuePair> keyValuePairs = loader.extractKeyValuePairs(new File("data/CO2_ppm_world.dat"), 1);
		
		Assert.assertTrue(keyValuePairs.size() > 0);
		
		
	}

}
