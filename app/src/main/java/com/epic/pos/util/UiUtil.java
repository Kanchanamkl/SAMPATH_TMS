package com.epic.pos.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.epic.pos.R;

public class UiUtil {

    public static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showDialogMessage(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void showDialogMessage(Context context, String title, String message, SuccessDialogListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (listener != null) {
                        listener.onOkClicked();
                    }
                }).show();
    }

    public static void showErrorDialog(Context context, String title, String msg, ErrorDialogListener listener) {
        new AlertDialog.Builder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onOkClicked();
                    }
                }).show();
    }

    public static void showErrorDialog(Context context, String title, String msg, String posBtn, String negBtn, OptionDialogListener listener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(posBtn, (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onOkClicked();
                    }
                }).setNegativeButton(negBtn, (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onCancelClicked();
                    }
                }).show();
    }

    public interface ErrorDialogListener {
        void onOkClicked();
    }

    public interface OptionDialogListener {
        void onOkClicked();

        void onCancelClicked();
    }

    public interface SuccessDialogListener {
        void onOkClicked();
    }

    public static void noInternetAlert(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.msg_title_no_network))
                .setMessage(context.getResources().getString(R.string.msg_turn_on_network))
                .setPositiveButton(context.getResources().getString(R.string.text_settings), (dialogInterface, i) -> {
                    context.startActivity(new Intent(Settings.ACTION_SETTINGS));
                    dialogInterface.dismiss();
                })
                .setNegativeButton(context.getResources().getString(R.string.text_cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void hideKeyboard(Context context) {
        // Check if no view has focus:
        View view = ((Activity) context).getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
