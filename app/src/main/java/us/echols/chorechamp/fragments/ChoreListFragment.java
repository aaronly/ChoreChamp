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

import java.util.Collections;
import java.util.List;

import us.echols.chorechamp.Chore;
import us.echols.chorechamp.R;
import us.echols.chorechamp.adapters.ChoreAdapter;
import us.echols.chorechamp.adapters.ChoreAdapterChild;
import us.echols.chorechamp.adapters.ChoreAdapterParent;
import us.echols.chorechamp.adapters.OnChoreClickListener;
import us.echols.chorechamp.adapters.OnChoreLongClickListener;
import us.echols.chorechamp.database.DbHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnChoreListListener} interface to handle interaction events.
 * Use the {@link ChoreListFragment#newInstance} factory method to create an instance of this fragment.
 */
public class ChoreListFragment extends Fragment {
    private static final String ARG_CHILD_NAME = "us.echols.chorechamp.ARG.child_name";

    private Context context; // the context that contains this fragment
    private View view; // the fragment view

    private ChoreAdapter adapter; // the chore list adapter for the RecyclerView
    private List<Chore> chores; // the chores displayed in this list
    private String childName; // the name of the child for this chore list

    private OnChoreListListener listener; // a listener object for this fragment

    public ChoreListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @param childName the name of the child for this chore list
     * @return A new instance of fragment ChoreListFragment.
     */
    public static ChoreListFragment newInstance(String childName) {
        ChoreListFragment fragment = new ChoreListFragment();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chore_list, container, false);

        // get the database helper instance
        DbHelper dbHelper = DbHelper.getInstance(context);

        if (childName.equals(getString(R.string.parent_name))) {
            chores = dbHelper.getChores();
            Collections.sort(chores, new Chore.ChoreComparator());
            adapter = new ChoreAdapterParent(context, chores);
        } else {
            chores = dbHelper.getChoresByChild(childName);
            Collections.sort(chores, new Chore.ChoreComparator());
            adapter = new ChoreAdapterChild(context, chores);
        }

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
        recyclerView.setAdapter((RecyclerView.Adapter)adapter);

        // set up click listener
        adapter.setOnChoreClickListener(new OnChoreClickListener() {
            @Override
            public void onChoreClick(int position) {
                onItemClick(position);
            }
        });

        // set up long click listener
        adapter.setOnChoreLongClickListener(new OnChoreLongClickListener() {
            @Override
            public void onChoreLongClick(int position) {
                onItemLongClick(position);
            }
        });

        // set default item animator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // add dividers
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        // add snap-to-item functionality
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Method that fires when an item is clicked
     *
     * @param position the position of the item clicked
     */
    private void onItemClick(int position) {
        Chore chore = chores.get(position);
        listener.onChoreClick(position, chore);
    }

    /**
     * Method that fires when an item is long clicked
     *
     * @param position the position of the item clicked
     */
    private void onItemLongClick(int position) {
        listener.onChoreLongClick(position);
    }

    public ChoreAdapter getAdapter() {
        return adapter;
    }

    /**
     * Add an chore to the list
     *
     * @param chore the chore to add
     */
    public void addChore(Chore chore) {
        chores.add(chore);
        Collections.sort(chores, new Chore.ChoreComparator());
        adapter.notifyDataSetChanged();
    }

    /**
     * Update an chore in this list
     *
     * @param chore the chore to be updated
     */
    public void updateChore(Chore chore) {
        int index = 0;
        for (Chore c : chores) {
            if (c.getId() == chore.getId()) {
                index = chores.indexOf(c);
                break;
            }
        }

        if (index >= 0) {
            chores.set(index, chore);
        }

        Collections.sort(chores, new Chore.ChoreComparator());
        adapter.notifyDataSetChanged();
    }

    public String getChildName() {
        return childName;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChoreListListener) {
            listener = (OnChoreListListener)context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnChoreListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnChoreListListener {
        void onChoreClick(int position, Chore chore);
        void onChoreLongClick(int position);
    }
}
