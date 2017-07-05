package us.echols.chorechamp.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import java.nio.charset.StandardCharsets;

import us.echols.chorechamp.PasswordUtility;
import us.echols.chorechamp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the {@link ConfirmPasswordDialogListener} interface to handle interaction events.
 * Use the {@link ConfirmPasswordDialog#newInstance} factory method to create an instance of this fragment.
 */
public class ConfirmPasswordDialog extends DialogFragment {

    private ConfirmPasswordDialogListener listener;

    public ConfirmPasswordDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddChildDialog.
     */
    public static ConfirmPasswordDialog newInstance() {
        return new ConfirmPasswordDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog_Alert);

        alertDialogBuilder.setTitle(getString(R.string.title_enter_password));

        final EditText editText = new EditText(getContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alertDialogBuilder.setView(editText);

        alertDialogBuilder.setPositiveButton(getString(android.R.string.ok),  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    byte[] password = editText.getText().toString().getBytes(StandardCharsets.UTF_8);
                    PasswordUtility passwordUtility = PasswordUtility.getInstance(getContext());
                    if(passwordUtility.verifyPassword(password)) {
                        listener.onConfirmPassword();
                    } else {
                        listener.onBadPassword();
                    }
                }
                closeDialog(dialog);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onCancel();
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
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface ConfirmPasswordDialogListener {
        void onConfirmPassword();
        void onBadPassword();
        void onCancel();
    }

    public void setConfirmPasswordDialogListener(ConfirmPasswordDialogListener listener) {
        this.listener = listener;
    }
}
