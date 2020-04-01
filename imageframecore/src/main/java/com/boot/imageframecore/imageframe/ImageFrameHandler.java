package com.boot.imageframecore.imageframe;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;


import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import java.lang.ref.SoftReference;


public class ImageFrameHandler implements WorkHandler.WorkMessageProxy {

    private Resources resources;
    private int[] resArray;
    private int width=0;
    private int height=0;
    private boolean isRunning;
    private final WorkHandler workHandler;


    private static final int RES = 1;
    private int type;
    private boolean isOpenCache;


    ImageFrameHandler(int type) {
        this.type = type;
        imageCache = new ImageCache();
        workHandler = new WorkHandler();
    }

    private ImageCache imageCache;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (onImageLoadListener != null) {
                onImageLoadListener.onImageLoad(bitmapDrawable);
            }
            switch (msg.what) {
                case RES:
                    load((int[]) msg.obj);
                    break;
            }
        }
    };

    private volatile float frameTime;
    private volatile int index = 0;
    private volatile BitmapDrawable bitmapDrawable;
    private OnImageLoadListener onImageLoadListener;
    private boolean loop;



    private void setImageFrame(Resources resources, @RawRes int[] resArray, int width, int height,
                               int fps,
                               OnImageLoadListener onPlayFinish) {
        this.width = width;
        this.height = height;
        this.resources = resources;
        if (imageCache == null) {
            imageCache = new ImageCache();
        }
        this.onImageLoadListener = onPlayFinish;
        frameTime = 1000f / fps + 0.5f;
        workHandler.addMessageProxy(this);
        this.resArray = resArray;
    }

    /**
     * load frame form file resources Array;
     *
     * @param resArray     resources Array
     * @param fps          The number of broadcast images per second
     * @param onPlayFinish finish callback
     */
    @Deprecated
    public void loadImage(Resources resources, @RawRes int[] resArray, int fps,
                          OnImageLoadListener onPlayFinish) {
        if (!isRunning) {
            setImageFrame(resources, resArray, width, height, fps, onPlayFinish);
            load(resArray);
        }
    }


    /**
     * loop play frame
     *
     * @param loop true is loop
     */
    public ImageFrameHandler setLoop(boolean loop) {
        if (!isRunning) {
            this.loop = loop;
        }
        return this;
    }


    /**
     * stop play frame
     */
    public void stop() {
        loop=false;
        isRunning = false;
    }

    /**
     * 新增方法
     * 暂停
     * stop play frame
     */
    public void pause() {
        isRunning = false;
        workHandler.getHandler().removeCallbacksAndMessages(null);
        // workHandler.removeMessageProxy(this);
        handler.removeCallbacksAndMessages(null);
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            switch (type) {
                case RES:
                    load(resArray);
                    break;
            }
        }

    }

    private void load(@RawRes int[] res) {
        Message message = Message.obtain();
        message.obj = res;
        message.what = RES;
        workHandler.getHandler().sendMessage(message);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case RES:
                loadInThreadFromRes((int[]) msg.obj);
                break;
        }
    }

    private void loadInThreadFromRes(final int[] resIds) {
        if (index < resIds.length) {
            int resId = resIds[index];
            if (bitmapDrawable != null) {
                imageCache.mReusableBitmaps.add(new SoftReference<>(bitmapDrawable.getBitmap()));
            }
            long start = System.currentTimeMillis();
            bitmapDrawable = BitmapLoadUtils.decodeSampledBitmapFromRes(resources, resId, width,
                    height, imageCache, isOpenCache);
            long end = System.currentTimeMillis();
            float updateTime = (frameTime - (end - start)) > 0 ? (frameTime - (end - start)) : 0;
            Message message = Message.obtain();
            message.what = RES;
            message.obj = resIds;
            handler.sendMessageAtTime(message, index == 0 ? 0 : (int) (SystemClock.uptimeMillis() + updateTime));
            index++;
        } else {
            if (loop) {
                index = 0;
                loadInThreadFromRes(resIds);
            } else {
                index++;
                bitmapDrawable = null;
                frameTime = 0;
                if (onImageLoadListener != null) {
                    onImageLoadListener.onPlayFinish();
                }
                isRunning = false;
                onImageLoadListener = null;
                workHandler.getHandler().removeCallbacksAndMessages(null);
                workHandler.removeMessageProxy(this);
                handler.removeCallbacksAndMessages(null);
                handler=null;
                resources = null;
            }
        }
    }




    public void setFps(int fps) {
        frameTime = 1000f / fps + 0.5f;
    }

    public void setOnImageLoaderListener(OnImageLoadListener onPlayFinish) {
        this.onImageLoadListener = onPlayFinish;
    }


    public interface OnImageLoadListener {

        void onImageLoad(BitmapDrawable drawable);

        void onPlayFinish();
    }


    private void openLruCache(boolean isOpenCache) {
        this.isOpenCache = isOpenCache;
    }


    /**
     * 改造成build构建者模式
     */
    public static class ResourceHandlerBuilder implements FrameBuild {
        @NonNull
        private final Resources resources;
        private int width;
        private int height;
        private int fps = 30;
        private int[] resArray;
        private OnImageLoadListener onPlayFinish;
        private ImageFrameHandler imageFrameHandler;
        private int startIndex;
        private int endIndex;

        public ResourceHandlerBuilder(@NonNull Resources resources, @NonNull @RawRes int[] resArray) {
            if (resArray.length == 0) {
                throw new IllegalArgumentException("resArray is not empty");
            }
            this.resources = resources;
            this.resArray = resArray;
            createHandler();
        }

        @Override
        public FrameBuild setLoop(boolean loop) {
            imageFrameHandler.setLoop(loop);
            return this;
        }

        @Override
        public FrameBuild stop() {
            imageFrameHandler.stop();
            return this;
        }

        @Override
        public FrameBuild setStartIndex(int startIndex) {
            if (startIndex >= resArray.length) {
                throw new IllegalArgumentException("startIndex is not to big resArray length");
            }
            this.startIndex = startIndex;
            return this;
        }

        @Override
        public FrameBuild setEndIndex(int endIndex) {
            if (endIndex > resArray.length) {
                throw new IllegalArgumentException("endIndex is not  big to resArray length");
            }
            if (endIndex <= startIndex) {
                throw new IllegalArgumentException("endIndex is not to small startIndex");
            }
            this.endIndex = endIndex;
            return this;
        }

        @Override
        public FrameBuild clip() {
            if (startIndex >= 0 && endIndex > 0 && startIndex < endIndex) {
                resArray = split(resArray, startIndex, endIndex);
            }
            return this;
        }


        @Override
        public FrameBuild setWidth(int width) {
            this.width = width;
            return this;
        }

        @Override
        public FrameBuild setHeight(int height) {
            this.height = height;
            return this;
        }

        @Override
        public FrameBuild setFps(int fps) {
            this.fps = fps;
            imageFrameHandler.setFps(fps);// 这里有一个重复计算 后期想个更好的办法支持动态换
            return this;
        }

        @Override
        public FrameBuild openLruCache(boolean isOpenCache) {
            imageFrameHandler.openLruCache(isOpenCache);
            return this;
        }

        @Override
        public FrameBuild setOnImageLoaderListener(OnImageLoadListener onPlayFinish) {
            this.onPlayFinish = onPlayFinish;
            return this;
        }

        @Override
        public ImageFrameHandler build() {
            if (!imageFrameHandler.isRunning) {
                clip();
                imageFrameHandler.setImageFrame(resources, resArray, width, height, fps,
                        onPlayFinish);
            }
            return imageFrameHandler;
        }

        private void createHandler() {
            if (imageFrameHandler == null) {
                imageFrameHandler = new ImageFrameHandler(RES);
            }
        }

        int[] split(int[] resArray, int start, int end) {
            int[] ints = new int[end - start];
            int index = 0;
            for (int i = start; i < end; i++) {
                ints[index] = resArray[i];
                index++;
            }
            return ints;
        }
    }

}
