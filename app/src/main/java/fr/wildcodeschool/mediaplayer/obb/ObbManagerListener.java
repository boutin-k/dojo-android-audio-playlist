package fr.wildcodeschool.mediaplayer.obb;

public interface ObbManagerListener {
  public void onObbStateChange(String path, int state);
}
