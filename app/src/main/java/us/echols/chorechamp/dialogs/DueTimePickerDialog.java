package us.echols.chorechamp.dialogs;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

import us.echols.chorechamp.activities.ChoreDetailsActivity;

/**
 * A dialog to select a time
 */
public class DueTimePickerDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

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
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        timeInMillis = c.getTimeInMillis();
        listener.setTimeFromPicker(timeInMillis);
    }

    private TimePickerListener listener;

    public interface TimePickerListener {
        void setTimeFromPicker(long timeInMillis);
    }
    public void onSetTimePickerListener(TimePickerListener listener) {
        this.listener = listener;
    }

}
