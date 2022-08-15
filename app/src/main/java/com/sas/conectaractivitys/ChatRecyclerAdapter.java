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


public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {


    private OnRVItemLongClickListener LongClickListener;
    private OnRVItemClickListener clickListener;
    private OnClickActionModeListener actionClickListener;

    private SparseBooleanArray actionModeSelectedItems = new SparseBooleanArray();

    private Context mContext;
    private Cursor mCursor;

    public ChatRecyclerAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        this.mCursor = cursor;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.molecule_chat, parent, false);
        ChatRecyclerAdapter.ViewHolder viewHolder = new ChatRecyclerAdapter.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        long id = mCursor.getLong(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_ID));
        String note = mCursor.getString(mCursor.getColumnIndex(NoteContract.ChatEntry.COLUMN_CHAT_NOTE));
        String timeStamp = mCursor.getString(mCursor.getColumnIndex(NoteContract.ChatEntry.COLUMN_CHAT_TIMESTAMP));

        holder.chatNoteView.setText(note);
        holder.chatDateView.setText(ListFormats.basicDate(timeStamp));

        holder.isItemChecked = actionModeSelectedItems.get(position); //Esto lo tuve que instanciar asi, porque los unicos bools que se almacen son los del spare.

        holder.isSelectedInActionMode(actionModeSelectedItems.get(position)); //Esto es para manter encendida o apagada la selecci'on despues del notify tras scrolear

        if (!ChatListActionMode.chatListActionModeState) {//esto es lo que deselecciona con fade
            holder.onDestroyFade(position);
            holder.isItemChecked = false;
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (LongClickListener != null && id != 0) {
                    LongClickListener.onRVItemLongClick(id, note, timeStamp, holder, position);
                }
                return true;//Se retorna true para que no se ejecute otro onClickListerner al soltar... i guess?
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (actionClickListener != null && id != 0) {
                    actionClickListener.onClickActionMode(id, note, timeStamp, holder);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void chatSwapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView chatNoteView;
        TextView chatDateView;
        boolean isItemChecked;
        ImageView listCheckedView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatNoteView = itemView.findViewById(R.id.chat_note_text);
            chatDateView = itemView.findViewById(R.id.chat_date);

            isItemChecked = false;
            listCheckedView = itemView.findViewById(R.id.chat_note_checked);
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
    }

    // Esta es la interfaz y metodo para ejecutar el context del mainactivity
    public interface OnRVItemLongClickListener {
        void onRVItemLongClick(long columnId, String note, String timestamp, ViewHolder viewHolder, int position);
    }

    public void setOnRVItemLongClickListener(OnRVItemLongClickListener LongClickListener) {
        this.LongClickListener = LongClickListener;
    }

    public interface OnRVItemClickListener {
        void onRVItemClickListener(long columnId, int type, String note, String timestamp);
    }

    public void setOnRVItemClickListener(OnRVItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface OnClickActionModeListener {
        void onClickActionMode(long columnId, String note, String timestamp, ViewHolder holder);
    }

    public void setOnClickActionModeListener(OnClickActionModeListener actionClickListener) {
        this.actionClickListener = actionClickListener;
    }
}
