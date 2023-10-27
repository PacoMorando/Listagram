package com.sas.listagram;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ListManager {
    private long id;
    private int type;
    private String title;
    private double budget;
    private int color;
    private String timeStamp;


    public void setListData(long id, int type, String title, double budget, int color, String timeStamp){
        this.id = id;
        this.type = type;
        this.title = title;
        this.budget = budget;
        this.color = color;
        this.timeStamp = timeStamp;
    }
    public void buildList(String title, int type, SQLiteDatabase mDatabase, ListRecyclerAdapter listRecyclerAdapter, Cursor cursor) {
        if (title.trim().isEmpty()) {
            title = "Sin título";
        }
        ContentValues cv = new ContentValues();
        cv.put(NoteContract.NoteEntry.COLUMN_TITLE, title);
        cv.put(NoteContract.NoteEntry.COLUMN_TYPE, type);
        cv.put(NoteContract.NoteEntry.COLUMN_COLOR, Math.floor(Math.random() * 4 + 1));

        mDatabase.insert(NoteContract.NoteEntry.TABLE_NAME, null, cv);
        listRecyclerAdapter.swapCursor(cursor);
    }

    public void toClickedList(long id, int type, String title, double budget, int color, String timeStamp, Context context) {
        switch (type) {
            case CheckListActivity.CHECKLIST_TYPE:
                startListTypeActivity(id, title, budget, color, timeStamp, context, CheckListActivity.class);
                break;
            case ChatListActivity.CHATLIST_TYPE:
                startListTypeActivity(id, title, budget, color, timeStamp, context, ChatListActivity.class);
                break;
            case UnitsActivity.UNITS_LIST_TYPE:
                startListTypeActivity(id, title, budget, color, timeStamp, context, UnitsActivity.class);
                break;
        }
    }

    public void toRecentlyCreatedList(Context context) {
        switch (type) {
            case CheckListActivity.CHECKLIST_TYPE:
                startListTypeActivity(id, title, budget, color, timeStamp, context, CheckListActivity.class);
                break;
            case ChatListActivity.CHATLIST_TYPE:
                startListTypeActivity(id, title, budget, color, timeStamp, context, ChatListActivity.class);
                break;
            case UnitsActivity.UNITS_LIST_TYPE:
                startListTypeActivity(id, title, budget, color, timeStamp, context, UnitsActivity.class);
                break;
        }
    }


    public void startListTypeActivity(long id, String title, double budget, int color, String timeStamp, Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("cID", id);
        intent.putExtra("cTitle", title);
        intent.putExtra("cTime", timeStamp);
        intent.putExtra("cColor", color);
        intent.putExtra("cBudget", budget);
        context.startActivity(intent);
    }

}

