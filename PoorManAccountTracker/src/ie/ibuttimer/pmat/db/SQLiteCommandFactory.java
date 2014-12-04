package ie.ibuttimer.pmat.db;

import android.text.TextUtils;
import ie.ibuttimer.pmat.util.Logger;

/**
 * Convenience class to generate SQLite SQL statements.<br>
 * <b>Note 1:</b> This class does not support all possible SQLite SQL strings.<br>
 * <b>Note 2:</b> This class does not guarantee a valid SQL statement, it generates what it's told too!
 * 
 * @see	<a href="http://www.sqlite.org">SQLite</a>
 * @author Ian Buttimer
 *
 */
public class SQLiteCommandFactory {

	
	public static class FieldDefinition {
		
		/** Add 'TEMPORARY' to create table command. */
		public static int TEMPORARY 		= 0x00000001;
		/** Add 'IF NOT EXIST' to create table command. */
		public static int IF_NOT_EXIST 		= 0x00000002;
		/** Add 'IF EXISTS' to drop table command. */
		public static int IF_EXISTS 		= 0x00000004;
		/** Column type is a signed integer, stored in 1, 2, 3, 4, 6, or 8 bytes depending on the magnitude of the value. */
		public static int INTEGER_VALUE 	= 0x00000008;
		/** Column type is a floating point value, stored as an 8-byte IEEE floating point number. */
		public static int REAL_VALUE 		= 0x00000010;
		/** Column type is a text string, stored using the database encoding (UTF-8, UTF-16BE or UTF-16LE). */
		public static int TEXT_VALUE 		= 0x00000020;
		/** Column type is a blob of data, stored exactly as it was input. */
		public static int BLOB_VALUE 		= 0x00000040;
		/** Column is primary key. */
		public static int PRIMARY_KEY 		= 0x00000080;
		/** Column is primary key ascending. */
		public static int PRIMARY_KEY_ASC 	= 0x00000100;
		/** Column is primary key descending. */
		public static int PRIMARY_KEY_DESC 	= 0x00000200;
		/** Column auto-increments. */
		public static int AUTOINCREMENT 	= 0x00000400;
		/** Column cannot be null. */
		public static int NOT_NULL		 	= 0x00000800;
		/** Column must be unique. */
		public static int UNIQUE		 	= 0x00001000;
		/** Column references another table. */
		public static int REFERENCES	 	= 0x00002000;
		/** Column default value. */
		public static int DEFAULT	 		= 0x00004000;
		/** Column collate binary, (Compares string data using memcmp()). */
		public static int COLLATE_BINARY	= 0x00008000;
		/** Column collate no case, (The same as binary, except the 26 upper case characters of ASCII are folded to their lower case equivalents before the comparison is performed). */
		public static int COLLATE_NOCASE	= 0x00010000;
		/** Column collate rtrim, (The same as binary, except that trailing space characters are ignored). */
		public static int COLLATE_RTRIM		= 0x00020000;
		/** Unique constraint on a table. */
		public static int UNIQUE_CONSTRAINT	= 0x00040000;
		/** Check between constraint on a column. */
		public static int CHECK_BETWEEN_CONSTRAINT	= 0x00080000;
		/** Check in constraint on a column. */
		public static int CHECK_IN_CONSTRAINT	= 0x00100000;

