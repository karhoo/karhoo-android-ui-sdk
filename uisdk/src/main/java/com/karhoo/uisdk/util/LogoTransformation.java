package com.karhoo.uisdk.util;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

public class LogoTransformation implements Transformation {

    private final int radius;

    public LogoTransformation(final int radius) {
        this.radius = radius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        final int size = Math.min(source.getWidth(), source.getHeight());

        final int x = (source.getWidth() - size) / 2;
        final int y = (source.getHeight() - size) / 2;

        final Bitmap sourceBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (sourceBitmap != source) {
            source.recycle();
        }

        final Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();
        final BitmapShader shader = new BitmapShader(sourceBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        canvas.drawRoundRect(new RectF(0, 0, source.getWidth(), source.getHeight()), radius, radius, paint);

        sourceBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "rounded(radius=" + radius + ")";
    }
}
