package us.echols.chorechamp.adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import us.echols.chorechamp.Chore;
import us.echols.chorechamp.R;

public class ChoreAdapterChild extends RecyclerView.Adapter<ChoreAdapterChild.myViewHolder> implements ChoreAdapter {

    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private final List<Chore> chores;
    private final Context context;

    private OnChoreClickListener clickListener;
    private OnChoreLongClickListener longClickListener;

    /**
     * Create a new chore list adapter.
     * The custom ViewHolder is included in this class.
     *
     * @param context the context this adapter is in
     * @param chores   the list of chores displayed in the adapter
     */
    public ChoreAdapterChild(Context context, List<Chore> chores) {
        this.chores = chores;
        this.context = context;
        selectedItems = new SparseBooleanArray();
    }

    /**
     * Inflate the layout and return the custom ViewHolder
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the RecyclerView
     * @return the ViewHolder for the RecyclerView
     */
    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the layout of the menu_main item view
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.chore_item, parent, false);
        return new myViewHolder(itemView);
    }

    /**
     * Populate the data from the TextViewHolder
     *
     * @param viewHolder the TextViewHolder that contains the data
     * @param position   the position of the item in the RecyclerView
     */
    @Override
    public void onBindViewHolder(myViewHolder viewHolder, int position) {
        // get the item at the position specified
        Chore chore = chores.get(position);

        // populate the text
        TextView nameTextView = viewHolder.nameTextView;
        nameTextView.setText(chore.getName());

        // populate the data of the item view
        TextView dueTextView = viewHolder.dueTextView;
        dueTextView.setText(chore.getNextDue());

        ImageView imageView = viewHolder.imageView;

        // get colors used to differentiate between selected and non-selected items
        int transparent = ContextCompat.getColor(context, android.R.color.transparent);
        int selectedColor = ContextCompat.getColor(context, R.color.colorAccent);
        int overdueColor = ContextCompat.getColor(context, R.color.colorOverdue);

        // change colors if item is selected or de-selected
        ConstraintLayout selectableItem = viewHolder.selectableItem;
        if (isSelected(position)) {
            selectableItem.setBackgroundColor(selectedColor);
            imageView.setImageResource(android.R.drawable.checkbox_on_background);
        } else {
            selectableItem.setBackgroundColor(transparent);
            imageView.setImageResource(android.R.drawable.checkbox_off_background);
        }

        Calendar now = Calendar.getInstance();
        // change colors if item is selected or de-selected
        if (chore.isOverdue(now)) {
            selectableItem.setBackgroundColor(overdueColor);
        } else {
            selectableItem.setBackgroundColor(transparent);
        }
    }

    /**
     * Get the number of items in this list
     *
     * @return the number of items in the list
     */
    @Override
    public int getItemCount() {
        return chores.size();
    }

    /**
     * Indicates if the item at position position is selected
     *
     * @param position position of the item to check
     * @return true if the item is selected, false otherwise
     */
    private boolean isSelected(int position) {
        return getSelectedIndexes().contains(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     *
     * @param position position of the item to toggle the selection status for
     */
    @Override
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    /**
     * Clear the selection status for all items
     */
    @Override
    public void clearSelections() {
        List<Integer> selectedIndexes = getSelectedIndexes();
        selectedItems.clear();
        for (Integer i : selectedIndexes) {
            notifyItemChanged(i);
        }
    }

    /**
     * Count the selected items
     *
     * @return selected items count
     */
    @Override
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     * Indicates the list of selected items
     *
     * @return list of selected items ids
     */
    @Override
    public List<Integer> getSelectedIndexes() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    @Override
    public List<Chore> getChores() {
        return chores;
    }

    class myViewHolder extends RecyclerView.ViewHolder {

        final TextView nameTextView;
        final TextView dueTextView;
        final ConstraintLayout selectableItem;
        final ImageView imageView;

        myViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.textView_chore);
            dueTextView = (TextView) itemView.findViewById(R.id.textView_due_date);
            selectableItem = (ConstraintLayout)itemView.findViewById(R.id.selectable_item);
            imageView = (ImageView)itemView.findViewById(R.id.imageView_icon);

            // setup the click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            clickListener.onChoreClick(position);
                        }
                    }
                }
            });

            // setup the long click listener
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (longClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            longClickListener.onChoreLongClick(position);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void setOnChoreClickListener(OnChoreClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void setOnChoreLongClickListener(OnChoreLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
}
