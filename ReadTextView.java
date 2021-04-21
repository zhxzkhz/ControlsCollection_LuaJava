package com.xlua.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.icu.text.DecimalFormat;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.luajava.LuaException;
import com.luajava.LuaFunction;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;


public class ReadTextView extends View {

    //顶部标题间距
    float topSpace = 0f;
    //底部信息间距
    float bottomSpace = 0f;

    //设置文本大小
    private int textSize = 24;

    //开始绘制文字位置
    private int textStart = 0;

    //绘制文字结束位置
    private int textEnd = 0;

    //左右边缘间距
    private float marginSpacing = 15f;

    //字间距
    private float fontSpacing = 1f;

    //字间距倍率
    private float fontWidthRatio = 1.05f;

    //上下边缘间距
    private float lineSpacing = 15f;

    //行高
    private float lineHeight = 8f;

    //行高，倍率
    private float lineRatio = 1f;

    private Handler hd = new Handler();

    private int maxLine = 0;

    private String title = "";

    private String text = "";

    //每页索引
    @SuppressLint("UseSparseArrays")
    private LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();

    //每页显示最大行数
    private int pageMaxLine = 0;


    public int getStatusBar() {
        return statusBar;
    }

    public void setStatusBar(int statusBar) {
        this.statusBar = statusBar;
        invalidate();
    }

    //状态栏高度
    private int statusBar = 0;

    //绘制测试线判断
    private boolean Test = true;

    public boolean isTest() {
        return Test;
    }

    public void setTest(boolean test) {
        Test = test;
        invalidate();
    }


    public int getTextStart() {
        return textStart;
    }

    public int getTextEnd() {
        return textEnd;
    }

    public float getFontWidthRatio() {
        return fontWidthRatio;
    }

    public void setFontWidthRatio(float fontWidthRatio) {
        this.fontWidthRatio = fontWidthRatio;
        hd.removeCallbacks(run);
        hd.post(run);
    }

    public float getFontSpacing() {
        return fontSpacing;
    }

    public void setFontSpacing(float fontSpacing) {
        this.fontSpacing = fontSpacing;
        hd.removeCallbacks(run);
        hd.post(run);
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
        invalidate();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        textPaint.setTextSize(textSize);
        ttPaint.setTextSize((float) (Math.sqrt(Math.pow(textSize,2) / 2f )- textSize / 12f));
        hd.removeCallbacks(run);
        AnalyseTextLine();
        invalidate();
    }

    public float getMarginSpacing() {
        return marginSpacing;
    }

    //左右边缘间距
    public void setMarginSpacing(float marginSpacing) {
        this.marginSpacing = marginSpacing;
        hd.removeCallbacks(run);
        AnalyseTextLine();
        invalidate();
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
        invalidate();
    }

    public float getLineRatio() {
        return lineRatio;
    }

    public void setLineRatio(float lineRatio) {
        this.lineRatio = lineRatio;
        invalidate();
    }

    public String getText() {
        return text;
    }

    private Runnable run = ReadTextView.this::AnalyseTextLine;

    public void setText(String text) {
        textStart = 0;
        maxLine = 0;
        textEnd = textStart;
        if (!this.text.equals(text)) {
            this.text = text;
            hd.removeCallbacks(run);
            if (getWidth() > 0) {
                AnalyseTextLine();
                invalidate();
            } else {
                post(run);
                post(this::invalidate);
                //hd.postDelayed(run, 200);
            }
        }
        //invalidate();
    }

    //设置字体样式
    public boolean setFontStyle(String path) {
        ///storage/self/primary/Android/kitty.ttf
        File file = new File(path);
        if (!file.isFile()) {
            return false;
        }
        Typeface tf = Typeface.createFromFile(path);
        textPaint.setTypeface(tf);
        ttPaint.setTypeface(tf);
        hd.removeCallbacks(run);
        hd.post(run);
        return true;
    }

    //画笔
    private TextPaint textPaint;

    //标题底部画笔
    private TextPaint ttPaint;

    //测量线画笔
    private Paint pt;

    public ReadTextView(Context context) {
        this(context, null);
    }

