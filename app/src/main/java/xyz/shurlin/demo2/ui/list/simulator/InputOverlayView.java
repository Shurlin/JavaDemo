package xyz.shurlin.demo2.ui.list.simulator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.function.Consumer;

import xyz.shurlin.demo2.R;

public class InputOverlayView extends FrameLayout {

    private TextView hint;
    private EditText input;
    private ImageButton confirm;

    public InputOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_input_overlay, this);

        hint = findViewById(R.id.hint);
        input = findViewById(R.id.input);
        confirm = findViewById(R.id.confirm);
    }

    public void show(String hintText, Consumer<String> onConfirm) {
        setVisibility(VISIBLE);
        hint.setText(hintText);
        input.setText("");

        confirm.setOnClickListener(v -> {
            setVisibility(GONE);
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(input.getWindowToken(), 0);
            onConfirm.accept(input.getText().toString());
        });
    }

}
