package com.waterfaity.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioSeekBar extends View {
    private final float density;
    private int mTextSize;
    private int lineHeight;
    private long totalLen = 60;
    private float ratio = 0.3F;
    private int circleRadius;
    private int circleRadiusLittle;
    private float padding;
    private OnProgressListener onProgressListener;
    private Paint paint;
    private Paint textPaint;
    private int colorTheme;
    private int colorTextNormal;
    private int colorLineNormal;
    private int marginLine;
    private SimpleDateFormat simpleDateFormat;

    public AudioSeekBar(Context context) {
        this(context, null);
    }

    public AudioSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        initData();
        initColor();
        initPaint();
    }

    private void initColor() {
        colorTheme = Color.parseColor("#237ae5");
        colorTextNormal = Color.parseColor("#cccccc");
        colorLineNormal = Color.parseColor("#55333333");
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineHeight);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(mTextSize);
    }

    private void initData() {
        lineHeight = (int) (density * 2 / 3);
        mTextSize = (int) (10 * density);
        padding = 16 * density;
        marginLine = (int) (density * 8);
        circleRadius = (int) (6F * density);
        circleRadiusLittle = (int) (3.25F * density);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        if (width != 0 && height != 0 && totalLen != 0) {
            int half = height / 2;
            float left = getPaddingLeft() + padding;
            float right = getWidth() - getPaddingRight() - padding;
            float realWidth = right - left;
            if (realWidth > 0) {
                float currentX = left + realWidth * ratio;
                float currentTextEndX = drawCurrentText(canvas, left, half, right, currentX);
                drawTotalText(canvas, left, half, right, currentX, currentTextEndX);
                drawLine(canvas, left, half, right, currentX);
                drawCircle(canvas, left, half, right, currentX);
            }
        }
    }

    private void drawCircle(Canvas canvas, float left, int half, float right, float currentX) {

        float startX = 0;
        if (currentX < left) {
            startX = left;
        } else if (currentX > right) {
            startX = right;
        } else {
            startX = currentX;
        }
        int dRadius = circleRadius - circleRadiusLittle;
        paint.setStyle(Paint.Style.STROKE);
        if (dRadius > 0) {
            for (int i = 0; i <= dRadius; i++) {
                float alpha = i / (float) dRadius;
                int argb = Color.argb((int) (alpha * 50), Color.red(colorTheme), Color.green(colorTheme), Color.blue(colorTheme));
                paint.setColor(argb);
                canvas.drawCircle(startX, half, circleRadius - i, paint);
            }
        }
        paint.setColor(colorTheme);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(startX, half, circleRadiusLittle, paint);
    }

    private void drawLine(Canvas canvas, float left, int half, float right, float currentX) {
        //下
        paint.setColor(colorLineNormal);
        canvas.drawLine(left, half, right, half, paint);
        //上
        paint.setColor(colorTheme);
        canvas.drawLine(left, half, currentX, half, paint);
    }

    private float drawCurrentText(Canvas canvas, float left, int half, float right, float currentX) {
        String current = getTimeString((long) (totalLen * ratio));
        Rect textRect = getTextRect(current);
        textPaint.setColor(colorTheme);
        int widthHalf = textRect.width() / 2;
        float startX = 0;
        if (currentX < widthHalf) {
            startX = widthHalf + left;
        } else if (currentX > right) {
            startX = right - widthHalf;
        } else {
            startX = currentX - widthHalf;
        }
        canvas.drawText(current, startX, half - marginLine, textPaint);
        return startX + textRect.width();
    }

    private void drawTotalText(Canvas canvas, float left, int half, float right, float currentX, float currentTextEndX) {
        String total = getTimeString(totalLen);
        Rect textRect = getTextRect(total);
        textPaint.setColor(colorTextNormal);
        float startX = right - textRect.width();
        float startY = 0;
        if (startX < currentTextEndX) {
            startY = half + marginLine + marginLine;
        } else {
            startY = half - marginLine;
        }
        canvas.drawText(total, startX, startY, textPaint);
    }

    public void setTotal(long totalLen) {
        this.totalLen = totalLen;
    }

    private String getTimeString(long time) {
        if (simpleDateFormat == null)
            simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(new Date(time));

//        int value = 1000;
//        if (time < 60 * value) {
//            return "00:" + (time < 10 * value ? ("0" + time) : time + "");
//        } else if (time < 60 * 10 * value) {
//            long l = time % (60 * value);
//            String temp = l < 10 * value ? ("0" + l) : l + "";
//            return "0" + (time / (60 * value)) + ":" + temp;
//        } else {
//            long l = time % (60 * value);
//            String temp = l < 10 * value ? ("0" + l) : l + "";
//            return (time / (60 * value)) + ":" + temp;
//        }
    }

    private Rect getTextRect(String text) {
        if (TextUtils.isEmpty(text)) return new Rect();
        Paint paint = new Paint();
        paint.setTextSize(mTextSize);
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    public void setCurrentLen(long currentLen) {
        ratio = totalLen == 0 ? 0 : currentLen / (float) totalLen;
        if (!isTouching)
            invalidate();
    }

    private boolean isTouching;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            isTouching = true;
            ratio = (((event.getX() - getPaddingLeft() - padding)) / (getWidth() - 2 * padding - getPaddingLeft() - getPaddingRight()));
            if (ratio < 0) ratio = 0;
            else if (ratio > 1) ratio = 1;
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            isTouching = false;
            if (onProgressListener != null) onProgressListener.onSeekTo((long) (totalLen * ratio));
        }
        return true;
    }

    public long getCurrentLen() {
        return (long) (totalLen * ratio);
    }

    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public interface OnProgressListener {
        void onSeekTo(long current);
    }
}