    public ReadTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = getResources().getDisplayMetrics().density;
        textSize = (int) (textSize * density);
        pt = new Paint();
        pt.setStrokeWidth(1);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0);
        textPaint.setSubpixelText(true);
        textPaint.setTextSize(textSize);
        textPaint.density = density;

        ttPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        ttPaint.setColor(Color.BLACK);
        ttPaint.setStyle(Paint.Style.FILL);
        ttPaint.setStrokeWidth(0);
        ttPaint.setSubpixelText(true);
        // 字体大小是正文的 x ^ 2 / - x / 12
        ttPaint.setTextSize((float) (Math.sqrt(Math.pow(textSize,2) / 2f )- textSize / 12f));
        ttPaint.density = density;

    }

    public void DownPage(LuaFunction func) throws LuaException {
        if (textEnd >= text.length()) {
            if (func.call() !=null && !((boolean) func.call()))
            return;
        }
        textStart = textEnd;
        invalidate();
    }

    public void UpPage(LuaFunction func) throws LuaException {

        int posIndex = 0;
        //等于0代表是上一章，
        if (textStart == 0) {
            if ((boolean) func.call()) {
                //hd.removeCallbacks(run);
                //AnalyseTextLine();
                int posIndex1 = maxLine / pageMaxLine;
                //获取能显示完整行数的页面数
                posIndex = maxLine - posIndex1 * pageMaxLine;
                if (posIndex == 0) {
                    posIndex = maxLine - pageMaxLine + 1;
                } else {
                    posIndex = posIndex1 * pageMaxLine + 1;
                }
            }
        } else {
            //判断统计行数没，如果没有，则先统计
            if (maxLine < 1) {
                hd.removeCallbacks(run);
                AnalyseTextLine();
            }
            for (Map.Entry<Integer, Integer> value : map.entrySet()) {
                if (value.getValue() == textStart) {
                    posIndex = value.getKey() - pageMaxLine;
                    break;
                }
            }
        }
        //Log.i("页数", String.valueOf(posIndex) + " @ textStart -> " + textStart + " Array -> " + map);
        if (map.containsKey(posIndex)) {
            //noinspection ConstantConditions
            textStart = map.get(posIndex);
        } else {
            Toast.makeText(getContext(), "上一页加载失败", Toast.LENGTH_SHORT).show();
        }
        invalidate();
    }


    //用于测量一章有多少行
    public void AnalyseTextLine() {
        if (text.length() < 1) {
            return;
        }
        long times = System.currentTimeMillis();
        map.clear();

        //每页最大行数计算
        CheckPageMaxLine();

        //使用控件宽高
        float widthPixels = getWidth() - marginSpacing * 2;

        if (widthPixels < textSize * fontWidthRatio + fontSpacing) {
            return;
        }

        //使用临时变量存储绘制时的位置
        boolean bool = false;

        int tts = 0;

        int fontNumber;

        //行数索引
        int lineIndex = 0;

        //临时储存一个文字
        String textStr;

        while (true) {
            float minWidth = 0;

            boolean skip = false;

            int fontIndex = 0;

            while (minWidth < widthPixels) {
                fontIndex++;

                if (tts + fontIndex > text.length()) {
                    skip = true;
                    fontIndex--;
                    break;
                }

                textStr = text.substring(tts + fontIndex - 1, tts + fontIndex);
                minWidth = minWidth + textPaint.measureText(textStr) * fontWidthRatio + fontSpacing;


                if (minWidth > widthPixels)
                    fontIndex--;

                if (textStr.equals("\n")) {
                    minWidth = widthPixels;
                }
            }


            lineIndex++;

            //字体大小改变时会造成位置发生改变，这个用于转换位置
            if (tts > textStart && !bool && textStart > 0 && lineIndex % pageMaxLine == 1) {
                bool = true;
                textStart = tts;
                Log.i("textStart", String.valueOf(textStart));
            }
            map.put(lineIndex, tts);

            fontNumber = fontIndex;

            tts = tts + fontNumber;

            if (skip) {
                Log.i("计算时间", String.valueOf(System.currentTimeMillis() - times));
                break;
            }

        }
        maxLine = lineIndex;

        if (pageMaxLine >= maxLine)
            textStart = 0;

        //Log.i("最大行数", String.valueOf(maxLine));
    }


    private int getPageMaxLine() {
        return pageMaxLine;
    }


    private void CheckPageMaxLine() {
        //获取测量标准线
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        //字体最终高度
        //字体高
        float fontHeight = fm.descent - fm.ascent;

        // 字体高度 * 倍数 = 字体行间隔距离
        // 字体行间隔距离 + 行间隔 = 最终行间隔距离
        float finalHeight = fontHeight * lineRatio + lineHeight;

        float heightPixels = getHeight() - lineSpacing * 2 - topSpace - bottomSpace - statusBar;
        // 画布高度 ÷ 最终行间隔距离 = 实际行数
        pageMaxLine = (int) ((heightPixels) / (finalHeight));
        //return pageMaxLine;
    }

    @SuppressLint("NewApi")
    DecimalFormat format1 = new DecimalFormat("##%");

    @SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint.FontMetrics fm1 = ttPaint.getFontMetrics();
        float fontHeight1 = fm1.descent - fm1.ascent;

        topSpace = fontHeight1 * 1.4f;
        bottomSpace = fontHeight1 * 1.4f;

        canvas.drawText(title, marginSpacing * 1.8f, fontHeight1 * 1.2f + statusBar, ttPaint);

        //使用控件宽高
        float widthPixels = getWidth() - marginSpacing * 2;
        float heightPixels = getHeight() - lineSpacing * 2 - topSpace - bottomSpace - statusBar;
        //获取测量标准线
        Paint.FontMetrics fm = textPaint.getFontMetrics();

        //字体最终高度
        //字体高
        float fontHeight = fm.descent - fm.ascent;

        // 字体高度 * 倍数 = 字体行间隔距离
        // 字体行间隔距离 + 行间隔 = 最终行间隔距离
        float finalHeight = fontHeight * lineRatio + lineHeight;

        // 画布高度 ÷ 最终行间隔距离 = 实际行数
        pageMaxLine = (int) ((heightPixels) / (finalHeight));


        //字体、高度变化是更新行数
        if (textStart > 0) {
            for (Map.Entry<Integer, Integer> value : map.entrySet()) {
                if (value.getValue() == textStart) {
                    int line = value.getKey();
                    if (line <= pageMaxLine) {
                        textStart = 0;
                        break;
                    }
                    while ((line % pageMaxLine) != 1) {
                        line--;
                    }
                    //noinspection ConstantConditions
                    textStart = map.get(line);
                    break;
                }
            }
        }

        int fontNumber;
        //使用临时变量存储绘制时的位置
        int tts = textStart;

        //行数索引采用的一，所以行数减一
        //行间距平摊高度
        float lineHeights = (heightPixels - pageMaxLine * finalHeight + lineHeight + fontHeight * (lineRatio - 1)) / (pageMaxLine - 1);

        // 字体高度 + 行间距平摊高度 = 字体绘制高度
        finalHeight = finalHeight + lineHeights;

        //绘制第一行时要减 top - ascent 的距离
        float topSpacing = finalHeight + fm.ascent;

        //行数索引
        int linIndex = 1;

        //临时储存一个文字
        String textStr;

        pt.setColor(Color.RED);

        while (pageMaxLine >= linIndex) {
            if (isTest()) {
                canvas.drawLine(marginSpacing, 0, marginSpacing, getHeight(), pt);
                canvas.drawLine(getWidth() - marginSpacing, 0, getWidth() - marginSpacing, getHeight(), pt);
            }

            float minWidth = 0;

            //用于标记是否换页
            boolean skip = false;

            //预估每行显示的文字数量作废

            //每行实际宽度，并用于标记是否换行
            float lineWidth = 0;
            int fontIndex = 0;
            while (minWidth < widthPixels) {
                fontIndex++;

                if (tts + fontIndex > text.length()) {
                    skip = true;
                    fontIndex--;
                    lineWidth = minWidth;
                    break;
                }

                textStr = text.substring(tts + fontIndex - 1, tts + fontIndex);
                minWidth = minWidth + textPaint.measureText(textStr) * fontWidthRatio + fontSpacing;


                if (minWidth > widthPixels)
                    fontIndex--;

                if (textStr.equals("\n")) {
                    lineWidth = minWidth;
                    minWidth = widthPixels;
                }
            }

            //一行字体的数量
            fontNumber = fontIndex;

            String ttt = text.substring(tts, tts + fontNumber);
            tts = tts + fontNumber;

            //canvas.drawText(text.substring(tts, tts + fontNumber), marginSpacing, fontHeight, textPaint);
            int index = 0;

            //文字 X 轴
            float w = 0;
            @SuppressLint("DrawAllocation") float[] wi = new float[fontNumber];

            float textWidth;
            while (index < fontNumber) {
                wi[index] = w;
                //if (index == fontNumber - 1) {
                if (index == 0) {
                    textWidth = textPaint.measureText(ttt.substring(index, index + 1));
                } else {
                    textWidth = textPaint.measureText(ttt.substring(index, index + 1)) * fontWidthRatio + fontSpacing;
                }
                w = w + textWidth;
                index++;
            }

            //字体间距平摊,减一个是为了留出间隙空间
            float wordSpace = (widthPixels - w) / (fontNumber - 1);

            if (lineWidth > 0) {
                //wordSpace = (lineWidth - w - marginSpacing ) / (fontNumber + 1);
                wordSpace = 0;
            }


            index = 0;
            while (index < fontNumber) {
                canvas.drawText(ttt.substring(index, index + 1), marginSpacing + wordSpace * index + wi[index], finalHeight * linIndex - topSpacing + lineSpacing + topSpace + statusBar, textPaint);
                index++;
            }

            if (isTest()) {
                canvas.drawLine(0, finalHeight * linIndex + fm.ascent - topSpacing + lineSpacing + topSpace + statusBar, getWidth() + topSpace, finalHeight * linIndex + fm.ascent - topSpacing + lineSpacing + topSpace + statusBar, pt);
                canvas.drawLine(0, finalHeight * linIndex + fm.descent - topSpacing + lineSpacing + topSpace + statusBar, getWidth(), finalHeight * linIndex + fm.descent - topSpacing + lineSpacing + topSpace + statusBar, pt);
            }
            if (skip) {
                break;
            }
            linIndex++;
        }

        textEnd = tts;
        String jd = format1.format(((float) (textEnd)) / text.length());
        canvas.drawText(jd, getWidth() * 0.97f - marginSpacing - ttPaint.measureText(jd), getHeight() - ttPaint.getTextSize() * 0.85f, ttPaint);

    }


}