		/** Convenience definition for 'integer primary key ascending'. */
		public static int INTEGER_PRIMARY_KEY_ASC = INTEGER_VALUE | PRIMARY_KEY_ASC;
		/** Convenience definition for 'integer unique'. */
		public static int INTEGER_UNIQUE = INTEGER_VALUE | UNIQUE;
		/** Convenience definition for 'integer not null'. */
		public static int INTEGER_NOT_NULL = INTEGER_VALUE | NOT_NULL;
		/** Convenience definition for 'integer default'. */
		public static int INTEGER_DEFAULT = INTEGER_VALUE | DEFAULT;
		/** Convenience definition for 'integer check in'. */
		public static int INTEGER_CHECK_IN_CONSTRAINT = INTEGER_VALUE | CHECK_IN_CONSTRAINT;
		/** Convenience definition for 'integer between'. */
		public static int INTEGER_CHECK_BETWEEN_CONSTRAINT = INTEGER_VALUE | CHECK_BETWEEN_CONSTRAINT;
		/** Convenience definition for 'text default'. */
		public static int TEXT_DEFAULT = TEXT_VALUE | DEFAULT;
		/** Convenience definition for 'real default'. */
		public static int REAL_DEFAULT = REAL_VALUE | DEFAULT;
		/** Convenience definition for 'text not null'. */
		public static int TEXT_NOT_NULL = TEXT_VALUE | NOT_NULL;
		/** Convenience definition for 'blob not null'. */
		public static int BLOB_NOT_NULL = BLOB_VALUE | NOT_NULL;
		/** Convenience definition for 'text not null collate nocase'. */
		public static int TEXT_NOT_NULL_COLLATE_NOCASE = TEXT_NOT_NULL | COLLATE_NOCASE;
		/** Convenience definition for 'integer references'. */
		public static int INTEGER_REFERENCES = INTEGER_VALUE | REFERENCES;
		
		protected String name;
		protected int definition;
		protected String refTable;
		protected String[] refField;
		protected String defaultValue;
		
		public FieldDefinition(String name, int definition, String refTable,
				String[] refField, String defaultValue) {
			super();
			this.name = name;
			this.definition = definition;
			this.refTable = refTable;
			this.refField = refField;
			this.defaultValue = defaultValue;
		}
		
		public FieldDefinition(String name, int definition, String refTable,
				String[] refField) {
			this(name,(definition & ~DEFAULT),refTable,refField,null);
		}
		
		public FieldDefinition(String name, int definition, String[] refField) {
			this(name,(definition & ~DEFAULT),null,refField,null);
		}
		
		public FieldDefinition(String name, int definition, String defaultValue) {
			this(name,(definition & ~REFERENCES),null,null,defaultValue);
		}

		public FieldDefinition(String name, int definition) {
			this(name,(definition & ~REFERENCES),null,null);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + definition;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result
					+ ((refField == null) ? 0 : refField.hashCode());
			result = prime * result
					+ ((refTable == null) ? 0 : refTable.hashCode());
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
			if (!(obj instanceof FieldDefinition))
				return false;
			FieldDefinition other = (FieldDefinition) obj;
			if (definition != other.definition)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (refField == null) {
				if (other.refField != null)
					return false;
			} else if (!refField.equals(other.refField))
				return false;
			if (refTable == null) {
				if (other.refTable != null)
					return false;
			} else if (!refTable.equals(other.refTable))
				return false;
			return true;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
	}
	
	
	public SQLiteCommandFactory() {
		super();
	}

