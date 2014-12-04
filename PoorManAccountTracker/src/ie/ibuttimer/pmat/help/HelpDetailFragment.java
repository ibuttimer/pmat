package ie.ibuttimer.pmat.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ie.ibuttimer.pmat.R;

/**
 * A fragment representing a single Help detail screen. This fragment is either
 * contained in a {@link HelpListActivity} in two-pane mode (on tablets) or a
 * {@link HelpDetailActivity} on handsets.
 */
public class HelpDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The help content this fragment is presenting.
	 */
	private HelpContent.HelpItem mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public HelpDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the help content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = HelpContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_help_detail,
				container, false);

		// Show the help content as text in a TextView.
		if (mItem != null) {
			((TextView) rootView.findViewById(R.id.help_detail))
					.setText(mItem.content);
		}

		return rootView;
	}
}
