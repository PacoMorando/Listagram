package com.sas.conectaractivitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sas.conectaractivitys.anim.Animations;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.ArrayList;

public class ChatListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ArrayList<Long> selectedItemIds = new ArrayList<>();
    private ArrayList<String> selectedItemTitles = new ArrayList<>();
    private ArrayList<Integer> positions = new ArrayList<>();
    public static final int CHATLIST_TYPE = 2;


    private EditText addChatNoteEditText;
    private ImageView addChatNoteButton;
    private LinearLayout chatListChatBox;

    private RecyclerView chatRecyclerView;
    private LinearLayoutManager chatLinearLayoutManager;
    private ChatRecyclerAdapter chatRecyclerAdapter;

    private SQLiteDatabase mDatabase;
    private NoteDBHelper dbHelper = new NoteDBHelper(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        dbHelper = new NoteDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        //Obteniendo los datos de la lista padre
        long parentId = getIntent().getLongExtra("cID",0);
        String parentTitle = getIntent().getStringExtra("cTitle");
        String parentTimestamp = getIntent().getStringExtra("cTime");
        int color = getIntent().getIntExtra("cColor", 1);
        System.out.println(parentId);

        Toolbar mainToolbar = findViewById(R.id.chat_list_toolbar);
        setSupportActionBar(mainToolbar);
        setTitle(parentTitle);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        addChatNoteEditText = findViewById(R.id.add_chat_note_et);
        addChatNoteButton = findViewById(R.id.add_chat_note_button);
        ColorManager.setChatBoxColor(color,addChatNoteEditText,addChatNoteButton);

        addChatNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChatNote(v);
                dbHelper.updateTimestamp(parentId, NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TIMESTAMP, NoteContract.NoteEntry.COLUMN_ID);
            }
        });

        chatListChatBox = findViewById(R.id.chat_list_chat_box);
        chatRecyclerView = findViewById(R.id.chat_list_rv);
        chatLinearLayoutManager = new LinearLayoutManager(this);
        chatLinearLayoutManager.setReverseLayout(true);
        chatLinearLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(chatLinearLayoutManager);
        chatRecyclerAdapter = new ChatRecyclerAdapter(this, getAllItems());
        chatRecyclerView.setAdapter(chatRecyclerAdapter);
        chatRecyclerView.scrollToPosition(0);

        ColorManager.setChatRecyclerBackground(color,chatRecyclerView);

        //Para detectar el teclado y scrolear hacia abajo
        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean b) {
                if (b) {
                    chatRecyclerView.scrollToPosition(0);
                }
            }
        });

        chatRecyclerAdapter.setOnRVItemLongClickListener(new ChatRecyclerAdapter.OnRVItemLongClickListener() {
            @Override
            public void onRVItemLongClick(long columnId, String note, String timestamp, ChatRecyclerAdapter.ViewHolder viewHolder, int position) {
                if (!ChatListActionMode.chatListActionModeState) {
                    ChatListActionMode.chatListActionModeState = true;
                    Animations.showHideVertical(chatListChatBox,true, getResources().getDimension(R.dimen.hideDisplacement));
                    UIUtil.hideKeyboard(ChatListActivity.this);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                    selectedItemIds.add(columnId);
                    selectedItemTitles.add(note);
                    positions.add(viewHolder.getAdapterPosition());

                    viewHolder.isItemChecked = true;
                    viewHolder.selectionFade(position);

                    startSupportActionMode(new ChatListActionMode(selectedItemIds,selectedItemTitles,positions,dbHelper,parentId,ChatListActivity.this,getAllItems(),chatRecyclerAdapter,chatListChatBox));
                }
            }
        });
    }

    private void addChatNote(View view) {
        String chatNote = addChatNoteEditText.getText().toString();
        if (chatNote.trim().isEmpty()) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(NoteContract.ChatEntry.COLUMN_CHAT_NOTE, chatNote);
        cv.put(NoteContract.ChatEntry.COLUMN_ID, getIntent().getLongExtra("cID", 0));

        mDatabase.insert(NoteContract.ChatEntry.TABLE_NAME, null, cv);
        chatRecyclerAdapter.chatSwapCursor(getAllItems());

        addChatNoteEditText.getText().clear();
        chatRecyclerView.smoothScrollToPosition(0);

    }

    private Cursor getAllItems() {

        return dbHelper.getAllListItems(getIntent().getLongExtra("cID", 0),
                NoteContract.ChatEntry.TABLE_NAME, NoteContract.ChatEntry.COLUMN_ID, NoteContract.ChatEntry.COLUMN_CHAT_TIMESTAMP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.chat_bar_search);
        SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        menu.findItem(R.id.chat_bar_search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Animations.showHideVertical(chatListChatBox, true, getResources().getDimension(R.dimen.hideDisplacement));
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Animations.showHideVertical(chatListChatBox, false, 0f);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText =  newText.toLowerCase();

        chatRecyclerAdapter.chatSwapCursor(dbHelper.listsSearchFilter(
                getIntent().getLongExtra("cID",0),
                newText,
                NoteContract.ChatEntry.TABLE_NAME,
                NoteContract.ChatEntry.COLUMN_CHAT_NOTE,
                NoteContract.ChatEntry.COLUMN_ID,
                NoteContract.ChatEntry.COLUMN_CHAT_TIMESTAMP));
        return true;
    }

}
