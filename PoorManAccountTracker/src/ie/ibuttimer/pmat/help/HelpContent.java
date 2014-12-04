package ie.ibuttimer.pmat.help;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
public class HelpContent {

	/**
	 * An array of help items.
	 */
	public static List<HelpItem> ITEMS = new ArrayList<HelpItem>();

	/**
	 * A map of sample (help) items, by ID.
	 */
	public static Map<String, HelpItem> ITEM_MAP = new HashMap<String, HelpItem>();

//	static {
//		// Add 3 sample items.
//		addItem(new HelpItem("1", "Item 1"));
//		addItem(new HelpItem("2", "Item 2"));
//		addItem(new HelpItem("3", "Item 3"));
//	}

	private static void addItem(HelpItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	
	public static void populateHelpItems(Context c, int topicsId, int itemsId) {
		
		if ( ITEMS.size() == 0 ) {
			 String[] topics = c.getResources().getStringArray(topicsId);
			 String[] items = c.getResources().getStringArray(itemsId);
			 
			 final int N = topics.length;
			 for ( int i = 0; i < N; ++i )
				 addItem(new HelpItem(Integer.toString(i), topics[i], items[i]));
		}
	}
	
	
	
	
	/**
	 * A help item representing a piece of content.
	 */
	public static class HelpItem {
		public String id;
		public String title;
		public String content;

		public HelpItem(String id, String title, String content) {
			this.id = id;
			this.title = title;
			this.content = content;
		}

		@Override
		public String toString() {
			return title;
		}
	}
}
