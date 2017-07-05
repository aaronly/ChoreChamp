package us.echols.chorechamp.dialogs;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

import us.echols.chorechamp.activities.ChoreDetailsActivity;

/**
 * A dialog to select a date
 */
public class DueDatePickerDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private long timeInMillis;
    private final Calendar c = Calendar.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        timeInMillis = bundle.getLong(ChoreDetailsActivity.TAG_TIME_IN_MILLIS);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        c.setTimeInMillis(timeInMillis);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        c.set(year, month, day);
        timeInMillis = c.getTimeInMillis();
        listener.setDateFromPicker(timeInMillis);
    }

    private DatePickerListener listener;
    public interface DatePickerListener {
        void setDateFromPicker(long timeInMillis);
    }
    public void onSetDatePickerListener(DatePickerListener listener) {
        this.listener = listener;
    }

}