	/**
	 * Generate an SQLite 'CREATE TABLE' string.<br>
	 * <b>Note:</b> This function does not create all possible 'CREATE TABLE' strings. 
	 * @param table				Table name
	 * @param tableDefinition	Table definition
	 * @param fields			Field definitions 
	 * @return					SQL string
	 * @see				<a href="http://www.sqlite.org/lang_createtable.html">CREATE TABLE As Understood By SQLite</a>
	 */
	static String getCreateTable(String table, int tableDefinition, FieldDefinition[] fields) {
		
		// "CREATE TEMPORARY TABLE table IF NOT EXISTS "
		StringBuffer cmdBuffer = new StringBuffer( "CREATE " );

		if ( (tableDefinition & FieldDefinition.TEMPORARY) != 0 )
			cmdBuffer.append("TEMPORARY ");
		
		cmdBuffer.append("TABLE " + table + " ");

		if ( (tableDefinition & FieldDefinition.IF_NOT_EXIST) != 0 )
			cmdBuffer.append("IF NOT EXISTS ");

		// add field definitions
		if ( fields.length > 0 ) {
	
			// e.g. "( field1 INTEGER PRIMARY KEY ASC , field2 INTEGER , field3 TEXT NOT NULL , UNIQUE( field2, field3 ) )"
			cmdBuffer.append(" ( ");
			
			for ( int i = 0; i < fields.length; ++i ) {

				FieldDefinition field = fields[i];
				
				if ( i > 0 )
					cmdBuffer.append(", ");
				
				if ( (field.definition & FieldDefinition.UNIQUE_CONSTRAINT) != 0 ) {
					cmdBuffer.append("UNIQUE ");
					// add the reference fields
					String fieldList = makeFieldList(field);
					if ( fieldList != null )
						cmdBuffer.append(fieldList);
				}
				else {
					// add column name
					cmdBuffer.append(field.name + " ");
				
					// add column type
					if ( (field.definition & FieldDefinition.INTEGER_VALUE) != 0 )
						cmdBuffer.append("INTEGER ");
					else if ( (field.definition & FieldDefinition.REAL_VALUE) != 0 )
						cmdBuffer.append("REAL ");
					else if ( (field.definition & FieldDefinition.TEXT_VALUE) != 0 )
						cmdBuffer.append("TEXT ");
					else if ( (field.definition & FieldDefinition.BLOB_VALUE) != 0 )
						cmdBuffer.append("BLOB ");
				}
				
				// add column constraint
				if ( (field.definition & 
						(FieldDefinition.PRIMARY_KEY|FieldDefinition.PRIMARY_KEY_ASC|FieldDefinition.PRIMARY_KEY_DESC)) != 0 ) {

					if ( (field.definition & FieldDefinition.PRIMARY_KEY) != 0 )
						cmdBuffer.append("PRIMARY KEY ");
					else if ( (field.definition & FieldDefinition.PRIMARY_KEY_ASC) != 0 )
						cmdBuffer.append("PRIMARY KEY ASC ");
					else
						cmdBuffer.append("PRIMARY KEY DESC ");

					if ( (field.definition & FieldDefinition.AUTOINCREMENT) != 0 )
						cmdBuffer.append("AUTOINCREMENT ");
				}
				
				if ( (field.definition & FieldDefinition.NOT_NULL) != 0 )
					cmdBuffer.append("NOT NULL ");
				
				if ( (field.definition & FieldDefinition.COLLATE_BINARY) != 0 )
					cmdBuffer.append("COLLATE BINARY ");
				else if ( (field.definition & FieldDefinition.COLLATE_NOCASE) != 0 )
					cmdBuffer.append("COLLATE NOCASE ");
				else if ( (field.definition & FieldDefinition.COLLATE_RTRIM) != 0 )
					cmdBuffer.append("COLLATE RTRIM ");
				

				if ( (field.definition & FieldDefinition.UNIQUE) != 0 )
					cmdBuffer.append("UNIQUE ");
				
				if ( (field.definition & FieldDefinition.DEFAULT) != 0 ) {
					// "DEFAULT NULL" or "DEFAULT "" " or "DEFAULT value"
					cmdBuffer.append("DEFAULT ");
					if ( field.defaultValue == null )
						cmdBuffer.append("NULL ");	// specify null if no value
					else if ( field.defaultValue.isEmpty() )
						cmdBuffer.append("\"\" ");	// specify empty string if its an empty string
					else
						cmdBuffer.append(field.defaultValue + " ");	// otherwise assume they know what they're doing
				}
				
				if ( (field.definition & FieldDefinition.REFERENCES) != 0 ) {
					// "REFERENCES ( field1, field2 )"
					if ( !TextUtils.isEmpty(field.refTable) ) {
						cmdBuffer.append("REFERENCES " + field.refTable + " ");
						// add the reference fields
						String fieldList = makeFieldList(field);
						if ( fieldList != null )
							cmdBuffer.append(fieldList);
					}
				}
				
				if ( (field.definition & FieldDefinition.CHECK_BETWEEN_CONSTRAINT) != 0 ) {
					// "CHECK (field BETWEEN value1 AND value2)"
					cmdBuffer.append("CHECK (" + field.name + " BETWEEN ");
					// add the check values
					int count = 0;
					final int N = field.refField.length;
					for ( int f = 0; f < N; ++f ) {
						if ( !TextUtils.isEmpty(field.refField[f]) ) {
							if ( count > 0 )
								cmdBuffer.append(" AND ");
							cmdBuffer.append(field.refField[f]);
							++count;
						}
					}
					cmdBuffer.append(") ");
				}
				
				if ( (field.definition & FieldDefinition.CHECK_IN_CONSTRAINT) != 0 ) {
					// "CHECK IN (field1, field2)"
					cmdBuffer.append("CHECK IN (");
					// add the check values
					String fieldList = makeFieldList(field);
					if ( fieldList != null )
						cmdBuffer.append(fieldList);
					cmdBuffer.append(") ");
				}
				
				
			}

			cmdBuffer.append(" )");
		}
		
		String cmd = cmdBuffer.toString();

		Logger.d(cmd);

		return cmd;
	}

