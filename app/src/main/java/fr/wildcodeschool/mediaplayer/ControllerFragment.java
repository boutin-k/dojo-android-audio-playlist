package fr.wildcodeschool.mediaplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import fr.wildcodeschool.mediaplayer.player.WildOnPlayerListener;
import fr.wildcodeschool.mediaplayer.player.WildPlayer;

@SuppressWarnings("unused")
public class ControllerFragment extends Fragment
  implements SeekBar.OnSeekBarChangeListener, WildOnPlayerListener {
  // Const
  private static final int UNDEFINED = -1;

  // SeekBar
  private SeekBar mSeekBar = null;
  private int mMaxValue = UNDEFINED;
  // SeekBar update delay
  private static final int SEEKBAR_DELAY = 1000;
  // Thread used to update the SeekBar position
  private final Handler mSeekBarHandler = new Handler();
  private Runnable mSeekBarThread;

  /**
   * Default constructor
   */
  public ControllerFragment() {
    // Required empty public constructor
  }

  /**
   * Called to have the fragment instantiate its user interface view. This is optional, and
   * non-graphical fragments can return null (which is the default implementation).
   * This will be called between onCreate(Bundle) and onActivityCreated(Bundle).
   * @param inflater LayoutInflater: The LayoutInflater object that can be used to inflate any
   *                 views in the fragment.
   * @param container ViewGroup: If non-null, this is the parent view that the fragment's UI
   *                  should be attached to. The fragment should not add the view itself,
   *                  but this can be used to generate the LayoutParams of the view.
   * @param savedInstanceState Bundle: If non-null, this fragment is being re-constructed from
   *                           a previous saved state as given here.
   * @return Return the View for the fragment's UI, or null.
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View lView = inflater.inflate(R.layout.fragment_controller, container, false);
    // SeekBar Initialization
    mSeekBar = lView.findViewById(R.id.seekBar);
    mSeekBar.setOnSeekBarChangeListener(this);
    // Set SeekBar max value if exists
    if (UNDEFINED != mMaxValue) mSeekBar.setMax(mMaxValue);
    return lView;
  }

  /**
   * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned,
   * but before any saved state has been restored in to the view. This gives subclasses a chance
   * to initialize themselves once they know their view hierarchy has been completely created.
   * The fragment's view hierarchy is not however attached to its parent at this point.
   * @param view View: The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
   * @param savedInstanceState Bundle: If non-null, this fragment is being re-constructed from
   *                           a previous saved state as given here.
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Thread used to update the SeekBar position according to the audio player
    mSeekBarThread = new Runnable() {
      @Override
      public void run() {
        // Widget should only be manipulated in UI thread
        mSeekBar.post(() -> {
          if (null != getPlayer()) {
            mSeekBar.setProgress(getPlayer().getCurrentPosition());
          }
        });
        // Launch a new request
        mSeekBarHandler.postDelayed(this, SEEKBAR_DELAY);
      }
    };
  }

  /**
   * Called when the fragment is visible to the user and actively running. This is generally
   * tied to Activity.onResume of the containing Activity's lifecycle.
   */
  @Override
  public void onResume() {
    super.onResume();
    // Check player validity
    if (null != getPlayer()) {
      // Update seekbar position
      mSeekBar.setProgress(getPlayer().getCurrentPosition());
      // Launch a new request
      mSeekBarHandler.postDelayed(mSeekBarThread, SEEKBAR_DELAY);
    }
  }

  /**
   * Called when the Fragment is no longer resumed. This is generally tied to Activity.onPause
   * of the containing Activity's lifecycle.
   */
  @Override
  public void onPause() {
    super.onPause();
    mSeekBarHandler.removeCallbacks(mSeekBarThread);
  }

  // --------------------------------------------------------------------------
  // WildOnPlayerListener interfaces
  // --------------------------------------------------------------------------

  /**
   * Called when the media file is ready for playback.
   * @param mp MediaPlayer: the MediaPlayer that is ready for playback
   */
  @Override
  public void onPrepared(MediaPlayer mp) {
    if (null != mSeekBar) {
      mSeekBar.setMax(mp.getDuration());
    }
    // Store data in class
    mMaxValue = mp.getDuration();
  }

  /**
   * Called when the end of a media source is reached during playback.
   * @param mp MediaPlayer: the MediaPlayer that reached the end of the file
   */
  @Override
  public void onCompletion(MediaPlayer mp) {
    if (null != mSeekBarThread) {
      mSeekBarHandler.removeCallbacks(mSeekBarThread);
    }
    if (null != mSeekBar) {
      mSeekBar.setProgress(0);
    }
  }

  // --------------------------------------------------------------------------
  // SeekBar interface
  // --------------------------------------------------------------------------

  /**
   * OnSeekBarChangeListener interface method implementation
   * @param seekBar Widget related to the event
   * @param progress Current position on the SeekBar
   * @param fromUser Define if it is a user action or a programmatic seekTo
   */
  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser && null != getPlayer()) {
      getPlayer().seekTo(progress);
    }
  }

  /**
   * OnSeekBarChangeListener interface method implementation
   * @param seekBar Widget related to the event
   */
  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    // Stop seekBarUpdate here
    mSeekBarHandler.removeCallbacks(mSeekBarThread);
  }

  /**
   * OnSeekBarChangeListener interface method implementation
   * @param seekBar Widget related to the event
   */
  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    // Restart seekBarUpdate here
    if (null != getPlayer() && getPlayer().isPlaying()) {
      mSeekBarHandler.postDelayed(mSeekBarThread, SEEKBAR_DELAY);
    }
  }

  // --------------------------------------------------------------------------
  // Buttons onClick
  // --------------------------------------------------------------------------

  /**
   * On play button click
   * Launch the playback of the media
   */
  public void playMedia(WildPlayer player) {
    if (null != mSeekBarThread && null != player && player.play()) {
      mSeekBarHandler.postDelayed(mSeekBarThread, SEEKBAR_DELAY);
    }
  }

  /**
   * On pause button click
   * Pause the playback of the media
   */
  public void pauseMedia(WildPlayer player) {
    if (null != mSeekBarThread && null != player && player.pause()) {
      mSeekBarHandler.removeCallbacks(mSeekBarThread);
    }
  }

  /**
   * On reset button click
   * Stop the playback of the media
   */
  public void stopMedia(WildPlayer player)
  {
    if ( null != mSeekBarThread && null != mSeekBar
      && null != player && player.stop() ) {
      mSeekBarHandler.removeCallbacks(mSeekBarThread);
      mSeekBar.setProgress(0);
    }
  }

  // --------------------------------------------------------------------------
  // Service WildPlayer
  // --------------------------------------------------------------------------

  /**
   * Get the wildPlayer instance
   * @return The WildPlayer instance or null.
   */
  private WildPlayer getPlayer() {
    FragmentActivity lActivity = getActivity();
    if (lActivity instanceof MainActivity)
      return ((MainActivity)getActivity()).getPlayer();
    return null;
  }
}
