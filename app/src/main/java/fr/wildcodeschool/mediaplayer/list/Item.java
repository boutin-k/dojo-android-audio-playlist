package fr.wildcodeschool.mediaplayer.list;

import android.graphics.Bitmap;

@SuppressWarnings("unused")
public class Item {
  final Bitmap bitmap;
  final String artist;
  final String title;

  public Item(String artist, String title, Bitmap bitmap) {
    this.artist = artist;
    this.title = title;
    this.bitmap = bitmap;
  }

  /**
   * Returns the item bitmap
   * @return Bitmap: The item bitmap
   */
  public Bitmap getBitmap() { return bitmap; }

  /**
   * Returns the item artist name
   * @return String: The item artist name
   */
  public String getArtist() { return artist; }

  /**
   * Returns the item song title
   * @return String: The item song title
   */
  public String getTitle() { return title; }
}