	/**
	 * Make a list of fields in the form "( field1, field2 )"
	 * @param field		FieldDefinition to use
	 * @return			List of fields as String
	 */
	private static String makeFieldList( FieldDefinition field ) {

		String list = null;
		if ( field.refField != null ) {
			StringBuffer fldBuffer = new StringBuffer();
			int count = 0;
			final int N = field.refField.length;
			for ( int f = 0; f < N; ++f ) {
				if ( !TextUtils.isEmpty(field.refField[f]) ) {
					if ( count > 0 )
						fldBuffer.append(", ");
					fldBuffer.append(field.refField[f]);
					++count;
				}
			}
			
			if ( fldBuffer.length() > 0 )
				list = "( " + fldBuffer.toString() + " ) ";
		}
		return list;
	}
	
	
	/**
	 * Generate an SQLite 'CREATE TABLE' string.<br>
	 * <b>Note:</b> This function does not create all possible 'CREATE TABLE' strings. 
	 * @param table		Table name
	 * @return			SQL string
	 * @see				<a href="http://www.sqlite.org/lang_createtable.html">CREATE TABLE As Understood By SQLite</a>
	 */
	static String getCreateTable(String table, FieldDefinition[] fields) {
		
		return getCreateTable(table, 0, fields);
	}

	/**
	 * Generate an SQLite 'DROP TABLE' string.<br>
	 * <b>Note:</b> This function does not create all possible 'DROP TABLE' strings. 
	 * @param table				Table name
	 * @param tableDefinition	Table definition
	 * @return					SQL string
	 * @see				<a href="http://www.sqlite.org/lang_droptable.html">DROP TABLE As Understood By SQLite</a>
	 */
	static String getDropTable(String table, int tableDefinition) {
		
		StringBuffer cmdBuffer = new StringBuffer( "DROP TABLE " );

		if ( (tableDefinition & FieldDefinition.IF_EXISTS) != 0 )
			cmdBuffer.append("IF EXISTS ");

		cmdBuffer.append(table);

		String cmd = cmdBuffer.toString();

		Logger.d(cmd);

		return cmd;
	}
	
	
	/**
	 * Generate a fully qualified column name 
	 * @param table		Table the column is in
	 * @param column	Column name
	 * @return			Qualified column name of the form "table.column"
	 */
	static String getQualifiedColumn(String table, String column) {
		return table + "." + column;
	}
	
	
	/**
	 * Generates the 'join' portion of an SQL 'SELECT JOIN' command.
	 * @param tables		Tables to select from
	 * @param join			Type of join; "INNER"/"OUTER"
	 * @param joinColumns	Columns to join on
	 * @return				SQL string of the form 't1 INNER/OUTER JOIN t2 ON t1.c2 = t2.c3 INNER/OUTER JOIN t3 ON t1.c3 = t3.c1"
	 */
	private static String getMultiJoinCommand(String[][] tables, String join, String[][] joinColumns) {
		
		StringBuffer cmdBuffer = new StringBuffer(tables[0][0]);
		final int T = tables.length;
		for ( int t = 0; t < T; ++t ) {

			// check arguments are correct
			if ( tables[t].length != 2 ) {
				Logger.w("getJoinCommand invalid number of tables for join " + t +": " + tables[t].length);
				throw new IllegalArgumentException();
			}
			int joinLen = joinColumns[t].length;
			if ( joinColumns[t].length != 2 ) {
				Logger.w("getJoinCommand invalid number of join columns for join " + t +": " + joinLen);
				throw new IllegalArgumentException();
			}
			
			cmdBuffer.append(" " + join + " JOIN " + tables[t][1] + " ON ");

			// add the join
			for ( int i = 0; i < joinLen; ++i ) {
				if ( !TextUtils.isEmpty(joinColumns[t][i]) ) {
					if ( i > 0 )
						cmdBuffer.append("= ");
					cmdBuffer.append(getQualifiedColumn(tables[t][i],joinColumns[t][i]) + " ");
				}
				else {
					Logger.w("getSelectInnerJoin null or empty join column");
					throw new IllegalArgumentException();
				}
			}
		}

		String cmd = cmdBuffer.toString();

		Logger.d(cmd);

		return cmd;
	}

