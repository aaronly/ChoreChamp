package us.echols.chorechamp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import us.echols.chorechamp.Achievement;
import us.echols.chorechamp.R;
import us.echols.chorechamp.adapters.AchievementAdapter;
import us.echols.chorechamp.database.DbHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AchievementListFragment#newInstance} factory method to create an instance of this fragment.
 */
public class AchievementListFragment extends Fragment {
    private static final String ARG_CHILD_NAME = "us.echols.chorechamp.ARG.child_name";

    private Context context; // the context that contains this fragment
    private View view; // the fragment view

    private AchievementAdapter adapter; // the chore list adapter for the RecyclerView
    private String childName; // the name of the child for this chore list

    private DbHelper dbHelper;

    public AchievementListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param childName the name of the child for this chore list
     * @return A new instance of fragment AchievementListFragment.
     */
    public static AchievementListFragment newInstance(String childName) {
        AchievementListFragment fragment = new AchievementListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHILD_NAME, childName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retain this fragment when activity is re-initialized
        setRetainInstance(true);

        if (getArguments() != null) {
            childName = getArguments().getString(ARG_CHILD_NAME);
        }

        // get the context of this fragment
        context = getActivity();

        dbHelper = DbHelper.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_achievement_list, container, false);

        List<Achievement> achievements;
        if(childName == null || childName.equals(getString(R.string.parent_name))) {
            achievements = dbHelper.getAchievements();
        } else {
            achievements = dbHelper.getAchievementsByChild(childName);
        }

        // initialize the adapter for the RecyclerView
        adapter = new AchievementAdapter(context, achievements);

        setupRecyclerView();

        return view;
    }

    /**
     * Set up the RecyclerView
     */
    private void setupRecyclerView() {

        // initialize the RecyclerView
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);

        // initialize the layout manager for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // link the adapter to the recyclerView
        recyclerView.setAdapter(adapter);

        // set default item animator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // add dividers
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        // add snap-to-item functionality
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    @SuppressWarnings("unused")
    public String getTitle() {
        return childName;
    }

}
