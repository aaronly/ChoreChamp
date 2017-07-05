package us.echols.chorechamp.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.echols.chorechamp.Achievement;
import us.echols.chorechamp.Chore;

@SuppressWarnings("UnusedReturnValue")
public class DbHelper extends SQLiteOpenHelper {

    @SuppressLint("StaticFieldLeak")
    private static DbHelper instance;

    /**
     * Instance initializer. Returns the copy in memory if one has already been created
     *
     * @param context the context in which to create
     * @return the DbHelper instance
     */
    public static synchronized DbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DbHelper(Context context) {
        super(context, DbDefinitions.DATABASE_NAME, null, DbDefinitions.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbDefinitions.SQL_CREATE_CHILD_TABLE);
        db.execSQL(DbDefinitions.SQL_CREATE_CHORE_TABLE);
        db.execSQL(DbDefinitions.SQL_CREATE_ACHIEVEMENT_TABLE);
        db.execSQL(DbDefinitions.SQL_CREATE_COMPLETED_ACHIEVEMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbDefinitions.SQL_DELETE_CHILD_TABLE);
        db.execSQL(DbDefinitions.SQL_DELETE_CHORE_TABLE);
        db.execSQL(DbDefinitions.SQL_DELETE_ACHIEVEMENT_TABLE);
        db.execSQL(DbDefinitions.SQL_DELETE_COMPLETED_ACHIEVEMENT_TABLE);

        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        // make sure that foreign key constrains are enabled for this database
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /* CHILD TABLE FUNCTIONS */

    public long addChild(String name) {
        long id = getChildId(name);
        if (id != 0) {
            return id;
        }

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(DbDefinitions.Child.COLUMN_NAME, name);
        return db.insert(DbDefinitions.Child.TABLE_NAME, null, values);
    }

    public long deleteChild(long id) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // get the table to delete from
        String table = DbDefinitions.Child.TABLE_NAME;

        // define the delete filter
        String selection = DbDefinitions.Child._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        return db.delete(table, selection, selectionArgs);
    }

    public long getChildId(String name) {
        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to query
        String table = DbDefinitions.Child.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.Child._ID,
                DbDefinitions.Child.COLUMN_NAME
        };

        // define the query filter
        String selection = DbDefinitions.Child.COLUMN_NAME + " = ?";
        String[] selectionArgs = {name};

        // define the sort order
        String sortOrder = DbDefinitions.Child._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // get the result
        long id = 0;
        if (c.moveToNext()) {
            id = c.getLong(c.getColumnIndexOrThrow(DbDefinitions.Child._ID));
        }
        c.close();