	/**
	 * Generates the 'join' portion of an SQL 'SELECT JOIN' command.
	 * @param tables		Tables to select from
	 * @param joinColumns	Columns to join on
	 * @return				SQL string of the form 't1 INNER/OUTER JOIN t2 ON t1.c2 = t2.c3 INNER/OUTER JOIN t3 ON t1.c3 = t3.c1"
	 */
	static String getMultiJoinCommand(String[][] tables, String[][] joinColumns) {
		
		return getMultiJoinCommand(tables, "INNER", joinColumns);
	}

	
	/**
	 * Generates the 'join' portion of an SQL 'SELECT JOIN' command.
	 * @param tables		Tables to select from
	 * @param join			Type of join; "INNER"/"OUTER"
	 * @param joinColumns	Columns to join on
	 * @return				SQL string of the form 't1 INNER/OUTER JOIN t2 ON t1.c2 = t2.c3"
	 */
	private static String getJoinCommand(String[] tables, String join, String[] joinColumns) {
		
		StringBuffer cmdBuffer = new StringBuffer( tables[0] + " " + join + " JOIN " + tables[1] + " ON " );

		// check arguments are correct
		if ( tables.length != 2 ) {
			Logger.w("getJoinCommand invalid number of tables " + tables.length);
			throw new IllegalArgumentException();
		}
		if ( joinColumns.length != 2 ) {
			Logger.w("getJoinCommand invalid number of join columns " + joinColumns.length);
			throw new IllegalArgumentException();
		}

		
		// add the join
		for ( int i = 0; i < joinColumns.length; ++i ) {
			if ( !TextUtils.isEmpty(joinColumns[i]) ) {
				if ( i > 0 )
					cmdBuffer.append("= ");
				cmdBuffer.append(getQualifiedColumn(tables[i],joinColumns[i]) + " ");
			}
			else {
				Logger.w("getSelectInnerJoin null or empty join column");
				throw new IllegalArgumentException();
			}
		}

		String cmd = cmdBuffer.toString();

		Logger.d(cmd);

		return cmd;
	}

	
	/**
	 * Generates the 'join' portion of an SQL 'SELECT JOIN' command.
	 * @param tables		Tables to select from
	 * @param joinColumns	Columns to join on
	 * @return				SQL string of the form 't1 INNER JOIN t2 ON t1.c2 = t2.c3"
	 */
	static String getInnerJoinCommand(String[] tables, String[] joinColumns) {
		
		return getJoinCommand(tables, "INNER", joinColumns);
	}
	
