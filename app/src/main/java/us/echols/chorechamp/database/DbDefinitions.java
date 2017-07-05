package us.echols.chorechamp.database;

import android.provider.BaseColumns;

final class DbDefinitions {
    private DbDefinitions() {}

    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "chorechamp.db";

    static class Child implements BaseColumns {
        static final String TABLE_NAME = "child";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_COMPLETED = "completed";
    }

    static final String SQL_CREATE_CHILD_TABLE =
            "CREATE TABLE " + Child.TABLE_NAME + "(" +
                    Child._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Child.COLUMN_NAME + " TEXT UNIQUE NOT NULL," +
                    Child.COLUMN_COMPLETED + " INTEGER DEFAULT 0)";
    static final String SQL_DELETE_CHILD_TABLE = "DROP TABLE IF EXISTS " + Child.TABLE_NAME;

    static class Chore implements BaseColumns {
        static final String TABLE_NAME = "chore";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_RECURRING = "recurring";
        static final String COLUMN_DUE_TIME = "due_time";
        static final String COLUMN_WEEKDAYS = "weekdays";
        static final String COLUMN_CHILD_ID = "child_id";
        static final String COLUMN_STATUS = "status";
    }

    static final String SQL_CREATE_CHORE_TABLE =
            "CREATE TABLE " + Chore.TABLE_NAME + "(" +
                    Chore._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Chore.COLUMN_NAME + " TEXT NOT NULL, " +
                    Chore.COLUMN_RECURRING + " INTEGER DEFAULT 0, " +
                    Chore.COLUMN_DUE_TIME + " INTEGER DEFAULT 0, " +
                    Chore.COLUMN_WEEKDAYS + " INTEGER DEFAULT 0, " +
                    Chore.COLUMN_STATUS + " INTEGER DEFAULT 0, " +
                    Chore.COLUMN_CHILD_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + Chore.COLUMN_CHILD_ID + ") " +
                    "REFERENCES " + Child.TABLE_NAME + "(" + Child._ID + ") ON DELETE CASCADE)";
    static final String SQL_DELETE_CHORE_TABLE = "DROP TABLE IF EXISTS " + Chore.TABLE_NAME;

    static class Achievement implements BaseColumns {
        static final String TABLE_NAME = "achievement";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_COUNT = "count";
    }

    static final String SQL_CREATE_ACHIEVEMENT_TABLE =
            "CREATE TABLE " + Achievement.TABLE_NAME + "(" +
                    Achievement._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Achievement.COLUMN_NAME + " TEXT NOT NULL, " +
                    Achievement.COLUMN_COUNT + " INTEGER NOT NULL DEFAULT 0)";
    static final String SQL_DELETE_ACHIEVEMENT_TABLE = "DROP TABLE IF EXISTS " + Achievement.TABLE_NAME;

    static class CompletedAchievement implements BaseColumns {
        static final String TABLE_NAME = "completed_achievement";
        static final String COLUMN_ACHIEVEMENT_ID = "achievement_id";
        static final String COLUMN_CHILD_ID = "child_id";
    }

    static final String SQL_CREATE_COMPLETED_ACHIEVEMENT_TABLE =
            "CREATE TABLE " + CompletedAchievement.TABLE_NAME + "(" +
                    CompletedAchievement._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CompletedAchievement.COLUMN_ACHIEVEMENT_ID + " INTEGER NOT NULL, " +
                    CompletedAchievement.COLUMN_CHILD_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + CompletedAchievement.COLUMN_ACHIEVEMENT_ID + ") " +
                    "REFERENCES " + Achievement.TABLE_NAME + "(" + Achievement._ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY (" + CompletedAchievement.COLUMN_CHILD_ID + ") " +
                    "REFERENCES " + Child.TABLE_NAME + "(" + Child._ID + ") ON DELETE CASCADE)";
    static final String SQL_DELETE_COMPLETED_ACHIEVEMENT_TABLE = "DROP TABLE IF EXISTS " + CompletedAchievement.TABLE_NAME;

}
