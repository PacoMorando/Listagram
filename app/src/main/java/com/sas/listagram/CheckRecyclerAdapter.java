package com.sas.listagram;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

public class CheckRecyclerAdapter extends RecyclerView.Adapter<CheckRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    private OnRVItemClickListener listener;
    private CheckRecyclerAdapter.OnRVItemLongClickListener longClickListener;
    private CheckRecyclerAdapter.OnClickActionModeListener actionClickListener;

    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private SparseBooleanArray actionModeSelectedItems = new SparseBooleanArray();


    public CheckRecyclerAdapter(Context mContext, Cursor mCursor) {
        this.mContext = mContext;
        this.mCursor = mCursor;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.molecule_check, parent, false);
        CheckRecyclerAdapter.ViewHolder viewHolder = new CheckRecyclerAdapter.ViewHolder(view);

        return viewHolder;
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        long id = mCursor.getLong(mCursor.getColumnIndex(NoteContract.CheckEntry.COLUMN_CHECK_ID));
        int itemChecked = mCursor.getInt(mCursor.getColumnIndex(NoteContract.CheckEntry.COLUMN_CHECK_IS_CHECKED));
        String note = mCursor.getString(mCursor.getColumnIndex(NoteContract.CheckEntry.COLUMN_CHECK_NOTE));
        String timestamp = mCursor.getString(mCursor.getColumnIndex(NoteContract.CheckEntry.COLUMN_CHECK_TIMESTAMP));
        boolean trueChecked = (itemChecked == 1);


        holder.textCheckNoteView.setText(note);



        holder.checkNoteBox.setChecked(selectedItems.get(position, trueChecked));//Esto lo hace para cuando el onBind se vuelva llamar tras hacer el scroll

        holder.isItemChecked = actionModeSelectedItems.get(position); //Esto lo tuve que instanciar asi, porque los unicos bools que se almacen son los del spare.

        holder.isSelectedInActionMode(actionModeSelectedItems.get(position));

        if (!CheckListActionMode.checkListActionModeState) {
            holder.onDestroyFade(position);
            holder.isItemChecked = false;
        }


        holder.checkNoteBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CheckListActionMode.checkListActionModeState) {
                    System.out.println("clicado uno");

                    /*cuando das click antes que este "onClickListener se ejecuta el codigo
                     * que cambia el estado de isChecked, de modo que si clicas el checkbox
                     * y estaba false, el siguiente codigo trabajara con el isChecked en true*/

                    boolean insideCheck = holder.checkNoteBox.isChecked();
                    System.out.println("boton estaba " + holder.checkNoteBox.isChecked());
                    System.out.println("Estado selectedItems " + selectedItems.get(position));

                    // El Sparse boolean array ayuda que se mantengan los checkboxes y que el
                    // recycler no los deseleccione al hacer scroll
                    /*Esto prepara el SparesBooleanArray porque cada que se abre la activity comienza en false,
                    entonces si el estado del isChecked antes del click era false siempre ejecutaria
                    la condicion que setea to do en false*/

                    if (selectedItems.get(position) == insideCheck) {
                        selectedItems.put(position, true);
                        System.out.println("Se ejecuto esto");
                    }
                    if (listener != null && id != 0) {
                        if (selectedItems.get(position, false)) {
                            selectedItems.put(position, false);
                            listener.onRVItemClick(id, 0, note);
                            System.out.println("Se ejecuto false");

                        } else {
                            selectedItems.put(position, true);
                            listener.onRVItemClick(id, 1, note);
                            System.out.println("Se ejecuto true");
                        }
                        System.out.println("SPARE SELECTED" + selectedItems.toString());
                    }
                }else {
                    keepingCheckboxState(holder.checkNoteBox);
                    System.out.println("SE EJECUTO EL KEEPING");
                }
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (longClickListener != null && id != 0) {
                    longClickListener.onRVItemLongClick(id, note, timestamp, holder, position);

                }
                return true;//Se retorna true para que no se ejecute otro onClickListerner al soltar
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckListActionMode.checkListActionModeState){

                    System.out.println("clicado " + note);

                    if (actionClickListener != null && id != 0) {

                        actionClickListener.onClickActionMode(id, note, timestamp, holder, position);

                        System.out.println(holder.checkNoteBox.isChecked() + " CHECADO");
                        System.out.println("Item fuera actionmode " + holder.isItemChecked);
                        System.out.println("SPARE ACTIONMODE " + actionModeSelectedItems.toString());
                        //keepingCheckboxState(holder.checkNoteBox);
                    }
                }
            }
        });

    }

    private void keepingCheckboxState(CheckBox itemCheckBox) {
        itemCheckBox.setChecked(!itemCheckBox.isChecked());
    }


    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    //Refresca el RecyclerView es decir vuelve a llamar al metodo OnBind (creo... jajaja)
    public void checkSwapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        if (newCursor != null) {
            selectedItems.clear();
            notifyDataSetChanged();
            System.out.println("SPARE SELECTED " + selectedItems.toString());
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkNoteBox;
        TextView textCheckNoteView;
        ImageView noteCheckedView;
        boolean isItemChecked;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkNoteBox = itemView.findViewById(R.id.check_note);
            textCheckNoteView = itemView.findViewById(R.id.text_check_note);
            noteCheckedView = itemView.findViewById(R.id.note_checked);
            isItemChecked = false;
        }

        public void selectionFade(int position) {
            if (isItemChecked) {
                noteCheckedView.animate().alpha(1f).setDuration(500);
                actionModeSelectedItems.put(position, true);
            } else {
                noteCheckedView.animate().alpha(0f).setDuration(500);
                actionModeSelectedItems.put(position, false);
            }
        }


        public void onDestroyFade(int position) {
            noteCheckedView.animate().alpha(0f).setDuration(500).withEndAction(new Runnable() {
                @Override
                public void run() {
                    actionModeSelectedItems.clear();
                }
            });
        }


        public void isSelectedInActionMode(boolean isItemChecked) {
            if (isItemChecked) {
                noteCheckedView.setAlpha(1f);
            } else {
                noteCheckedView.setAlpha(0f);
            }
        }


    }


    // Esta es la interfaz y metodo para ejecutar el check change en el CheckListActivity
    public interface OnRVItemLongClickListener {
        void onRVItemLongClick(long columnId, String note, String timestamp, CheckRecyclerAdapter.ViewHolder viewHolder, int position);
    }

    public void setOnRVItemLongClickListener(CheckRecyclerAdapter.OnRVItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public interface OnRVItemClickListener {
        void onRVItemClick(long id, int isChecked, String note);
    }

    public void setOnRVItemClickListener(OnRVItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnClickActionModeListener {
        void onClickActionMode(long columnId, String note, String timestamp, CheckRecyclerAdapter.ViewHolder holder, int position);
    }

    public void setOnClickActionModeListener(CheckRecyclerAdapter.OnClickActionModeListener actionClickListener) {
        this.actionClickListener = actionClickListener;
    }


}