	/**
	 * Generates a list of columns that may be used as the projection for an SQL 'SELECT' command.
	 * @param tables		Tables to select from
	 * @param columns		Columns to select from tables
	 * @return				SQL string of the form 't1.c1 t2.c2"
	 */
	static String getColumnList(String[] tables, String[][] columns) {
		
		StringBuffer cmdBuffer = new StringBuffer();

		// check arguments are correct
		final int N = tables.length;
		final int M = tables.length;
		if ( N != M ) {
			String str = "getColumnList number of tables " + N + " and number of columns " + M + " do not match.";
			Logger.w(str);
			throw new IllegalArgumentException(str);
		}

		// add all the columns as fully qualified entities 
		int count = 0;
		for ( int i = 0; i < N; ++i ) {
			if ( !TextUtils.isEmpty(tables[i]) ) {
				if ( (columns[i] != null) ) {
					for ( int j = 0; j < columns[i].length; ++j ) {
						if ( !TextUtils.isEmpty(columns[i][j]) ) {
							if ( count++ > 0 )
								cmdBuffer.append(", ");
							cmdBuffer.append(getQualifiedColumn(tables[i],columns[i][j]) + " ");
						}
						else {
							Logger.w("getColumnList null or empty column");
							throw new IllegalArgumentException();
						}
					}
				}
				else {
					// treat null as 'all columns'
					if ( count++ > 0 )
						cmdBuffer.append(", ");
					cmdBuffer.append(getQualifiedColumn(tables[i],"*") + " ");
				}
			}
			else {
				String str = "getColumnList null or empty table at index " + i;
				Logger.w(str);
				throw new IllegalArgumentException(str);
			}
		}
		
		String cmd = cmdBuffer.toString();

		Logger.d(cmd);

		return cmd;
	}

	/**
	 * Generates an array columns that may be used as the projection for an SQL 'SELECT' command.
	 * @param tables		Tables to select from
	 * @param columns		Columns to select from tables
	 * @return				Array of strings of the form 't1.c1"
	 */
	static String[] getProjection(String[] tables, String[][] columns) {
		
		String list = getColumnList(tables, columns);
		String[] projection;
		
		if ( !TextUtils.isEmpty(list) ) {
			int count = 1;
			int start = 0;
			int index;

			// each comma indicates an additional column
			while ( (index = list.indexOf(',', start)) >= 0 ) {
				++count;
				start = index + 1;
			}
			
			projection = new String[count];

			count = 0;
			start = 0;
			while ( (index = list.indexOf(',', start)) >= 0 ) {
				projection[count] = list.substring(start, index).trim();
				start = index + 1;
				++count;
			}
			if ( count < projection.length )
				projection[count] = list.substring(start).trim();	// add the last bit
		}
		else
			projection = null;
		
		return projection;
	}

