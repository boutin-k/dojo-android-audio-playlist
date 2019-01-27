package fr.wildcodeschool.mediaplayer.list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fr.wildcodeschool.mediaplayer.R;

import java.util.List;

public class ItemRecyclerViewAdapter
  extends RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder> {
  // Item list
  private final List<Item> mValues;
  // Click event listener
  private final ItemFragment.OnItemClickListener mListener;

  /**
   * Constructor
   * @param items List of items to display in the RecyclerView
   * @param listener onItemClickEvent listener
   */
  ItemRecyclerViewAdapter(List<Item> items, ItemFragment.OnItemClickListener listener) {
    mValues   = items;
    mListener = listener;
  }

  /**
   * Called when RecyclerView needs a new RecyclerView.ViewHolder of the
   * given type to represent an item.
   * @param parent ViewGroup: The ViewGroup into which the new View will be added after
   *               it is bound to an adapter position.
   * @param viewType int: The view type of the new View.
   * @return A new ViewHolder that holds a View of the given view type.
   */
  @Override @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater
      .from(parent.getContext())
      .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  /**
   * Called by RecyclerView to display the data at the specified position. This method should
   * update the contents of the itemView to reflect the item at the given position.
   * @param holder VH: The ViewHolder which should be updated to represent the contents of the
   *               item at the given position in the data set.
   * @param position int: The position of the item within the adapter's data set.
   */
  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    holder.mItem = mValues.get(position);
    holder.mItemImage.setImageBitmap(holder.mItem.bitmap);
    holder.mItemArtist.setText(holder.mItem.artist);
    holder.mItemTitle.setText(holder.mItem.title);
    // Manage item click event
    holder.mView.setOnClickListener((View v) -> {
      if (null != mListener) mListener.onItemClick(holder.mItem);
    });
  }

  /**
   * Returns the total number of items in the data set held by the adapter.
   * @return The total number of items in this adapter.
   */
  @Override
  public int getItemCount() {
    return mValues.size();
  }


  class ViewHolder extends RecyclerView.ViewHolder {
    private final View      mView;
    private final ImageView mItemImage;
    private final TextView  mItemArtist;
    private final TextView  mItemTitle;
    private Item mItem;

    private ViewHolder(View view) {
      super(view);
      mView = view;
      mItemImage  = view.findViewById(R.id.item_image);
      mItemArtist = view.findViewById(R.id.item_artist);
      mItemTitle  = view.findViewById(R.id.item_title);
    }
  }
}
