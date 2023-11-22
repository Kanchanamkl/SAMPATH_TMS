package com.epic.pos.util;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class InvoiceNumberTextWatcher implements TextWatcher {

    private EditText editText;
    private int length;
    private TextWatcher textWatcher;

    public InvoiceNumberTextWatcher(EditText et, int length) {
        this.editText = et;
        this.length = length;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!s.toString().equals("")) {
            editText.removeTextChangedListener(this);

            int val = Integer.parseInt(s.toString());

            if (String.valueOf(val).length() <= length) {
                String invoiceNo = ValidatorUtil.getInstance().zeroPadString(String.valueOf(val), length);
                editText.setText(invoiceNo);
                editText.setSelection(invoiceNo.length());
            } else {
                editText.setText(s.subSequence(0, s.length() - 1).toString());
                editText.setSelection(s.length() - 1);
            }

            editText.addTextChangedListener(this);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public InvoiceNumberTextWatcher addTextChangedListener(TextWatcher textWatcher) {
        this.textWatcher = textWatcher;
        return this;
    }
}