	/**
	 * Generates an SQL 'SELECT INNER JOIN' command.
	 * @param tables		Tables to select from
	 * @param columns		Columns to select from tables
	 * @param join			Columns to join on
	 * @param selection		A selection criteria to apply when filtering rows. If null then all rows are included.
	 * @param selectionArgs	You may include ?s in selection, which will be replaced by the values from selectionArgs, 
	 * 						in order that they appear in the selection. The values will be bound as Strings. 
	 * @param order			Optional ORDER BY statement without the 'ORDER BY'
	 * @return				SQL string of the form 'SELECT a.c1 b.c2 FROM a INNER JOIN b ON a.c2 = b.c3;"
	 */
	static String getSelectInnerJoin(String[] tables, String[][] columns, String[] join, String selection, 
			String[] selectionArgs, String order) {
		
		StringBuffer cmdBuffer = new StringBuffer( "SELECT " );

		// check arguments are correct
		if ( tables.length != 2 ) {
			Logger.w("getSelectInnerJoin invalid number of tables " + tables.length);
			throw new IllegalArgumentException();
		}
		if ( columns.length != 2 ) {
			Logger.w("getSelectInnerJoin invalid number of columns " + columns.length);
			throw new IllegalArgumentException();
		}
		if ( join.length != 2 ) {
			Logger.w("getSelectInnerJoin invalid number of joins " + join.length);
			throw new IllegalArgumentException();
		}
		if ( !TextUtils.isEmpty(selection) ) {
			if ( selection.contains("?") && (selectionArgs == null) ) {
				Logger.w("getSelectInnerJoin missing selection arguments");
				throw new IllegalArgumentException();
			}
		}

		// add all the columns as fully qualified entities 
		cmdBuffer.append( getColumnList(tables, columns) );

		// add the join
		cmdBuffer.append("FROM " + getInnerJoinCommand(tables, join) );

		// add the where
		if ( !TextUtils.isEmpty(selection) ) {
			cmdBuffer.append( "WHERE " );
			
			int start = 0;
			int index;
			int i = 0;
			while ( (index = selection.indexOf('?', start)) >= 0 ) {
				cmdBuffer.append( selection.substring(start, index) );
				
				if ( (selectionArgs != null) && (i < selectionArgs.length) ) {
					cmdBuffer.append( selectionArgs[i] );
					start = index + 1;
				}
				else {
					Logger.w("getSelectInnerJoin insufficient arguments");
					throw new IllegalArgumentException();
				}
			}
			if ( start < selection.length() )
				cmdBuffer.append( selection.substring(start) );
			
			cmdBuffer.append( " " );
		}
		
		// add the order by
		if ( !TextUtils.isEmpty(order) )
			cmdBuffer.append( "ORDER BY " + order);

		cmdBuffer.append(";");

		String cmd = cmdBuffer.toString();

		Logger.d(cmd);

		return cmd;
	}
	

	/**
	 * Return a row count sql
	 * @param table	- table to query
	 * @return
	 */
	public static String getRowCount(String table) {
		return "SELECT COUNT(*) FROM " + table + ";";
	}
	
	/**
	 * Class to to return selections and selection arguments strings. 
	 * @author Ian Buttimer
	 *
	 */
	public static class SelectionArgs {
		public String selection;
		public String[] selectionArgs;
		/**
		 * @param selection
		 * @param selectionArgs
		 */
		public SelectionArgs(String selection, String[] selectionArgs) {
			super();
			this.selection = selection;
			this.selectionArgs = selectionArgs;
		}
	}
	
	private enum FilterType { INCLUSION_FILTER, EXCLUSION_FILTER };
	/**
	 * Generates a filter of the form 'field =/!= ?'/id arguments for an SQL selection.
	 * @param fields	- field names to use
	 * @param ids		- ids to use
	 * @return			a SelectionArgs object
	 */
	private static SelectionArgs makeFieldFilter(String[] fields, String[] values, String method, FilterType type) {
		
		final int N = fields.length;
		if ( values.length != N ) {
			Logger.w(method + " length of arrays do not match; " + N + " " + values.length);
			throw new IllegalArgumentException();
		}
		String test;
		String join;
		if ( type == FilterType.INCLUSION_FILTER ) {
			test = "=";
			join = " OR";
		}
		else {
			test = "!=";
			join = " AND";
		}

		StringBuffer selection = new StringBuffer("(");
		String[] selectionArgs = new String[N];
		for ( int i = 0; i < N; ++i ) {
			if ( i > 0 )
				selection.append(join);
			selection.append(" " + fields[i] + test + "?");
			selectionArgs[i] = values[i];
		}
		selection.append(")");

		return new SelectionArgs(selection.toString(), selectionArgs);
	}
	
