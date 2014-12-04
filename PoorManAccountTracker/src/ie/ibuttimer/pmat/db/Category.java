/**
 * 
 */
package ie.ibuttimer.pmat.db;

import ie.ibuttimer.pmat.db.SQLiteCommandFactory.SelectionArgs;
import ie.ibuttimer.widget.TextViewAdapterBase;
import ie.ibuttimer.widget.TextViewAdapterInterface;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @author Ian Buttimer
 *
 */
public class Category extends TextViewAdapterBase implements TextViewAdapterInterface, Parcelable  {

	/* fields stored in super class:
	 * - category id
	 * - category name
	 *  */
	protected int parent;	// id of parent
	protected int level;	// level in category tree
	protected String path;	// path to this category
	protected int flags;	// flags
	
	/** Value of category parent field for root level categories */
	public static final int NO_PARENT_CATEGORY = -1;

	/** Value of category level field for root level categories */
	public static final int ROOT_LEVEL_CATEGORY = 0;

	/** Value of no flags for category */
	public static final int NO_FLAGS_CATEGORY = 0;
	/** Value of flag for Unassigned category type */
	public static final int UNASSIGNED_CATEGORY = 0x01;
	/** Value of flag for Transfer category type */
	public static final int TRANSFER_CATEGORY = 0x02;
	/** Value of flag for Income category type */
	public static final int INCOME_CATEGORY = 0x04;
	/** Value of flag for Expense category type */
	public static final int EXPENSE_CATEGORY = 0x08;
	/** Value of flag for Imbalance category type */
	public static final int IMBALANCE_CATEGORY = 0x10;
	/** Value of flag for Split category type */
	public static final int SPLIT_CATEGORY = 0x20;
	/** Value of flag for a category type root */
	public static final int ROOT_CATEGORY = 0x100;
	
	/** Category type mask */
	public static final int CATEGORY_MASK = UNASSIGNED_CATEGORY | TRANSFER_CATEGORY | INCOME_CATEGORY | EXPENSE_CATEGORY | IMBALANCE_CATEGORY | SPLIT_CATEGORY;
	/** Convenience flag of Unassigned category type root */
	public static final int UNASSIGNED_CATEGORY_ROOT = UNASSIGNED_CATEGORY | ROOT_CATEGORY;
	/** Convenience flag of Transfer category type root */
	public static final int TRANSFER_CATEGORY_ROOT = TRANSFER_CATEGORY | ROOT_CATEGORY;
	/** Convenience flag of Income category type root */
	public static final int INCOME_CATEGORY_ROOT = INCOME_CATEGORY | ROOT_CATEGORY;
	/** Convenience flag of Expense category type root */
	public static final int EXPENSE_CATEGORY_ROOT = EXPENSE_CATEGORY | ROOT_CATEGORY;
	/** Convenience flag of Imbalance category type root */
	public static final int IMBALANCE_CATEGORY_ROOT = IMBALANCE_CATEGORY | ROOT_CATEGORY;
	/** Convenience flag of Split category type root */
	public static final int SPLIT_CATEGORY_ROOT = SPLIT_CATEGORY | ROOT_CATEGORY;
	
	
	/** Path separator used in category paths, */
	public static final String PATH_SEPARATOR = ".";
	/** Regular expression for path separator used in category paths, */
	public static final String PATH_SEPARATOR_REGEX = "\\.";
	
	/**
	 * Constructor
	 * @param id		Category ID
	 * @param name		Display name
	 * @param parent	Parent category ID
	 * @param level		Category tree level
	 * @param path		Path
	 * @param flags		Flags
	 */
	public Category(int id, String name, int parent, int level, String path, int flags) {
		super(id, name);
		this.parent = parent;
		this.level = level;
		this.path = path;
		this.flags = flags;
	}

	/**
	 * Constructor
	 * @param id		Category ID
	 * @param name		Display name
	 * @param parent	Parent category ID
	 * @param level		Category tree level
	 * @param path		Path
	 */
	public Category(int id, String name, int parent, int level, String path) {
		this(id,name,parent,level,path,NO_FLAGS_CATEGORY);
	}

	/**
	 * Constructor
	 * @param id		Category ID
	 * @param name		Display name
	 * @param parent	Parent category ID
	 * @param flags		Flags
	 */
	public Category(int id, String name, int parent, int flags) {
		this(id,name,parent,ROOT_LEVEL_CATEGORY,"",flags);
	}

