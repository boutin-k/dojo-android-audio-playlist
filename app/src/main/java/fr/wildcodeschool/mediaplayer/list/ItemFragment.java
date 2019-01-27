package fr.wildcodeschool.mediaplayer.list;

import android.content.Context;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fr.wildcodeschool.mediaplayer.R;

public class ItemFragment extends Fragment {
  // Item list
  private static final List<Item> ITEMS = new ArrayList<>();
  // RecyclerViewAdapter
  private static ItemRecyclerViewAdapter mAdapter;
  // Click event listener
  private OnItemClickListener mListener;

  public ItemFragment() {
    // Mandatory empty constructor
  }

  /**
   * Called to have the fragment instantiate its user interface view. This is optional, and
   * non-graphical fragments can return null (which is the default implementation).
   * @param inflater LayoutInflater: The LayoutInflater object that can be used to inflate any
   *                 views in the fragment.
   * @param container ViewGroup: If non-null, this is the parent view that the fragment's UI
   *                  should be attached to. The fragment should not add the view itself,
   *                  but this can be used to generate the LayoutParams of the view.
   * @param savedInstanceState Bundle: If non-null, this fragment is being re-constructed from a
   *                           previous saved state as given here.
   * @return Return the View for the fragment's UI, or null.
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_item_list, container, false);
    // Set the adapter
    if (view instanceof RecyclerView) {
      // Get RecyclerView
      RecyclerView recyclerView = (RecyclerView) view;
      // Get ViewAdapter
      mAdapter = new ItemRecyclerViewAdapter(ITEMS, mListener);
      // Add adapter to RecyclerView
      recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
      recyclerView.setAdapter(mAdapter);
    }
    return view;
  }

  /**
   * Add an item in the ITEMS list.
   * @param item Item: item to insert in the list
   */
  public static void addItem(Item item) {
    ITEMS.add(item);
  }

  /**
   * Inform the adapter that the list content has changed
   */
  public static void notifyDataSetChanged() {
    if (null != mAdapter) mAdapter.notifyDataSetChanged();
  }

  /**
   * Called when a fragment is first attached to its context. onCreate(Bundle) will be called after this.
   * @param context Context: fragment context
   */
  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    if (context instanceof OnItemClickListener) {
      mListener = (OnItemClickListener) context;
    } else {
      throw new RuntimeException(context.toString()
        + " must implement OnItemClickListener");
    }
  }

  /**
   * Called when the fragment is no longer attached to its activity. This is called after onDestroy()
   */
  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * Called when the view previously created by onCreateView has been detached from the fragment.
   */
  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mAdapter = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   */
  public interface OnItemClickListener {
    void onItemClick(Item item);
  }
}
