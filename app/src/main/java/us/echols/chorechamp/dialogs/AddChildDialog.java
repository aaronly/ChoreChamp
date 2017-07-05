package us.echols.chorechamp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import us.echols.chorechamp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the {@link AddChildDialogListener} interface to handle interaction events.
 * Use the {@link AddChildDialog#newInstance} factory method to create an instance of this fragment.
 */
public class AddChildDialog extends DialogFragment {

    private AddChildDialogListener listener;

    public AddChildDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddChildDialog.
     */
    public static AddChildDialog newInstance() {
        return new AddChildDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog_Alert);

        alertDialogBuilder.setTitle(getString(R.string.title_add_child));

        final EditText editText = new EditText(getContext());
        alertDialogBuilder.setView(editText);
        editText.setSingleLine(true);

        alertDialogBuilder.setPositiveButton(getString(android.R.string.ok),  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onAddNewChild(editText.getText().toString());
                }
                closeDialog(dialog);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeDialog(dialog);
            }

        });
        return alertDialogBuilder.create();

    }

    private void closeDialog(DialogInterface dialog) {
        if (dialog != null && ((Dialog)dialog).isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddChildDialogListener) {
            listener = (AddChildDialogListener)context;
        } else {
            throw new RuntimeException(context.toString() + " must implement AddChildDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface AddChildDialogListener {
        void onAddNewChild(String name);
    }
}
