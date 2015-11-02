package com.fang.videocamera.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Toast;

import com.fang.videocamera.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.interfaces.LineDataProvider;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

@EFragment(R.layout.fragment_chart)
public class ChartFragment extends BaseFragment {
    private Typeface tf;
    @ViewById(R.id.chart1)
    LineChart mChart;
    static int VisibleXRangeMaximum = 12;
    int x = 1;

    @Click(R.id.btn_click)
    void click(View view) {
        // Toast.makeText(getActivity(),"ccccc",Toast.LENGTH_SHORT).show();
        addData((float) (Math.random() * 50) + 60f);

    }


    @AfterViews
    void init() {
        //mChart.setBackgroundColor(Color.rgb(104, 241, 175));
        mChart.setBackgroundColor(Color.BLACK);
        mChart.setDescription("");
        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(false);

        mChart.setDrawGridBackground(false);
        tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");
        LimitLine ll1 = new LimitLine(100f, "100");
        ll1.setLineWidth(2f);

        ll1.enableDashedLine(5f, 5f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(6f);
        ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(50f, "50");
        ll2.setLineWidth(2f);

        ll2.enableDashedLine(5f, 5f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(6f);
        ll2.setTypeface(tf);
        //X轴
        mChart.getXAxis().setEnabled(false);
        //Y轴
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.setTextColor(Color.WHITE);
       /* leftAxis.setAxisMaxValue(220f);
        leftAxis.setAxisMinValue(-20f);
        leftAxis.setStartAtZero(false);*/
        leftAxis.setStartAtZero(false);


        // leftAxis.enableGridDashedLine(10f, 10f, 0f);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);


        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);


        Legend l = mChart.getLegend();

        l.setForm(Legend.LegendForm.LINE);


    }


/*    private void setData(int count, float range) {

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            xVals.add((0 + i) + "");
        }

        ArrayList<Entry> vals1 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult) + 20;// + (float)
            // ((mult *
            // 0.1) / 10);
            vals1.add(new Entry(val, i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(vals1, "压力");
        set1.setDrawCubic(true);
        set1.setCubicIntensity(0.2f);
        //set1.setDrawFilled(true);
        set1.setDrawCircles(false);
        set1.setLineWidth(1.8f);
        set1.setCircleSize(4f);
        set1.setCircleColor(Color.WHITE);
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setColor(Color.WHITE);
        set1.setFillColor(Color.WHITE);
        set1.setFillAlpha(100);
        set1.setDrawHorizontalHighlightIndicator(false);
        set1.setFillFormatter(new FillFormatter() {
            @Override
            public float getFillLinePosition(LineDataSet dataSet, LineDataProvider dataProvider) {
                return -10;
            }
        });

        // create a data object with the datasets
        LineData data = new LineData(xVals, set1);
       data.setValueTypeface(tf);
        data.setValueTextSize(9f);
        data.setDrawValues(true);

        // set data
        mChart.setData(data);
    }*/

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "压力");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(3f);
        set.setCircleSize(4f);

        set.setFillAlpha(100);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawHorizontalHighlightIndicator(true);
        set.setDrawValues(true);
        return set;
    }

    public  void addData(float value) {
            //data在初始化时 setdata 了一个空的
        LineData data = mChart.getData();

        if (data != null) {
            LineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
           /* data.addXValue(mMonths[data.getXValCount() % 12] + " "
                    + (year + data.getXValCount() / 12));*/
            //添加X轴的数据
            data.addXValue(x++ + "");
            data.addEntry(new Entry(value, set.getEntryCount()), 0);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(VisibleXRangeMaximum);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);
            //line向左移动一个
            mChart.moveViewToX(data.getXValCount() - (1 + VisibleXRangeMaximum));
        }
    }

}
