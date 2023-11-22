package com.epic.pos.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.epic.pos.R;
import com.epic.pos.databinding.CustomDialogFragmentBinding;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * CustomDialogFragment
 *
 * @author Sameera Jayarathna.
 * @version 1.0
 * @since 13 May, 2021
 */
public class CustomDialogFragment extends DialogFragment {

    private CustomDialogFragmentBinding binding;

    // required parameters
    private String title;
    private String message;

    // optional parameters
    private String leftBtnText;
    private OnClickListener onLeftClickListener;
    private String rightBtnText;
    private OnClickListener onRightClickListener;

    public CustomDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static CustomDialogFragment newInstance(
            String title,
            String message,
            String leftBtnText,
            OnClickListener onLeftClickListener,
            String rightBtnText,
            OnClickListener onRightClickListener) {
        CustomDialogFragment frag = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("leftBtnText", leftBtnText);
        args.putString("rightBtnText", rightBtnText);
        frag.setArguments(args);
        frag.setOnLeftClickListener(onLeftClickListener);
        frag.setOnRightClickListener(onRightClickListener);
        return frag;
    }

    public void setOnLeftClickListener(OnClickListener onLeftClickListener) {
        this.onLeftClickListener = onLeftClickListener;
    }

    public void setOnRightClickListener(OnClickListener onRightClickListener) {
        this.onRightClickListener = onRightClickListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pick a style based on the num.
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogTheme);
        if (getArguments() != null) {
            title = getArguments().getString("title");
            message = getArguments().getString("message");
            leftBtnText = getArguments().getString("leftBtnText");
            rightBtnText = getArguments().getString("rightBtnText");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.custom_dialog_fragment, container, false);
        View view = binding.getRoot();
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        binding.tvTitle.setText(title);
        binding.tvMessage.setText(message);
        binding.btnLeft.setText(leftBtnText);
        binding.btnRight.setText(rightBtnText);

        if (leftBtnText == null || leftBtnText.equals(""))
            binding.btnLeft.setVisibility(View.GONE);
        if (rightBtnText == null || rightBtnText.equals(""))
            binding.btnRight.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_left)
    void onClickLeft(View view) {
        onLeftClickListener.onClick();
    }

    @OnClick(R.id.btn_right)
    void onClickRight(View view) {
        onRightClickListener.onClick();
    }

    //Builder Class
    public static class CustomDialogBuilder {

        // required parameters
        private String title;
        private String message;

        // optional parameters
        private String leftBtnText;
        private OnClickListener onLeftClickListener;
        private String rightBtnText;
        private OnClickListener onRightClickListener;

        public CustomDialogBuilder(String title, String message) {
            this.title = title;
            this.message = message;
        }

        public CustomDialogBuilder setLeftBtnText(String leftBtnText, OnClickListener onClickListener) {
            this.leftBtnText = leftBtnText;
            this.onLeftClickListener = onClickListener;
            return this;
        }

        public CustomDialogBuilder setRightBtnText(String rightBtnText, OnClickListener onClickListener) {
            this.rightBtnText = rightBtnText;
            this.onRightClickListener = onClickListener;
            return this;
        }

        public CustomDialogFragment build() {
            return CustomDialogFragment.newInstance(
                    title,
                    message,
                    leftBtnText,
                    onLeftClickListener,
                    rightBtnText,
                    onRightClickListener);
        }

    }

    public interface OnClickListener {
        void onClick();
    }
}


