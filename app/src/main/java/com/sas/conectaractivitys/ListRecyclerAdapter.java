package com.sas.conectaractivitys;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListRecyclerAdapter extends RecyclerView.Adapter<ListRecyclerAdapter.ViewHolder> {

    private OnRVItemLongClickListener LongClickListener;
    private OnRVItemClickListener clickListener;
    private OnClickActionModeListener actionClickListener;

    private SparseBooleanArray actionModeSelectedItems = new SparseBooleanArray();


    private Context mContext; //Este va a hacer las veces del array "listRow"
    public Cursor mCursor;


    public ListRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.molecule_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }


        String title = mCursor.getString(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TITLE));
        String totalNumber = mCursor.getString(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_BUDGET));
        long id = mCursor.getLong(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_ID));
        String timeStamp = mCursor.getString(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TIMESTAMP));
        int type = mCursor.getInt(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TYPE));
        int color = mCursor.getInt(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_COLOR));
        double budget = mCursor.getDouble(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_BUDGET));


        holder.listTitleView.setText(title);
        holder.listLastModifView.setText(ListFormats.basicDate(timeStamp));
        holder.listTotalNumberView.setText("Tipo: " + type + ", Id: " + id);
        holder.listIconView.setImageResource(iconSelected(type));
        holder.listIconView.setBackgroundResource(iconColorSelected(color));
        holder.setTextPreview(type, id);


        holder.isItemChecked = actionModeSelectedItems.get(position); //Esto lo tuve que instanciar asi, porque los unicos bools que se almacen son los del spare.

        holder.isSelectedInActionMode(actionModeSelectedItems.get(position)); //Esto es para manter encendida o apagada la selecci'on despues del notify tras scrolear

        if (!MainActionMode.actionModeState) {//esto es lo que deselecciona con fade
            holder.onDestroyFade(position);
            holder.isItemChecked = false;
        }


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (LongClickListener != null && id != 0) {
                    LongClickListener.onRVItemLongClick(id, title, timeStamp, holder, position);
                }
                return true;//Se retorna true para que no se ejecute otro onClickListerner al soltar... i guess?
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (clickListener != null && id != 0) {
                    clickListener.onRVItemClickListener(id, type, title, budget, color, timeStamp);
                }

                if (actionClickListener != null && id != 0) {
                    actionClickListener.onClickActionMode(id, title, timeStamp, holder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }


    public void swapCursor(Cursor newCursor) {
        /*Aunque no entiendo muy bien este metodo ayuda a ejecturar del notify
         * desde otros metodos fuera del metodo donde fue instaciado el adapter*/
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView listIconView;
        TextView listTitleView;
        TextView listLastModifView;
        TextView listTotalView;
        TextView listTotalNumberView;

        boolean isItemChecked;
        ImageView listCheckedView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            listIconView = itemView.findViewById(R.id.list_icon);
            listTitleView = itemView.findViewById(R.id.list_title);
            listLastModifView = itemView.findViewById(R.id.list_last_modif);
            listTotalView = itemView.findViewById(R.id.list_total);
            listTotalNumberView = itemView.findViewById(R.id.list_total_number);

            isItemChecked = false;
            listCheckedView = itemView.findViewById(R.id.list_checked);
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

        private void setTextPreview(int listType, long parentId) {
            NoteDBHelper dbHelper = new NoteDBHelper(mContext);
            switch (listType) {
                case CheckListActivity.CHECKLIST_TYPE:
                    listTotalView.setText(dbHelper.getLastMessage(
                            NoteContract.CheckEntry.COLUMN_CHECK_ID,
                            parentId, NoteContract.CheckEntry.TABLE_NAME));
                    break;
                case ChatListActivity.CHATLIST_TYPE:
                    listTotalView.setText(dbHelper.getLastMessage(
                            NoteContract.ChatEntry.COLUMN_CHAT_ID,
                            parentId, NoteContract.ChatEntry.TABLE_NAME));
                    break;
                case UnitsActivity.UNITS_LIST_TYPE:
                    listTotalView.setText("Total Lista: " + String.format("$%.2f", dbHelper.totalUnits(parentId)));
                    break;
            }
        }

        @Override
        public String toString() {
            return (String) listTitleView.getText();
        }
    }


// Algo se le tiene que hacer a esta fuincion
    public int iconSelected(int type) {

        switch (type) {
            case 1:
                return R.drawable.ic_checklist;
            case 2:
                return R.drawable.ic_chatlisticon;
            case 3:
                return R.drawable.ic_unitslisticon;
            default:
                return R.drawable.ic_error;
        }
    }

    public int iconColorSelected(int color) {

        switch (color) {
            case 1:
                return R.drawable.background_orange;
            case 2:
                return R.drawable.background_green;
            case 3:
                return R.drawable.background_purple;
            case 4:
                return R.drawable.background_red;
            default:
                return R.drawable.background_blue_solid;
        }
    }



    // Esta es la interfaz y metodo para ejecutar el context del mainactivity
    public interface OnRVItemLongClickListener {
        void onRVItemLongClick(long columnId, String title, String timestamp, ViewHolder viewHolder, int position);
    }

    public void setOnRVItemLongClickListener(OnRVItemLongClickListener LongClickListener) {
        this.LongClickListener = LongClickListener;
    }

    public interface OnRVItemClickListener {
        void onRVItemClickListener(long columnId, int type, String title, double budget, int color, String timestamp);
    }

    public void setOnRVItemClickListener(OnRVItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface OnClickActionModeListener {
        void onClickActionMode(long columnId, String title, String timestamp, ViewHolder holder);
    }

    public void setOnClickActionModeListener(OnClickActionModeListener actionClickListener) {
        this.actionClickListener = actionClickListener;
    }

}