	/**
	 * Constructor
	 * @param id		Category ID
	 * @param level		Category tree level
	 * @param path		Path
	 * @param flags		Flags
	 */
	public Category(String name, int level, String path, int flags) {
		this(0,name,NO_PARENT_CATEGORY,level,path,flags);
	}

	/**
	 * Constructor
	 * @param id		Category ID
	 * @param name		Display name
	 * @param parent	Parent category ID
	 */
	public Category(int id, String name, int parent) {
		this(id,name,parent,ROOT_LEVEL_CATEGORY,"");
	}

	/**
	 * Constructor
	 * @param name		Display name
	 * @param level		Category tree level
	 * @param path		Path
	 */
	public Category(String name, int level, String path) {
		this(0,name,NO_PARENT_CATEGORY,level,path);
	}

	/**
	 * Constructor
	 * @param name		Display name
	 */
	public Category(String name) {
		this(0,name,NO_PARENT_CATEGORY,ROOT_LEVEL_CATEGORY,"");
	}

	/**
	 * @return the parent
	 */
	public int getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(int parent) {
		this.parent = parent;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @param level the level to set
	 * @param path the path to set
	 */
	public void setLevelPath(int level, String path) {
		this.level = level;
		this.path = path;
	}

	/**
	 * @return the flags
	 */
	public int getFlags() {
		return this.flags;
	}

	/**
	 * Return the category type flag
	 * @return the type flag
	 */
	public int getTypeFlag() {
		return this.flags & CATEGORY_MASK;
	}

	/**
	 * Return the category type flag
	 * @return the type flag
	 */
	public static int getTypeFlag(int flags) {
		return flags & CATEGORY_MASK;
	}

	/**
	 * @param flags the flags to set
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}

	/**
	 * Checks if the category root type flag is set
	 * @return true if the root type flag is set, false otherwise
	 */
	public boolean isRootTypeFlag() {
		return ((flags & ROOT_CATEGORY) != 0);
	}

	/**
	 * Make a category path
	 * @param branches	Array of strings to make path from
	 * @return			Path
	 */
	public static String makePath(String[] branches) {
		return makePath(branches, branches.length);
	}

	/**
	 * Make a category path
	 * @param branches	Array of strings to make path from
	 * @param level		Level to descend to
	 * @return			Path
	 */
	public static String makePath(String[] branches, int level) {
		StringBuffer path = new StringBuffer();
		int N = branches.length;
		if ( level < N )
			N = level;
		for ( int i = 0; i < N; ++i ) {
			if ( path.length() > 0 )
				path.append(PATH_SEPARATOR);
			if ( !TextUtils.isEmpty(branches[i]) )
				path.append(branches[i]);
		}
		return path.toString();
	}

	/**
	 * Append <code>branch</branch> to <code>path</code>
	 * @param path		Base path
	 * @param branch	Path to append
	 * @return			Extended path
	 */
	public static String appendPath(String path, String branch) {
		StringBuffer sb = new StringBuffer();
		boolean havePath = !TextUtils.isEmpty(path);
		boolean haveBranch = !TextUtils.isEmpty(branch);
		if ( havePath )
			sb.append(path);
		if ( havePath && haveBranch )
			sb.append(PATH_SEPARATOR);
		if ( haveBranch )
			sb.append(branch);
		return sb.toString();
	}

	/**
	 * Return the absolute path of this object.<br>
	 * E.g. if this is "b" and it's parent is "a", the absolute path is "a.b". 
	 * @return			Absolute path
	 */
	public String getAbsolutePath() {
		return appendPath(this.path, getName());
	}

	/**
	 * Splits <code>path</code> into its individual elements up to the specified level.<br>
	 * <b>Note:</b> If the level specified is <= 0 it is ignored and the full path split is returned.
	 * @param path		Path to split
	 * @param level		level to split to 
	 * @return			Split path
	 */
	public static String[] splitPath(String path, int level) {
		String[] splits;
		String[] branches = path.split(Category.PATH_SEPARATOR_REGEX);
		if ( (level > 0) && (level < branches.length) ) {
			splits = new String[level];
			System.arraycopy(branches, 0, splits, 0, level);
		}
		else
			splits = branches;
		return splits;
	}
	
	/**
	 * Splits <code>path</code> into its individual elements.
	 * @param path		Path to split
	 * @return			Split path
	 */
	public static String[] splitPath(String path) {
		return splitPath(path, 0);
	}
	
	/**
	 * Splits the path of <code>category</code> into its individual elements up to the specified level.<br>
	 * <b>Note:</b> If the level specified is <= 0 it is ignored and the full path split is returned.
	 * @param category	Category whose path to split
	 * @param level		level to split to
	 * @return			Split path
	 */
	public static String[] splitPath(Category category, int level) {
		return splitPath(category.path, level);
	}
	
	/**
	 * Splits the path of <code>category</code> into its individual elements up to the specified level.<br>
	 * <b>Note:</b> If the level specified is <= 0 it is ignored and the full path split is returned.
	 * @param category	Category whose path to split
	 * @param level		level to split to
	 * @return			Split path
	 */
	public String[] splitPath(int level) {
		return splitPath(this.path, level);
	}

	/**
	 * Splits the path of this object into its individual elements.
	 * @return			Split path
	 */
	public String[] splitPath() {
		return splitPath(this.path, 0);
	}

	
	/**
	 * Checks if the level of this Category is the same as the specified level
	 * @param level 	The level to check
	 * @return			true if levels are the same, false otherwise
	 */
	public boolean isSameLevel(int level) {
		return (this.level == level);
	}

	/**
	 * Checks if the level of this Category is the same as the level of the specified Category
	 * @param category 	The Category to check
	 * @return			true if levels are the same, false otherwise
	 */
	public boolean isSameLevel(Category category) {
		return (this.level == category.level);
	}

	/**
	 * Checks if the level & name of this Category is the same as the level & name of the specified Category
	 * @param category 	The Category to check
	 * @return			true if level & name are the same, false otherwise
	 */
	public boolean isSameLevelAndName(Category category) {
		return (isSameLevel(category) && (getName().compareTo(category.getName()) == 0));
	}

	/**
	 * Checks if the level & name of this Category is the same as the level & name of the specified Category
	 * @param category 	The Category to check
	 * @return			true if level & name are the same, false otherwise
	 */
	public boolean isSameLevelNameAndPath(Category category) {
		return (isSameLevelAndName(category) && (this.path.compareTo(category.path) == 0));
	}

	/**
	 * Compares the level of the two Category objects
	 * @param category1 	Category to compare
	 * @param category2 	Category to compare
	 * @return				0 if the levels are equal, a negative integer if <code>category1</code> comes before the <code>category2</code>,<br>
	 * 						or a positive integer if <code>category1</code> is comes after <code>category2</code>.
 	 */
	public static int compareLevel(Category category1, Category category2) {
		return (category1.level - category2.level);
	}

	/**
	 * Compares the level of this Category to the level of the specified Category
	 * @param category 		The Category to check
	 * @return				0 if the levels are equal, a negative integer if this comes before <code>category</code>, or a positive integer if this comes after <code>category</code>.
	 */
	public int compareLevel(Category category) {
		return compareLevel(this, category);
	}

	
	/**
	 * Retrieve all the categories in the database
	 * @param cr			- Content Resolver to use
	 * @param selection		- A filter declaring which rows to return
	 * @param selectionArgs	- Values to replace ? in <code>selection</code>
	 * @return		an ArrayList of Category objects
	 */
	private static ArrayList<Category> loadCategoriesFromProvider(ContentResolver cr, String selection, String[] selectionArgs) {
		ArrayList<Category> categories = new ArrayList<Category>();
		Cursor c = cr.query(DatabaseManager.CATEGORY_URI, null, selection, selectionArgs, null);
		if (c.moveToFirst()) {
			int idIdx = c.getColumnIndex(DatabaseManager.CATEGORY_ID);
			int nameIdx = c.getColumnIndex(DatabaseManager.CATEGORY_NAME);
			int parentIdx = c.getColumnIndex(DatabaseManager.CATEGORY_PARENT);
			int levelIdx = c.getColumnIndex(DatabaseManager.CATEGORY_LEVEL);
			int pathIdx = c.getColumnIndex(DatabaseManager.CATEGORY_PATH);
			int flagsIdx = c.getColumnIndex(DatabaseManager.CATEGORY_FLAGS);
			do {
				// Extract the details.
				Category category = new Category(c.getInt(idIdx), c.getString(nameIdx), 
						c.getInt(parentIdx), c.getInt(levelIdx), c.getString(pathIdx), c.getInt(flagsIdx));

				categories.add( category );
			} while(c.moveToNext());
		}
		c.close();
		return categories;
	}

	
	/**
	 * Retrieve all the categories in the database
	 * @param cr	- Content Resolver to use
	 * @return		an ArrayList of Category objects
	 */
	public static ArrayList<Category> loadCategoriesFromProvider(ContentResolver cr) {
		return loadCategoriesFromProvider(cr, null, null);	// return all records
	}


	/**
	 * Retrieve the category with the specified id from the database
	 * @param cr	- Content Resolver to use
	 * @param id	- id of category to retrieve
	 * @return		a Category object
	 */
	public static Category loadFromProvider(ContentResolver cr, long id) {
		ArrayList<Category> category = loadFromProvider(cr, new long[] { id });	// return all records
		if ( category.size() == 1 )
			return category.get(0);
		else 
			return null;
	}
	

	/**
	 * Retrieve the categories with the specified ids from the database
	 * @param cr	- Content Resolver to use
	 * @param ids	- ids of category to retrieve
	 * @return		an ArrayList of Category objects
	 */
	public static ArrayList<Category> loadFromProvider(ContentResolver cr, long[] ids) {
		SelectionArgs args = SQLiteCommandFactory.makeIdSelection(DatabaseManager.CATEGORY_ID, ids);
		return loadCategoriesFromProvider(cr, args.selection, args.selectionArgs);	// return all records
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + flags;
		result = prime * result + level;
		result = prime * result + parent;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		if (!(obj instanceof Category))
			return false;
		Category other = (Category) obj;
		if (flags != other.flags)
			return false;
		if (level != other.level)
			return false;
		if (parent != other.parent)
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + " [parent=" + parent + ", level=" + level + ", path="
				+ path + ", flags=" + flags + ", toString()="
				+ super.toString() + "]";
	}




	/**
	 * Compare Category objects based on follow order of precedence:<br>
	 * <ol>
	 * <li>level</li>
	 * <li>path alphabetic order</li>
	 * <li>name alphabetic order</li>
	 * </ol> 
	 * 
	 * This results in the following order:<br>
	 * <ul>
	 * <li>a</li>
	 * <li>b</li>
	 * <li>a.b</li>
	 * <li>b.a</li>
	 * <li>a.b.c</li>
	 * <li>etc.</li>
	 * </ul> 
	 * 
	 * @author Ian Buttimer
	 *
	 */
	public static class CompareLevelPathName implements Comparator<Category> {

		@Override
		public int compare(Category lhs, Category rhs) {

			if ( lhs.equals(rhs) )
				return 0;		// equal
			int res = Category.compareLevel(lhs, rhs);				// compare level
			if ( res == 0 ) {
				res = lhs.getPath().compareTo(rhs.getPath());		// compare path
				if ( res == 0 ) {
					res = lhs.getName().compareTo(rhs.getName());	// compare name
				}
			}
			return res;
		}
	};

	/**
	 * Compare Category objects based on follow order of precedence:<br>
	 * <ol>
	 * <li>type</li>
	 * <li>level</li>
	 * <li>path alphabetic order</li>
	 * <li>name alphabetic order</li>
	 * </ol> 
	 * 
	 * This results in the following order:<br>
	 * <ul>
	 * <li>a</li>
	 * <li>a.b</li>
	 * <li>a.b.c</li>
	 * <li>b</li>
	 * <li>b.a</li>
	 * <li>etc.</li>
	 * </ul> 
	 * 
	 * @author Ian Buttimer
	 *
	 */
	public static class CompareTypeLevelPathName implements Comparator<Category> {

		@Override
		public int compare(Category lhs, Category rhs) {

			if ( lhs.equals(rhs) )
				return 0;		// equal
			int res = lhs.getTypeFlag() - rhs.getTypeFlag();			// compare type
			if ( res == 0 ) {
				res = Category.compareLevel(lhs, rhs);					// compare level
				if ( res == 0 ) {
					res = lhs.getPath().compareTo(rhs.getPath());		// compare path
					if ( res == 0 ) {
						res = lhs.getName().compareTo(rhs.getName());	// compare name
					}
				}
			}
			return res;
		}
	};

	
	
	
	/* Parcelable interface - related functions */
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int writeflags) {
		super.writeToParcel(dest, writeflags);
		dest.writeInt(parent);
		dest.writeInt(level);
		dest.writeString(path);
		dest.writeInt(flags);
	}
	
	public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
		public Category createFromParcel(Parcel in) {
			return new Category(in);
		}
		
		public Category[] newArray(int size) {
			return new Category[size];
		}
	};
	
	private Category(Parcel in) {
		super(in);
		parent = in.readInt();
		level = in.readInt();
		path = in.readString();
		flags = in.readInt();
	}

	/* TextViewAdapterInterface interface related functions */
	
	@Override
	public String toDisplayString() {
		return getPrefix() + getAbsolutePath();
	};

}
