package com.sas.conectaractivitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sas.conectaractivitys.anim.Animations;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.ArrayList;
import java.util.logging.LogRecord;

public class UnitsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private ArrayList<Long> selectedItemIds = new ArrayList<>();
    private ArrayList<String> selectedItemTitles = new ArrayList<>();
    private ArrayList<Double> selectedItemValues = new ArrayList<>();
    private ArrayList<Integer> positions = new ArrayList<>();
    public static final int UNITS_LIST_TYPE = 3;

    private TextView title;
    private TextView total;
    private TextView date;
    private TextView selectedTotal;
    private ImageView addUnitButton;
    private EditText addUnitEditText;
    private EditText addCostEditText;
    private EditText addBudgetEditText;
    private TextView budgetTextView;
    private TextView budgetLeft;
    private RelativeLayout budgetMenu;
    private ImageButton budgetCloseButton;
    private ImageButton newBudgetButton;
    private LinearLayout unitsChatBox;

    private UnitsRecyclerAdapter unitsRecyclerAdapter;
    private LinearLayoutManager unitLinearLayoutManager;
    private RecyclerView unitsRecyclerView;
    private SQLiteDatabase mDatabase;
    private NoteDBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_units);

        //IMPORTANTE! La database se crea ANTES del recycler view
        dbHelper = new NoteDBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        long parentId = getIntent().getLongExtra("cID", 0);
        String parentTitle = getIntent().getStringExtra("cTitle");
        String parentTimestamp = getIntent().getStringExtra("cTime");
        double parentBudget = getIntent().getDoubleExtra("cBudget", 0);
        int color = getIntent().getIntExtra("cColor", 1);

        Toolbar unitToolbar = findViewById(R.id.unit_toolbar);
        TextView uTitle = unitToolbar.findViewById(R.id.unit_list_title);
        uTitle.setText("   " + parentTitle);
        setSupportActionBar(unitToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        title = findViewById(R.id.unit_title);
        title.setText(parentTitle);
        total = findViewById(R.id.unit_total);
        date = findViewById(R.id.unit_date);
        date.setText(parentTimestamp);
        selectedTotal = findViewById(R.id.selected_total);
        unitsChatBox = findViewById(R.id.units_chat_box);

        budgetTextView = findViewById(R.id.budget);
        budgetTextView.setOnClickListener(this::setAddBudget);
        budgetMenu = findViewById(R.id.budget_menu);
        budgetCloseButton = findViewById(R.id.budget_close);
        budgetCloseButton.setOnClickListener(this::closeEditBudget);
        newBudgetButton = findViewById(R.id.budget_done);
        newBudgetButton.setOnClickListener(this::newBudget);
        addBudgetEditText = findViewById(R.id.add_budget);
        if (parentBudget > 0) {
            addBudgetEditText.setText(String.valueOf(parentBudget));
            //setSeleccion establece la ubicación del cursor, en este casa esta al final
            addBudgetEditText.setSelection(addBudgetEditText.getText().length());
        }
        //Esto sirve para darle uso al boton de "listo" del softkeyboard
        addBudgetEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                newBudget(v);
                return false;
            }
        });
        budgetLeft = findViewById(R.id.budget_left);

        addUnitEditText = findViewById(R.id.add_unit_et);
        //Esto sirve para darle uso al boton de "listo" del softkeyboard
        addUnitEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    addUnit(v);
                }
                return false;
            }
        });
        addCostEditText = findViewById(R.id.add_cost_et);
        //Esto sirve para darle uso al boton de "listo" del softkeyboard
        addCostEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    addUnit(v);
                }
                return false;
            }
        });
        addUnitButton = findViewById(R.id.add_unit_button);
        ColorManager.setChatBoxColor(color, addUnitEditText, addCostEditText, addUnitButton);
        addUnitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUnit(v);
                //dbHelper.updateTimestamp(parentId, NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TIMESTAMP, NoteContract.NoteEntry.COLUMN_ID);
            }
        });


        unitsRecyclerView = findViewById(R.id.units_rv);
        unitLinearLayoutManager = new LinearLayoutManager(this);
        unitLinearLayoutManager.setReverseLayout(true);
        unitLinearLayoutManager.setStackFromEnd(true);
        unitsRecyclerView.setLayoutManager(unitLinearLayoutManager);
        unitsRecyclerAdapter = new UnitsRecyclerAdapter(this, getAllItems());
        unitsRecyclerView.setAdapter(unitsRecyclerAdapter);
        unitsRecyclerView.scrollToPosition(0);

        //Para detectar el teclado y scrolear hacia abajo para que no tape la lista
       KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean b) {
                if (b) {
                    unitsRecyclerView.scrollToPosition(0);
                }
            }
        });

        unitsRecyclerAdapter.setOnRVItemLongClickListener(new UnitsRecyclerAdapter.OnRVItemLongClickListener() {
            @Override
            public void onRVItemLongClick(long columnId, String unitName, double unitValue, String timestamp, UnitsRecyclerAdapter.ViewHolder viewHolder, int position) {
                if (!UnitsActionMode.unitsActionModeState) {
                    UnitsActionMode.unitsActionModeState = true;
                    Animations.showHideVertical(unitsChatBox, true, getResources().getDimension(R.dimen.hideDisplacement));
                    UIUtil.hideKeyboard(UnitsActivity.this);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                    selectedItemIds.add(columnId);
                    selectedItemTitles.add(unitName);
                    selectedItemValues.add(unitValue);
                    positions.add(viewHolder.getAdapterPosition());

                    viewHolder.isItemChecked = true;
                    viewHolder.selectionFade(position);

                    startSupportActionMode(new UnitsActionMode(selectedItemIds, selectedItemTitles, selectedItemValues, positions, dbHelper, UnitsActivity.this, getAllItems(), unitsRecyclerAdapter,
                            total,budgetLeft, selectedTotal, parentBudget, parentId, unitsChatBox));
                }
            }
        });

        unitsRecyclerAdapter.setOnRVItemClickListener(new UnitsRecyclerAdapter.OnRVItemClickListener() {
            @Override
            public void onRVItemClickListener(long columnId, int isChecked, String unitName) {
                if (!UnitsActionMode.unitsActionModeState) {
                    dbHelper.updateCheck(columnId, isChecked, NoteContract.UnitEntry.TABLE_NAME, NoteContract.UnitEntry.COLUMN_UNIT_IS_CHECKED, NoteContract.UnitEntry.COLUMN_UNIT_ID);
                    setSelectedTotalText(parentId);
                }
            }
        });


        setBudgetText(parentBudget);
        setBudgetLeft(parentBudget);
        setTotalText();
        setSelectedTotalText(parentId);
    }

    private void setAddBudget(View view) {
        addBudgetEditText.setVisibility(View.VISIBLE);
        budgetTextView.setVisibility(View.INVISIBLE);
        budgetMenu.setVisibility(View.VISIBLE);
        Animations.fadeView(unitsChatBox, Animations.fastFade, false);

        //Para que se focusee autmaaticamente el editText y se ponga el teclado
        addBudgetEditText.requestFocus();
        UIUtil.showKeyboard(UnitsActivity.this, addBudgetEditText);
        /*ESTA ES OTRA MANERA DE HACER FOCUS A UN EDIT TEXT, PERO TAMBIEN NECESITA EL REQUEST FOCUS
          InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.showSoftInput(addBudget, InputMethodManager.SHOW_IMPLICIT);*/
    }

    private void newBudget(View view) {
        String newBudget = addBudgetEditText.getText().toString();
        if (!newBudget.trim().isEmpty()) {
            setBudgetText(Double.valueOf(newBudget));
            dbHelper.updateBudget(getIntent().getLongExtra("cID", 0), NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_BUDGET, newBudget, NoteContract.NoteEntry.COLUMN_ID);
            setBudgetLeft(Double.valueOf(newBudget));
        } else {
            dbHelper.updateBudget(getIntent().getLongExtra("cID", 0), NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_BUDGET, "0", NoteContract.NoteEntry.COLUMN_ID);
            setBudgetText(0);
            setBudgetLeft(0);
        }
        closeEditBudget(view);
    }

    private void closeEditBudget(View view) {
        addBudgetEditText.setVisibility(View.INVISIBLE);
        budgetTextView.setVisibility(View.VISIBLE);
        budgetMenu.setVisibility(View.GONE);

        UIUtil.hideKeyboard(this);
        Animations.fadeView(unitsChatBox, Animations.fastFade, true);
    }


    private void setBudgetText(double budget) {
        if (budget != 0) {
            budgetTextView.setText("PRESUPUESTO : " + String.format("$%.2f", budget));
        } else {
            budgetTextView.setText("AGREGA PRESUPUESTO");
        }
    }

    private void setBudgetLeft(double budget) {
        double bLeft = budget - dbHelper.totalUnits(getIntent().getLongExtra("cID", 0));
        if (budget == 0) {
            budgetLeft.setVisibility(View.GONE);
        } else {
            budgetLeft.setVisibility(View.VISIBLE);
            budgetLeft.setText("RESTAN: " + String.format("$%.2f", bLeft));
        }
    }

    private void setTotalText() {
        long parentId = getIntent().getLongExtra("cID", 0);
        total.setText("Total Lista: " + String.format("$%.2f", dbHelper.totalUnits(parentId)));
    }

    private void setSelectedTotalText(long parentId) {
        double total = dbHelper.selectedTotalUnits(parentId);
        if (total != 0) {
            selectedTotal.setVisibility(View.VISIBLE);
            selectedTotal.setText("SELEC: " + String.format("$%.2f", total));
        } else {
            selectedTotal.setVisibility(View.GONE);
        }
    }


    public void addUnit(View view) {
        String unitTitle = addUnitEditText.getText().toString();
        String unitCost = addCostEditText.getText().toString();

        if (unitTitle.trim().isEmpty()) {
            return;
        } else if (unitCost.trim().isEmpty()) {
            unitCost = "0.00";
        }

        ContentValues cv = new ContentValues();
        cv.put(NoteContract.UnitEntry.COLUMN_UNIT_TITLE, unitTitle);
        cv.put(NoteContract.UnitEntry.COLUMN_UNIT_COST, unitCost);
        cv.put(NoteContract.UnitEntry.COLUMN_ID, getIntent().getLongExtra("cID", 0));
        cv.put(NoteContract.UnitEntry.COLUMN_UNIT_IS_CHECKED, 0);

        mDatabase.insert(NoteContract.UnitEntry.TABLE_NAME, null, cv);
        unitsRecyclerAdapter.unitsSwapCursor(getAllItems());

        addUnitEditText.getText().clear();
        addCostEditText.getText().clear();

        setTotalText();
        setBudgetLeft(getIntent().getDoubleExtra("cBudget", 0));

        addUnitEditText.requestFocus();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                unitsRecyclerView.smoothScrollToPosition(0);
            }
        }, 200);

        dbHelper.updateTimestamp(getIntent().getLongExtra("cID", 0), NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TIMESTAMP, NoteContract.NoteEntry.COLUMN_ID);

    }

    /*PARA DETECTAR CUANDO CAMBIA DE TAMANO EL VIEW, LO USE PARA HACER SCROLLTOPOSITION 0 PERO POR ALGUNA RAZON
    NO ME LO DETECTO BIEN EN EL ADD UNIT, AL PARECER EL REQUEST FOCUS 
    private void heightListener(){
        unitsRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                System.out.println("BOTTOM " + bottom);
                System.out.println("OLDBOTTOM " + oldBottom);

                if (bottom != oldBottom && !MainActionMode.actionModeState){
                    unitsRecyclerView.scrollToPosition(0);
                    System.out.println("SE DEBIO EJECUTAR EL SCROLL");
                }
            }
        });
    }*/


    private Cursor getAllItems() {

        return dbHelper.getAllListItems(getIntent().getLongExtra("cID", 0),
                NoteContract.UnitEntry.TABLE_NAME, NoteContract.UnitEntry.COLUMN_ID, NoteContract.UnitEntry.COLUMN_UNIT_TIMESTAMP);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.unit_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.unit_bar_search);
        SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        menu.findItem(R.id.unit_bar_search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Animations.showHideVertical(unitsChatBox, true, getResources().getDimension(R.dimen.hideDisplacement));
                Toast.makeText(UnitsActivity.this, "HOLI", Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Animations.showHideVertical(unitsChatBox, false, 0f);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_value_asc:
                sortOptions(NoteContract.UnitEntry.COLUMN_UNIT_COST, NoteContract.SORT_DESC);
                return true;
            case R.id.sort_value_desc:
                sortOptions(NoteContract.UnitEntry.COLUMN_UNIT_COST, NoteContract.SORT_ASC);
                return true;
            case R.id.units_color_asc:
                sortOptions(NoteContract.UnitEntry.COLUMN_UNIT_IS_CHECKED, NoteContract.SORT_DESC);
                return true;
            case R.id.units_color_desc:
                sortOptions(NoteContract.UnitEntry.COLUMN_UNIT_IS_CHECKED, NoteContract.SORT_ASC);// Estan invertidos por la forma en la que se ordena el recylerview
                return true;
            case R.id.units_date_desc:
                sortOptions(NoteContract.UnitEntry.COLUMN_UNIT_TIMESTAMP, NoteContract.SORT_DESC);
                return true;
            case R.id.units_date_asc:
                sortOptions(NoteContract.UnitEntry.COLUMN_UNIT_TIMESTAMP, NoteContract.SORT_ASC);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortOptions(String columnToSortFor, String sortCriterion) {
        unitsRecyclerAdapter.unitsSwapCursor(dbHelper.listsSortBy(
                getIntent().getLongExtra("cID", 0),
                NoteContract.UnitEntry.TABLE_NAME,
                columnToSortFor,
                NoteContract.UnitEntry.COLUMN_ID,
                NoteContract.UnitEntry.COLUMN_UNIT_TIMESTAMP,
                sortCriterion));
        unitsRecyclerView.scrollToPosition(0);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();

        unitsRecyclerAdapter.unitsSwapCursor(dbHelper.listsSearchFilter(
                getIntent().getLongExtra("cID", 0),
                newText,
                NoteContract.UnitEntry.TABLE_NAME,
                NoteContract.UnitEntry.COLUMN_UNIT_TITLE,
                NoteContract.UnitEntry.COLUMN_ID,
                NoteContract.UnitEntry.COLUMN_UNIT_TIMESTAMP));
        return true;
    }
}