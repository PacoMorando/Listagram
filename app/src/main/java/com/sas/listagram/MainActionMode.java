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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sas.listagram.anim.Animations;

import java.util.ArrayList;

public class MainActionMode implements MyActionMode {

    public static boolean actionModeState = false;
    private boolean editingModeState = false;

    private ArrayList<Long> selectedItemIds;
    private ArrayList<String> selectedItemTitles;
    private final ArrayList<Integer> positions;

    private NoteDBHelper dbHelper;
    private Context mContext;
    private Cursor mCursor;
    private View viewToShow;
    private ListRecyclerAdapter listRecyclerAdapter;


    public MainActionMode(ArrayList<Long> selectedItemIds, ArrayList<String> selectedItemTitles, ArrayList<Integer> positions,
                          NoteDBHelper dbHelper, Context mContext, Cursor mCursor, ListRecyclerAdapter listRecyclerAdapter, View viewToShow) {
        this.selectedItemIds = selectedItemIds;
        this.selectedItemTitles = selectedItemTitles;
        this.positions = positions;
        this.dbHelper = dbHelper;
        this.mContext = mContext;
        this.mCursor = mCursor;
        this.listRecyclerAdapter = listRecyclerAdapter;
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
        System.out.println("ACTION MODE " + MainActionMode.actionModeState);
        listPrepareActionMode(mode, menu);
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
        actionModeState = false;
        editingModeState = false;
        selectedItemIds.clear();
        selectedItemTitles.clear();
        positions.clear();
        listRecyclerAdapter.notifyDataSetChanged();
        Animations.showHideVertical(viewToShow, actionModeState, 0f);
        unlockOrientation();
    }


    //------------- METHODS FOR onPrepareActionMode -----------------------

    private void listPrepareActionMode(ActionMode mode, Menu menu) {
        System.out.println("List prepare ejecutado");
        listRecyclerAdapter.setOnClickActionModeListener(new ListRecyclerAdapter.OnClickActionModeListener() {
            @Override
            public void onClickActionMode(long columnId, String title, String timestamp, ListRecyclerAdapter.ViewHolder holder) {
                if (MainActionMode.actionModeState && !editingModeState) {

                    ListItemChecker(columnId, title, holder, holder.isItemChecked, mode);

                    mode.setTitle(String.valueOf(selectedItemIds.size()));

                    itemVisible(menu.findItem(R.id.context_edit), selectedItemIds.size());
                }
            }
        });
    }


    private void ListItemChecker(long columnId, String title, ListRecyclerAdapter.ViewHolder viewHolder, boolean isThisItemChecked, ActionMode mode) {
        if (!isThisItemChecked) {
            viewHolder.isItemChecked = true;
            viewHolder.selectionFade(viewHolder.getAdapterPosition());
            selectedItemIds.add(columnId);
            selectedItemTitles.add(title);
            positions.add(viewHolder.getAdapterPosition());

            System.out.println(title);
            System.out.println(viewHolder.getAdapterPosition());
            System.out.println(selectedItemTitles);
            System.out.println(positions);

        } else {
            viewHolder.isItemChecked = false;
            viewHolder.selectionFade(viewHolder.getAdapterPosition());
            selectedItemIds.remove(columnId);
            selectedItemTitles.remove(title);
            positions.remove((Integer) viewHolder.getAdapterPosition());

            System.out.println(positions);
            System.out.println(selectedItemTitles);

            if (selectedItemIds.size() == 0) {
                mode.finish();
            }
        }
    }


    private void itemVisible(MenuItem menuItem, int selectedItemNumber) {
        menuItem.setVisible(selectedItemNumber == 1);
    }


//-------------- DELETE METHODS FOR onActionItemClicked -----------------------

    //Aqui voy a usar el type para modular esta funcion
    private void deleteAlertDialog(ActionMode mode) {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(mContext);
        if (selectedItemIds.size() > 0) {
            alert.setTitle("Se eliminaran:")
                    .setMessage(namesOfItemsSelected())
                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteSelectedLists();
                            listRecyclerAdapter.swapCursor(mCursor);
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

    private void deleteSelectedLists() {
        for (long itemSelected : selectedItemIds) {
            if (itemSelected != 0) {
                dbHelper.deleteList(itemSelected, NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_ID);
            }
        }
    }

    private String namesOfItemsSelected() {
        StringBuilder names = new StringBuilder();
        for (String itemTitle : selectedItemTitles) {
            names.append(itemTitle).append("\n");
        }
        return names.toString();
    }


//-------------- EDIT METHODS FOR onActionItemClicked -----------------------


    public void editDialog(Long id, String title, ActionMode mode) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View alertView = layoutInflater.inflate(R.layout.dialog_edit, null);

        EditText editTextNewTitle = alertView.findViewById(R.id.new_title);
        editTextNewTitle.setText(title);

        final AlertDialog editDialog = new AlertDialog.Builder(mContext)
                .setView(alertView)
                .setTitle("Introduce un nuevo título: ")
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
        dbHelper.updateTitle(selectedItemIds.get(0), NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry.COLUMN_TITLE, newTitle, NoteContract.NoteEntry.COLUMN_ID);
        listRecyclerAdapter.swapCursor(mCursor);
    }


    //----------------------METHODS onDestroyActionMode-------------------

    private void unlockOrientation() {
        MainActivity mainActivity = (MainActivity) mContext;
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

}