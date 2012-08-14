package igpp.docgen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class PrintMap {

	/**
	 * Print the map in "keyword = value" format
	 **/
	static public void valueList(PrintStream out, String prefix, HashMap<String, Object> map)
	{
		// ArrayList<HashMap<String, Object>> arrayType = new ArrayList<HashMap<String, Object>>();
		// ArrayList<String> stringArray = new ArrayList<String>();
		
		Set<String> keySet = (Set<String>) map.keySet();
		for(String key : keySet) {
			int n = 0;
			boolean simple = true;
			try {	// Try as an ArrayList<String> - if exception is thrown cast was not possible.
				@SuppressWarnings("unchecked")
				ArrayList<String> items = (ArrayList<String>) map.get(key);
				for(String item : items) {
					out.println(prefix + key + "[" + n + "]=" + item);
					n++;				
				}
				simple = false;
			} catch (Exception e) {
				// Do nothing - try next pattern
			}
			try {	// Try as an ArrayList<HashMap<String, Object>> - if exception is thrown cast was not possible.
				// } else if(map.get(key).getClass().isInstance(arrayType) ) {
				@SuppressWarnings("unchecked")
				ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) map.get(key);
				for(HashMap<String, Object> mapItem : list) {
					valueList(out, prefix + key + "[" + n + "].",  mapItem);
					n++;
				}
				simple = false;
			} catch(Exception e) {
				// Do nothing - try next pattern
			} 
			if(simple) {	// If simple, print as a single value.
				out.println(prefix + key + "=" + map.get(key));
			}
		}
	}
}
