package com.sas.listagram;

import android.provider.BaseColumns;

public class NoteContract {


    public static final int SORT_BY_DATE = 0;
    /*public static final int SORT_BY_VALUE = 1;
    public static final int SORT_BY_SELECTED = 2;
    public static final int SORT_BY_UNSELECTED = 3;
    public static final int SORT_BY_ALPHABET = 4;*/

    public static final String SORT_ASC = " ASC";
    public static final String SORT_DESC = " DESC";

    private NoteContract() {
    }
    // Se tiene que crear una clase por cada tabla que querramos crear
    public static final class NoteEntry implements BaseColumns {
        public static final String COLUMN_ID = "id";
        public static final String TABLE_NAME = "noteList";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_BUDGET = "budget";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_COLOR = "color";
        public static final String COLUMN_SORT_STATE = "sort_state";
        public static final String COLUMN_TIMESTAMP = "last_modification_timestamp";
        public static final String COLUMN_CREATION_TIMESTAMP = "creation_timestamp";
    }
    public static final class CheckEntry implements BaseColumns {
        public static final String COLUMN_CHECK_ID = "id";
        public static final String TABLE_NAME = "checkList";
        public static final String COLUMN_CHECK_NOTE = "check_note";
        public static final String COLUMN_CHECK_IS_CHECKED = "is_checked";
        public static final String COLUMN_CHECK_TIMESTAMP = "last_modification_timestamp";
        public static final String COLUMN_ID = "list_id";
    }
    public static final class ChatEntry implements BaseColumns {
        public static final String COLUMN_CHAT_ID = "id";
        public static final String TABLE_NAME = "chatList";
        public static final String COLUMN_CHAT_NOTE = "chat_note";
        public static final String COLUMN_CHAT_TIMESTAMP = "last_modification_timestamp";
        public static final String COLUMN_ID = "list_id";
    }
    public static final class UnitEntry implements BaseColumns {
        public static final String COLUMN_UNIT_ID = "id";
        public static final String TABLE_NAME = "unitList";
        public static final String COLUMN_UNIT_TITLE = "title";
        public static final String COLUMN_UNIT_IS_CHECKED = "is_checked";
        public static final String COLUMN_UNIT_COST = "cost";
        public static final String COLUMN_UNIT_TIMESTAMP = "last_modification_timestamp";
        public static final String COLUMN_ID = "list_id";
    }

}
