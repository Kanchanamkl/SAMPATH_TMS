package com.epic.pos.view;

import android.app.AlertDialog;
import android.content.Context;

import java.util.List;

public class MultiAppListDialog {

    private Context context;

    public MultiAppListDialog(Context context) {
        this.context = context;
    }

    public void showDialog(String title, List<String> appList, Listener listener) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        // add a list
        String[] data = new String[appList.size()];
        for (int i = 0; i < appList.size(); i++) {
            data[i] = appList.get(i);
        }

        builder.setItems(data, (dialog, index) ->
                listener.onApplicationClicked(appList.get(index), index));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    public interface Listener{
        void onApplicationClicked(String name, int index);
    }
}
