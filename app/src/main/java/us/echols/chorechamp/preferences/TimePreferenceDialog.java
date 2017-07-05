package us.echols.chorechamp.preferences;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import us.echols.chorechamp.R;

@SuppressWarnings("deprecation")
public class TimePreferenceDialog extends PreferenceDialogFragmentCompat {

    private TimePicker timePicker;

    public static TimePreferenceDialog newInstance(String key) {
        TimePreferenceDialog fragment = new TimePreferenceDialog();
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        timePicker = (TimePicker)view.findViewById(R.id.edit);

        // Exception when there is no TimePicker
        if (timePicker == null) {
            throw new IllegalStateException("Dialog view must contain a TimePicker with id 'edit'");
        }

        // Get the time from the related Preference
        Long time = null;
        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            time = ((TimePreference)preference).getTime();
        }

        // Set the time to the TimePicker
        if (time != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            boolean is24hour = DateFormat.is24HourFormat(getContext());

            timePicker.setIs24HourView(is24hour);
            timePicker.setCurrentHour(hours);
            timePicker.setCurrentMinute(minutes);
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());

            // Get the related Preference and save the value
            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                TimePreference timePreference = (TimePreference)preference;

                Date date = new Date(calendar.getTimeInMillis());
                CharSequence timeString = DateFormat.getTimeFormat(getContext()).format(date);
                timePreference.setSummary(timeString);

                if (timePreference.callChangeListener(calendar.getTimeInMillis())) {
                    timePreference.setTime(calendar.getTimeInMillis());
                }
            }
        }
    }
}
