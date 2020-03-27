package com.ttp.consumer.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LineChart mWeekLineChart;
    private TextView tvDay;
    private TextView tvWeek;
    private TextView tvMonth;

    /**
     *  最大值  x_value_max   31
     *  间距  x_horizontalSpacing   5
      *
     *  如何 定位 绘制点内容
     *    value = 28
     *   1.
     *     余数
     *     int remainder =  x_value_max% x_horizontalSpacing
     *      bool  isPass=  remainder  == 0
     *
     *   2.
     *    int pointCount =   value /remainder
     *    if(!isPass){
     *        pointCount = pointCount + 1;   //   7
     *          // 5  10  15  20  25   28
     *    }
     *
     *
     *
     */

    private int[] mWeekPoints = new int[]
            {0, 0, 0, 0, 0,0,
                    7, 34, 1, 4, 0,
                    7, 2, 1, 4, 0,
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWeekLineChart = (LineChart) findViewById(R.id.line_chart_week);
        mWeekLineChart.setData(mWeekPoints);


    }



}
