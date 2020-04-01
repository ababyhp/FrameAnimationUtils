package com.boot.imageframecore.imageframe;


import android.content.Context;

import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;


public class ImageFrameCustomView extends AppCompatImageView {
    private ImageFrameHandler imageFrameHandler;

    public ImageFrameCustomView(Context context) {
        super(context);
    }

    public ImageFrameCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageFrameCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onDetachedFromWindow() {
        if (imageFrameHandler != null) {
            imageFrameHandler.stop();
        }
        super.onDetachedFromWindow();
    }

    public void startImageFrame(final ImageFrameHandler imageFrameHandler) {
        if (this.imageFrameHandler == null) {
            this.imageFrameHandler = imageFrameHandler;
        } else {
            this.imageFrameHandler.stop();
            this.imageFrameHandler = imageFrameHandler;
        }

        post(new Runnable() {
            @Override
            public void run() {
                imageFrameHandler.start();
            }
        });

    }

    @Nullable
    public ImageFrameHandler getImageFrameHandler() {
        return imageFrameHandler;
    }
}
