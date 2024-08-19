package com.jaianper.piborgrc.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

/**
 * @author Jaider Pechene
 * @version 1.0
 */

// A simple SurfaceView whose width and height can be set from the outside
public class GStreamerSurfaceView extends SurfaceView
{
    public int media_width = 1280;
    public int media_height = 720;

    // Mandatory constructors, they do not do much
    public GStreamerSurfaceView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public GStreamerSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public GStreamerSurfaceView (Context context)
    {
        super(context);
    }

    // Called by the layout manager to find out our size and give us some rules.
    // We will try to maximize our size, and preserve the media's aspect ratio if
    // we are given the freedom to do so.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = 0, height = 0;
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        Log.i ("GStreamer", "onMeasure called with " + media_width + "x" + media_height);
        // Obey width rules
        switch (wMode)
        {
            case MeasureSpec.AT_MOST:
                if (hMode == MeasureSpec.EXACTLY)
                {
                    width = Math.min(hSize * media_width / media_height, wSize);
                    break;
                }
            case MeasureSpec.EXACTLY:
                width = wSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                width = media_width;
        }

        // Obey height rules
        switch (hMode)
        {
            case MeasureSpec.AT_MOST:
                if (wMode == MeasureSpec.EXACTLY)
                {
                    height = Math.min(wSize * media_height / media_width, hSize);
                    break;
                }
            case MeasureSpec.EXACTLY:
                height = hSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                height = media_height;
        }

        // Finally, calculate best size when both axis are free
        if (hMode == MeasureSpec.AT_MOST && wMode == MeasureSpec.AT_MOST)
        {
            int correct_height = width * media_height / media_width;
            int correct_width = height * media_width / media_height;

            if (correct_height < height)
                height = correct_height;
            else
                width = correct_width;
        }

        // Obey minimum size
        width = Math.max (getSuggestedMinimumWidth(), width);
        height = Math.max (getSuggestedMinimumHeight(), height);
        setMeasuredDimension(width, height);
    }
}