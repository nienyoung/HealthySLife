package com.example.admin.healthyslife_android.myWidget;
import android.content.Context;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;

public class MarqueTextView extends android.support.v7.widget.AppCompatTextView {

    public MarqueTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MarqueTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueTextView(Context context) {
        super(context);
    }

    @Override

    public boolean isFocused() {
        return true;
    }
}