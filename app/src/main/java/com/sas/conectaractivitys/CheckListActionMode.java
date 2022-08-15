package com.sas.conectaractivitys;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sas.conectaractivitys.anim.Animations;
import java.util.ArrayList;

public class CheckListActionMode implements MyActionMode {

    public static boolean checkListActionModeState = false;
    private boolean editingModeState = false;

    private ArrayList<Long> selectedItemIds;
    private ArrayList<String> selectedItemTitles;
    private final ArrayList<Integer> positions;

    private NoteDBHelper dbHelper;
    private Long parentId;
    private Context mContext;
    private Cursor mCursor;
    private CheckRecyclerAdapter checkRecyclerAdapter;
    private View viewToShow;


    public CheckListActionMode(ArrayList<Long> selectedItemIds, ArrayList<String> selectedItemTitles, ArrayList<Integer> positions, NoteDBHelper dbHelper,
                               Long parentId, Context mContext, Cursor mCursor, CheckRecyclerAdapter checkRecyclerAdapter, View viewToShow) {
        this.selectedItemIds = selectedItemIds;
        this.selectedItemTitles = selectedItemTitles;
        this.positions = positions;
        this.dbHelper = dbHelper;
        this.parentId = parentId;
        this.mContext = mContext;
        this.mCursor = mCursor;
        this.checkRecyclerAdapter = checkRecyclerAdapter;
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
        checkListPrepareActionMode(mode, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_delete:
                deleteAlertDialog(mode);
                return true;
            case R.id.context_edit:
                editingModeState = true;
                editDialog(selectedItemIds.get(0), selectedItemTitles.get(0), mode);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        checkListActionModeState = false;
        editingModeState = false;
        selectedItemIds.clear();
        selectedItemTitles.clear();
        positions.clear();
        checkRecyclerAdapter.notifyDataSetChanged();//No recuerdo porque se debe de ejecutar este
        Animations.showHideVertical(viewToShow,checkListActionModeState,0f);
        unlockOrientation();
    }

    //------------- METODOS PARA onPrepareActionMode -----------------------

    private void checkListPrepareActionMode(ActionMode mode, Menu menu) {
        System.out.println("CheckList prepare ejecutado");
        checkRecyclerAdapter.setOnClickActionModeListener(new CheckRecyclerAdapter.OnClickActionModeListener() {
            @Override
            public void onClickActionMode(long columnId, String note, String timestamp, CheckRecyclerAdapter.ViewHolder holder, int position) {
                if (CheckListActionMode.checkListActionModeState && !editingModeState) {

                    checkListItemChecker(columnId, note, holder, holder.isItemChecked, mode);

                    mode.setTitle(String.valueOf(selectedItemIds.size()));

                    itemVisible(menu.findItem(R.id.context_edit), selectedItemIds.size());
                }
            }
        });
    }

    // Gestiona lo necesario para seleccionar y deseleccionar elementos
    private void checkListItemChecker(long columnId, String title, CheckRecyclerAdapter.ViewHolder viewHolder, boolean isThisItemChecked, ActionMode mode) {
        System.out.println("Se ejecuto checkListItemChecker");
        if (!isThisItemChecked) {
            viewHolder.isItemChecked = true;
            viewHolder.selectionFade(viewHolder.getAdapterPosition());
            selectedItemIds.add(columnId);
            selectedItemTitles.add(title);
            positions.add(viewHolder.getAdapterPosition());

        } else {
            viewHolder.isItemChecked = false;
            viewHolder.selectionFade(viewHolder.getAdapterPosition());
            selectedItemIds.remove(columnId);
            selectedItemTitles.remove(title);
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
                            checkRecyclerAdapter.checkSwapCursor(mCursor);
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
                dbHelper.deleteList(itemSelected, NoteContract.CheckEntry.TABLE_NAME, NoteContract.CheckEntry.COLUMN_CHECK_ID);
            }
        }
    }


    //-------------- EDIT METHODS PARA onActionItemClicked -----------------------

    public void editDialog(Long id, String title, ActionMode mode) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View alertView = layoutInflater.inflate(R.layout.dialog_edit, null);

        EditText editTextNewTitle = alertView.findViewById(R.id.new_title);
        editTextNewTitle.setText(title);

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
                        setUpgradeTitle(editTextNewTitle);
                        dbHelper.updateTimestamp(parentId, NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TIMESTAMP, NoteContract.NoteEntry.COLUMN_ID);
                        editDialog.dismiss();
                        mode.finish();
                    }
                });
            }
        });
        editDialog.show();
    }

    private void setUpgradeTitle(EditText newTitleEditText) {
        String newTitle = newTitleEditText.getText().toString();
        //Este if revisa si está vacío el edittext para introducir nuevo título
        if (newTitle.trim().isEmpty()) {
            return;
        }
        dbHelper.updateTitle(selectedItemIds.get(0), NoteContract.CheckEntry.TABLE_NAME, NoteContract.CheckEntry.COLUMN_CHECK_NOTE, newTitle, NoteContract.CheckEntry.COLUMN_CHECK_ID);
        checkRecyclerAdapter.checkSwapCursor(mCursor);
    }


    //----------------------METHODS onDestroyActionMode-------------------

    private void unlockOrientation() {
                CheckListActivity checkListActivity = (CheckListActivity) mContext;
                checkListActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
}