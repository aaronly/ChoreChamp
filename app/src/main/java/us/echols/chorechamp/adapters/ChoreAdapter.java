package us.echols.chorechamp.adapters;

import java.util.List;

import us.echols.chorechamp.Chore;

public interface ChoreAdapter {
    void setOnChoreClickListener(OnChoreClickListener clickListener);
    void setOnChoreLongClickListener(OnChoreLongClickListener longClickListener);
    void notifyDataSetChanged();
    void notifyItemRemoved(int position);
    void notifyItemRangeRemoved(int firstPosition, int count);
    void toggleSelection(int position);
    int getSelectedItemCount();
    void clearSelections();
    List<Integer> getSelectedIndexes();
    List<Chore> getChores();

}
