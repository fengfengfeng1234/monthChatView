package com.ttp.consumer.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/**
 * 折线图
 * tlp
 * 2020.3.27
 * 1. 模仿支付宝 统计折线图.
 * 2. Y轴数 模仿极客时间
 */
public class LineChart extends View {

    // 月份中最后一天
    private int month_max_day;
    // 月份
    private int month = 4;
    private int year = 2020;

    private int mWidth, mHeight;//View 的宽和高
    private int chatViewHeight, chatViewWidth; // chatView 视图高度 宽度
    //常量 横坐标向值
    private final int x_horizontalSpacing = 5;
    //纵坐标向值
    private int y_horizontalSpacing = 3;
    //点的数组，-1表示该日还没到
    private int[] mPoints;

    // 画笔
    private Paint mDatePaint = new Paint();//日期画笔

    private Paint mConnectionLinePaint = new Paint();//线条画笔
    // 线条
    private Paint yLinePaint = new Paint();//Y轴 线条画笔
    // 点位
    private Paint mPointPaint = new Paint();//点画笔

    // 数据
    private Integer[] mXItems;//X轴的文字

    private float mStrokeWidth = 1.5f;//线条的宽度
    private float mConnectionLineWidth = 3f;//线条的宽度
    private float mFontSize;//字体的大小

    private int mDateTextColor = Color.parseColor("#fa267b");//日期字体颜色
    private int yLinePaintColor = Color.parseColor("#fa267b");//日期字体颜色
    private int mDarkColor = Color.parseColor("#5b7fdf");//点、线的颜色(深色)
    private int mConnectionLineColor = Color.parseColor("#d5d8f7");// 连接线

    private float mPointRadius;//点的半径

    public LineChart(Context context) {
        this(context, null);
    }

    public LineChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint(context);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private void initPaint(Context context) {
        //日期画笔
        mFontSize = sp2px(context, 15);
        mDatePaint.setTextSize(mFontSize);
        mDatePaint.setColor(mDateTextColor);

        yLinePaint.setAntiAlias(true);
        yLinePaint.setStrokeWidth(mStrokeWidth);//设置线条宽度
        yLinePaint.setStyle(Paint.Style.FILL);
        yLinePaint.setColor(yLinePaintColor);
        //画点位
        //点画笔
        mPointPaint.setTextSize(mFontSize);
        mPointPaint.setColor(mDarkColor);
        //连接线
        mConnectionLinePaint.setAntiAlias(true);
        mConnectionLinePaint.setStrokeWidth(mConnectionLineWidth);//设置线条宽度
        mConnectionLinePaint.setStyle(Paint.Style.FILL);
        mConnectionLinePaint.setColor(mConnectionLineColor);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        mWidth = widthSize;
        mHeight = heightSize;
        setMeasuredDimension(mWidth, mHeight);

        /**
         * 设计 chatViewHeight = 总高度 -  y 轴刻度 字体高度 * 2
         */
        chatViewHeight = (int) (mHeight - xCoordinateOffset - mFontSize);

        chatViewWidth = mWidth - xCoordinateOffset - 100;
    }


    /**
     * @param valueMax    横纵坐标 显示值的最大值
     * @param windowValue 控件本身 长宽
     * @return 计算单位像素的 尺度
     */
    public float calculationCoordinate
    (int valueMax, int windowValue) {
        //(每一份间距)
        int value_scale = valueMax;
        //(实际屏幕间距)
        int windows_value_scale = windowValue;
        // (一份的刻度)
        float scale = windows_value_scale / value_scale;

        return scale;
    }

    /**
     * 获取 点 x 轴
     * 根据数据下标  确定 x 轴
     *
     * @return
     */
    public float getCalculationX(int value) {
        return calculationCoordinate
                (value,
                        chatViewWidth);
    }


    /**
     * 给一个值 n  能给出 被  三整除的值
     */
    // Y轴辅助坐标系
    public int[] auxiliaryLine(int[] items) {
        int max = MAX(items.clone());
        int value = max;

        if (max < y_horizontalSpacing * y_horizontalSpacing) {
            value = y_horizontalSpacing * y_horizontalSpacing;
        } else {
            value = max;
        }

        while (value % y_horizontalSpacing != 0) {
            value = value + 1;
        }
        int[] array = new int[y_horizontalSpacing];

        int value2 = value / y_horizontalSpacing;

        for (int i = 0; i < y_horizontalSpacing; i++) {
            array[i] = (value2 + value2 * i);
        }
        return array;
    }


    public static int MAX(int[] arr) {
        Arrays.sort(arr);
        return arr[arr.length - 1];
    }

    // 对 View 原来的原点坐标系  偏移 原点
    int xCoordinateOffset = 60;
    int yCoordinateOffset = 60;


