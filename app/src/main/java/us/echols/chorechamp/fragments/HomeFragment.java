package us.echols.chorechamp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import us.echols.chorechamp.Chore;
import us.echols.chorechamp.R;
import us.echols.chorechamp.database.DbHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private DbHelper dbHelper;
    private TextView textView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbHelper = DbHelper.getInstance(getContext());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textView = (TextView)view.findViewById(R.id.textView_home);
        textView.setText(getChoreText());

        return view;
    }

    private String getChoreText() {
        String[] childNames = dbHelper.getChildNames();
        StringBuilder sb = new StringBuilder();

        for (String childName : childNames) {
            List<Chore> chores = dbHelper.getChoresByChild(childName);

            sb.append(childName);
            if (chores.size() > 1) {
                sb.append("'s current chores:\n");
            } else if (chores.size() == 1) {
                sb.append("'s current chore:\n");
            } else {
                sb.append(" has no chores to do.\n");
            }
            for (Chore chore : chores) {
                sb.append(chore.getName()).append(" is due on ").append(chore.getNextDue()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public void onResume() {
        textView.setText(getChoreText());
        super.onResume();
    }
}
