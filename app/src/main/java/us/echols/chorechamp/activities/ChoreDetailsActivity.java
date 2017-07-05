package us.echols.chorechamp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import us.echols.chorechamp.Chore;
import us.echols.chorechamp.R;
import us.echols.chorechamp.database.DbHelper;
import us.echols.chorechamp.dialogs.DueDatePickerDialog;
import us.echols.chorechamp.dialogs.DueTimePickerDialog;

public class ChoreDetailsActivity extends AppCompatActivity {

    private DbHelper dbHelper;

    private Switch switchFreq;
    private Button buttonDate;
    private TextView textViewDueDate;
    private TextInputEditText editTextChoreName;

    private DueDatePickerDialog datePickerDialog;
    private DueTimePickerDialog timePickerDialog;

    private static final DateFormat FULL_DATE_TIME_FORMAT = new SimpleDateFormat("EEEE, MMMM d 'at' h:mma");
    private static final DateFormat SHORT_TIME_FORMAT = new SimpleDateFormat("h:mm a");

    public static final String TAG_TIME_IN_MILLIS = "us.echols.chorechamp.TAG.time_in_millis";
    private static final String TAG_DATE_PICKER = "us.echols.chorechamp.TAG.pick_date";
    private static final String TAG_TIME_PICKER = "us.echols.chorechamp.TAG.pick_time";

    private static final String ARG_CHORE_ID = "chore_id";
    private static final String ARG_NAME = "chore_name";
    private static final String ARG_DUE_DATETIME = "chore_due_datetime";
    private static final String ARG_FREQUENCY = "chore_frequency";
    private static final String ARG_WEEKDAYS = "chore_weekdays";

    private Chore chore;
    private long choreId;
    private String name;
    private long timeInMillis;
    private Boolean isRecurring;
    private boolean[] weekdays;
    private String childName;

