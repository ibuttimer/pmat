package ie.ibuttimer.pmat.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class KeyValueSplitter {

	private String source;			// String to split
	private String fieldSeperator;	// Regular expression to use to separate fields
	private String keyValSeperator;	// Regular expression string to separate key/value pairs
	private String fieldUnifier;	// Regular expression to use to unify fields
	private String[] defaultKeys;	// keys to use for result if keyValSeperator is not specified

	/** The regular expression for the default unifier, a quoted string; e.g. "this is all one value" */  
	public static final String DEFAULT_UNIFIER_REGEX = "\\\"";
	/** The default unifier, a quoted string; e.g. "this is all one value" */  
	public static final String DEFAULT_UNIFIER = "\"";
	/** The regular expression for the default key/value pair assignment, an equals character; e.g. key=value */  
	public static final String DEFAULT_KV_ASSIGN_REGEX = "=";
	/** The default key/value pair assignment, an equals character; e.g. key=value */  
	public static final String DEFAULT_KV_ASSIGN = "=";

	/** The default key name stub; i.e. key<field num> e.g. key1 */  
	public static final String DEFAULT_KEY_STUB = "key";
	
	/**
	 * Constructor
	 * @param source			String to split
	 * @param fieldSeperator	Regular expression to use to separate fields
	 * @param keyValSeperator	Regular expression string to separate key/value pairs
	 * @param fieldUnifier		Regular expression to use to unify fields
	 * @param defaultKeys		Keys to use for result if keyValSeperator is not specified
	 */
	public KeyValueSplitter(String source, String fieldSeperator,
			String keyValSeperator, String fieldUnifier, String[] defaultKeys) {
		super();
		this.source = source;
		this.fieldSeperator = fieldSeperator;
		this.keyValSeperator = keyValSeperator;
		this.fieldUnifier = fieldUnifier;
		this.setDefaultKeys(defaultKeys);
	}



	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}



	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}



	/**
	 * @return the fieldSeperator
	 */
	public String getFieldSeperator() {
		return fieldSeperator;
	}



	/**
	 * @param fieldSeperator the fieldSeperator to set
	 */
	public void setFieldSeperator(String fieldSeperator) {
		this.fieldSeperator = fieldSeperator;
	}



	/**
	 * @return the keyValSeperator
	 */
	public String getKeyValSeperator() {
		return keyValSeperator;
	}



	/**
	 * @param keyValSeperator the keyValSeperator to set
	 */
	public void setKeyValSeperator(String keyValSeperator) {
		this.keyValSeperator = keyValSeperator;
	}



	/**
	 * @return the fieldUnifier
	 */
	public String getFieldUnifier() {
		return fieldUnifier;
	}

	/**
	 * @param fieldUnifier the fieldUnifier to set
	 */
	public void setFieldUnifier(String fieldUnifier) {
		this.fieldUnifier = fieldUnifier;
	}

	/**
	 * @return the defaultKeys
	 */
	public String[] getDefaultKeys() {
		return defaultKeys;
	}

	/**
	 * @param defaultKeys the defaultKeys to set
	 */
	public void setDefaultKeys(String[] defaultKeys) {
		this.defaultKeys = defaultKeys;
	}

	/**
	 * Splits the source string using the supplied separators into key/value pairs. 
	 */
	public LinkedHashMap<String,String> splitString() {
		
		return KeyValueSplitter.splitKeyValueString( source, fieldSeperator, keyValSeperator, fieldUnifier, defaultKeys );
	}
	

	static enum markerMode { OUTSIDE_MARK, START_MARK, INSIDE_MARK, END_MARK };

	/**
	 * Split the source string based on the <code>separator</code> regular expression specified ignoring 
	 * separator characters within the <code>unifier</code> regular expression.<br>
	 * See Pattern for regular expression syntax.
	 * @param source		String to split
	 * @param separator		Regular expression to use to separate fields
	 * @param unifier		Regular expression to use to unify fields
	 * @return				String[], the elements of which do not contain separator characters but do contain unifier characters.
	 */
	private static String[] split(String source, String separator, String unifier) {

		final char VALID_CHAR = 'v';
		final char SEPARATOR_CHAR = 's';
		final char UNIFIER_CHAR = 'u';
		char[] mask = new char[source.length()];	// mask of source string
		
		for ( int i = 0; i < mask.length; ++i )
			mask[i] = VALID_CHAR;	// default all chars valid
		
		boolean separators = false;	// flag to indicate existence of separator chars in string
		if ( !TextUtils.isEmpty(separator) ) {
			Pattern s = Pattern.compile(separator);
			Matcher ms = s.matcher(source);
			while ( ms.find() ) {
				for ( int i = ms.start(); i < ms.end(); ++i ) {
					mask[i] = SEPARATOR_CHAR;	// mark separator chars
					separators = true;
				}
			}
		}
		
		if ( !separators ) {
			// no separators so just return the source
			String[] res = new String[1];
			res[0] = new String(source);
			return res;
		}

		ArrayList<String> splits = new ArrayList<String>();	// temp storage for fields

		boolean unifiers = false;	// flag to indicate existence of unifier chars in string
		if ( !TextUtils.isEmpty(unifier) ) {
			Pattern u = Pattern.compile(unifier);
			Matcher mu = u.matcher(source);
			while ( mu.find() ) {
				for ( int i = mu.start(); i < mu.end(); ++i ) {
					mask[i] = UNIFIER_CHAR;	// mark unifier chars
					unifiers = true;
				}	
			}
		}

		// convert all separator char inside a unifier block to valid chars
		if ( unifiers ) {
			markerMode uni = markerMode.OUTSIDE_MARK;
			for ( int i = 0; i < mask.length; ++i ) {
	
				if ( mask[i] == UNIFIER_CHAR ) {
					switch ( uni ) {
						case OUTSIDE_MARK:
							uni = markerMode.START_MARK;	// open unified block
							break;
						case INSIDE_MARK:
							uni = markerMode.END_MARK;		// close unified block
							break;
						default:							// no change
							break;	
					}
					mask[i] = VALID_CHAR;	// mark all unifier chars as valid
				}
				else {
					switch ( uni ) {
						case START_MARK:
							uni = markerMode.INSIDE_MARK;	// inside unified block
							mask[i] = VALID_CHAR;
							break;
						case END_MARK:
							uni = markerMode.OUTSIDE_MARK;	// outside unified block
							break;
						case OUTSIDE_MARK:					// no change
							break;
						default:							// INSIDE_MARK change to valid
							mask[i] = VALID_CHAR;
							break;
					}
				}
			}
		}
		
		// generate the strings based on the mask
		int start = -1;
		for ( int i = 0; i < mask.length; ++i ) {

			if ( mask[i] == SEPARATOR_CHAR ) {
				if ( start >= 0 ) {
					splits.add(source.substring(start, i));
					start = -1;
				}
			}
			else {
				if ( start < 0 )
					start = i;
			}
		}
		if ( start >= 0 )
			splits.add(source.substring(start, source.length()));
		
		// convert the ArrayList to an array of Strings 
		String[] res = new String[splits.size()];
		for ( int i = 0; i < splits.size(); ++i )
			res[i] = splits.get(i);
		
		return res;
	}
	
	
	/**
	 * Make a default map key name
	 * @param index		field index
	 * @return
	 */
	public static String makeDefaultKey(int index) {
		return (DEFAULT_KEY_STUB + Integer.toString(index));
	}
	
	/**
	 * Make a default key/value assignment text
	 * @param key		key name
	 * @param value		value
	 * @return
	 */
	public static String makeDefaultKeyValueAssignment(String key, String value) {
		return key + KeyValueSplitter.DEFAULT_KV_ASSIGN + value;
	}

	/**
	 * Splits the source string using the supplied separators into key/value pairs. 
	 * See Pattern for regular expression syntax.
	 *  
	 * @param source			String to parse
	 * @param fieldSeperator	Regular expression string to separate fields
	 * @param keyValSeperator	Regular expression string to separate key/value pairs
	 * @param fieldUnifier		Regular expression string to separate fields
	 * @return					LinkedHashMap containing the key/value pairs in the order they appeared in <code>source</code>  
	 */
	public static LinkedHashMap<String,String> splitKeyValueString( String source, String fieldSeperator, String keyValSeperator, 
																		String fieldUnifier, String[] defaultKeys ) {
		
		LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
		
		if ( !TextUtils.isEmpty(source) ) {

			// split into fields 
			String[] fields = split(source, fieldSeperator, fieldUnifier);
			
			boolean hasKeyVal = !TextUtils.isEmpty(keyValSeperator);
			
			int i = 0;
			for ( String field: fields ) {
				// split fields into key/value pairs
				String[] elements = split(field, keyValSeperator, null );
				String key;
				String value;

				key = value = null;

				if ( !hasKeyVal ) {
					// only values so use the default keys in order
					if ( (defaultKeys != null) && (defaultKeys.length >= fields.length) )
						key = defaultKeys[i];
					else
						key = makeDefaultKey(i);
					if ( elements.length >= 1 )
						value = elements[0];
				}
				else {
					if ( elements.length >= 1 )
						key = elements[0];
					if ( elements.length >= 2 )
						value = elements[1];
				}

				if ( key != null )
					map.put(key, value);

				++i;
			}
		}
		
		return map;
	}
	
}
