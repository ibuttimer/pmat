/**
 * 
 */
package ie.ibuttimer.pmat.db;

import ie.ibuttimer.pmat.db.SQLiteCommandFactory.SelectionArgs;
import ie.ibuttimer.pmat.util.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Base class to represent an object as it appears in the database
 * @author Ian Buttimer
 * @param <? extends DatabaseObject>
 *
 */
public class DatabaseObject implements Cloneable {
	
	/* variables to hold the field names of all the subclass variables grouped by type */ 
	private ArrayList<String> longFields;
	private ArrayList<String> intFields;
	private ArrayList<String> dateFields;
	private ArrayList<String> doubleFields;
	private ArrayList<String> stringFields;
	private int numLongFields;
	private int numIntFields;
	private int numDateFields;
	private int numDoubleFields;
	private int numStringFields;

	private Constructor<? extends DatabaseObject> ctor;	// constructor for subclasses

	protected DatabaseObject() {
		super();
	}

	/**
	 * @param cls
	 */
	public DatabaseObject(Class<? extends DatabaseObject> cls) {
		super();
		try {
			this.ctor = cls.getConstructor();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set the field name of the database object
	 * @param longFields	- names of long variables
	 * @param intFields		- names of integer variables
	 * @param dateFields	- names of date variables
	 * @param doubleFields	- names of double variables
	 * @param stringFields	- names of string variables
	 */
	public void setFields(ArrayList<String> longFields, ArrayList<String> intFields, ArrayList<String> dateFields,
							ArrayList<String> doubleFields, ArrayList<String> stringFields) {
		this.longFields = longFields;
		this.intFields = intFields;
		this.dateFields = dateFields;
		this.doubleFields = doubleFields;
		this.stringFields = stringFields;
		numLongFields = longFields.size();
		numIntFields = intFields.size();
		numDateFields = dateFields.size();
		numDoubleFields = doubleFields.size();
		numStringFields = stringFields.size();
	}
	
	
	/**
	 * Return a new instance of a subclass
	 * @return
	 */
	private DatabaseObject getNewInstance() {
		DatabaseObject obj = null;
		try {
			obj = (DatabaseObject) ctor.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	

	/**
	 * Create a clone of this object
	 * @return
	 */
	public <T> Object cloneThis() {
		
		T copy = (T) getNewInstance();
		Field f[] = getClass().getDeclaredFields();
		final int N = f.length;
		for ( int i = 0; i < N; ++i ) {
			int modifiers = f[i].getModifiers();
			if ( Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) )
				continue;	// not need to set this field

			Object value = null;
			try {
				value = f[i].get(this);
			} catch (Exception e) {
				Logger.w("Error getting field " + f[i]);
				e.printStackTrace();
			}

			((DatabaseObject) copy).saveField(f[i], value);
		}
		return copy;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return cloneThis();
	}
	

	/**
	 * Delete an object from the database
	 * @param cr		- Content Resolver to use
	 * @param uri		- URI to use
	 * @param idName	- name of id field
	 * @param id		- Id of object to delete 
	 * @return			<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	protected static boolean deleteIdFromProvider(ContentResolver cr, Uri uri, String idName, long id) {
		return deleteIdFromProvider(cr, uri, idName, new long[] { id });
	}

	/**
	 * Delete objects from the database
	 * @param cr		- Content Resolver to use
	 * @param uri		- URI to use
	 * @param idName	- name of id field
	 * @param ids		- Ids of object to delete 
	 * @return			<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	protected static boolean deleteIdFromProvider(ContentResolver cr, Uri uri, String idName, long[] ids) {
		SelectionArgs args = SQLiteCommandFactory.makeIdSelection(idName, ids);
		return (cr.delete(uri, args.selection, args.selectionArgs) > 0);
	}

	/**
	 * Save the specified field value
	 * @param field	- field to save
	 * @param value	- value to set the field
	 */
	protected void saveField(Field field, Object value) {
		
		try {
			field.set(this, value);
		} catch (Exception e) {
			Logger.w("Error setting field '" + field + "' to " + value.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Save the specified field value
	 * @param field	- field to save
	 * @param value	- value to set the field
	 */
	protected void saveField(String field, Object value) {
		
		try {
			saveField(getClass().getDeclaredField(field), value);
		} catch (NoSuchFieldException e) {
			Logger.w("Error no such field '" + field + "'");
			e.printStackTrace();
		}
	}

	/**
	 * Get the specified field value
	 * @param field	- field to get
	 */
	protected Object getField(Field field) {
		
		Object value = null;
		try {
			value = field.get(this);
		} catch (Exception e) {
			Logger.w("Error getting field '" + field);
			e.printStackTrace();
		}
		return value;
	}
	
	/**
	 * Get the specified field value
	 * @param field	- field to get
	 */
	protected Object getField(String field) {
		
		Object value = null;
		try {
			value = getField(getClass().getDeclaredField(field));
		} catch (NoSuchFieldException e) {
			Logger.w("Error no such field '" + field + "'");
			e.printStackTrace();
		}
		return value;
	}
	
	
	/**
	 * Return a Calendar object representing the specified date string
	 * @param dateStr
	 * @return
	 */
	protected static GregorianCalendar getCalendar( String dateStr ) {
		Date date = DatabaseManager.parseDatabaseTimestamp(dateStr);
		GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
		cal.setTime(date);
		return cal;
	}
	
	
	/**
	 * Retrieve objects from the database
	 * @param cr			- Content Resolver to use
	 * @param uri			- Uri to use
	 * @param selection		- A filter declaring which rows to return
	 * @param selectionArgs	- Values to replace ? in <code>selection</code>
	 * @param useList		- ArrayList of objects to use or if null a new ArrayList will be created
	 * @return				an ArrayList of objects
	 */
	protected <T extends DatabaseObject> ArrayList<T> loadFromProvider(ContentResolver cr, Uri uri, String selection, String[] selectionArgs, ArrayList<T> useList) {
		ArrayList<T> list = (useList == null ? new ArrayList<T>() : useList);
		Cursor c = cr.query(uri, null, selection, selectionArgs, null);
		if (c.moveToFirst()) {
			int i;
			int[] longIndices = new int[numLongFields];
			int[] intIndices = new int[numIntFields];
			int[] dateIndices = new int[numDateFields];
			int[] doubleIndices = new int[numDoubleFields];
			int[] stringIndices = new int[numStringFields];
			int cnt = 0;

			for ( i = 0; i < numLongFields; ++i ) {
				longIndices[i] = c.getColumnIndex(longFields.get(i));
			}
			for ( i = 0; i < numIntFields; ++i ) {
				intIndices[i] = c.getColumnIndex(intFields.get(i));
			}
			for ( i = 0; i < numDateFields; ++i ) {
				dateIndices[i] = c.getColumnIndex(dateFields.get(i));
			}
			for ( i = 0; i < numDoubleFields; ++i ) {
				doubleIndices[i] = c.getColumnIndex(doubleFields.get(i));
			}
			for ( i = 0; i < numStringFields; ++i ) {
				stringIndices[i] = c.getColumnIndex(stringFields.get(i));
			}

			do {
				T obj;
				
				// get object from provided list or create new
				if ( cnt >= list.size() )
					obj = (T) getNewInstance();
				else
					obj = list.get(cnt);
				++cnt;

				// Extract the details.
				for ( i = numLongFields - 1; i >= 0; --i) {
					String field = longFields.get(i);
					if ( longIndices[i] >= 0 )
						obj.saveField(field, c.getLong(longIndices[i]));
				}
				for ( i = numIntFields - 1; i >= 0; --i) {
					String field = intFields.get(i);
					if ( intIndices[i] >= 0 )
						obj.saveField(field, c.getInt(intIndices[i]));
				}
				for ( i = numDateFields - 1; i >= 0; --i) {
					String field = dateFields.get(i);
					if ( dateIndices[i] >= 0 ) {
						obj.saveField(field, getCalendar( c.getString(dateIndices[i]) ));
					}
				}
				for ( i = numDoubleFields - 1; i >= 0; --i) {
					String field = doubleFields.get(i);
					if ( doubleIndices[i] >= 0 )
						obj.saveField(field, c.getDouble(doubleIndices[i]));
				}
				for ( i = numStringFields - 1; i >= 0; --i) {
					String field = stringFields.get(i);
					if ( stringIndices[i] >= 0 )
						obj.saveField(field, c.getString(stringIndices[i]));
				}

				list.add( obj );
			} while(c.moveToNext());
		}
		c.close();
		return list;
	}

	/**
	 * Retrieve objects from the database
	 * @param cr			- Content Resolver to use
	 * @param uri			- Uri to use
	 * @param selection		- A filter declaring which rows to return
	 * @param selectionArgs	- Values to replace ? in <code>selection</code>
	 * @return				an ArrayList of objects
	 */
	protected <T extends DatabaseObject> ArrayList<T> loadFromProvider(ContentResolver cr, Uri uri, String selection, String[] selectionArgs) {
		return loadFromProvider(cr, uri, selection, selectionArgs, null);
	}

	/**
	 * Retrieve all the objects in the database
	 * @param cr			- Content Resolver to use
	 * @param uri			- Uri to use
	 * @return				an ArrayList of objects
	 */
	public <T extends DatabaseObject> ArrayList<T> loadAllFromProvider(ContentResolver cr, Uri uri) {
		return loadFromProvider(cr, uri, (String)null, (String[])null);
	}

	/**
	 * Retrieve the object(s) in the database with the specified value
	 * @param cr	- Content Resolver to use
	 * @param uri	- Uri to use
	 * @param field - value field name
	 * @param test	- "=" or "!="
	 * @param value	- value of object to retrieve
	 * @return		an ArrayList of objects
	 */
	public<T extends DatabaseObject> ArrayList<T> loadFromProvider(ContentResolver cr, Uri uri, String field, String test, String value) {
		String selection = "(" + field + test + "?)";
		String[] selectionArgs = new String[] {	value };
		return loadFromProvider(cr, uri, selection, selectionArgs);
	}

	/**
	 * Retrieve the object(s) in the database with the specified value
	 * @param cr	- Content Resolver to use
	 * @param uri	- Uri to use
	 * @param field - value field name
	 * @param value	- value of object to retrieve
	 * @return		an ArrayList of objects
	 */
	public<T extends DatabaseObject> ArrayList<T> loadFromProvider(ContentResolver cr, Uri uri, String field, String value) {
		return loadFromProvider(cr, uri, field, "=", value);
	}

	/**
	 * Retrieve the object in the database with the specified id
	 * @param cr	- Content Resolver to use
	 * @param uri	- Uri to use
	 * @param field - id field name
	 * @param id	- id of object to retrieve
	 * @return		an ArrayList of objects
	 */
	public<T extends DatabaseObject> ArrayList<T> loadIdFromProvider(ContentResolver cr, Uri uri, String field, long id) {
		return loadFromProvider(cr, uri, field, Long.toString(id));
	}

	/**
	 * Retrieve the all objects in the database excluding the specified id
	 * @param cr	- Content Resolver to use
	 * @param uri	- Uri to use
	 * @param field - id field name
	 * @param id	- id of object to exclude
	 * @return		an ArrayList of objects
	 */
	public<T extends DatabaseObject> ArrayList<T> loadFromProviderExcluding(ContentResolver cr, Uri uri, String field, String value) {
		return loadFromProvider(cr, uri, field, "!=", value);
	}

	/**
	 * Retrieve the all objects in the database excluding the specified id
	 * @param cr	- Content Resolver to use
	 * @param uri	- Uri to use
	 * @param field - id field name
	 * @param id	- id of object to exclude
	 * @return		an ArrayList of objects
	 */
	public<T extends DatabaseObject> ArrayList<T> loadFromProviderExcludingId(ContentResolver cr, Uri uri, String field, long id) {
		return loadFromProviderExcluding(cr, uri, field, Long.toString(id));

	}

	/**
	 * Retrieve the object in the database with the specified uri id
	 * @param cr	- Content Resolver to use
	 * @param uri	- Uri to use
	 * @param field - id field name
	 * @param id	- Uri to use with the id of object to retrieve
	 * @return		an ArrayList of objects
	 */
	public<T extends DatabaseObject> ArrayList<T> loadUriIdFromProvider(ContentResolver cr, Uri uri, String field, Uri id) {
		return loadIdFromProvider(cr, uri, field, ContentUris.parseId(id));
	}

	/**
	 * Update an object in the database
	 * @param cr		- Content Resolver to use
	 * @param uri		- URI to use
	 * @param field		- name of id field
	 * @param id		- Id of object to delete 
	 * @param values	- ContentValues containing updated data
	 * @return			<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	protected static boolean updateIdInProvider(ContentResolver cr, Uri uri, String field, long id, ContentValues values) {
		String selection = "(" + field + "=?)";
		String[] selectionArgs = new String[] {	Long.toString(id) };
		return (cr.update(uri, values, selection, selectionArgs) > 0);
	}

	
	/**
	 * Update an object from the content data
	 * @param obj		- Object to update or if null a new object will be created
	 * @param values	- ContentValues to extract data from
	 * @return 
	 */
	protected <T extends DatabaseObject> T updateFromValues(T obj, ContentValues values) {

		if ( obj == null ) {
			obj = (T) getNewInstance();
		}
		
		// Extract the details.
		int i;
		for ( i = numLongFields - 1; i >= 0; --i) {
			String field = longFields.get(i);
			if ( values.containsKey(field) )
				obj.saveField(field, values.getAsLong(field));
		}
		for ( i = numIntFields - 1; i >= 0; --i) {
			String field = intFields.get(i);
			if ( values.containsKey(field) )
				obj.saveField(field, values.getAsInteger(field));
		}
		for ( i = numDateFields - 1; i >= 0; --i) {
			String field = dateFields.get(i);
			if ( values.containsKey(field) )
				obj.saveField(field, getCalendar( values.getAsString(field) ));
		}
		for ( i = numDoubleFields - 1; i >= 0; --i) {
			String field = doubleFields.get(i);
			if ( values.containsKey(field) )
				obj.saveField(field, values.getAsDouble(field));
		}
		for ( i = numStringFields - 1; i >= 0; --i) {
			String field = stringFields.get(i);
			if ( values.containsKey(field) )
				obj.saveField(field, values.getAsString(field));
		}
		return obj;
	}
	
	/**
	 * Update this object from the content data
	 * @param trans		- Object to update or if null a new object will be created
	 * @param values	- ContentValues to extract data from
	 * @return			Updated object
	 */
	public <T extends DatabaseObject> T updateFromValues(ContentValues values) {
		return updateFromValues((T) this, values);
	}
	
	/**
	 * Create a new object from the content data
	 * @param values	- ContentValues to extract data from
	 * @return			new Transaction object
	 */
	public <T extends DatabaseObject> T createFromValues(ContentValues values) {
		return updateFromValues(null, values);
	}

	
	/**
	 * Create a ContentValues to represent the object
	 * @param obj		- Object to convert to a ContentValues 
	 * @return 			ContentValues 
	 */
	protected <T extends DatabaseObject> ContentValues toContentValues(T obj) {

		ContentValues values = new ContentValues();
		Class<? extends DatabaseObject> cls = obj.getClass();
		
		// Extract the details.
		int i;
		for ( i = numLongFields - 1; i >= 0; --i) {
			String field = longFields.get(i);
			try {
				long num = (Long) cls.getDeclaredField(field).get(obj);
				values.put(field, num);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for ( i = numIntFields - 1; i >= 0; --i) {
			String field = intFields.get(i);
			try {
				int num = (Integer) cls.getDeclaredField(field).get(obj);
				values.put(field, num);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for ( i = numDateFields - 1; i >= 0; --i) {
			String field = dateFields.get(i);
			try {
				GregorianCalendar cal = (GregorianCalendar) cls.getDeclaredField(field).get(obj);
				values.put(field, DatabaseManager.makeDatabaseTimestamp(cal));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for ( i = numDoubleFields - 1; i >= 0; --i) {
			String field = doubleFields.get(i);
			try {
				double num = (Double) cls.getDeclaredField(field).get(obj);
				values.put(field, num);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for ( i = numStringFields - 1; i >= 0; --i) {
			String field = stringFields.get(i);
			try {
				String str = (String) cls.getDeclaredField(field).get(obj);
				values.put(field, str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return values;
	}

	
	/**
	 * Return the specified field as a string.<br>
	 * <b>Note:</b> Dates are returned as time stamp strings.
	 * @param values	- ContentValues to retrieve field from
	 * @param field		- field to retrieve
	 * @return			string or null if field not contained in <code>values</code>
	 */
	public String contentValuesFieldToString(ContentValues values, String field) {
		String str = null;
		
		if ( values.containsKey(field) ) {
			if ( longFields.contains(field) ) {
				str = Long.toString(values.getAsLong(field));
			}
			else if ( intFields.contains(field) ) {
				str = Integer.toString(values.getAsInteger(field));
			}
			else if ( dateFields.contains(field) ) {
				str = values.getAsString(field);
			}
			else if ( doubleFields.contains(field) ) {
				str = Double.toString(values.getAsDouble(field));
			}
			else if ( stringFields.contains(field) ) {
				str = values.getAsString(field);
			}
		}
		return str;
	}
	
	/**
	 * Return the specified field as a string.<br>
	 * <b>Note:</b> Dates are returned as time stamp strings.
	 * @param field		- field to retrieve
	 * @return			string or null if field doesn't
	 */
	public String fieldToString(String field) {
		String str = null;
		
		if ( longFields.contains(field) ) {
			str = Long.toString((Long) getField(field));
		}
		else if ( intFields.contains(field) ) {
			str = Integer.toString((Integer) getField(field));
		}
		else if ( dateFields.contains(field) ) {
			GregorianCalendar cal = (GregorianCalendar) getField(field);
			str = DatabaseManager.makeDatabaseTimestamp(cal);
		}
		else if ( doubleFields.contains(field) ) {
			str = Double.toString((Double) getField(field));
		}
		else if ( stringFields.contains(field) ) {
			str = (String) getField(field);
		}
		return str;
	}

	/**
	 * Tests if the data in the object is valid
	 * @param obj		- Object to test
	 * @return 
	 */
	public <T extends DatabaseObject> boolean isValid(T obj) {

		// Extract the details.
		boolean valid = true;
		int i;
		int fields = 0;
		for ( i = numLongFields - 1; i >= 0 && valid; --i, ++fields) {
			String field = longFields.get(i);
			Long num = (Long) obj.getField(field);
			valid = (num != null);
		}
		for ( i = numIntFields - 1; i >= 0 && valid; --i, ++fields) {
			String field = intFields.get(i);
			Integer num = (Integer) obj.getField(field);
			valid = (num != null);
		}
		for ( i = numDateFields - 1; i >= 0 && valid; --i, ++fields) {
			String field = dateFields.get(i);
			Calendar date = (Calendar) obj.getField(field);
			valid = (date != null);
		}
		for ( i = numDoubleFields - 1; i >= 0 && valid; --i, ++fields) {
			String field = doubleFields.get(i);
			Double num = (Double) obj.getField(field);
			valid = (num != null);
		}
		for ( i = numStringFields - 1; i >= 0 && valid; --i, ++fields) {
			String field = stringFields.get(i);
			String str = (String) obj.getField(field);
			valid = (str != null);
		}
		if ( fields == 0 )
			valid = false;
		return valid;
	}

	/**
	 * Tests if the data in the object is valid
	 * @param obj		- Object to test
	 * @return 
	 */
	public <T extends DatabaseObject> boolean isValid() {

		return isValid(this);
	}
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ctor == null) ? 0 : ctor.hashCode());
		result = prime * result
				+ ((dateFields == null) ? 0 : dateFields.hashCode());
		result = prime * result
				+ ((doubleFields == null) ? 0 : doubleFields.hashCode());
		result = prime * result
				+ ((intFields == null) ? 0 : intFields.hashCode());
		result = prime * result
				+ ((longFields == null) ? 0 : longFields.hashCode());
		result = prime * result + numDateFields;
		result = prime * result + numDoubleFields;
		result = prime * result + numIntFields;
		result = prime * result + numLongFields;
		result = prime * result + numStringFields;
		result = prime * result
				+ ((stringFields == null) ? 0 : stringFields.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DatabaseObject))
			return false;
		DatabaseObject other = (DatabaseObject) obj;
		if (ctor == null) {
			if (other.ctor != null)
				return false;
		} else if (!ctor.equals(other.ctor))
			return false;
		if (dateFields == null) {
			if (other.dateFields != null)
				return false;
		} else if (!dateFields.equals(other.dateFields))
			return false;
		if (doubleFields == null) {
			if (other.doubleFields != null)
				return false;
		} else if (!doubleFields.equals(other.doubleFields))
			return false;
		if (intFields == null) {
			if (other.intFields != null)
				return false;
		} else if (!intFields.equals(other.intFields))
			return false;
		if (longFields == null) {
			if (other.longFields != null)
				return false;
		} else if (!longFields.equals(other.longFields))
			return false;
		if (numDateFields != other.numDateFields)
			return false;
		if (numDoubleFields != other.numDoubleFields)
			return false;
		if (numIntFields != other.numIntFields)
			return false;
		if (numLongFields != other.numLongFields)
			return false;
		if (numStringFields != other.numStringFields)
			return false;
		if (stringFields == null) {
			if (other.stringFields != null)
				return false;
		} else if (!stringFields.equals(other.stringFields))
			return false;
		return true;
	}

}
