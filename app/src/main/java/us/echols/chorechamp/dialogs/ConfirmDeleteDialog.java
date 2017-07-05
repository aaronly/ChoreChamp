package us.echols.chorechamp.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * A simple yes or no dialog that asks the user to confirm delete
 */
public class ConfirmDeleteDialog extends DialogFragment {

    private final static String ARG_TITLE = "title";
    private final static String ARG_MESSAGE = "message";
    private String title;
    private String message;

    /**
     * Create a new instance of the delete confirmation dialog
     *
     * @param title the title of the dialog
     * @param message the text message of the dialog
     * @return A new instance of fragment ConfirmDeleteDialogFragment
     */
    public static ConfirmDeleteDialog newInstance(String title, String message) {
        ConfirmDeleteDialog fragment = new ConfirmDeleteDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the type of item this list contains
        if (getArguments() != null) {
            this.title = getArguments().getString(ARG_TITLE);
            this.message = getArguments().getString(ARG_MESSAGE);
        }

        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(title);
                if(message != null && !message.isEmpty()) {
                    builder.setMessage(message);
                }
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        listener.onConfirmDelete();
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                    }
                });
                return builder.create();
    }

    private ConfirmDeleteListener listener;
    public interface ConfirmDeleteListener {
        void onConfirmDelete();
    }
    public void setConfirmDeleteListener(ConfirmDeleteListener listener) {
        this.listener = listener;
    }

}