	/**
	 * Generates a selection filter of the form 'field = ?'/id arguments for an SQL selection.
	 * @param fields	- field names to use
	 * @param ids		- ids to use
	 * @return			a SelectionArgs object
	 */
	public static SelectionArgs makeFieldSelectionFilter(String[] fields, String[] values) {
		return makeFieldFilter(fields, values, "makeFieldSelectionFilter", FilterType.INCLUSION_FILTER);
	}
	
	/**
	 * Generates an exclusion filter of the form 'field != ?'/id arguments for an SQL selection.
	 * @param fields	- field names to use
	 * @param ids		- ids to use
	 * @return			a SelectionArgs object
	 */
	public static SelectionArgs makeFieldExclusionFilter(String[] fields, String[] values) {
		return makeFieldFilter(fields, values, "makeFieldExclusionFilter", FilterType.EXCLUSION_FILTER);
	}

	/**
	 * Generates 'field = ?'/id arguments for an SQL selection.
	 * @param fields	- field names to use
	 * @param ids		- ids to use
	 * @return			a SelectionArgs object
	 */
	public static SelectionArgs makeIdSelection(String[] fields, long[] ids) {
		
		final int N = ids.length;
		if ( fields.length != N ) {
			Logger.w("makeIdSelection length of arrays do not match; " + N + " " + fields.length);
			throw new IllegalArgumentException();
		}

		String[] values = new String[ids.length];
		for ( int i = 0; i < N; ++i )
			values[i] = Long.toString(ids[i]);

		return makeFieldSelectionFilter(fields, values);
	}
	
	/**
	 * Generates 'field = ?'/id arguments for an SQL selection.
	 * @param field		- field name to use
	 * @param ids		- ids to use
	 * @return			a SelectionArgs object
	 */
	public static SelectionArgs makeIdSelection(String field, long[] ids) {
		
		final int N = ids.length;
		String[] fields = new String[N];
		for ( int i = 0; i < N; ++i )
			fields[i] = field;

		return makeIdSelection(fields, ids);
	}

	/**
	 * Generates 'field = ?'/id arguments for an SQL selection.
	 * @param fields	- field names to use
	 * @param id		- id to use
	 * @return			a SelectionArgs object
	 */
	public static SelectionArgs makeIdSelection(String[] fields, long id) {
		
		final int N = fields.length;
		long[] ids = new long[N];
		for ( int i = 0; i < N; ++i )
			ids[i] = id;

		return makeIdSelection(fields, ids);
	}
	
	/**
	 * Generates 'field = ?'/id arguments for an SQL selection.
	 * @param field		- field name to use
	 * @param id		- id to use
	 * @return			a SelectionArgs object
	 */
	public static SelectionArgs makeIdSelection(String field, long id) {
		return makeFieldSelectionFilter(new String[] { field }, new String[] { Long.toString(id) } );
	}
	
	/**
	 * Generates 'table1.field1 = table2.field2' arguments for an SQL selection.
	 * @param tables	- table names to use
	 * @param fields	- field names to use
	 * @return			a SelectionArgs object
	 */
	public static String makeFieldSelection(String[] tables, String[] fields) {
		
		final int N = tables.length;
		if ( fields.length != N ) {
			Logger.w("makeFieldSelection length of arrays do not match; " + N + " " + fields.length);
			throw new IllegalArgumentException();
		}
		if ( N % 2 != 0) {
			Logger.w("makeFieldSelection uneven array length; " + N);
			throw new IllegalArgumentException();
		}
		
		StringBuffer selection = new StringBuffer("(");
		for ( int i = 0; i < N; ++i ) {
			if ( i > 0 && i % 2 == 0 )
				selection.append(" AND");
			else if ( i % 2 != 0 )
				selection.append("=");
			if ( !TextUtils.isEmpty(tables[i]) )
				selection.append(" " + getQualifiedColumn(tables[i], fields[i]));
			else
				selection.append(fields[i]);
		}
		selection.append(")");

		return selection.toString();
	}

	
}
