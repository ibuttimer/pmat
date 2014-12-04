/**
 * 
 */
package ie.ibuttimer.pmat.util;

import ie.ibuttimer.pmat.PreferenceControl;
import ie.ibuttimer.pmat.R;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

/**
 * Utility class to play short sounds. 
 * 
 * @author Ian Buttimer
 *
 */
public class AudioPlayer {

	private Context context;
	private int resourceId;
	private MediaPlayer mediaPlayer;
	
	/**
	 * Default constructor
	 * @param context		- application context
	 * @param resourceId	- id of resource to play 
	 */
	public AudioPlayer(Context context, int resourceId) {
		
		this.context = context;
		this.resourceId = resourceId;

		if ( PreferenceControl.isSoundEnabled(context) )
			initMediaPlayer();
		else
			mediaPlayer = null;
	}

	
	/**
	 * Initialise the player
	 */
	private void initMediaPlayer() {
		mediaPlayer = MediaPlayer.create(context, resourceId);
	}

	
	/**
	 * Play the sound
	 */
	public void play() {
		
		if ( mediaPlayer != null ) {
			AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			int result = audioManager.requestAudioFocus(new PmatAudioService(), AudioManager.STREAM_MUSIC,
				    										AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				mediaPlayer.start(); // no need to call prepare(); create() does that for you
			}
			else {
				// could not get audio focus so forget it
				cleanUp();
			}
		}
	}
	
	
	/**
	 * Play the specified sound
	 * @param context		- application context
	 * @param resourceId	- id of resource to play
	 */
	public static void playSound(Context context, int resourceId) {
		
		AudioPlayer player = new AudioPlayer(context,R.raw.multimedia_button_click_026);
		player.play();
	}

	/**
	 * Play a button click sound
	 * @param context		- application context
	 */
	public static void playButtonClick(Context context) {
		playSound(context, R.raw.multimedia_button_click_026);
	}
	
	
	/**
	 * Tidy up after sound has played
	 */
	private void cleanUp() {
		if ( mediaPlayer != null ) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
	
	/**
	 * Utility class to handle audio focus change
	 * @author Ian Buttimer
	 */
	class PmatAudioService extends Service implements AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {
		// ....
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:
				// resume playback
				if (mediaPlayer == null) initMediaPlayer();
				else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
				mediaPlayer.setVolume(1.0f, 1.0f);
				break;

			case AudioManager.AUDIOFOCUS_LOSS:
				// Lost focus for an unbounded amount of time: stop playback and release media player
				if (mediaPlayer.isPlaying()) mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
				// Lost focus for a short time, but we have to stop
				// playback. We don't release the media player because playback
				// is likely to resume
				if (mediaPlayer.isPlaying()) mediaPlayer.pause();
				break;

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
				// Lost focus for a short time, but it's ok to keep playing
				// at an attenuated level
				if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
				break;
			}
			
			
		}

		@Override
		public IBinder onBind(Intent arg0) {
			// clients can not bind to the service
			return null;
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			cleanUp();
		}
	}
	
}
