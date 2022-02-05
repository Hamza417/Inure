package app.simple.inure.terminal.shortcuts;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;

import static java.lang.Math.ceil;

public class TextIcon {
    ////////////////////////////////////////////////////////////
    public static Bitmap getTextIcon(String text, int color, int width, int height) {
        text = text.trim();
        String[] lines = text.split("\\s*\n\\s*");
        int nLines = lines.length;
        Rect R = new Rect();
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShadowLayer(2, 10, 10, 0xFF000000);
        p.setColor(color);
        p.setSubpixelText(true);
        p.setTextSize(256);
        p.setTextAlign(Align.CENTER);
        float[] HH = new float[nLines];
        float H = 0f;
        float W = 0f;
        for (int i = 0; i < nLines; ++i) {
            p.getTextBounds(lines[i], 0, lines[i].length(), R);
            float h = (float) Math.abs(R.top - R.bottom);
            float w = (float) Math.abs(R.right - R.left);
            if (nLines > 1) {
                h += 0.1f * h; // Add space between lines.
            }
            HH[i] = h;
            H += h;
            if (w > W) {
                W = w;
            }
        }
        float f = ((float) width) * H / ((float) height);
        int hBitmap;
        int wBitmap;
        if (W < f) {
            wBitmap = (int) ceil(f);
            hBitmap = (int) ceil(H);
        } else {
            wBitmap = (int) ceil(W);
            hBitmap = (int) ceil(height * wBitmap / (float) width);
        }
        
        Bitmap b = Bitmap.createBitmap(wBitmap, hBitmap, Config.ARGB_8888);
        b.setDensity(Bitmap.DENSITY_NONE);
        Canvas c = new Canvas(b);
        
        W = wBitmap / 2f;
        float top = hBitmap / 2f - H / 2f + HH[0] / 2f;
        for (int i = 0; i < nLines; ++i) {
            top += HH[i] / 2f;
            c.drawText(lines[i], W, top, p);
            top += HH[i] / 2f;
        }
        return (
                Bitmap.createScaledBitmap(
                        b
                        , width
                        , height
                        , true
                                         )
        );
    }
    ////////////////////////////////////////////////////////////
}
