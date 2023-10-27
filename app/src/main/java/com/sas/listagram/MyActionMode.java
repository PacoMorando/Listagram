package com.sas.listagram;

import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.view.ActionMode;

public interface MyActionMode <mContext,id,type,title,timestamp,itemView> extends ActionMode.Callback {
    @Override
    boolean onCreateActionMode(ActionMode mode, Menu menu);

    @Override
    boolean onPrepareActionMode(ActionMode mode, Menu menu);

    @Override
    boolean onActionItemClicked(ActionMode mode, MenuItem item);

    @Override
    void onDestroyActionMode(ActionMode mode);
}
