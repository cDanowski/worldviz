package org.n52.v3d.worldviz.dataaccess.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.v3d.worldviz.dataaccess.importtools.KeyValuePair;

public class SimpleTextFileLoader {

	public SimpleTextFileLoader() {
		
	}
	
	public List<KeyValuePair> extractKeyValuePairs(File datFile, int numberOfLinesToSkip){
		/*
		 * TODO format looks like, each value is separated from attribute with a tab!:
		 * 
		 * header line
		 * attribute	value
		 */
		List<KeyValuePair> keyValuePairs = new ArrayList<KeyValuePair>();
		
		/*
		 * create reader and parse the file
		 */
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(datFile));
			
			String line;
			while ((line = br.readLine()) != null) {
				if(numberOfLinesToSkip > 0){
					/*
					 * decrease number of lines to skip and skip this run.
					 */
					numberOfLinesToSkip --;
					continue;
				}
				
				KeyValuePair keyValuePair = parseAsKeyValuePair("	", line);
				keyValuePairs.add(keyValuePair);
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return keyValuePairs;
	}

	private KeyValuePair parseAsKeyValuePair(String separator, String line) {

		String[] keyAndValue = line.split(separator);
		
		KeyValuePair keyValuePair = new KeyValuePair(keyAndValue[0], keyAndValue[1]);
		
		return keyValuePair;
	}

}