        return id;
    }

    public String getChildName(long id) {
        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to delete from
        String table = DbDefinitions.Child.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.Child._ID,
                DbDefinitions.Child.COLUMN_NAME
        };

        // define the query filter
        String selection = DbDefinitions.Child._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        // define the sort order
        String sortOrder = DbDefinitions.Child._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // get the result
        String result = null;
        if (c.moveToNext()) {
            result = c.getString(c.getColumnIndexOrThrow(DbDefinitions.Child.COLUMN_NAME));
        }
        c.close();

        return result;
    }

    public String[] getChildNames() {
        List<String> results = new ArrayList<>();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to delete from
        String table = DbDefinitions.Child.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.Child._ID,
                DbDefinitions.Child.COLUMN_NAME
        };

        // define the sort order
        String sortOrder = DbDefinitions.Child._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, null, null, null, null, sortOrder);

        // get the result
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbDefinitions.Child.COLUMN_NAME));
            results.add(name);
        }
        c.close();

        String[] childNames = new String[0];
        return results.toArray(childNames);
    }

    public int updateChildChoreCount(String childName) {
        long childId = getChildId(childName);
        int completed = getAchievementCount(childName);

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(DbDefinitions.Child.COLUMN_COMPLETED, completed + 1);

        // Which row to update, based on the title
        String selection = DbDefinitions.Child._ID + " = ?";
        String[] selectionArgs = {String.valueOf(childId)};

        return db.update(DbDefinitions.Child.TABLE_NAME, values, selection, selectionArgs);
    }

    private int getAchievementCount(String childName) {

        long childId = getChildId(childName);

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to delete from
        String table = DbDefinitions.Child.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.Child._ID,
                DbDefinitions.Child.COLUMN_COMPLETED
        };

        // define the query filter
        String selection = DbDefinitions.Child._ID + " = ?";
        String[] selectionArgs = {String.valueOf(childId)};

        // define the sort order
        String sortOrder = DbDefinitions.Child._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        int count = 0;
        // get the result
        if (c.moveToNext()) {
            count = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Child.COLUMN_COMPLETED));
        }
        c.close();

        return count;
    }

    /* CHORE TABLE FUNCTIONS */

    public long addChore(Chore chore) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        String childName = chore.getChildName();
        long childId = getChildId(childName);

        values.put(DbDefinitions.Chore.COLUMN_NAME, chore.getName());
        values.put(DbDefinitions.Chore.COLUMN_RECURRING, chore.isRecurring());
        values.put(DbDefinitions.Chore.COLUMN_DUE_TIME, chore.getTimeInMillis());
        int weekdayMask = weekdaysArrayToInt(chore.getWeekdays());
        values.put(DbDefinitions.Chore.COLUMN_WEEKDAYS, weekdayMask);
        values.put(DbDefinitions.Chore.COLUMN_CHILD_ID, childId);
        values.put(DbDefinitions.Chore.COLUMN_STATUS, chore.getStatus().getValue());
        return db.insert(DbDefinitions.Chore.TABLE_NAME, null, values);
    }

    public Chore getChoreById(long id) {
        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to delete from
        String table = DbDefinitions.Chore.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.Chore._ID,
                DbDefinitions.Chore.COLUMN_NAME,
                DbDefinitions.Chore.COLUMN_RECURRING,
                DbDefinitions.Chore.COLUMN_DUE_TIME,
                DbDefinitions.Chore.COLUMN_WEEKDAYS,
                DbDefinitions.Chore.COLUMN_CHILD_ID,
                DbDefinitions.Chore.COLUMN_STATUS
        };

        // define the query filter
        String selection = DbDefinitions.Chore._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        // define the sort order
        String sortOrder = DbDefinitions.Chore._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // get the result
        Chore chore = null;
        if (c.moveToNext()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_NAME));
            int recurring = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_RECURRING));
            boolean isRecurring = false;
            int weekdayMask;
            boolean[] weekdays = null;
            if (recurring == 1) {
                isRecurring = true;
                weekdayMask = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_WEEKDAYS));
                weekdays = weekdaysIntToArray(weekdayMask);
            }
            long timeInMillis = c.getLong(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_DUE_TIME));
            long childId = c.getLong(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_CHILD_ID));
            int statusInt = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_STATUS));
            Chore.StatusType status = Chore.StatusType.getEnum(statusInt);

            if (isRecurring) {
                chore = new Chore(id, name, getChildName(childId), timeInMillis, weekdays);
            } else {
                chore = new Chore(id, name, getChildName(childId), timeInMillis);
            }
            chore.setStatus(status);
        }
        c.close();

        return chore;
    }

    public int updateChore(Chore chore) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(DbDefinitions.Chore.COLUMN_NAME, chore.getName());
        values.put(DbDefinitions.Chore.COLUMN_RECURRING, chore.isRecurring());
        values.put(DbDefinitions.Chore.COLUMN_DUE_TIME, chore.getTimeInMillis());
        int weekdayMask = weekdaysArrayToInt(chore.getWeekdays());
        values.put(DbDefinitions.Chore.COLUMN_WEEKDAYS, weekdayMask);
        values.put(DbDefinitions.Chore.COLUMN_STATUS, chore.getStatus().getValue());

        // Which row to update, based on the title
        String selection = DbDefinitions.Chore._ID + " = ?";
        String[] selectionArgs = {String.valueOf(chore.getId())};

        return db.update(DbDefinitions.Chore.TABLE_NAME, values, selection, selectionArgs);
    }

    public List<Chore> getChoresByChild(String childName) {
        List<Chore> chores = new ArrayList<>();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to delete from
        String table = DbDefinitions.Chore.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.Chore._ID,
                DbDefinitions.Chore.COLUMN_NAME,
                DbDefinitions.Chore.COLUMN_RECURRING,
                DbDefinitions.Chore.COLUMN_DUE_TIME,
                DbDefinitions.Chore.COLUMN_WEEKDAYS,
                DbDefinitions.Chore.COLUMN_CHILD_ID,
                DbDefinitions.Chore.COLUMN_STATUS
        };

        // define the query filter
        String selection = DbDefinitions.Chore.COLUMN_CHILD_ID + " = ? AND " +
                DbDefinitions.Chore.COLUMN_STATUS + " = ?";
        long childId = getChildId(childName);
        Chore.StatusType status = Chore.StatusType.ACTIVE;
        String[] selectionArgs = {String.valueOf(childId), String.valueOf(status.getValue())};

        // define the sort order
        String sortOrder = DbDefinitions.Chore._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // get the result
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(DbDefinitions.Chore._ID));
            String name = c.getString(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_NAME));
            int recurring = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_RECURRING));
            boolean isRecurring = false;
            int weekdayMask;
            boolean[] weekdays = null;
            if (recurring == 1) {
                isRecurring = true;
                weekdayMask = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_WEEKDAYS));
                weekdays = weekdaysIntToArray(weekdayMask);
            }
            long timeInMillis = c.getLong(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_DUE_TIME));

            if (isRecurring) {
                chores.add(new Chore(id, name, getChildName(childId), timeInMillis, weekdays));
            } else {
                chores.add(new Chore(id, name, getChildName(childId), timeInMillis));
            }
            chores.get(chores.size() - 1).setStatus(status);

        }
        c.close();

        return chores;
    }

    public List<Chore> getChores() {
        List<Chore> chores = new ArrayList<>();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to delete from
        String table = DbDefinitions.Chore.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.Chore._ID,
                DbDefinitions.Chore.COLUMN_NAME,
                DbDefinitions.Chore.COLUMN_RECURRING,
                DbDefinitions.Chore.COLUMN_DUE_TIME,
                DbDefinitions.Chore.COLUMN_WEEKDAYS,
                DbDefinitions.Chore.COLUMN_CHILD_ID,
                DbDefinitions.Chore.COLUMN_STATUS
        };

        // define the query filter
        String selection = DbDefinitions.Chore.COLUMN_STATUS + " = ?";
        Chore.StatusType status = Chore.StatusType.PENDING;
        String[] selectionArgs = {String.valueOf(status.getValue())};

        // define the sort order
        String sortOrder = DbDefinitions.Chore._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // get the result
        while (c.moveToNext()) {
            long id = c.getLong(c.getColumnIndexOrThrow(DbDefinitions.Chore._ID));
            String name = c.getString(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_NAME));
            int recurring = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_RECURRING));
            boolean isRecurring = false;
            int weekdayMask;
            boolean[] weekdays = null;
            if (recurring == 1) {
                isRecurring = true;
                weekdayMask = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_WEEKDAYS));
                weekdays = weekdaysIntToArray(weekdayMask);
            }
            long timeInMillis = c.getLong(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_DUE_TIME));
            long childId = c.getLong(c.getColumnIndexOrThrow(DbDefinitions.Chore.COLUMN_CHILD_ID));

            if (isRecurring) {
                chores.add(new Chore(id, name, getChildName(childId), timeInMillis, weekdays));
            } else {
                chores.add(new Chore(id, name, getChildName(childId), timeInMillis));
            }
            chores.get(chores.size() - 1).setStatus(status);

        }
        c.close();

        return chores;
    }

    public int deleteChores(List<Chore> chores) {
        // return 0 if the set was null or included no items
        if (chores == null || chores.isEmpty()) return 0;

        String tableName = null; // the name of the table to delete rows from
        String selection = null; // the 'where' part of the query
        String[] selectionArgs = new String[chores.size()]; // the arguments for the query

        int index = 0;
        for (Chore chore : chores) {
            if (index == 0) { // if it is the first (or only) item
                tableName = DbDefinitions.Chore.TABLE_NAME;
                selection = DbDefinitions.Chore._ID + " IN (?";
            } else { // for additional items
                selection += ",?";
            }
            // add the id to the selection arguments
            selectionArgs[index++] = String.valueOf(chore.getId());
        }

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Issue SQL statement.
        return db.delete(tableName, selection + ")", selectionArgs);
    }

    private int weekdaysArrayToInt(boolean[] weekdays) {
        int result = 0;
        if (weekdays == null) {
            return result;
        }

        for (int n = 0; n < weekdays.length; n++) {
            if (weekdays[n]) {
                result += Math.pow(2, n);
            }
        }

        return result;
    }

    private boolean[] weekdaysIntToArray(int weekdays) {
        boolean[] result = new boolean[7];

        for (int n = result.length - 1; n >= 0; n--) {
            if (weekdays >= Math.pow(2, n)) {
                weekdays -= Math.pow(2, n);
                result[n] = true;
            }
        }
        return result;
    }

    public int resetRecurringChores() {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(DbDefinitions.Chore.COLUMN_STATUS, Chore.StatusType.ACTIVE.getValue());

        // Which row to update, based on the title
        String selection = DbDefinitions.Chore.COLUMN_RECURRING + " = ?";
        String[] selectionArgs = {String.valueOf(1)};

        return db.update(DbDefinitions.Chore.TABLE_NAME, values, selection, selectionArgs);
    }

    /* ACHIEVEMENTS */

    private Map<String, Integer> createAchievements() {
        Map<String, Integer> achievements = new HashMap<>();

        achievements.put("Complete Your First Chore", 1);
        achievements.put("Complete 5 Total Chores", 5);
        achievements.put("Complete 10 Total Chores", 10);
        achievements.put("Complete 15 Total Chores", 15);
        for (int i = 25; i <= 175; i += 25) {
            achievements.put("Complete " + i + " Total Chores", i);
        }
        for (int i = 200; i <= 1000; i += 50) {
            achievements.put("Complete " + i + " Total Chores", i);
        }

        return achievements;
    }

    public long[] addAchievements() {
        if(getAchievements().size() > 0) {
            return null;
        }

        Map<String, Integer> achievements = createAchievements();
        String[] names = new String[achievements.size()];
        achievements.keySet().toArray(names);

        long[] ids = new long[achievements.size()];

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();

            db.beginTransaction();

            for (int i = 0; i < achievements.size(); i++) {
                String name = names[i];
                int count = achievements.get(name);
                values.put(DbDefinitions.Achievement.COLUMN_NAME, name);
                values.put(DbDefinitions.Achievement.COLUMN_COUNT, count);
                ids[i] = db.insert(DbDefinitions.Achievement.TABLE_NAME, null, values);
            }

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return ids;
    }

    public List<Achievement> getAchievements() {
        List<Achievement> achievements = new ArrayList<>();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to delete from
        String table = DbDefinitions.Achievement.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.Achievement._ID,
                DbDefinitions.Achievement.COLUMN_NAME,
                DbDefinitions.Achievement.COLUMN_COUNT
        };

        // define the sort order
        String sortOrder = DbDefinitions.Achievement.COLUMN_COUNT + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, null, null, null, null, sortOrder);

        // get the result
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbDefinitions.Achievement.COLUMN_NAME));
            int count = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Achievement.COLUMN_COUNT));

            Achievement achievement = new Achievement(name, count);
            achievement.setComplete(false);
            achievements.add(achievement);
        }
        c.close();

        return achievements;

    }

    public List<Achievement> getAchievementsByChild(String childName) {
        long childId = getChildId(childName);
        List<Achievement> achievements = new ArrayList<>();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // create a new query builder
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // get the tables to join
        String tables = DbDefinitions.Achievement.TABLE_NAME
                + " INNER JOIN "
                + DbDefinitions.CompletedAchievement.TABLE_NAME
                + " ON "
                + DbDefinitions.Achievement.TABLE_NAME + "." + DbDefinitions.Achievement._ID
                + " = "
                + DbDefinitions.CompletedAchievement.TABLE_NAME + "." + DbDefinitions.CompletedAchievement.COLUMN_ACHIEVEMENT_ID;
        qb.setTables(tables);

        // define the columns to return
        String[] projection = {
                DbDefinitions.Achievement.TABLE_NAME + "." + DbDefinitions.Achievement._ID,
                DbDefinitions.Achievement.TABLE_NAME + "." + DbDefinitions.Achievement.COLUMN_NAME,
                DbDefinitions.Achievement.TABLE_NAME + "." + DbDefinitions.Achievement.COLUMN_COUNT,
                DbDefinitions.CompletedAchievement.TABLE_NAME + "." + DbDefinitions.CompletedAchievement.COLUMN_CHILD_ID
        };

        // define the query filter
        String selection = DbDefinitions.CompletedAchievement.TABLE_NAME + "." + DbDefinitions.CompletedAchievement.COLUMN_CHILD_ID + " = ?";
        String[] selectionArgs = {String.valueOf(childId)};

        // define the sort order
        String sortOrder = DbDefinitions.Achievement.TABLE_NAME + "." + DbDefinitions.Achievement._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // get the result
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbDefinitions.Achievement.COLUMN_NAME));
            int count = c.getInt(c.getColumnIndexOrThrow(DbDefinitions.Achievement.COLUMN_COUNT));


            Achievement achievement = new Achievement(name, count);
            achievement.setComplete(true);
            achievements.add(achievement);
        }
        c.close();

        return achievements;

    }

    public Achievement checkForAchievements(String childName) {
        Achievement result = null;

        int count = getAchievementCount(childName);

        List<Achievement> achievements = getAchievements();

        for(int i = achievements.size() - 1; i >= 0; i--) {
            Achievement achievement = achievements.get(i);
            if(count >= achievement.getCount() && !checkForAchievementCompletion(childName, achievement)) {
                updateAchievement(childName, achievement);
                result = achievement;
                break;
            }
        }

        return result;
    }

    private void updateAchievement(String childName, Achievement achievement) {
        long childId = getChildId(childName);
        long achievementId = getAchievementId(achievement);

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        values.put(DbDefinitions.CompletedAchievement.COLUMN_ACHIEVEMENT_ID, achievementId);
        values.put(DbDefinitions.CompletedAchievement.COLUMN_CHILD_ID, childId);

        db.insert(DbDefinitions.CompletedAchievement.TABLE_NAME, null, values);

    }

    private long getAchievementId(Achievement achievement) {
        int count = achievement.getCount();

        // get the database in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to query
        String table = DbDefinitions.Achievement.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.Achievement._ID,
                DbDefinitions.Achievement.COLUMN_COUNT
        };

        // define the query filter
        String selection = DbDefinitions.Achievement.COLUMN_COUNT + " = ?";
        String[] selectionArgs = {String.valueOf(count)};

        // define the sort order
        String sortOrder = DbDefinitions.Achievement._ID + " ASC";

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

        // get the result
        long id = 0;
        if (c.moveToNext()) {
            id = c.getLong(c.getColumnIndexOrThrow(DbDefinitions.Achievement._ID));
        }
        c.close();

        return id;

    }

    private boolean checkForAchievementCompletion(String childName, Achievement achievement) {
        boolean result = false;

        long childId = getChildId(childName);
        long achievementId = getAchievementId(achievement);

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getReadableDatabase();

        // get the table to delete from
        String table = DbDefinitions.CompletedAchievement.TABLE_NAME;

        // define the columns to return
        String[] projection = {
                DbDefinitions.CompletedAchievement.COLUMN_ACHIEVEMENT_ID,
                DbDefinitions.CompletedAchievement.COLUMN_CHILD_ID
        };

        // define the query filter
        String selection = DbDefinitions.CompletedAchievement.COLUMN_ACHIEVEMENT_ID + " = ? AND " +
                DbDefinitions.CompletedAchievement.COLUMN_CHILD_ID + " = ?";
        String[] selectionArgs = {String.valueOf(achievementId), String.valueOf(childId)};

        // get a cursor pointing to the results of the query
        Cursor c = db.query(table, projection, selection, selectionArgs, null, null, null);

        // get the result
        if (c.moveToNext()) {
            result = true;

        }
        c.close();

        return result;
    }
}
