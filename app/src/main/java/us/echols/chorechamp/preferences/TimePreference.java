package us.echols.chorechamp.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import us.echols.chorechamp.R;

@SuppressWarnings({"unused", "WeakerAccess"})
public class TimePreference extends DialogPreference {

    private Calendar calendar;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        calendar = new GregorianCalendar();
    }

    long getTime() {
        return calendar.getTimeInMillis();
    }
    void setTime(long timeInMillis) {
        calendar.setTimeInMillis(timeInMillis);
        persistLong(timeInMillis);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String timeString = a.getString(index);
        return Double.valueOf(timeString).longValue();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            setTime(getPersistedLong(getTime()));
        } else {
            if (defaultValue == null) {
                setTime(System.currentTimeMillis());
            } else {
                setTime((long)defaultValue);
            }
        }
        setSummary(getSummary());
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_dialog_time_picker;
    }

    @Override
    public CharSequence getSummary() {
        if (calendar == null) {
            return null;
        }
        return DateFormat.getTimeFormat(getContext()).format(new Date(getTime()));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent, use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current setting value
        myState.value = calendar.getTimeInMillis();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        calendar.setTimeInMillis(myState.value);
    }


    private static class SavedState extends BaseSavedState {
        long value;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            value = source.readLong();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(value);
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}

