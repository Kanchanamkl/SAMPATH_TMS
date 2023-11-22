package com.epic.pos.view;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.epic.pos.data.DccData;
import com.epic.pos.data.db.dbpos.modal.CardDefinition;

import java.util.List;

public class CDTListDialog {

    private Context context;

    public CDTListDialog(Context context) {
        this.context = context;
    }

    public void showDialog(String title, List<CardDefinition> cardDefinitions, Listener listener) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        // add a list
        String[] data = new String[cardDefinitions.size()];
        for (int i = 0; i < cardDefinitions.size(); i++) {
            data[i] = cardDefinitions.get(i).getCardLabel();
        }

        builder.setItems(data, (dialog, index) -> listener.onCDTClicked(cardDefinitions.get(index)));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    public interface Listener{
        void onCDTClicked(CardDefinition cardDefinition);
    }

}
