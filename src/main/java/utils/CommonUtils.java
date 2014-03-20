/**
 * 
 */
package utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author kate
 * 
 */
public class CommonUtils {

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	for (Entry<T, E> entry : map.entrySet()) {
	    if (value.equals(entry.getValue())) {
		return entry.getKey();
	    }
	}
	return null;
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
	Set<T> keys = new HashSet<T>();
	for (Entry<T, E> entry : map.entrySet()) {
	    if (value.equals(entry.getValue())) {
		keys.add(entry.getKey());
	    }
	}
	return keys;
    }

    /**
     * Ergodic process of a map
     * 
     * @param map
     */
    public static <T, E> void ergodicAMap(Map<T, E> map) {
	for (Entry<T, E> entry : map.entrySet()) {
	    T key = entry.getKey();
	    E value = entry.getValue();
	    // System.out.println(key.toString() + "=" + value);
	}
    }
}
