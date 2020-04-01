package com.boot.imageframecore.imageframe;


public interface FrameBuild {
   FrameBuild setLoop(boolean loop);

   FrameBuild stop();

   FrameBuild clip();

   FrameBuild setStartIndex(int startIndex);

   FrameBuild setEndIndex(int endIndex);

   FrameBuild setWidth(int width);

   FrameBuild setHeight(int height);

   FrameBuild setFps(int fps);

   FrameBuild openLruCache(boolean isOpenCache);

   FrameBuild setOnImageLoaderListener(ImageFrameHandler.OnImageLoadListener onPlayFinish);

  ImageFrameHandler build();
}
