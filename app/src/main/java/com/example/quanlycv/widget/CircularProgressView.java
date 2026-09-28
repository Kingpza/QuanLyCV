package com.example.quanlycv.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.quanlycv.R;

public class CircularProgressView extends View {

    private int progress = 75; // 0 to 100
    private Paint trackPaint;
    private Paint progressPaint;
    private Paint dotPaint;
    private Paint textPaint;
    private RectF circleBounds;

    private int trackColor;
    private int progressColor;
    private int textColor;
    private final float strokeWidthDp = 6f;

    public CircularProgressView(Context context) {
        super(context);
        init(context);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        trackColor = ContextCompat.getColor(context, R.color.divider);
        progressColor = ContextCompat.getColor(context, R.color.primary);
        textColor = ContextCompat.getColor(context, R.color.primary);

        float density = getResources().getDisplayMetrics().density;
        float strokeWidthPx = strokeWidthDp * density;

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidthPx);
        trackPaint.setColor(trackColor);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidthPx);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(progressColor);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(progressColor);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        circleBounds = new RectF();
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        invalidate();
    }

    public void setColors(int progressColor, int trackColor, int textColor) {
        this.progressColor = progressColor;
        this.trackColor = trackColor;
        this.textColor = textColor;

        progressPaint.setColor(progressColor);
        dotPaint.setColor(progressColor);
        trackPaint.setColor(trackColor);
        textPaint.setColor(textColor);
        invalidate();
    }

    @Override
    protected void onDraw(@androidx.annotation.NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float density = getResources().getDisplayMetrics().density;
        float strokeWidthPx = strokeWidthDp * density;
        float halfStroke = strokeWidthPx / 2f;

        float cx = width / 2f;
        float cy = height / 2f;
        float radius = Math.min(width, height) / 2f - strokeWidthPx;

        if (radius <= 0) return;

        circleBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Draw background track ring
        canvas.drawCircle(cx, cy, radius, trackPaint);

        // Draw progress arc
        float sweepAngle = 360f * (progress / 100f);
        if (sweepAngle > 0) {
            canvas.drawArc(circleBounds, -90, sweepAngle, false, progressPaint);

            // Draw dot at tip of arc
            double angleRad = Math.toRadians(-90 + sweepAngle);
            float dotX = (float) (cx + radius * Math.cos(angleRad));
            float dotY = (float) (cy + radius * Math.sin(angleRad));
            canvas.drawCircle(dotX, dotY, halfStroke * 1.5f, dotPaint);
        }

        // Draw percentage text
        float textSize = radius * 0.65f;
        textPaint.setTextSize(textSize);

        String text = progress + "%";
        // Vertically center text
        float textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(text, cx, textY, textPaint);
    }
}
