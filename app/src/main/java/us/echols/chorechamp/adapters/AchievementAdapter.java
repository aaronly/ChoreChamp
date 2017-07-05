package us.echols.chorechamp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import us.echols.chorechamp.Achievement;
import us.echols.chorechamp.R;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {
    private final List<Achievement> achievements;
    private final Context context;

    /**
     * Create a new chore list adapter.
     * The custom ViewHolder is included in this class.
     *
     * @param context      the context this adapter is in
     * @param achievements the list of achievements displayed in the adapter
     */
    public AchievementAdapter(Context context, List<Achievement> achievements) {
        this.achievements = achievements;
        this.context = context;
    }

    /**
     * Inflate the layout and return the custom ViewHolder
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type of the RecyclerView
     * @return the ViewHolder for the RecyclerView
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the layout of the menu_main item view
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.achievement_item, parent, false);

        return new ViewHolder(itemView);
    }

    /**
     * Populate the data from the TextViewHolder
     *
     * @param viewHolder the TextViewHolder that contains the data
     * @param position   the position of the item in the RecyclerView
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // get the item at the position specified
        Achievement achievement = achievements.get(position);

        // populate the data of the item view
        TextView titleTextView = viewHolder.titleTextView;
        titleTextView.setText(achievement.getName());

        ImageView imageView = viewHolder.imageView;
        if(achievement.isComplete()) {
            imageView.setImageResource(R.drawable.ic_achievement);
        } else {
            imageView.setImageResource(R.drawable.ic_achievement_incomplete);
        }
    }

    /**
     * Get the number of items in this list
     *
     * @return the number of items in the list
     */
    @Override
    public int getItemCount() {
        return achievements.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final TextView titleTextView;
        final ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView)itemView.findViewById(R.id.textView_achievement);
            imageView = (ImageView)itemView.findViewById(R.id.image_achievement);
        }
    }

}
