package fr.wildcodeschool.mediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import fr.wildcodeschool.mediaplayer.json.JsonParser;
import fr.wildcodeschool.mediaplayer.list.Item;
import fr.wildcodeschool.mediaplayer.list.ItemFragment;
import fr.wildcodeschool.mediaplayer.notification.MediaNotification;
import fr.wildcodeschool.mediaplayer.notification.NotificationReceiver;
import fr.wildcodeschool.mediaplayer.obb.ObbManager;
import fr.wildcodeschool.mediaplayer.obb.ObbManagerListener;
import fr.wildcodeschool.mediaplayer.player.WildPlayer;
import fr.wildcodeschool.mediaplayer.service.MediaService;

import static android.os.storage.OnObbStateChangeListener.*;

@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity
  implements ServiceConnection, ItemFragment.OnItemClickListener, ObbManagerListener {
  // TAG
  private static final String TAG = "MainActivity";

  // Fragments
  private ControllerFragment mControllerFragment;

  // OBB
  private ObbManager mObbManager;

  // Bound service
  MediaService mService;
  boolean mBound = false;

  // Notification
  private MediaNotification mNotification = null;

  /**
   * Application context accessor
   * https://possiblemobile.com/2013/06/context/
   */
  private static Context appContext;
  public  static Context getAppContext() {
    return appContext;
  }

  /**
   * Called when the activity is starting.
   * @param savedInstanceState Bundle: If the activity is being re-initialized after previously
   * being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Get fragment instance
    mControllerFragment =
      (ControllerFragment) getSupportFragmentManager()
        .findFragmentById(R.id.controller_fragment);

    // Initialization of the application context
    MainActivity.appContext = getApplicationContext();

    // mount the OBB file
    mObbManager = new ObbManager(this, this);
    if (RESULT_OK == mObbManager.requestReadObbPermission()) {
      // Permission has been granted
      if (!mObbManager.isObbMounted()) {
        mObbManager.mountMainObb();
      }
    }

    // Bind to MediaService
    Intent intent = new Intent(this, MediaService.class);
    bindService(intent, this, Context.BIND_AUTO_CREATE);

    // Create the notification
    mNotification =
      new MediaNotification.Builder(getApplicationContext())
        .addActions(NotificationReceiver.class)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.greenday))
        .setContentTitle(getString(R.string.song_title))
        .setContentText(getString(R.string.song_description))
        .buildNotification();
    mNotification.register();
  }

  /**
   * Callback for the result from requesting permissions.
   * This method is invoked for every call on requestPermissions
   * @param requestCode int: The request code passed in requestPermissions
   * @param permissions String: The requested permissions. Never null.
   * @param grantResults int: The grant results for the corresponding permissions.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String permissions[],
                                         @NonNull int[] grantResults) {
    if (requestCode == ObbManager.PERMISSIONS_REQUEST_READ_OBB) {
      if (grantResults.length > 0
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // Permission has been granted
        mObbManager.mountMainObb();
        Log.i(TAG, "OBB_PERMISSION GRANTED");
      } else {
        Log.e(TAG, "OBB_PERMISSION REFUSED");
        finish();
      }
    }
  }

  /**
   * Perform any final cleanup before an activity is destroyed. This can happen either because
   * the activity is finishing (someone called finish() on it), or because the system is
   * temporarily destroying this instance of the activity to save space.
   * You can distinguish between these two scenarios with the isFinishing() method.
   */
  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Release the service
    unbindService(this);
    mBound = false;
    // Release the notification
    if (null != mNotification)
      mNotification.unregister();
    // If obbManager always exists and is always mounted
    if (null != mObbManager && mObbManager.isObbMounted())
      mObbManager.unmountMainObb();
  }

  // --------------------------------------------------------------------------
  // Player validity
  // --------------------------------------------------------------------------

  /**
   * Get the validity of mediaPlayer instance
   * @return boolean: Returns the validity of th WildPlayer
   */
  private boolean isPlayerReady() {
    return mBound
      && (null != mService)
      && (null != mService.getPlayer());
  }

  /**
   * Return the instance of the WildPlayer stored in the service
   * @return WildPlayer: The instance of the WildPlayer
   */
  public WildPlayer getPlayer() {
    return isPlayerReady() ? mService.getPlayer() : null;
  }

  // --------------------------------------------------------------------------
  // Buttons onClick
  // --------------------------------------------------------------------------

  /**
   * On play button click
   * Launch the playback of the media
   */
  public void playMedia(View v) {
    mControllerFragment.playMedia(getPlayer());
  }

  /**
   * On pause button click
   * Pause the playback of the media
   */
  public void pauseMedia(View v) {
    mControllerFragment.pauseMedia(getPlayer());
  }

  /**
   * On reset button click
   * Stop the playback of the media
   */
  public void stopMedia(View v) {
    mControllerFragment.stopMedia(getPlayer());
  }

  // --------------------------------------------------------------------------
  // Service interface
  // --------------------------------------------------------------------------

  /**
   * Called when a connection to the Service has been established, with the IBinder of the
   * communication channel to the Service.
   * @param className ComponentName: The concrete component name of the service that has been connected.
   * @param service IBinder: The IBinder of the Service's communication channel, which you can now make calls on.
   */
  @Override
  public void onServiceConnected(ComponentName className, IBinder service) {
    // We've bound to MediaService, cast the IBinder and get MediaService instance
    MediaService.MediaBinder binder = (MediaService.MediaBinder) service;
    mService = binder.getService();
    mBound = true;

    mService.createMediaPlayer(R.string.song, mControllerFragment);
  }

  /**
   * Called when a connection to the Service has been lost. This typically happens when the
   * process hosting the service has crashed or been killed.
   * @param name ComponentName: The concrete component name of the service whose connection has been lost.
   */
  @Override
  public void onServiceDisconnected(ComponentName name) {
    mBound = false;
  }

  // --------------------------------------------------------------------------
  // APK expansion file
  // --------------------------------------------------------------------------

  /**
   * Used for receiving notifications from StorageManager about OBB file states.
   * @param path String: path to the OBB file the state change has happened on
   * @param state int: the current state of the OBB
   */
  @Override
  public void onObbStateChange(String path, int state) {
    if (MOUNTED == state) {
      // Get the file from OBB mounted path
      File file = new File(mObbManager.getFilePath("data.json"));

      // try with statement works here because FileInputStream
      // implement Closeable interface.
      try (FileInputStream lFileInputStream = new FileInputStream(file)) {
        // Parse JSON content and update the recyclerView
        JsonParser.getInstance().readJsonStream(lFileInputStream);
        addRecyclerViewItems();
      } catch (IOException e) { e.printStackTrace(); }
    }
  }

  // --------------------------------------------------------------------------
  // RecyclerView
  // --------------------------------------------------------------------------

  /**
   * update the content of RecyclerView
   */
  private void addRecyclerViewItems() {
    // Loop on the Json items
    for (JsonParser.Song item: JsonParser.getInstance().getSongList()) {
      Bitmap lBitmap = BitmapFactory.decodeFile(mObbManager.getFilePath(item.cover));
      ItemFragment.addItem(new Item(item.artist, item.title, lBitmap));
    }
    ItemFragment.notifyDataSetChanged();
  }

  /**
   * Called when an item of the RecyclerView emit a click event
   * @param item Item: The item in the RecyclerView related to the emit event
   */
  @Override
  public void onItemClick(Item item) {
    Toast.makeText(this, item.getArtist(), Toast.LENGTH_SHORT).show();
  }
}
