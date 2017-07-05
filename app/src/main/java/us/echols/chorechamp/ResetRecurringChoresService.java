package us.echols.chorechamp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import us.echols.chorechamp.database.DbHelper;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in a service on a separate handler thread.
 */
public class ResetRecurringChoresService extends IntentService {

    public ResetRecurringChoresService() {
        super("ResetRecurringChoresService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("ChoreChamp", "Reset Recurring Chores service running");
        DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
        dbHelper.resetRecurringChores();
    }
}