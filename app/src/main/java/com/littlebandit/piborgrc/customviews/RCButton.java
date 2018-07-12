package com.littlebandit.piborgrc.customviews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.littlebandit.piborgrc.R;

/**
 * @author J414NP3R
 */

public class RCButton extends AppCompatImageButton
{
    public RCButton(Context context) {
        this(context, null);
    }

    public RCButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RCButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int padding = (int)getResources().getDimension(R.dimen.button_padding);

        ShapeDrawable sdOval = new ShapeDrawable(new OvalShape());
        sdOval.getPaint().setColor(Color.parseColor("#33FFFFFF"));
        setBackgroundDrawable(sdOval);
        setPadding(padding,padding,padding,padding);
    }
}
