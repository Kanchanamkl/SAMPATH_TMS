package com.epic.pos.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.epic.pos.R;
import com.epic.pos.data.DccData;

import java.text.DecimalFormat;
import java.util.List;

public class DCCSelectionDialog {

    private Context context;
    LinearLayout dcclayout;
    public DCCSelectionDialog(Context context) {
        this.context = context;
    }

    public void showDialogDCC(List<DccData> dccData, Listenerdcc listener) {


        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dccui, null);

        dcclayout =promptsView.findViewById(R.id.laydccselection);


        final TextView txtexchangerate = promptsView.findViewById(R.id.txtexchange);
        final TextView txtmarkupvalue = promptsView.findViewById(R.id.txtmarkupvalue);
        txtexchangerate.setText("Exchange Rate: ");
        txtmarkupvalue.setText("Mark-Up Value: ");

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(promptsView);
       // builder.setTitle(title);

        // add a list
        String[] data = new String[dccData.size()];


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i=0; i < dccData.size(); i++) {
            Log.d("DCC DATA ",i+" - "+ dccData.get(i).getCurrancysymbol() +" - "+dccData.get(i).getDccamount() );
            data[i] =dccData.get(i).getCurrancysymbol()+" - "+dccData.get(i).getDccamount();

            View to_add = inflater.inflate(R.layout.dcccurrancyui,dcclayout,false);
            TextView amtdcc = (TextView) to_add.findViewById(R.id.txtamountdcc);
            TextView currencydcc = (TextView) to_add.findViewById(R.id.txtcurrancydcc);
            ImageView curimage = to_add.findViewById(R.id.imgcurrancyimage);



            int imageResource = context.getResources().getIdentifier( "@drawable/"+dccData.get(i).getCurrancysymbol().toLowerCase(), null, context.getPackageName() );

            if(imageResource==0){
                imageResource = context.getResources().getIdentifier( "@drawable/"+"noimage", null, context.getPackageName() );
            }
            Drawable res = context.getResources().getDrawable(imageResource);
            curimage.setImageDrawable(res);



            long dccrate = Long.parseLong(dccData.get(i).getDccamount().replace(".","").replace(",",""));
            Double cal = Double.valueOf(dccrate/1000000000);



            String ba = dccData.get(i).getBaseamount().replace(".","").replace(",","");

            double baseamount = Long.parseLong(ba)/100;

            Log.d("DCC_baseamount : ", String.valueOf(baseamount));
            Log.d("DCC_DCCRate : ", String.valueOf(cal));

            String dccamout=  String.valueOf(cal * baseamount);
            amtdcc.setText(dccamout);


            currencydcc.setText(dccData.get(i).getCurrancysymbol());

            dcclayout.addView(to_add);

            int finalI = i;
            to_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onDCCClicked(dccData.get(finalI));
                }
            });

        }

//
//        builder.setItems(data, (dialog, index) -> {
//            listener.onDCCClicked(dccData.get(index));
//        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
      //  dialog.getWindow().setLayout(500, 750);

    }

    public interface Listenerdcc{
        void onDCCClicked(DccData cardDefinition);
    }
}
