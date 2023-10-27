package com.sas.listagram;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NoteDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "notelist.db";
    public static final int DATABASE_VERSION = 1;


    public NoteDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_NOTE_TABLE = "CREATE TABLE " +
                NoteContract.NoteEntry.TABLE_NAME + " (" +
                NoteContract.NoteEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteContract.NoteEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                NoteContract.NoteEntry.COLUMN_BUDGET + " REAL DEFAULT '0', " +
                NoteContract.NoteEntry.COLUMN_SORT_STATE + " INTEGER DEFAULT '" + NoteContract.SORT_BY_DATE + "', " +
                NoteContract.NoteEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
                NoteContract.NoteEntry.COLUMN_COLOR + " INTEGER NOT NULL, " +
                NoteContract.NoteEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), " + //Revisar esto cuando este dispuesto a desinstalar la app o con el otro cel
                NoteContract.NoteEntry.COLUMN_CREATION_TIMESTAMP + " TIMESTAMP DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime'))" +
                ");";
        final String SQL_CREATE_UNITS_TABLE = "CREATE TABLE " +
                NoteContract.UnitEntry.TABLE_NAME + " (" +
                NoteContract.UnitEntry.COLUMN_UNIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteContract.UnitEntry.COLUMN_UNIT_TITLE + " TEXT NOT NULL, " +
                NoteContract.UnitEntry.COLUMN_UNIT_IS_CHECKED + " BOOLEAN NOT NULL, " +
                NoteContract.UnitEntry.COLUMN_UNIT_COST + " REAL NOT NULL, " +
                NoteContract.UnitEntry.COLUMN_UNIT_TIMESTAMP + " TIMESTAMP DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), " +
                NoteContract.UnitEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + NoteContract.UnitEntry.COLUMN_ID + ") REFERENCES " + NoteContract.NoteEntry.TABLE_NAME + " (" + NoteContract.NoteEntry.COLUMN_ID + ") ON DELETE CASCADE);";

        final String SQL_CREATE_CHAT_TABLE = "CREATE TABLE " +
                NoteContract.ChatEntry.TABLE_NAME + " (" +
                NoteContract.ChatEntry.COLUMN_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteContract.ChatEntry.COLUMN_CHAT_NOTE + " TEXT NOT NULL, " +
                NoteContract.ChatEntry.COLUMN_CHAT_TIMESTAMP + " TIMESTAMP DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), " +
                NoteContract.ChatEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + NoteContract.ChatEntry.COLUMN_ID + ") REFERENCES " + NoteContract.NoteEntry.TABLE_NAME + " (" + NoteContract.NoteEntry.COLUMN_ID + ") ON DELETE CASCADE);";

        final String SQL_CREATE_CHECK_TABLE = "CREATE TABLE " +
                NoteContract.CheckEntry.TABLE_NAME + " (" +
                NoteContract.CheckEntry.COLUMN_CHECK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteContract.CheckEntry.COLUMN_CHECK_NOTE + " TEXT NOT NULL, " +
                NoteContract.CheckEntry.COLUMN_CHECK_IS_CHECKED + " BOOLEAN NOT NULL, " +
                NoteContract.CheckEntry.COLUMN_CHECK_TIMESTAMP + " TIMESTAMP DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), " +
                NoteContract.CheckEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + NoteContract.CheckEntry.COLUMN_ID + ") REFERENCES " + NoteContract.NoteEntry.TABLE_NAME + " (" + NoteContract.NoteEntry.COLUMN_ID + ") ON DELETE CASCADE);";

        db.execSQL(SQL_CREATE_NOTE_TABLE);
        db.execSQL(SQL_CREATE_UNITS_TABLE);
        db.execSQL(SQL_CREATE_CHAT_TABLE);
        db.execSQL(SQL_CREATE_CHECK_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NoteContract.NoteEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NoteContract.UnitEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NoteContract.ChatEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NoteContract.CheckEntry.TABLE_NAME);
        onCreate(db);
    }


    public double totalUnits(long columnId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT SUM (" + NoteContract.UnitEntry.COLUMN_UNIT_COST + ")" +
                " FROM " + NoteContract.UnitEntry.TABLE_NAME +
                " WHERE " + NoteContract.UnitEntry.COLUMN_ID + " = " + columnId;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        } else return 0;
    }

    public double selectedTotalUnits(long columnId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT SUM (" + NoteContract.UnitEntry.COLUMN_UNIT_COST + ")" +
                " FROM " + NoteContract.UnitEntry.TABLE_NAME +
                " WHERE " + NoteContract.UnitEntry.COLUMN_ID + " = " + columnId + " AND " + NoteContract.UnitEntry.COLUMN_UNIT_IS_CHECKED + " = '1'";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            return cursor.getDouble(0);
        } else return 0;
    }

    public void updateCheck(long id, int isChecked, String tableName, String isCheckedColumn, String columnId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                "UPDATE " + tableName +
                        " SET " + isCheckedColumn + " = '" + isChecked +
                        "' WHERE " + columnId + " = '" + id + "'";
        db.execSQL(query);
    }

    public void updateBudget(long id, String tableName, String columnBudget, String newBudget, String columnID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                "UPDATE " + tableName +
                        " SET " + columnBudget + " = " + newBudget +
                        " WHERE " + columnID + " = '" + id + "'";

        db.execSQL(query);
        System.out.println(query);
    }

    public void updateTimestamp(long id, String tableName, String columnTimestampName, String columnID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                "UPDATE " + tableName +
                        " SET " + columnTimestampName + " = datetime(CURRENT_TIMESTAMP, 'localtime')" +
                        " WHERE " + columnID + " = '" + id + "'";

        db.execSQL(query);
    }

    public void deleteList(long id, String tableName, String column) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                "DELETE FROM " + tableName +
                        " WHERE " + column + " = " + id + ";";

        db.execSQL(query);
    }

    public void updateTitle(long id, String tableName, String columnTitle, String newTitle, String columnId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                "UPDATE " + tableName +
                        " SET " + columnTitle + " = '" + newTitle +
                        "' WHERE " + columnId + " = '" + id + "'";
        db.execSQL(query);
    }

    public void updateCost(long id, String tableName, String columnCost, double newCost, String columnId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query =
                "UPDATE " + tableName +
                        " SET " + columnCost + " = '" + newCost +
                        "' WHERE " + columnId + " = '" + id + "'";
        db.execSQL(query);
    }

    public Cursor getAllItems(String tableName, String columnTimestamp) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                tableName,
                null,
                null,
                null,
                null,
                null,
                columnTimestamp + NoteContract.SORT_DESC
        );
    }

    public Cursor getLastItem(String tableName, String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + tableName +
                "  WHERE  " + id + " = (SELECT MAX(" + id + ")  FROM " + tableName + ");";

        return db.rawQuery(query, null);
    }

    public String getLastMessage(String id, long parentId, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + tableName +
                "  WHERE " + id + " = (SELECT MAX(" + id + ")  FROM " + tableName + " WHERE list_id = " + parentId + ");";
        Cursor cursor = db.rawQuery(query, null);
        //System.out.println(query);
        if (cursor.moveToFirst()) {
            return cursor.getString(1);
        } else return "Lista vacía";
    }

    public Cursor getAllListItems(long parentId, String tableName, String columnId, String columnTimeStamp) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                tableName, //Nombre de la tabla
                null, //Columnas de la tabla a mostrar. "null" llama a todas las columnas de la tabla
                columnId + " = " + parentId, // Filtra similar al "WHERE"
                null,
                null,
                null,
                columnTimeStamp + NoteContract.SORT_DESC
        );
    }

    public Cursor listsSortBy(long id, String tableName, String columnForSort, String columnId, String columnTimestamp, String order) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                tableName,
                null,
                columnId + " = " + id,
                null,
                null,
                null,
                columnForSort + " " + order + ", " + columnTimestamp + " DESC"
        );
    }

    public Cursor searchFilter(String newText, String tableName, String columnTitle) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                "SELECT * FROM " + tableName + " WHERE " + columnTitle + " LIKE '%" + newText + "%'" + " ORDER BY " + columnTitle + " ASC";
        return db.rawQuery(query, null);
    }

    public Cursor listsSearchFilter(long id, String newText, String tableName, String columnTitle, String columnId, String columnTimestamp) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query =
                "SELECT * FROM " + tableName +
                        " WHERE " + columnTitle +
                        " LIKE '%" + newText + "%'" +
                        " AND " + columnId + " = '" + id + "'" +
                        " ORDER BY " + columnTimestamp + " DESC";
        return db.rawQuery(query, null);
    }

}
