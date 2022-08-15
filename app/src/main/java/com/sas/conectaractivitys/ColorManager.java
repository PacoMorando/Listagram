package com.sas.conectaractivitys;

import android.widget.EditText;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

public class ColorManager {

    public static void setChatBoxColor(int color, EditText chatEditText, ImageView addButton) {
        switch (color) {
            case 1:
                chatEditText.setBackgroundResource(R.drawable.outline_chat_orange);
                addButton.setBackgroundResource(R.drawable.background_orange);
                break;
            case 2:
                chatEditText.setBackgroundResource(R.drawable.outline_chat_green);
                addButton.setBackgroundResource(R.drawable.background_green);
                break;
            case 3:
                chatEditText.setBackgroundResource(R.drawable.outline_chat_purple);
                addButton.setBackgroundResource(R.drawable.background_purple);
                break;
            case 4:
                chatEditText.setBackgroundResource(R.drawable.outline_chat_red);
                addButton.setBackgroundResource(R.drawable.background_red);
                break;
        }
    }

    public static void setChatBoxColor(int color,  EditText chatEditText, EditText costEditText, ImageView addButton) {
        setChatBoxColor(color,chatEditText,addButton);
        switch (color) {
            case 1:
                costEditText.setBackgroundResource(R.drawable.outline_chat_orange);
                break;
            case 2:
                costEditText.setBackgroundResource(R.drawable.outline_chat_green);
                break;
            case 3:
                costEditText.setBackgroundResource(R.drawable.outline_chat_purple);
                break;
            case 4:
                costEditText.setBackgroundResource(R.drawable.outline_chat_red);
                break;
        }
    }

    public static void setChatRecyclerBackground(int color, RecyclerView recyclerView) {
        switch (color) {
            case 1:
                recyclerView.setBackgroundResource(R.drawable.background_chat_orange);
                break;
            case 2:
                recyclerView.setBackgroundResource(R.drawable.background_chat_green);
                break;
            case 3:
                recyclerView.setBackgroundResource(R.drawable.background_chat_purple);
                break;
            case 4:
                recyclerView.setBackgroundResource(R.drawable.background_chat_red);
                break;
        }
    }

}
