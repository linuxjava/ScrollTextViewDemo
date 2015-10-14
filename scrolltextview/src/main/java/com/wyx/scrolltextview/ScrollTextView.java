package com.wyx.scrolltextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by winney on 15/10/13.
 */

public class ScrollTextView extends View {
    private String currentText = "-";
    private String nextText = "";
    private static final int DEVIDER = 0;

    private boolean animating;

    private final TextPaint paint;

    private final Rect maxBound;

    private int curTextX = 0;
    private int nextTextX = 0;
    private int textY = 0;
    private final Rect clipRect;

    private float offsetY = 0;
    private final float VELOCITY = 80;

    private final ArrayList<Integer> changeNumIndex = new ArrayList<Integer>();

    private int diotWidth = 0;
    private int charWidth = 0;
    private int chineseWidth = 0;

    private int curCharWidth = 0;
    private int nextCharWidth = 0;

    public ScrollTextView(Context context) {
        this(context, null);
    }

    public ScrollTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new TextPaint();
        maxBound = new Rect();
        clipRect = new Rect();

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScrollTextView);
        int textColor = array.getColor(R.styleable.ScrollTextView_textColor, Color.BLACK); // 提供默认值，放置未指定
        float textSize = array.getDimension(R.styleable.ScrollTextView_textSize, 136);
        int textStyle = array.getInt(R.styleable.ScrollTextView_textStyle, 0);

        setTextWithAnim(array.getString(R.styleable.ScrollTextView_text));
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, textStyle));
        paint.setAntiAlias(true);
        array.recycle();


        diotWidth = (int) paint.measureText(".");
        charWidth = (int) paint.measureText("1");
        chineseWidth = (int) paint.measureText("日");

    }


    public ScrollTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            if (isChineseChar(getMeasureText())) {
                result = getMeasureText().length() * chineseWidth;
            } else {
                result = getMeasureText().length() * charWidth;
            }

            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        return result;
    }


    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = paint.getFontMetricsInt(null);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumHeight());
        return result;
    }

    public static boolean isChineseChar(String str) {
        boolean temp = false;
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            temp = true;
        }
        return temp;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (changeNumIndex.size() == 0) {
            compareString();
            resize();
        }
        canvas.save();
        canvas.clipRect(clipRect);
        drawOrginChar(canvas);
        drawSwitchChar(canvas);
        canvas.restore();
    }

    public void setTextColor(int color) {
        paint.setColor(color);
    }


    private void drawOrginChar(Canvas canvas) {
        int tmp = curCharWidth + DEVIDER;
        int correctValue = 0;
        int indexDiot = currentText.indexOf('.');

        for (int i = 0; i < currentText.length(); i++) {
            if (changeNumIndex.contains(i)) {
                continue;
            }
            if (indexDiot != -1 && correctValue == 0 && i > indexDiot) {
                correctValue = charWidth - diotWidth;
            }
            canvas.drawText(currentText.charAt(i) + "", curTextX + tmp * i - correctValue, textY, paint);
        }
    }

    private void drawSwitchChar(Canvas canvas) {
        int curTX = curCharWidth + DEVIDER;
        int nextTx = nextCharWidth + DEVIDER;
        int correctValue = 0;
        int curCorrectValue = 0;

        int indexDiot = nextText.indexOf('.');
        int currentIndexDiot = currentText.indexOf('.');

        for (int i = 0; i < changeNumIndex.size(); i++) {
            int index = changeNumIndex.get(i);

            if (indexDiot != -1 && correctValue == 0 && index > indexDiot) {
                correctValue = charWidth - diotWidth;
            }
            if (currentIndexDiot != -1 && curCorrectValue == 0 && index > currentIndexDiot) {
                curCorrectValue = charWidth - diotWidth;
            }
            if (index < nextText.length()) {
                canvas.drawText(nextText.charAt(index) + "", nextTextX + nextTx * index - correctValue, textY + maxBound.height() - offsetY, paint);
            }
            if (index < currentText.length()) {
                canvas.drawText(currentText.charAt(index) + "", curTextX + curTX * index - curCorrectValue, textY - offsetY, paint);
            }

        }
    }

    private void compareString() {
        if (currentText.length() == nextText.length()) {
            for (int i = 0; i < currentText.length(); i++) {
                if (currentText.charAt(i) != nextText.charAt(i)) {
                    changeNumIndex.add(i);
                }
            }
        } else {
            for (int i = 0; i < getMeasureText().length(); i++) {
                changeNumIndex.add(i);
            }
        }
    }

    public String getMeasureText() {
        return currentText.length() > nextText.length() ? currentText : nextText;
    }

    public String getText() {
        return currentText;
    }

    public void setTextWithAnim(String nextText) {
        this.nextText = nextText;
        if (isChineseChar(nextText)) {
            nextCharWidth = chineseWidth;
        } else {
            nextCharWidth = charWidth;
        }
        resize();
        startAnimation();
    }

    public void setText(String nextText) {
        this.currentText = nextText;
        this.nextText = "";
        if (isChineseChar(nextText)) {
            nextCharWidth = chineseWidth;
        } else {
            nextCharWidth = charWidth;
        }
        resize();
        invalidate();
    }


    private void resize() {
        resetPosition();
    }

    private void resetPosition() {
        curTextX = getWidth() / 2 - (currentText.length() * curCharWidth) / 2;
        nextTextX = getWidth() / 2 - (nextText.length() * nextCharWidth) / 2;

        paint.getTextBounds(getMeasureText(), 0, getMeasureText().length(), maxBound);

        textY = getHeight() / 2 + maxBound.height() / 2;
        clipRect.left = Math.min(curTextX, nextTextX);
        clipRect.right = curTextX + (DEVIDER + Math.max(curCharWidth, nextCharWidth)) * getMeasureText().length();
        clipRect.top = textY - maxBound.height();
        clipRect.bottom = textY + 10;

    }

    SwitchAnimation animation = new SwitchAnimation();

    public void startAnimation() {
        animating = true;
        post(animation);
    }

    int test = 100;

    private void stopAnimation() {
        animating = false;
        currentText = nextText;
        offsetY = 0;
        changeNumIndex.clear();
        curCharWidth = nextCharWidth;
// testFun();
        FrameAnimationController.removeAnimation();

    }

    private void testFun() {
        currentText = test + "";
        test *= 2;
        nextText = test + "";
        if (test < 0) {
            test = 100;
        }
        startAnimation();
    }

    private final class SwitchAnimation implements Runnable {

        @Override
        public void run() {
            if (!animating) {
                return;
            }
            doAnimation();
            FrameAnimationController.requestAnimationFrame(this);
        }

    }

    private void doAnimation() {
        offsetY += VELOCITY * FrameAnimationController.ANIMATION_FRAME_DURATION / 1000;
        if (offsetY <= 0) {
            stopAnimation();
        } else if (offsetY >= maxBound.height()) {
            stopAnimation();
        }
        moveView(offsetY);
    }

    private void moveView(float position) {
        invalidate();
    }


}