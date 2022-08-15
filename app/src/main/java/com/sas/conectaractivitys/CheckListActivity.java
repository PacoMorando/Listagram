package com.sas.conectaractivitys;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.sas.conectaractivitys.anim.Animations;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.ArrayList;

public class CheckListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ArrayList<Long> selectedItemIds = new ArrayList<>();
    private ArrayList<String> selectedItemTitles = new ArrayList<>();
    private ArrayList<Integer> positions = new ArrayList<>();
    public static final int CHECKLIST_TYPE = 1;


    private EditText addCheckNoteEditText;
    private ImageView addCheckNoteButton;
    private LinearLayout checklistChatBox;

    private RecyclerView checkRecyclerView;
    private CheckRecyclerAdapter checkRecyclerAdapter;
    private SQLiteDatabase mDatabase;
    private NoteDBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        //Instanciado DB y DB helper
        //IMPORTANTE! La database se crea ANTES del recycler view y del adapter
        dbHelper = new NoteDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        //Obteniendo los datos de la lista padre
        long parentId = getIntent().getLongExtra("cID", 0);
        String parentTitle = getIntent().getStringExtra("cTitle");
        String parentTimestamp = getIntent().getStringExtra("cTime");
        int color = getIntent().getIntExtra("cColor", 1);

        //Instanciando y seteando la toolbar
        Toolbar mainToolbar = findViewById(R.id.check_list_toolbar);
        setSupportActionBar(mainToolbar);
        setTitle(parentTitle);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Instanciando y seteando los Views
        checklistChatBox = findViewById(R.id.checklist_chat_box);
        addCheckNoteEditText = findViewById(R.id.add_check_note_et);
        addCheckNoteButton = findViewById(R.id.add_check_note_button);
        ColorManager.setChatBoxColor(color,addCheckNoteEditText,addCheckNoteButton);
        addCheckNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // esto en una funcion
                addCheckNote(v);
                dbHelper.updateTimestamp(parentId, NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TIMESTAMP, NoteContract.NoteEntry.COLUMN_ID);
            }
        });




        //Instanciando y seteando el RecyclerView y su respectivo Adapter
        checkRecyclerView = findViewById(R.id.check_list_rv);
        checkRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkRecyclerAdapter = new CheckRecyclerAdapter(this, getAllItems());
        checkRecyclerView.setAdapter(checkRecyclerAdapter);

        //Seteando ClickListener de los items
        checkRecyclerAdapter.setOnRVItemClickListener(new CheckRecyclerAdapter.OnRVItemClickListener() {
            @Override
            public void onRVItemClick(long id, int isChecked, String note) {
                if (!CheckListActionMode.checkListActionModeState) {
                    System.out.println("Se ejecuto por fuera el click");
                    dbHelper.updateCheck(id, isChecked, NoteContract.CheckEntry.TABLE_NAME, NoteContract.CheckEntry.COLUMN_CHECK_IS_CHECKED, NoteContract.CheckEntry.COLUMN_CHECK_ID);
                    dbHelper.updateTimestamp(parentId, NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TIMESTAMP, NoteContract.NoteEntry.COLUMN_ID);
                }
            }
        });

        checkRecyclerAdapter.setOnRVItemLongClickListener(new CheckRecyclerAdapter.OnRVItemLongClickListener() {
            @Override
            public void onRVItemLongClick(long columnId, String note, String timestamp, CheckRecyclerAdapter.ViewHolder viewHolder, int position) {
                if (!CheckListActionMode.checkListActionModeState) {
                    CheckListActionMode.checkListActionModeState = true;
                    Animations.showHideVertical(checklistChatBox,true, getResources().getDimension(R.dimen.hideDisplacement));
                    UIUtil.hideKeyboard(CheckListActivity.this);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                    selectedItemIds.add(columnId);
                    selectedItemTitles.add(note);
                    positions.add(viewHolder.getAdapterPosition());

                    viewHolder.isItemChecked = true;
                    viewHolder.selectionFade(position);

                    startSupportActionMode(new CheckListActionMode(selectedItemIds, selectedItemTitles, positions, dbHelper, parentId, CheckListActivity.this, getAllItems(), checkRecyclerAdapter,checklistChatBox));
                }
            }
        });
    }


    private void addCheckNote(View view) {
        String checkNote = addCheckNoteEditText.getText().toString();
        if (checkNote.trim().isEmpty()) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(NoteContract.CheckEntry.COLUMN_CHECK_NOTE, checkNote);
        cv.put(NoteContract.CheckEntry.COLUMN_ID, getIntent().getLongExtra("cID", 0));
        cv.put(NoteContract.CheckEntry.COLUMN_CHECK_IS_CHECKED, 0);

        mDatabase.insert(NoteContract.CheckEntry.TABLE_NAME, null, cv);
        checkRecyclerAdapter.checkSwapCursor(getAllItems());

        addCheckNoteEditText.getText().clear();
        checkRecyclerView.scrollToPosition(0);
    }

    private Cursor getAllItems() {

        return dbHelper.getAllListItems(getIntent().getLongExtra("cID", 0),
                NoteContract.CheckEntry.TABLE_NAME, NoteContract.CheckEntry.COLUMN_ID, NoteContract.CheckEntry.COLUMN_CHECK_TIMESTAMP);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.check_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.check_bar_search);
        SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        menu.findItem(R.id.check_bar_search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Animations.showHideVertical(checklistChatBox, true, getResources().getDimension(R.dimen.hideDisplacement));
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Animations.showHideVertical(checklistChatBox, false, 0f);
                return true;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_unchecked:
                sortOptions(NoteContract.CheckEntry.COLUMN_CHECK_IS_CHECKED, NoteContract.SORT_ASC);
                return true;
            case R.id.sort_checked:
                sortOptions(NoteContract.CheckEntry.COLUMN_CHECK_IS_CHECKED, NoteContract.SORT_DESC);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortOptions(String columnToSortFor, String sortCriterion) {
        checkRecyclerAdapter.checkSwapCursor(dbHelper.listsSortBy(
                getIntent().getLongExtra("cID", 0),
                NoteContract.CheckEntry.TABLE_NAME,
                columnToSortFor,
                NoteContract.CheckEntry.COLUMN_ID,
                NoteContract.CheckEntry.COLUMN_CHECK_TIMESTAMP,
                sortCriterion));
        checkRecyclerView.scrollToPosition(0);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();

        checkRecyclerAdapter.checkSwapCursor(dbHelper.listsSearchFilter(
                getIntent().getLongExtra("cID", 0),
                newText,
                NoteContract.CheckEntry.TABLE_NAME,
                NoteContract.CheckEntry.COLUMN_CHECK_NOTE,
                NoteContract.CheckEntry.COLUMN_ID,
                NoteContract.CheckEntry.COLUMN_CHECK_TIMESTAMP));
        return true;
    }


}