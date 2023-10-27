package com.sas.listagram;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sas.listagram.anim.Animations;

import java.util.ArrayList;

public class UnitsActionMode implements MyActionMode {

    public static boolean unitsActionModeState = false;
    private boolean editingModeState = false;

    private ArrayList<Long> selectedItemIds;
    private ArrayList<String> selectedItemTitles;
    private ArrayList<Double> selectedItemValues;
    private final ArrayList<Integer> positions;

    private NoteDBHelper dbHelper;
    private Context mContext;
    private Cursor mCursor;
    private View viewToShow;
    private UnitsRecyclerAdapter unitsRecyclerAdapter;

    private TextView unitsTotalTextView;
    private TextView selectedTotalTextView;
    private TextView budgetLeft;
    private double parentBudget;
    private Long parentId;


    public UnitsActionMode(ArrayList<Long> selectedItemIds, ArrayList<String> selectedItemTitles, ArrayList<Double> selectedItemValues, ArrayList<Integer> positions,
                           NoteDBHelper dbHelper, Context mContext, Cursor mCursor, UnitsRecyclerAdapter unitsRecyclerAdapter,
                           TextView unitsTotalTextView, TextView budgetLeft, TextView selectedTotalTextView, double parentBudget, long parentId, View viewToShow) {
        this.selectedItemIds = selectedItemIds;
        this.selectedItemTitles = selectedItemTitles;
        this.selectedItemValues = selectedItemValues;
        this.positions = positions;
        this.dbHelper = dbHelper;
        this.mContext = mContext;
        this.mCursor = mCursor;
        this.unitsRecyclerAdapter = unitsRecyclerAdapter;
        this.unitsTotalTextView = unitsTotalTextView;
        this.budgetLeft = budgetLeft;
        this.selectedTotalTextView = selectedTotalTextView;
        this.parentBudget = parentBudget;
        this.parentId = parentId;
        this.viewToShow = viewToShow;
    }