    private int resultCode = RESULT_CANCELED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chore_details);

        dbHelper = DbHelper.getInstance(this);

        // get the activity that called this activity
        Intent intent = getIntent();
        childName = intent.getStringExtra(MainActivity.EXTRA_CHILD_NAME);

        choreId = intent.getLongExtra(MainActivity.EXTRA_CHORE_ID, 0);
        if (savedInstanceState != null) {
            choreId = savedInstanceState.getLong(ARG_CHORE_ID);
            name = savedInstanceState.getString(ARG_NAME);
            timeInMillis = savedInstanceState.getLong(ARG_DUE_DATETIME);
            isRecurring = savedInstanceState.getBoolean(ARG_FREQUENCY);
            weekdays = savedInstanceState.getBooleanArray(ARG_WEEKDAYS);
        } else if (choreId > 0) {
            chore = dbHelper.getChoreById(choreId);
            name = chore.getName();
            timeInMillis = chore.getTimeInMillis();
            isRecurring = chore.isRecurring();
            weekdays = chore.getWeekdays();
        } else {
            isRecurring = false;
        }

        // get toolbar properties
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Calendar now = Calendar.getInstance();
        if (timeInMillis == 0) {
            getDefaultDueTime(now);
        }
        if (weekdays == null) {
            weekdays = new boolean[7];
            int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
            weekdays[dayOfWeek - 1] = true;
        }

        datePickerDialog = new DueDatePickerDialog();
        datePickerDialog.onSetDatePickerListener(new DueDatePickerDialog.DatePickerListener() {
            @Override
            public void setDateFromPicker(long timeInMillis) {
                updateDueDate(timeInMillis);
            }
        });

        timePickerDialog = new DueTimePickerDialog();
        timePickerDialog.onSetTimePickerListener(new DueTimePickerDialog.TimePickerListener() {
            @Override
            public void setTimeFromPicker(long timeInMillis) {
                updateDueTime(timeInMillis);
            }
        });

        editTextChoreName = (TextInputEditText)findViewById(R.id.editText_chore_name);
        if (name != null) {
            editTextChoreName.setText(name);
        }

        buttonDate = (Button)findViewById(R.id.button_date);
        buttonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDateOrDays();
            }
        });

        Button buttonTime = (Button)findViewById(R.id.button_time);
        buttonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDueTime();
            }
        });

        textViewDueDate = (TextView)findViewById(R.id.textView_due_date);

        switchFreq = (Switch)findViewById(R.id.switch_frequency);
        switchFreq.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isRecurring = isChecked;
                updateDueDateText(isChecked);
            }
        });
        switchFreq.setChecked(isRecurring);

        updateDueDateText(switchFreq.isChecked());

        Button buttonSave = (Button)findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChoreToDatabase();
            }
        });

        Button buttonCancel = (Button)findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getDefaultDueTime(Calendar now) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String key = getString(R.string.settings_time_key);
        timeInMillis = sharedPref.getLong(key, 330581700000L);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(timeInMillis);
        time.set(Calendar.YEAR, now.get(Calendar.YEAR));
        time.set(Calendar.MONTH, now.get(Calendar.MONTH));
        time.set(Calendar.DATE, now.get(Calendar.DATE));
        timeInMillis = time.getTimeInMillis();
    }

    private void updateDueDateText(boolean isChecked) {
        if (isChecked) {
            switchFreq.setText(getString(R.string.chore_freq_recurring));
            buttonDate.setText(getString(R.string.chore_select_days));
            String weekdays = getWeekdaysString();
            String timeText = SHORT_TIME_FORMAT.format(new Date(timeInMillis));
            textViewDueDate.setText(weekdays + " at " + timeText);
        } else {
            switchFreq.setText(getString(R.string.chore_freq_once));
            buttonDate.setText(getString(R.string.chore_select_date));
            String dueDateText = FULL_DATE_TIME_FORMAT.format(new Date(timeInMillis));
            textViewDueDate.setText(dueDateText);
        }
    }

    private String getWeekdaysString() {
        String finalString = "";

        boolean everydayFlag = true;
        boolean[] everydayTest = new boolean[]{true, true, true, true, true, true, true};
        boolean weekdayFlag = true;
        boolean[] weekdayTest = new boolean[]{false, true, true, true, true, true, false};
        boolean weekendFlag = true;
        boolean[] weekendTest = new boolean[]{true, false, false, false, false, false, true};

        StringBuilder weekdayText = new StringBuilder();
        String[] daysOfWeek = getResources().getStringArray(R.array.chore_days_of_the_week);
        for (int n = 0; n < weekdays.length; n++) {
            if (weekdays[n]) {
                weekdayText.append(daysOfWeek[n]).append(", ");
            }

            if (everydayFlag && everydayTest[n] != weekdays[n]) {
                everydayFlag = false;
            }
            if (weekdayFlag && weekdayTest[n] != weekdays[n]) {
                weekdayFlag = false;
            }
            if (weekendFlag && weekendTest[n] != weekdays[n]) {
                weekendFlag = false;
            }
        }
//        if (weekdayText.length() > 0) {
//            weekdayText.setLength(weekdayText.length() - 2);
//        }

        if (everydayFlag) {
            finalString = "Everyday";
        } else if (weekdayFlag) {
            finalString = "Weekdays";
        } else if (weekendFlag) {
            finalString = "Weekends";
        } else if (weekdayText.length() > 0) {
            weekdayText.setLength(weekdayText.length() - 2);
            finalString = weekdayText.toString();
        }

        return finalString;
    }

    private void setDateOrDays() {
        if (switchFreq.isChecked()) {
            setDaysOfWeek();
        } else {
            setDueDate();
        }
    }

    private void setDaysOfWeek() {
        final boolean[] selection = Arrays.copyOf(weekdays, weekdays.length);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMultiChoiceItems(R.array.chore_days_of_the_week, selection, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                selection[which] = isChecked;
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                weekdays = selection;
                dialog.dismiss();
                updateDueDateText(switchFreq.isChecked());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Display the date picker dialog to let the user select a new due date
     */
    private void setDueDate() {
        // check if the start date picker has arguments
        if (datePickerDialog.getArguments() == null) {
            // set up a bundle to transfer the date
            Bundle bundle = new Bundle();
            bundle.putLong(TAG_TIME_IN_MILLIS, timeInMillis);
            datePickerDialog.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().add(datePickerDialog, TAG_DATE_PICKER).addToBackStack(null).commit();
        } else {
            datePickerDialog.show(getSupportFragmentManager(), TAG_DATE_PICKER);
        }
    }

    private void updateDueDate(long timeInMillis) {
        Calendar oldDate = Calendar.getInstance();
        oldDate.setTimeInMillis(this.timeInMillis);
        int hour = oldDate.get(Calendar.HOUR_OF_DAY);
        int minute = oldDate.get(Calendar.MINUTE);

        Calendar newDate = Calendar.getInstance();
        newDate.setTimeInMillis(timeInMillis);
        newDate.set(Calendar.HOUR_OF_DAY, hour);
        newDate.set(Calendar.MINUTE, minute);

        this.timeInMillis = newDate.getTimeInMillis();
        updateDueDateText(switchFreq.isChecked());
    }

    /**
     * Display the time picker dialog to let the user select a new due time
     */
    private void setDueTime() {
        // check if the start date picker has arguments
        if (timePickerDialog.getArguments() == null) {
            // set up a bundle to transfer the date
            Bundle bundle = new Bundle();
            bundle.putLong(TAG_TIME_IN_MILLIS, timeInMillis);
            timePickerDialog.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().add(timePickerDialog, TAG_TIME_PICKER).addToBackStack(null).commit();
        } else {
            timePickerDialog.show(getSupportFragmentManager(), TAG_TIME_PICKER);
        }
    }

    private void updateDueTime(long timeInMillis) {
        Calendar oldTime = Calendar.getInstance();
        oldTime.setTimeInMillis(this.timeInMillis);
        int year = oldTime.get(Calendar.YEAR);
        int month = oldTime.get(Calendar.MONTH);
        int day = oldTime.get(Calendar.DATE);

        Calendar newTime = Calendar.getInstance();
        newTime.setTimeInMillis(timeInMillis);
        newTime.set(Calendar.YEAR, year);
        newTime.set(Calendar.MONTH, month);
        newTime.set(Calendar.DATE, day);

        this.timeInMillis = newTime.getTimeInMillis();
        updateDueDateText(switchFreq.isChecked());
    }

    private void saveChoreToDatabase() {
        name = editTextChoreName.getText().toString();
        if(isRecurring) {
            chore = new Chore(choreId, name, childName, timeInMillis, weekdays);
        } else {
            chore = new Chore(choreId, name, childName, timeInMillis);
        }

        if(choreId == 0) {
            choreId = dbHelper.addChore(chore);
            chore.setId(choreId);
        } else {
            dbHelper.updateChore(chore);
        }

        resultCode = RESULT_OK;
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARG_CHORE_ID, choreId);
        outState.putString(ARG_NAME, name);
        outState.putLong(ARG_DUE_DATETIME, timeInMillis);
        outState.putBoolean(ARG_FREQUENCY, isRecurring);
        outState.putBooleanArray(ARG_WEEKDAYS, weekdays);
    }

    @Override
    public void finish() {
        Intent intent = new Intent(this, MainActivity.class);
        if(resultCode == RESULT_OK) {
            intent.putExtra(getString(R.string.intent_chore_id), chore.getId());
        }
        setResult(resultCode, intent);
        super.finish();
    }

}
