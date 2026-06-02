package com.example.shoeapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class FakeQrCodeView extends View {
    private static final int[][] PATTERN = {
            {1,1,1,1,1,1,1,0,1,0,1,0,1,0,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,1,0,0,1,0,1,0,0,1,0,0,0,0,0,1},
            {1,0,1,1,1,0,1,0,1,0,1,0,1,0,1,0,1,1,1,0,1},
            {1,0,1,1,1,0,1,0,0,1,0,1,1,0,1,0,1,1,1,0,1},
            {1,0,1,1,1,0,1,0,1,0,1,0,0,0,1,0,1,1,1,0,1},
            {1,0,0,0,0,0,1,0,0,1,0,1,0,0,1,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,0,1,0,1,0,1,0,1,1,1,1,1,1,1},
            {0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0},
            {1,0,1,1,0,1,1,1,0,0,1,0,1,1,0,1,1,0,1,1,0},
            {0,1,0,0,1,0,0,0,1,0,0,1,0,0,1,0,0,1,0,0,1},
            {1,0,1,0,1,1,0,1,0,1,0,0,1,0,0,1,1,0,1,0,1},
            {0,1,0,1,0,0,0,0,1,0,1,0,0,1,0,0,0,1,0,1,0},
            {1,0,0,0,1,1,1,1,0,1,0,1,1,0,1,1,0,0,0,1,1},
            {0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,1,0,0},
            {1,1,1,1,1,1,1,0,0,1,1,0,1,0,1,0,1,1,0,1,0},
            {1,0,0,0,0,0,1,0,1,0,0,1,0,1,0,1,0,0,1,0,1},
            {1,0,1,1,1,0,1,0,0,1,1,0,1,0,1,0,1,0,0,1,0},
            {1,0,1,1,1,0,1,0,1,0,0,1,0,1,0,1,0,1,0,0,1},
            {1,0,1,1,1,0,1,0,0,1,1,0,1,0,1,0,1,0,1,1,0},
            {1,0,0,0,0,0,1,0,1,0,0,1,0,1,0,1,0,0,0,1,0},
            {1,1,1,1,1,1,1,0,0,1,1,0,1,0,1,0,1,1,1,0,1}
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FakeQrCodeView(Context context) {
        super(context);
    }

    public FakeQrCodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        float left = (getWidth() - size) / 2f;
        float top = (getHeight() - size) / 2f;
        float cell = size / PATTERN.length;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(left, top, left + size, top + size, paint);

        paint.setColor(Color.rgb(10, 10, 10));
        paint.setAntiAlias(false);
        for (int y = 0; y < PATTERN.length; y++) {
            for (int x = 0; x < PATTERN[y].length; x++) {
                if (PATTERN[y][x] == 1) {
                    canvas.drawRect(left + x * cell, top + y * cell, left + (x + 1) * cell, top + (y + 1) * cell, paint);
                }
            }
        }

        paint.setAntiAlias(true);
        float logoSize = size * 0.18f;
        float logoLeft = left + size / 2f - logoSize / 2f;
        float logoTop = top + size / 2f - logoSize / 2f;
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize), 8f, 8f, paint);
        paint.setColor(Color.rgb(255, 85, 0));
        canvas.drawRoundRect(new RectF(logoLeft + 4, logoTop + 4, logoLeft + logoSize - 4, logoTop + logoSize - 4), 6f, 6f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(logoSize * 0.45f);
        paint.setFakeBoldText(true);
        canvas.drawText("S", left + size / 2f, top + size / 2f + logoSize * 0.16f, paint);
        paint.setFakeBoldText(false);
    }
}
