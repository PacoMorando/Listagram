package com.sas.conectaractivitys;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Format;


public class UnitsRecyclerAdapter extends RecyclerView.Adapter<UnitsRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private OnRVItemLongClickListener longClickListener;
    private OnRVItemClickListener clickListener;
    private OnClickActionModeListener actionClickListener;

    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private SparseBooleanArray actionModeSelectedItems = new SparseBooleanArray();

    public UnitsRecyclerAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.molecule_unit, parent, false);
        UnitsRecyclerAdapter.ViewHolder viewHolder = new UnitsRecyclerAdapter.ViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }
        long id = mCursor.getLong(mCursor.getColumnIndex(NoteContract.UnitEntry.COLUMN_UNIT_ID));
        String timeStamp = mCursor.getString(mCursor.getColumnIndex(NoteContract.UnitEntry.COLUMN_UNIT_TIMESTAMP));
        String unitName = mCursor.getString(mCursor.getColumnIndex(NoteContract.UnitEntry.COLUMN_UNIT_TITLE));
        double unitValue = mCursor.getDouble(mCursor.getColumnIndex(NoteContract.UnitEntry.COLUMN_UNIT_COST));
        long idList = mCursor.getLong(mCursor.getColumnIndex(NoteContract.UnitEntry.COLUMN_ID));
        int itemChecked = mCursor.getInt(mCursor.getColumnIndex(NoteContract.UnitEntry.COLUMN_UNIT_IS_CHECKED));
        boolean trueChecked = (itemChecked == 1);


        holder.unitNameView.setText(unitName);
        holder.unitValueView.setText(String.format("$%.2f", unitValue));
        holder.unitDateView.setText(ListFormats.basicDate(timeStamp));

        holder.selectRedItem(selectedItems.get(position, trueChecked));


        holder.isItemChecked = actionModeSelectedItems.get(position); //Esto lo tuve que instanciar asi, porque los unicos bools que se almacen son los del spare.
        holder.isSelectedInActionMode(actionModeSelectedItems.get(position)); //Esto es para manter encendida o apagada la selecci'on despues del notify tras scrolear


        if (!UnitsActionMode.unitsActionModeState) {//esto es lo que deselecciona con fade
            holder.onDestroyFade(position);
            holder.isItemChecked = false;
        }


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (longClickListener != null && id != 0) {
                    longClickListener.onRVItemLongClick(id, unitName, unitValue, timeStamp, holder, position);
                }
                return true;//Se retorna true para que no se ejecute otro onClickListerner al soltar... i guess?
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UnitsActionMode.unitsActionModeState) {
                    if (clickListener != null && id != 0) {
                        if (holder.selectRedItem(selectedItems.get(position,trueChecked))){
                            holder.selectRedItem(false);
                            selectedItems.put(position,false);
                            clickListener.onRVItemClickListener(id, 0, unitName);
                        }else{
                            holder.selectRedItem(true);
                            selectedItems.put(position,true);
                            clickListener.onRVItemClickListener(id, 1, unitName);
                        }
                        System.out.println(selectedItems.toString());

                    }

                } else {
                    if (actionClickListener != null && id != 0) {
                        actionClickListener.onClickActionMode(id, unitName, unitValue, timeStamp, holder);
                    }
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void unitsSwapCursor(Cursor newCursor) {
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

        TextView unitNameView;
        TextView unitValueView;
        TextView unitDateView;
        boolean isItemChecked;
        ImageView listCheckedView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            unitNameView = itemView.findViewById(R.id.unit_name);
            unitValueView = itemView.findViewById(R.id.unit_value);
            unitDateView = itemView.findViewById(R.id.unit_date);
            listCheckedView = itemView.findViewById(R.id.unit_checked);
            isItemChecked = false;

        }

        public void selectionFade(int position) {
            if (isItemChecked) {
                listCheckedView.animate().alpha(1f).setDuration(500);
                actionModeSelectedItems.put(position, true);
            } else {
                listCheckedView.animate().alpha(0f).setDuration(500);
                actionModeSelectedItems.put(position, false);
            }
        }


        public void onDestroyFade(int position) {
            listCheckedView.animate().alpha(0f).setDuration(500).withEndAction(new Runnable() {
                @Override
                public void run() {
                    actionModeSelectedItems.clear();
                }
            });
        }


        public void isSelectedInActionMode(boolean isItemChecked) {
            if (isItemChecked) {
                listCheckedView.setAlpha(1f);
            } else {
                listCheckedView.setAlpha(0f);
            }
        }

        public boolean selectRedItem(boolean isChecked) {
            if (isChecked){
                unitNameView.setTextColor(mContext.getResources().getColor(R.color.selected_text));
                unitValueView.setTextColor(mContext.getResources().getColor(R.color.selected_text));
                unitDateView.setTextColor(mContext.getResources().getColor(R.color.selected_text));
            }else{
                unitNameView.setTextColor(mContext.getResources().getColor(R.color.black_text));
                unitValueView.setTextColor(mContext.getResources().getColor(R.color.black_text));
                unitDateView.setTextColor(mContext.getResources().getColor(R.color.black_text));
            }
            return isChecked;
        }

    }

    // Esta son las interfaces y método para ejecutar en el context del UnitsActivity
    public interface OnRVItemLongClickListener {
        void onRVItemLongClick(long columnId, String unitName, double unitValue, String timestamp, ViewHolder viewHolder, int position);
    }

    public void setOnRVItemLongClickListener(OnRVItemLongClickListener LongClickListener) {
        this.longClickListener = LongClickListener;
    }

    public interface OnRVItemClickListener {
        void onRVItemClickListener(long columnId, int isChecked, String unitName);
    }

    public void setOnRVItemClickListener(OnRVItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface OnClickActionModeListener {
        void onClickActionMode(long columnId, String unitName, double unitValue, String timestamp, ViewHolder holder);
    }

    public void setOnClickActionModeListener(OnClickActionModeListener actionClickListener) {
        this.actionClickListener = actionClickListener;
    }
}
