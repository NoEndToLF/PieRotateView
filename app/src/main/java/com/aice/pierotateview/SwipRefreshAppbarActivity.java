package com.aice.pierotateview;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aice.pierotateview.listener.AppBarLayoutStateChangeListener;
import com.google.android.material.appbar.AppBarLayout;
import com.wxy.pierotateview.model.PieRotateViewModel;
import com.wxy.pierotateview.view.PieRotateView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.ielse.view.SwitchView;
import top.defaults.colorpicker.ColorPickerPopup;

import static com.aice.pierotateview.listener.AppBarLayoutStateChangeListener.State.EXPANDED;


public class SwipRefreshAppbarActivity extends AppCompatActivity {

    @BindView(R.id.pie)
    PieRotateView pie;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.btn_circle_color)
    Button btnCircleColor;
    @BindView(R.id.btn_text_color)
    Button btnTextColor;
    @BindView(R.id.btn_arraw_color)
    Button btnArrawColor;
    @BindView(R.id.linear_parent)
    LinearLayout linearParent;
    @BindView(R.id.switch_fling)
    SwitchView switchFling;
    @BindView(R.id.tv_fling)
    TextView tvFling;
    @BindView(R.id.seekbar)
    AppCompatSeekBar seekbar;
    @BindView(R.id.tv_recovre_time)
    TextView tvRecovreTime;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.appbar_layout)
    AppBarLayout appbarLayout;
    private AppBarLayoutStateChangeListener.State appbarState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swiprefresh_appbar);
        ButterKnife.bind(this);
        List<PieRotateViewModel> list = new ArrayList<>();
        list.add(new PieRotateViewModel("红扇", 20, getResources().getColor(R.color.colorAccent)));
        list.add(new PieRotateViewModel("绿扇", 10, getResources().getColor(R.color.colorPrimaryDark)));
        list.add(new PieRotateViewModel("蓝扇", 10, Color.BLUE));
        list.add(new PieRotateViewModel("黄扇", 60, Color.parseColor("#FF7F00")));
        list.add(new PieRotateViewModel("粉扇", 50, Color.parseColor("#EE7AE9")));
        pie.setPieRotateViewModelList(list);
        pie.setOnPromiseParentTouchListener(new PieRotateView.onPromiseParentTouchListener() {
            @Override
            public void onPromiseTouch(boolean promise) {
                swipe.setEnabled(promise);
            }
        });
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pie.setEnableTouch(false);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        list.clear();
                        list.add(new PieRotateViewModel("红扇", 20, getResources().getColor(R.color.colorAccent)));
                        list.add(new PieRotateViewModel("绿扇", 10, getResources().getColor(R.color.colorPrimaryDark)));
                        pie.notifyDataChanged();
                        swipe.setRefreshing(false);
                        if (appbarState==EXPANDED){
                            swipe.setEnabled(true);
                            pie.setEnableTouch(true);
                        }else {
                            pie.setEnableTouch(false);
                            swipe.setEnabled(false);
                        }
                    }
                },2000);
            }
        });
        appbarLayout.addOnOffsetChangedListener(new AppBarLayoutStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                appbarState=state;
                switch (state) {
                    case EXPANDED:
                        swipe.setEnabled(true);
                        pie.setEnableTouch(true);
                        break;
                    case COLLAPSED:
                    case INTERMEDIATE:
                        if (!swipe.isRefreshing()){
                            swipe.setEnabled(false);}
                        pie.setEnableTouch(false);
                        break;
                }
            }
        });
        pie.setOnSelectionListener(new PieRotateView.onSelectionListener() {
            @Override
            public void onSelect(int position, String percent) {
                tvName.setText(list.get(position).getName() + "数额：" + list.get(position).getNum() + " 占比：" + percent);
                tvName.setTextColor(list.get(position).getColor());
            }
        });
        if (pie.isFling()) {
            tvFling.setText("是否允许Fling：是");
        } else {
            tvFling.setText("是否允许Fling：否");
        }
        switchFling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchFling.isOpened()) {
                    pie.setFling(true);
                    tvFling.setText("是否允许Fling：是");
                    pie.notifySettingChanged();
                } else {
                    pie.setFling(false);
                    tvFling.setText("是否允许Fling：否");

                    pie.notifySettingChanged();
                }
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int recoverTime = (int) ((float) progress / 100 * 1000);
                pie.setRecoverTime(recoverTime);
                tvRecovreTime.setText("recoverTime：" + recoverTime);
                pie.notifySettingChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @OnClick({R.id.btn_circle_color, R.id.btn_text_color, R.id.btn_arraw_color})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_circle_color:
                new ColorPickerPopup.Builder(this)
                        .initialColor(Color.RED) // Set initial color
                        .enableBrightness(true) // Enable brightness slider or not
                        .enableAlpha(true) // Enable alpha slider or not
                        .okTitle("确定")
                        .cancelTitle("取消")
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(linearParent, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                pie.setCircleColor(color);
                                pie.notifySettingChanged();
                            }
                        });
                break;
            case R.id.btn_text_color:
                new ColorPickerPopup.Builder(this)
                        .initialColor(Color.RED) // Set initial color
                        .enableBrightness(true) // Enable brightness slider or not
                        .enableAlpha(true) // Enable alpha slider or not
                        .okTitle("确定")
                        .cancelTitle("取消")
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(linearParent, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                pie.setTextColor(color);
                                pie.notifySettingChanged();
                            }
                        });
                break;
            case R.id.btn_arraw_color:
                new ColorPickerPopup.Builder(this)
                        .initialColor(Color.RED) // Set initial color
                        .enableBrightness(true) // Enable brightness slider or not
                        .enableAlpha(true) // Enable alpha slider or not
                        .okTitle("确定")
                        .cancelTitle("取消")
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(linearParent, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                pie.setArrawColor(color);
                                pie.notifySettingChanged();
                            }
                        });
                break;
            default:
        }
    }
}
