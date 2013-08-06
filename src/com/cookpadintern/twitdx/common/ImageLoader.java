/*
author:huydx
github:https://github.com/huydx
 */
package com.cookpadintern.twitdx.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.cookpadintern.twitdx.R;
import com.cookpadintern.twitdx.util.CloseableUtils;

public class ImageLoader {
    private static final String TAG = ImageLoader.class.getSimpleName();
    private final int stubId = R.drawable.no_image;

    private MemoryCache mMemoryCache = new MemoryCache();
    private FileCache mFileCache;
    private Map<ImageView, String> mImageViews =
            Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private ExecutorService mExecutorService;

    public ImageLoader(Context context){
        mFileCache = new FileCache(context);
        mExecutorService = Executors.newFixedThreadPool(5);
    }

    public void DisplayImage(String url, ImageView imageView) {
        mImageViews.put(imageView, url);
        Bitmap bitmap = mMemoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            queuePhoto(url, imageView);
            imageView.setImageResource(stubId);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad photo = new PhotoToLoad(url, imageView);
        mExecutorService.submit(new PhotosLoader(photo));
    }

    private Bitmap getBitmap(String url) {
        //from SD cache
        Bitmap bitmap = getBitmapFromFile(url);
        if (bitmap != null) return bitmap;

        //from web
        try {
            return getBitmapFromNetwork(url);
        } catch (IOException e) {
            Log.d(TAG, "An error occurred while fetching image from network", e);
            return null;
        }
    }

    public static void CopyStream(InputStream is, OutputStream os) throws IOException {
        final int bufferSize = 1024;
        byte[] bytes = new byte[bufferSize];
        while (true) {
            int count = is.read(bytes, 0, bufferSize);
            if (count == -1)
                break;
            os.write(bytes, 0, count);
        }
    }

    private Bitmap getBitmapFromFile(String url) {
        File file = mFileCache.getFile(url);
        return decodeFile(file);
    }

    private Bitmap getBitmapFromNetwork(String url) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);

            is = conn.getInputStream();
            File file = mFileCache.getFile(url);
            os = new FileOutputStream(file);
            CopyStream(is, os);

            return decodeFile(file);
        } catch (IOException e) {
            CloseableUtils.close(is);
            CloseableUtils.close(os);
            throw e; // rethrow exception
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File file) {
        try {
            //decode image size
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, bitmapOptions);

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70;
            int width = bitmapOptions.outWidth;
            int height = bitmapOptions.outHeight;
            int scale = 1;
            while (true) {
                if ((width / 2) < REQUIRED_SIZE || (height / 2) < REQUIRED_SIZE) break;

                width /= 2;
                height /= 2;
                scale *= 2;
            }

            //decode with inSampleSize
            BitmapFactory.Options sampleBitmapOptions = new BitmapFactory.Options();
            sampleBitmapOptions.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, sampleBitmapOptions);
        } catch (FileNotFoundException e) {}
        return null;
    }

    //Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad)) return;

            Bitmap bitmap = getBitmap(photoToLoad.url);
            mMemoryCache.put(photoToLoad.url, bitmap);
            if (imageViewReused(photoToLoad)) return;

            BitmapDisplayer bd = new BitmapDisplayer(bitmap, photoToLoad);
            Activity activity = (Activity) photoToLoad.imageView.getContext();
            activity.runOnUiThread(bd);
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag = mImageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url)) {
            return true;
        } else {
            return false;
        }
    }

    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photo;

        public BitmapDisplayer(Bitmap bitmap, PhotoToLoad photo) {
            this.bitmap = bitmap;
            this.photo = photo;
        }

        public void run() {
            if (imageViewReused(photo)) return;

            if (bitmap != null) {
                photo.imageView.setImageBitmap(bitmap);
            } else {
                photo.imageView.setImageResource(stubId);
            }
        }
    }

    public void clearCache() {
        mMemoryCache.clear();
        mFileCache.clear();
    }
}

