package com.stefano.andrea.utils;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;

import com.stefano.andrea.activities.R;

import java.util.ArrayList;
import java.util.List;

/**
 * SelectableAdapter
 */
public abstract class SelectableAdapter<VH extends SelectableHolder> extends RecyclerView.Adapter<VH> {

    private SparseBooleanArrayParcelable mSelectedItems;
    private Activity mActivity;
    private ActionMode.Callback mCallback;
    private ActionMode mActionMode;

    public SelectableAdapter(Activity activity, ActionMode.Callback callback) {
        mSelectedItems = new SparseBooleanArrayParcelable();
        mActivity = activity;
        mCallback = callback;
    }

    public SparseBooleanArrayParcelable saveActionmode () {
        return mSelectedItems;
    }

    /**
     * Restore the action mode on device configuration change
     * @param selectedItems The selected items before the destroy of the previous instance
     */
    public void restoreActionMode (SparseBooleanArrayParcelable selectedItems) {
        this.mSelectedItems = selectedItems;
        startActionMode();
        int count = getSelectedItemCount();
        String title = mActivity.getApplicationContext().getResources().getQuantityString(R.plurals.action_mode_count_selected, count, count);
        mActionMode.setTitle(title);
    }

    /**
     * Indicates if the item at position position is selected
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
        int count = getSelectedItemCount();
        if (count == 0) {
            mActionMode.finish();
        } else {
            String title = mActivity.getApplicationContext().getResources().getQuantityString(R.plurals.action_mode_count_selected, count, count);
            mActionMode.setTitle(title);
            mActionMode.invalidate();
        }
        notifyItemChanged(position);
    }

    /**
     * Verify if the selection mode in enabled
     * @return true if selection mode enabled, false otherwise
     */
    public boolean isEnabledSelectionMode () {
        return mActionMode != null;
    }

    /**
     * Start the selection mode
     */
    public void startActionMode () {
        if (!isEnabledSelectionMode()) {
            mActionMode = ((AppCompatActivity) mActivity).startSupportActionMode(mCallback);
        }
    }

    /**
     * Finish the action mode if not closed by another listener
     */
    public void finishActionMode () {
        if (isEnabledSelectionMode()) {
            mActionMode.finish();
            clearSelection();
        }
    }

    /**
     * Stop the selection mode
     */
    public void stopActionMode () {
        if (isEnabledSelectionMode()) {
            clearSelection();
        }
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        if (mSelectedItems.size() > 0) {
            mSelectedItems.clear();
            notifyDataSetChanged();
        }
        mActionMode = null;
    }

    /**
     * Count the selected items
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    /**
     * Indicates the list of selected items
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); ++i) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }
}
