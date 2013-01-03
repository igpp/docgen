package igpp.docgen;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ValueMap {

	/**
	 * Print the map in "keyword = value" format
	 **/
	static public void print(PrintStream out, String prefix, HashMap<String, Object> map)
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
					print(out, prefix + key + "[" + n + "].",  mapItem);
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
	
	/**
	 * Print the map in "keyword = value" format
	 **/
	static public void encodeForXML(HashMap<String, Object> map)
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
					items.set(n, igpp.util.Encode.htmlEncode(item));
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
					encodeForXML(mapItem);
					n++;
				}
				simple = false;
			} catch(Exception e) {
				// Do nothing - try next pattern
			} 
			if(simple) {	// If simple, print as a single value.
				if(key instanceof String) map.put(key, igpp.util.Encode.htmlEncode((String) map.get(key)));
			}
		}
	}
}
