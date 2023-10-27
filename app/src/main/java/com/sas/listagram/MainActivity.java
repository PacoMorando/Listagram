package com.sas.listagram;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sas.listagram.anim.Animations;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ArrayList<Long> selectedItemIds = new ArrayList<>();
    private ArrayList<String> selectedItemTitles = new ArrayList<>();
    private ArrayList<Integer> positions = new ArrayList<>();
    public static final int LIST_TYPE = 0;
    private boolean fabState = false;

    private SQLiteDatabase mDatabase;
    private NoteDBHelper dbHelper;
    private final ListManager listManager = new ListManager();

    private FloatingActionButton addButton;

    private FloatingActionButton createCheckList;
    private FloatingActionButton createChatList;
    private FloatingActionButton createUnitsList;

    private ListRecyclerAdapter listRecyclerAdapter;
    private RecyclerView listRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        int orientation = MainActivity.this.getRequestedOrientation();
        //Toast.makeText(this, String.valueOf(orientation), Toast.LENGTH_SHORT).show();

        //Aqui creamos la base de datos con un helper que contiene la logica de la DB
        dbHelper = new NoteDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        setTitle("Listagram");

        createCheckList = findViewById(R.id.create_checklist);
        createCheckList.setOnClickListener(v -> createListDialog(CheckListActivity.CHECKLIST_TYPE));
        createChatList = findViewById(R.id.create_chat_list);
        createChatList.setOnClickListener(v -> createListDialog(ChatListActivity.CHATLIST_TYPE));
        createUnitsList = findViewById(R.id.create_units_list);
        createUnitsList.setOnClickListener(v -> createListDialog(UnitsActivity.UNITS_LIST_TYPE));

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(this::pressedFab);

        listRecyclerView = findViewById(R.id.list_rv);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listRecyclerAdapter = new ListRecyclerAdapter(this, getAllItems());
        listRecyclerView.setAdapter(listRecyclerAdapter);

        listRecyclerAdapter.setOnRVItemLongClickListener(new ListRecyclerAdapter.OnRVItemLongClickListener() {
            @Override
            public void onRVItemLongClick(long columnId, String title, String timestamp, ListRecyclerAdapter.ViewHolder viewHolder, int position) {
                if (!MainActionMode.actionModeState) {
                    MainActionMode.actionModeState = true;
                    hideFab();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                    selectedItemIds.add(columnId);
                    selectedItemTitles.add(title);
                    positions.add(viewHolder.getAdapterPosition());

                    viewHolder.isItemChecked = true;
                    viewHolder.selectionFade(position);

                    startSupportActionMode(new MainActionMode(selectedItemIds, selectedItemTitles, positions, dbHelper, MainActivity.this, getAllItems(), listRecyclerAdapter, addButton));
                }
            }
        });


        listRecyclerAdapter.setOnRVItemClickListener(new ListRecyclerAdapter.OnRVItemClickListener() {
            @Override
            public void onRVItemClickListener(long columnId, int type, String title, double budget, int color, String timestamp) {
                if (!MainActionMode.actionModeState) {
                    listManager.toClickedList(columnId, type, title, budget, color, timestamp, MainActivity.this);
                    mainToolbar.collapseActionView();//Esto es para quitar el searchView si est'a activado
                    //Toast.makeText(MainActivity.this, dbHelper.getLastMessage("id",columnId,"chatList"), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        listRecyclerAdapter.swapCursor(getAllItems());
    }

    //Este tal vez va en el ListManager
    private void createListDialog(int type) {
        fabState = false;
        Animations.fabRotation(addButton, 0f);
        Animations.displaceButtonsInColumn(new FloatingActionButton[]{createCheckList, createChatList, createUnitsList}, 0f, 200, false);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View createListView = layoutInflater.inflate(R.layout.dialog_edit, null);
        EditText editSetTitle = createListView.findViewById(R.id.new_title);

        final AlertDialog editDialog = new AlertDialog.Builder(MainActivity.this)
                .setView(createListView)
                .setTitle("Introduce un título para la lista: ")
                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listManager.buildList(editSetTitle.getText().toString(), type, mDatabase, listRecyclerAdapter, getAllItems());
                        gettingData();
                    }
                })
                .setNegativeButton("CANCELAR ", null)
                .create();
        editDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        editDialog.show();
        editSetTitle.requestFocus();
    }


    private void pressedFab(View view) {
        if (!fabState) {
            fabState = true;
            Animations.fabRotation(view, 135f);
            Animations.displaceButtonsInColumn(new FloatingActionButton[]{createCheckList, createChatList, createUnitsList}, -(getResources().getDimension(R.dimen.displacement)), 200, fabState);
        } else {
            fabState = false;
            Animations.fabRotation(view, 0f);
            Animations.displaceButtonsInColumn(new FloatingActionButton[]{createCheckList, createChatList, createUnitsList}, 0f, 200, fabState);
        }
    }

    private void hideFab() {
        if (fabState) {
            Animations.displaceButtonsInColumn(new FloatingActionButton[]{createCheckList, createChatList, createUnitsList}, 0f, 50, false);
            Animations.fabRotation(addButton, 0f);
            Animations.showHideVertical(addButton, true, getResources().getDimension(R.dimen.hideDisplacement));
            fabState = false;
        } else {
            Animations.showHideVertical(addButton, true, getResources().getDimension(R.dimen.hideDisplacement));
        }
    }

    //Este va en el dbHelper
    private void gettingData() {
        Cursor cursor = dbHelper.getLastItem(NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_ID);
        if (cursor.moveToFirst()) {
            do {
                listManager.setListData(
                        cursor.getLong(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_ID)),
                        cursor.getInt(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TITLE)),
                        cursor.getDouble(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_BUDGET)),
                        cursor.getInt(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_COLOR)),
                        cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TIMESTAMP))
                );
            } while (cursor.moveToNext());
        }
        cursor.close();
        listManager.toRecentlyCreatedList(MainActivity.this);
    }


    private Cursor getAllItems() {
        return dbHelper.getAllItems(NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TIMESTAMP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.main_bar_search);
        SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        menu.findItem(R.id.main_bar_search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                hideFab();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Animations.showHideVertical(addButton,false,0f);
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
        newText = newText.toLowerCase();
        listRecyclerAdapter.swapCursor(dbHelper.searchFilter(newText, NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TITLE));
        return true;
    }


}