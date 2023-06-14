package app.simple.inure.glide.transformation;

/**
 * Copyright (C) 2020 Wasabeef
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.google.android.renderscript.Toolkit;

import java.security.MessageDigest;

import androidx.annotation.NonNull;
import app.simple.inure.BuildConfig;
import app.simple.inure.glide.internals.FastBlur;

public class Blur extends BitmapTransformation {
    
    private static final int VERSION = BuildConfig.VERSION_CODE;
    private static final String ID = "app.simple.inure.glide.transformations.BlurTransformation." + VERSION;
    
    private static final int MAX_RADIUS = 25;
    private static final int DEFAULT_DOWN_SAMPLING = 1;
    
    private final int radius;
    private final int sampling;
    
    public Blur() {
        this(MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
    }
    
    public Blur(int radius) {
        this(radius, DEFAULT_DOWN_SAMPLING);
    }
    
    public Blur(int radius, int sampling) {
        this.radius = radius;
        this.sampling = sampling;
    }
    
    @Override
    protected Bitmap transform(@NonNull Context context, @NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        
        int width = toTransform.getWidth();
        int height = toTransform.getHeight();
        int scaledWidth = width / sampling;
        int scaledHeight = height / sampling;
        
        Bitmap bitmap = pool.get(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        
        setCanvasBitmapDensity(toTransform, bitmap);
        
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / (float) sampling, 1 / (float) sampling);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(toTransform, 0, 0, paint);
        
        try {
            bitmap = Toolkit.INSTANCE.blur(bitmap, radius);
        } catch (IllegalArgumentException e) {
            bitmap = FastBlur.blur(bitmap, radius, true);
        }
        
        return bitmap;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "BlurTransformation(radius=" + radius + ", sampling=" + sampling + ")";
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof Blur &&
                ((Blur) o).radius == radius &&
                ((Blur) o).sampling == sampling;
    }
    
    @Override
    public int hashCode() {
        return ID.hashCode() + radius * 1000 + sampling * 10;
    }
    
    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update((ID + radius + sampling).getBytes(CHARSET));
    }
}