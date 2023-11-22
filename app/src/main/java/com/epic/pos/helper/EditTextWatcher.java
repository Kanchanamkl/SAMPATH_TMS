package com.epic.pos.helper;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class EditTextWatcher implements TextWatcher {

    private EditText editText;
    private int length;

    public EditTextWatcher(EditText et, int length) {
        this.editText = et;
        this.length = length;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int length = this.length;
        if (!s.toString().equals("")) {
            editText.removeTextChangedListener(this);

            int maxLength = 20;
            InputFilter[] fArray = new InputFilter[1];
            fArray[0] = new InputFilter.LengthFilter(maxLength);
            editText.setFilters(fArray);

            InputFilter[] FilterArray1 = new InputFilter[1];
            FilterArray1[0] = new InputFilter.LengthFilter(length);
            editText.setFilters(FilterArray1);

            if (s.toString().equals("0") || s.toString().equals("") || s.toString().equals("0.0") || s.toString().equals("")) {
                editText.setText("");
//                editText.setText("0.00");
//                editText.setSelection(4);
            } else if (s.toString().length() < length) {
                String cleanString = s.toString().replace(".", "").replace(",", "");
                String formatted;

                if ((cleanString.charAt(cleanString.length() - 1) == '0') && (cleanString.charAt(cleanString.length() - 2) == '0')) {
                    System.out.println("Format : " + cleanString);
                    double parsed = Double.parseDouble(cleanString);
                    NumberFormat numberFormat = new DecimalFormat("#,###.##");
                    formatted = numberFormat.format(parsed / 100) + ".00";
                } else if (cleanString.charAt(cleanString.length() - 1) == '0') {
                    double parsed = Double.parseDouble(cleanString);
                    NumberFormat numberFormat = new DecimalFormat("#,###.##");
                    formatted = numberFormat.format(parsed / 100) + "0";
                } else {
                    System.out.println("Format : " + cleanString);
                    double parsed = Double.parseDouble(cleanString);
                    NumberFormat numberFormat = new DecimalFormat("#,###.##");
                    formatted = numberFormat.format(parsed / 100);
                }
                editText.setText(formatted);
                editText.setSelection(formatted.length());
            } else {
                editText.setText(s.toString().substring(0, s.length() - 1));
                editText.setSelection(s.toString().length() - 1);
            }
            editText.addTextChangedListener(this);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