    /**
     * @return x轴绘制 辅助点位数量
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private Integer[] mathPoints() {

        month_max_day = getDaysOfMonth(year, month);

        int remainder = month_max_day % x_horizontalSpacing;
        boolean isPass = remainder == 0;
        int pointCount = (month_max_day - remainder) / x_horizontalSpacing;
        if (!isPass) {
            pointCount = pointCount + 1;
        }

        ArrayList<Integer> arrayList = new ArrayList(pointCount);

        for (int i = 0; i < pointCount; i++) {
            if ((pointCount - 1 == 6 && i == 5) || (i == pointCount - 1 && !isPass)) {
                arrayList.add(month_max_day);
                break;
            }
            arrayList.add(x_horizontalSpacing + i * x_horizontalSpacing);
        }
        arrayList.add(0, 1);
        return arrayList.stream().toArray(Integer[]::new);
    }


    /**
     * https://blog.csdn.net/dongyuxu342719/article/details/78131697
     *
     * @return
     */
    public int getDaysOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        int lastDay = calendar.getActualMaximum(calendar.DAY_OF_MONTH);
        return lastDay;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDraw(Canvas canvas) {


        mXItems = mathPoints();
        float x_scale = getCalculationX(month_max_day);
        int[] values = auxiliaryLine(mPoints);

        int max = MAX(values.clone());
        float y_scale = (chatViewHeight / max);

        // 预估半径一个合理值
        mPointRadius = x_scale / 3;
        canvas.translate(xCoordinateOffset, yCoordinateOffset);

        // x 轴坐标线
        canvas.drawLine(-xCoordinateOffset, chatViewHeight, chatViewWidth + 30, chatViewHeight, yLinePaint);

        // 绘制x轴 刻度
        for (int i = 0; i < mXItems.length; i++) {
            // 计算字体宽度   得到字体宽度一半 居中
            String text = mXItems[i] + "";
            float textWidth = mPointPaint.measureText(text);
            canvas.drawText(text, x_scale * mXItems[i] - textWidth / 2, chatViewHeight + mFontSize, mDatePaint);
        }

        // 绘制Y轴 刻度
        for (int i = 0; i < y_horizontalSpacing; i++) {
            float Y = chatViewHeight - y_scale * values[i];
            float textWidth = mPointPaint.measureText(values[i] + "");
            canvas.drawLine(-xCoordinateOffset, Y, chatViewWidth + 30, Y, yLinePaint);
            canvas.drawText(values[i] + "", x_scale * mXItems[mXItems.length - 1] + textWidth / 2, Y + 25, mDatePaint);
        }
        PointF previousPoint = null;

        // 绘制点位
        for (int i = 0; i < mPoints.length; i++) {
            PointF pointF = new PointF();
            // 日期是从1 开始
            pointF.x = x_scale * (i + 1);
            pointF.y = chatViewHeight - y_scale * mPoints[i];

            /**
             * 待优化
             *  圆圈之类 不出现白线的.
             */
            if (i != 0) {
                canvas.drawLine(previousPoint.x, previousPoint.y, pointF.x, pointF.y, mConnectionLinePaint);
            }
            previousPoint = pointF;

            canvas.drawCircle(pointF.x, pointF.y, mPointRadius, mPointPaint);
        }

        canvas.save();

    }

    public void setData(int[] points) {
        mPoints = points;
        invalidate();
    }


    /**
     * 获取:
     *  原点： (x,y)
     *  宽度   viewWight
     *  x轴:   计算数量
     *         1. 获取 每一刻度 ( x_scale )
     *         2. 像素长度 从而 计算 点位 x轴 应该放置的位置。
     *  y轴:
     *      数值中 获得最大值, 然后根据最大值 取2的幂次方。
     *      1. 获取 每一刻度 ( y_scale )
     *      2. 像素长度 从而 计算 点位 y轴 应该放置的位置。
     *  点位：
     *      根据   点位的 xy点 通过  y_scale and   x_scale 放置坐标系
     *
     *  UI
     *      绘制连接线 连接线
     *
     *  horizontalSpacing = 5; //横向值 容纳五个
     *
     * 场景1：
     *      y轴 刻度 数据显示 (y_size)  已知 刻度显示  3-1  3-5  3-10  3-15
     *
     *     求 x（带修改）
     *      1.x轴点位 有15个数据。
     *
     *      2. x_horizontalSpacing = 5
     *
     *      3. x_max = y_size * x_horizontalSpacing
     *
     *      4. x_value_scale = x_max/y_horizontalSpacing(每一份间距)
     *
     *      5. x_windows_value_scale = width/x_horizontalSpacing(实际屏幕间距)
     *
     *      6. x_scale = x_value_scale/x_windows_value_scale (一份的刻度)
     *
     *      7. y_point =  原点 +  x_scale * item.index
     *
     *
     *
     *     求 y
     *         1. y_max =  items sort max 获取数据集  最大的数据 --- > 向上去 2的幂次方
     *
     *         2. y_horizontalSpacing = 4
     *
     *         3. y_value_scale = y_max/y_horizontalSpacing(每一份间距)
     *
     *         4. y_windows_value_scale = height/y_horizontalSpacing(实际屏幕间距)
     *
     *         5. y_scale = y_value_scale/y_windows_value_scale (一份的刻度)
     */

}
