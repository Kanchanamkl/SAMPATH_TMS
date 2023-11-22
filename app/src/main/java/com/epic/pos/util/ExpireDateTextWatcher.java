package com.epic.pos.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


public class ExpireDateTextWatcher implements TextWatcher {

    private EditText editText;
    private TextWatcher textWatcher;

    public ExpireDateTextWatcher(EditText et) {
        this.editText = et;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int removed, int added) {
        if (!s.toString().isEmpty()){
            editText.removeTextChangedListener(this);

            if (start == 1 && (start + added) == 2 && !has(s, '/')) {
                editText.setText(s + "/");
            }else if (start == 2 && (start + added == 3) && !has(s,'/')){
                editText.setText(s.charAt(0) + "" + s.charAt(1) + "/" + s.charAt(2));
            } else if (start == 3 && (start - removed == 2) && has(s, '/')) {
                editText.setText(s.toString().replace("/", ""));
            }

            editText.setSelection(editText.getText().length());

            editText.addTextChangedListener(this);
        }
    }

    private boolean has(CharSequence s, char c) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    public ExpireDateTextWatcher addTextChangedListener(TextWatcher textWatcher) {
        this.textWatcher = textWatcher;
        return this;
    }
}