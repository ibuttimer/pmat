/**
 * 
 */
package ie.ibuttimer.pmat.db;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

/**
 * @author Ian Buttimer
 *
 */
public class User extends DatabaseObject {

	/* Note: variables have to protected to allow DatabaseObject.saveField() to access them */
	protected long userId;				// id
	protected String userPhone;			// phone number
	protected String userName;			// display name
	protected String userPass;			// password
	protected String userChallenge;		// challenge
	protected String userResponse;		// response

	protected static ArrayList<String> clsLongFields;
	protected static ArrayList<String> clsIntFields;
	protected static ArrayList<String> clsDateFields;
	protected static ArrayList<String> clsDoubleFields;
	protected static ArrayList<String> clsStringFields;
	
	static {
		clsLongFields = new ArrayList<String>();
		clsLongFields.add(DatabaseManager.USER_ID);
		
		clsIntFields = new ArrayList<String>();

		clsDateFields = new ArrayList<String>();

		clsDoubleFields = new ArrayList<String>();

		clsStringFields = new ArrayList<String>();
		clsStringFields.add(DatabaseManager.USER_PHONE);
		clsStringFields.add(DatabaseManager.USER_NAME);
		clsStringFields.add(DatabaseManager.USER_PASS);
		clsStringFields.add(DatabaseManager.USER_CHALLENGE);
		clsStringFields.add(DatabaseManager.USER_RESPONSE);
	}
	private static final Uri uri = DatabaseManager.USER_URI;
	
	
	/** Since there is no reliable method of retrieving your phone number (due to number porting etc.)
	 * the default user's phone number is zero. */
	public static final int DEFAULT_PHONE_NUMBER = 0;		

	
	
	/**
	 * Constructor 
	 */
	public User() {
		super(User.class);
		setFields(clsLongFields, clsIntFields, clsDateFields, clsDoubleFields, clsStringFields);
	}

	/**
	 * Constructor for the user in the database with the specified id
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of user to retrieve
	 */
	public User(ContentResolver cr, long id) {
		this();
		User db = loadFromProvider(cr, id);
		if ( db != null ) {
			ContentValues values = toContentValues(db);
			this.updateFromValues(values);
		}
	}

	/**
	 * Constructor for the user in the database with the specified phone number
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of user to retrieve
	 */
	public User(ContentResolver cr, String phone) {
		this();
		User db = loadFromProvider(cr, phone);
		if ( db != null ) {
			ContentValues values = toContentValues(db);
			this.updateFromValues(values);
		}
	}

	/**
	 * Delete a user from the database
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of Object to delete 
	 * @return		<code>true</code> if successfully deleted, <code>false</code> otherwise
	 */
	public static boolean deleteUserFromProvider(ContentResolver cr, long id) {
		return deleteIdFromProvider(cr, uri, DatabaseManager.USER_ID, id);
	}

	/**
	 * Retrieve the user in the database with the specified id
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of user to retrieve
	 * @return		an Account object
	 */
	public User loadFromProvider(ContentResolver cr, long id) {
		ArrayList<User> list = loadIdFromProvider(cr, uri, DatabaseManager.USER_ID, id);
		if ( list.size() == 1 )
			return list.get(0);
		else
			return null;
	}
	
	/**
	 * Retrieve the user in the database with the specified phone number
	 * @param cr	- Content Resolver to use
	 * @param phone	- phone of user to retrieve
	 * @return		an Account object
	 */
	public User loadFromProvider(ContentResolver cr, String phone) {
		ArrayList<User> list = loadFromProvider(cr, uri, DatabaseManager.USER_PHONE, phone);
		if ( list.size() == 1 )
			return list.get(0);
		else
			return null;
	}
	
	/**
	 * Retrieve all the users in the database excluding the specified id
	 * @param cr	- Content Resolver to use
	 * @param id	- Id of user to exclude
	 * @return		an Account object
	 */
	public ArrayList<User> loadFromProviderExcluding(ContentResolver cr, long id) {
		return loadFromProviderExcludingId(cr, uri, DatabaseManager.USER_PHONE, id);
	}

	/**
	 * Retrieve all the users in the database excluding the user with the specified phone
	 * @param cr	- Content Resolver to use
	 * @param phone	- phone of user to exclude
	 * @return		an Account object
	 */
	public ArrayList<User> loadFromProviderExcluding(ContentResolver cr, String phone) {
		return loadFromProviderExcluding(cr, uri, DatabaseManager.USER_PHONE, phone);
	}

	/**
	 * Retrieve all the objects in the database
	 * @param cr			- Content Resolver to use
	 * @return				an ArrayList of objects
	 */
	public ArrayList<User> loadAllFromProvider(ContentResolver cr) {
		return loadFromProvider(cr, uri, (String)null, (String[])null);
	}

	/**
	 * @return the userId
	 */
	public long getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(long userId) {
		this.userId = userId;
	}

	/**
	 * @return the userPhone
	 */
	public String getUserPhone() {
		return userPhone;
	}

	/**
	 * @param userPhone the userPhone to set
	 */
	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the userPass
	 */
	public String getUserPass() {
		return userPass;
	}

	/**
	 * @param userPass the userPass to set
	 */
	public void setUserPass(String userPass) {
		this.userPass = userPass;
	}

	/**
	 * @return the userChallenge
	 */
	public String getUserChallenge() {
		return userChallenge;
	}

	/**
	 * @param userChallenge the userChallenge to set
	 */
	public void setUserChallenge(String userChallenge) {
		this.userChallenge = userChallenge;
	}

	/**
	 * @return the userResponse
	 */
	public String getUserResponse() {
		return userResponse;
	}

	/**
	 * @param userResponse the userResponse to set
	 */
	public void setUserResponse(String userResponse) {
		this.userResponse = userResponse;
	}

	@Override
	public Object clone () {
		User user = new User();
		user.userId = userId;				// id
		user.userPhone = userPhone;			// phone number
		user.userName = userName;			// display name
		user.userPass = userPass;			// password
		user.userChallenge = userChallenge;		// challenge
		user.userResponse = userResponse;		// response
		return user;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((userChallenge == null) ? 0 : userChallenge.hashCode());
		result = prime * result + (int) (userId ^ (userId >>> 32));
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		result = prime * result
				+ ((userPass == null) ? 0 : userPass.hashCode());
		result = prime * result
				+ ((userPhone == null) ? 0 : userPhone.hashCode());
		result = prime * result
				+ ((userResponse == null) ? 0 : userResponse.hashCode());
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
		if (!(obj instanceof User))
			return false;
		User other = (User) obj;
		if (userChallenge == null) {
			if (other.userChallenge != null)
				return false;
		} else if (!userChallenge.equals(other.userChallenge))
			return false;
		if (userId != other.userId)
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		if (userPass == null) {
			if (other.userPass != null)
				return false;
		} else if (!userPass.equals(other.userPass))
			return false;
		if (userPhone == null) {
			if (other.userPhone != null)
				return false;
		} else if (!userPhone.equals(other.userPhone))
			return false;
		if (userResponse == null) {
			if (other.userResponse != null)
				return false;
		} else if (!userResponse.equals(other.userResponse))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "User [userId=" + userId + ", userPhone=" + userPhone
				+ ", userName=" + userName + ", userPass=" + userPass
				+ ", userChallenge=" + userChallenge + ", userResponse="
				+ userResponse + "]";
	}


}
