/*
 * Copyright (C) 2017 by nebulaM <nebulam12@gmail.com>
 */
package com.github.batterystate;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.github.batterystate.R;

/**
 * Dialog for changing theme
 */

public class ThemeDialog extends DialogFragment implements View.OnClickListener{
    public static final String TAG="ThemeDialog";
    interface onCloseListener{
        /**
         * @param tag name of the dialog being closed
         */
        void onDialogClose(String tag, int Value);
    }
    private onCloseListener mOnCloseListener;
    private ImageView[] mTheme=new ImageView[4];
    //default -1 to indicate user close this dialog without giving any input
    private int selected=-1;
    /*public static ThemeDialog newInstance() {
        ThemeDialog frag = new ThemeDialog();
        return frag;
    }*/
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view=inflater.inflate(R.layout.fragment_dialog_theme, null);
        //Setup clickable theme buttons
        mTheme[0]=(ImageView) view.findViewById(R.id.dialog_theme_circle_0);
        mTheme[1]=(ImageView) view.findViewById(R.id.dialog_theme_circle_1);
        mTheme[2]=(ImageView) view.findViewById(R.id.dialog_theme_circle_2);
        mTheme[3]=(ImageView) view.findViewById(R.id.dialog_theme_circle_3);
        for(ImageView clickable : mTheme){
            clickable.setOnClickListener(this);
        }
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.dialog_theme_circle_0:
                selected=0;
                break;
            case R.id.dialog_theme_circle_1:
                selected=1;
                break;
            case R.id.dialog_theme_circle_2:
                selected=2;
                break;
            case R.id.dialog_theme_circle_3:
                selected=3;
                break;
            default:
                break;
        }
        this.dismiss();
    }

    public void setOnCloseListener(onCloseListener onCloseListener){
        mOnCloseListener=onCloseListener;
    }
    @Override
    public void onDismiss (DialogInterface dialog) {
        super.onDismiss(dialog);
        mOnCloseListener.onDialogClose(TAG,selected);
    }
}
