/**
 * 
 */
package ie.ibuttimer.pmat.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An extended HashMap which allows the looking up of mappings for key or value.
 * @author Ian Buttimer
 *
 */
public class TwoWayHashMap<K, V> extends HashMap<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 545597578097176677L;
	
	private HashMap<V, K> reverseLookup;

	/**
	 * 
	 */
	public TwoWayHashMap() {
		super();
		reverseLookup = new HashMap<V, K>();
	}

	/**
	 * @param capacity
	 */
	public TwoWayHashMap(int capacity) {
		super(capacity);
		reverseLookup = new HashMap<V, K>(capacity);
	}

	/**
	 * @param map
	 */
	public TwoWayHashMap(Map<K, V> map) {
		super(map.size());
		reverseLookup = new HashMap<V, K>(map.size());
		putAll(map);
	}

	/**
	 * @param capacity
	 * @param loadFactor
	 */
	public TwoWayHashMap(int capacity, float loadFactor) {
		super(capacity, loadFactor);
		reverseLookup = new HashMap<V, K>(capacity, loadFactor);
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#clone()
	 */
	@Override
	public Object clone() {
        try {
        	throw new CloneNotSupportedException();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
	}

	/**
	 * Returns the value of the mapping with the specified key.<br>
	 * <b>Note:</b> This is equivalent to calling <code>get(key)</code>.
	 * @param key	- the key.
	 * @return		the value of the mapping with the specified key, or null if no mapping for the specified key is found.
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	public V getValue(Object key) {
		return super.get(key);
	}

	/**
	 * Returns the key of the mapping with the specified value.<br>
	 * @param value	- the value.
	 * @return		the key of the mapping with the specified value, or null if no mapping for the specified key is found.
	 */
	public K getKey(Object value) {
		return reverseLookup.get(value);
	}

	/**
	 * Returns the value of the mapping with the specified key or value.
	 * @param search	- key or value to search for
	 * @return
	 */
	public Object getKeyOrValue(Object search) {
		Object result = super.get(search);
		if ( result == null ) {
			// not found in <K, V> map, search <V, K> map
			result = reverseLookup.get(search);
		}
		return result;
	}

	/**
	 * Returns whether this map contains the specified value or key.
	 * @param search	- key or value to search for
	 * @return			<code>true</code> if this map contains the specified value, <code>false</code> otherwise.
	 */
	public boolean containsKeyOrValue(Object search) {
		boolean result = super.containsKey(search);
		if ( !result ) {
			// not found in <K, V> map, search <V, K> map
			result = reverseLookup.containsKey(search);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(K key, V value) {
		reverseLookup.put(value, key);
		return super.put(key, value);
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		super.putAll(map);
		reverseLookup = new HashMap<V, K>(map.size());
		Set<K> keys = (Set<K>) map.keySet();
		for ( K key : keys ) {
			V val = map.get(key);
			reverseLookup.put(val, key);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#remove(java.lang.Object)
	 */
	@Override
	public V remove(Object key) {
		reverseLookup.remove(get(key));
		return super.remove(key);
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		reverseLookup.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((reverseLookup == null) ? 0 : reverseLookup.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof TwoWayHashMap))
			return false;
		TwoWayHashMap<K, V> other = (TwoWayHashMap<K, V>) obj;
		if (reverseLookup == null) {
			if (other.reverseLookup != null)
				return false;
		} else if (!reverseLookup.equals(other.reverseLookup))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TwoWayHashMap [" + super.toString() + 
				", reverseLookup=" + reverseLookup + "]";
	}
}
