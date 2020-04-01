package com.boot.imageframe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.boot.imageframecore.imageframe.ImageFrameCustomView;
import com.boot.imageframecore.imageframe.ImageFrameHandler;

public class MainActivity extends AppCompatActivity {

    private ImageFrameHandler build;
    private ImageFrameCustomView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.img);
        TypedArray typedArray = getResources().obtainTypedArray(R.array.xplane);
        if (typedArray != null) {
            int len = typedArray.length();
            int[] resId = new int[len];
            for (int i = 0; i < len; i++) {
                resId[i] = typedArray.getResourceId(i, -1);
            }
            typedArray.recycle();
            build = new ImageFrameHandler.ResourceHandlerBuilder(getResources(), resId)
                    .setFps(80)
                    .setLoop(true)
                    .build();
            build.setOnImageLoaderListener(new ImageFrameHandler.OnImageLoadListener() {
                @Override
                public void onImageLoad(BitmapDrawable drawable) {
                    imageView.setImageDrawable(drawable);
                }

                @Override
                public void onPlayFinish() {
                    Log.d("log", "onPlayFinish");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            });
            imageView.startImageFrame(build);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        build.start();

    }
}