    // METODOS DE LA INTERFAZ QUE SE ENCARGA DE EJECUTAR EL ACTION MODE
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.context_menu, menu);
        mode.setTitle(String.valueOf(selectedItemIds.size()));
        mode.getMenu().findItem(R.id.close_edit).setVisible(false);//Revisar estos botones para quitarlos
        menu.findItem(R.id.done_edit).setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        unitsListPrepareActionMode(mode, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.context_delete) {
            deleteAlertDialog(mode);
            return true;
        }
        if (item.getItemId() == R.id.context_edit) {
            editingModeState = true;
            editDialog(selectedItemIds.get(0), selectedItemTitles.get(0), mode);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        unitsActionModeState = false;
        editingModeState = false;
        selectedItemIds.clear();
        selectedItemTitles.clear();
        unitListSettings();
        positions.clear();
        unitsRecyclerAdapter.notifyDataSetChanged();
        Animations.showHideVertical(viewToShow, unitsActionModeState, 0f);
        unlockOrientation();
    }

    //------------- METODOS PARA onPrepareActionMode -----------------------

    private void unitsListPrepareActionMode(ActionMode mode, Menu menu) {
        System.out.println("UnitsList prepare ejecutado");
        unitsRecyclerAdapter.setOnClickActionModeListener(new UnitsRecyclerAdapter.OnClickActionModeListener() {
            @Override
            public void onClickActionMode(long columnId, String unitName, double unitValue, String timestamp, UnitsRecyclerAdapter.ViewHolder holder) {
                if (UnitsActionMode.unitsActionModeState && !editingModeState) {

                    unitsListItemChecker(columnId, unitName, unitValue, holder, holder.isItemChecked, mode);

                    mode.setTitle(String.valueOf(selectedItemIds.size()));

                    itemVisible(menu.findItem(R.id.context_edit), selectedItemIds.size());
                }
            }
        });
    }

    // Gestiona lo necesario para seleccionar y deseleccionar elementos
    private void unitsListItemChecker(long columnId, String unitName, double unitValue, UnitsRecyclerAdapter.ViewHolder viewHolder, boolean isThisItemChecked, ActionMode mode) {
        if (!isThisItemChecked) {
            viewHolder.isItemChecked = true;
            viewHolder.selectionFade(viewHolder.getAdapterPosition());
            selectedItemIds.add(columnId);
            selectedItemTitles.add(unitName);
            selectedItemValues.add(unitValue);
            positions.add(viewHolder.getAdapterPosition());

        } else {
            viewHolder.isItemChecked = false;
            viewHolder.selectionFade(viewHolder.getAdapterPosition());
            selectedItemIds.remove(columnId);
            selectedItemTitles.remove(unitName);
            selectedItemValues.remove(unitValue);
            positions.remove((Integer) viewHolder.getAdapterPosition());

            if (selectedItemIds.size() == 0) {
                mode.finish();
            }
        }
    }

    //Este método apaga el ícono de editar si hay mas de un elemento seleccionado
    private void itemVisible(MenuItem menuItem, int selectedItemNumber) {
        menuItem.setVisible(selectedItemNumber == 1);
    }

    //-------------- DELETE METHODS PARA onActionItemClicked -----------------------

    private void deleteAlertDialog(ActionMode mode) {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(mContext);
        if (selectedItemIds.size() > 0) {
            alert.setTitle("Se eliminaran:")
                    .setMessage(namesOfItemsSelected())
                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteSelectedLists();
                            dbHelper.updateTimestamp(parentId, NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TIMESTAMP, NoteContract.NoteEntry.COLUMN_ID);
                            unitsRecyclerAdapter.unitsSwapCursor(mCursor);
                            mode.finish();
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    })
                    .show();
        } else {
            mode.finish();
        }
    }

    //Enlista los titulos de los elementos seleccionados para mostrarlos en el deleteDialog para confirmar eliminarlos
    private String namesOfItemsSelected() {
        StringBuilder names = new StringBuilder();
        for (String itemTitle : selectedItemTitles) {
            names.append(itemTitle).append("\n");
        }
        return names.toString();
    }

    private void deleteSelectedLists() {
        for (long itemSelected : selectedItemIds) {
            if (itemSelected != 0) {
                dbHelper.deleteList(itemSelected, NoteContract.UnitEntry.TABLE_NAME, NoteContract.UnitEntry.COLUMN_UNIT_ID);
            }
        }
    }


    //-------------- EDIT METHODS PARA onActionItemClicked -----------------------

    public void editDialog(Long id, String title, ActionMode mode) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View alertView = layoutInflater.inflate(R.layout.dialog_unit_edit, null);

        EditText editTextNewUnitName = alertView.findViewById(R.id.new_unit_title);
        EditText editTextNewUnitValue = alertView.findViewById(R.id.new_unit_value);

        editTextNewUnitName.setText(title);
        editTextNewUnitValue.setHint("$" + selectedItemValues.get(0));

        final AlertDialog editDialog = new AlertDialog.Builder(mContext)
                .setView(alertView)
                .setTitle("Modo de edición")
                .setPositiveButton("ACEPTAR", null)
                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editingModeState = false;
                    }
                })
                .create();

        editDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = editDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setUpgradeUnitTitleAndValue(editTextNewUnitName, editTextNewUnitValue);
                        dbHelper.updateTimestamp(parentId, NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TIMESTAMP, NoteContract.NoteEntry.COLUMN_ID);
                        editDialog.dismiss();
                        mode.finish();
                    }
                });
            }
        });
        editDialog.show();
    }

    private void setUpgradeUnitTitleAndValue(EditText newUnitTitleEditText, EditText newCostEditText) {
        String newUnitTitle = newUnitTitleEditText.getText().toString();
        String newUnitCost = newCostEditText.getText().toString();

        if (!newUnitTitle.trim().isEmpty()) {
            editUpgrade(newUnitTitle);
        }
        if (!newUnitCost.trim().isEmpty()) {
            editValueUpgrade(Double.parseDouble(newUnitCost));
        }
        unitsRecyclerAdapter.unitsSwapCursor(mCursor);
    }

    private void editUpgrade(String newTitle) {
        dbHelper.updateTitle(selectedItemIds.get(0),
                NoteContract.UnitEntry.TABLE_NAME,
                NoteContract.UnitEntry.COLUMN_UNIT_TITLE,
                newTitle,
                NoteContract.UnitEntry.COLUMN_UNIT_ID);
    }

    private void editValueUpgrade(double newValue) {
        dbHelper.updateCost(selectedItemIds.get(0),
                NoteContract.UnitEntry.TABLE_NAME,
                NoteContract.UnitEntry.COLUMN_UNIT_COST,
                newValue,
                NoteContract.UnitEntry.COLUMN_UNIT_ID);
    }

    //----------------------METHODS onDestroyActionMode-------------------

    private void unlockOrientation() {
        UnitsActivity unitsActivity = (UnitsActivity) mContext;
        unitsActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public void unitListSettings() {
        setSelectedTotalTextView();
        selectedItemValues.clear();
        unitsTotalTextView.setText("Total Lista: " + String.format("$%.2f", dbHelper.totalUnits(parentId)));
        setBudgetLeftTextView(parentBudget);
    }

    private void setSelectedTotalTextView() {
        double total = dbHelper.selectedTotalUnits(parentId);
        if (total != 0) {
            selectedTotalTextView.setVisibility(View.VISIBLE);
            selectedTotalTextView.setText("SELEC: " + String.format("$%.2f", total));
        } else {
            selectedTotalTextView.setVisibility(View.GONE);
        }
    }

    private void setBudgetLeftTextView(double budget) {
        double bLeft = budget - dbHelper.totalUnits(parentId);
        if (budget == 0) {
            budgetLeft.setVisibility(View.GONE);
        } else {
            budgetLeft.setVisibility(View.VISIBLE);
            budgetLeft.setText("RESTAN: " + String.format("$%.2f", bLeft));
        }
    }

}