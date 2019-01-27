package fr.wildcodeschool.mediaplayer.obb;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.app.Activity.*;

public class ObbManager {
  // Read OBB permission
  public static final int PERMISSIONS_REQUEST_READ_OBB = 1;
  // const
  private static final String OBB_KEY = null;
  private static final String OBB_NAME = "main.1.fr.wildcodeschool.mediaplayer.obb";
  private final String RAW_PATH;

  private OnObbStateChangeListener mObbListener;

  private StorageManager mStorageManager;
  private AppCompatActivity mActivity;

  public ObbManager(AppCompatActivity activity, ObbManagerListener listener) {
    // Store activity context
    mActivity = activity;

    // Get storage manager
    mStorageManager = (StorageManager) mActivity.getSystemService(Context.STORAGE_SERVICE);
    RAW_PATH = mActivity.getObbDir().getAbsolutePath() + File.separator + OBB_NAME;

    // ObbStateChangeListener
    mObbListener = new OnObbStateChangeListener() {
      @Override
      public void onObbStateChange(String path, int state) {
        super.onObbStateChange(path, state);
        if (null != listener) listener.onObbStateChange(path, state);
      }
    };
  }

  public int requestReadObbPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (ContextCompat.checkSelfPermission(mActivity, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(mActivity, new String[]{READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_OBB);
        return RESULT_CANCELED;
      }
    }
    return RESULT_OK;
  }

  public void mountMainObb() {
    mStorageManager.mountObb(RAW_PATH, OBB_KEY, mObbListener);
  }

  public void unmountMainObb() {
    mStorageManager.unmountObb(RAW_PATH, true, mObbListener);
  }

  public boolean isObbMounted() {
    return mStorageManager.isObbMounted(RAW_PATH);
  }

  public String getFilePath(String filename) {
    return mStorageManager.getMountedObbPath(RAW_PATH) + File.separator + filename;
  }
}