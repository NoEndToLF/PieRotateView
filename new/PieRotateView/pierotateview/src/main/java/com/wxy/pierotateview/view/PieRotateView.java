package com.wxy.pierotateview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.wxy.pierotateview.R;
import com.wxy.pierotateview.model.PieRotateViewModel;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.Nullable;

public class PieRotateView extends View {
    private Paint piePaint,circlePaint,textPaint;
    protected static final int DEFUALT_VIEW_WIDTH=400;
    protected static final int DEFUALT_VIEW_HEIGHT=400;
    private float radius,centerX,centerY;
    private int selectPosition;
    private boolean isFirstDraw;
    private float downX,downY,lastX,lastY;
    private float lastRotateDregee,rotateDregee,moveRotateDregee;
    private float sum;
    private int mActivePointerId;
    private  final int INVALID_POINTER = -1;
    protected VelocityTracker mVelocityTracker;
    private ValueAnimator anim;//回弹动画
    private  float recoverStartValue;

    public void setCircleColor(int circleColor) {
        circlePaint.setColor(circleColor);
    }
    public void setTextColor(int textColor) {
        textPaint.setColor(textColor);
    }

    public void setOnSelectionListener(PieRotateView.onSelectionListener onSelectionListener) {
        this.onSelectionListener = onSelectionListener;
    }

    private onSelectionListener onSelectionListener;
    private List<PieRotateViewModel> pieRotateViewModelList;
    public void setPieRotateViewModelList(List<PieRotateViewModel> pieRotateViewModelList) {
        this.pieRotateViewModelList = pieRotateViewModelList;
        for (PieRotateViewModel pieRotateViewModel:pieRotateViewModelList){
            sum+=pieRotateViewModel.getNum();
        }
        selectPosition=0;
        invalidate();
    }

    public PieRotateView(Context context) {
        this(context,null);
    }

    public PieRotateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PieRotateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        piePaint=new Paint();
        piePaint.setAntiAlias(true);
        piePaint.setColor(Color.BLUE);
        circlePaint=new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.parseColor("#75ffffff"));
        textPaint=new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        isFirstDraw=true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width=0,height=0;
        int width_specMode= MeasureSpec.getMode(widthMeasureSpec);
        int height_specMode= MeasureSpec.getMode(heightMeasureSpec);
        switch (width_specMode){
            //宽度精确值
            case MeasureSpec.EXACTLY:
                switch (height_specMode){
                    //高度精确值
                    case MeasureSpec.EXACTLY:
                        width= MeasureSpec.getSize(widthMeasureSpec);
                        height= MeasureSpec.getSize(heightMeasureSpec);
                        break;
                    case MeasureSpec.AT_MOST:
                    case MeasureSpec.UNSPECIFIED:
                        width= MeasureSpec.getSize(widthMeasureSpec);
                        height=width;
                        break;
                }
                break;
            //宽度wrap_content
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                switch (height_specMode){
                    //高度精确值
                    case MeasureSpec.EXACTLY:
                        height= MeasureSpec.getSize(heightMeasureSpec);
                        width=height;
                        break;
                    case MeasureSpec.AT_MOST:
                    case MeasureSpec.UNSPECIFIED:
                        height=DEFUALT_VIEW_HEIGHT;
                        width=DEFUALT_VIEW_WIDTH;
                        break;
                }
                break;
        }
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (radius==0){
            radius= (Math.min(getWidth()-getPaddingLeft()-getPaddingRight(),getHeight())-getPaddingTop()-getPaddingBottom())/2;
                centerX=getPaddingLeft()+(getWidth()-getPaddingLeft()-getPaddingRight())/2;
                centerY=getPaddingTop()+(getHeight()-getPaddingTop()-getPaddingBottom())/2;
        }
        //先画所有扇形
        drawDataArc(canvas);
        if (isFirstDraw){
            if (onSelectionListener!=null){
                onSelectionListener.onSelect(selectPosition);
            }
            isFirstDraw=false;
        }
        //画中间的圆形
        canvas.drawCircle(centerX,centerY,radius/2.4f,circlePaint);
        //画Textf
        textPaint.setTextSize(radius/4.2f);
        textPaint.setColor(Color.WHITE);
        canvas.drawText(trimFloat(pieRotateViewModelList.get(selectPosition).getNum()/sum)+"%",centerX,centerY-getTextOffset(textPaint,"20%"),textPaint);
        //画指针
        float arrowRadius=radius/4f;
        canvas.save();
        Path area=new Path();
        area.addCircle(centerX,centerY,radius, Path.Direction.CCW);
        canvas.clipPath(area);
        Path arrowPath=new Path();
        arrowPath.moveTo(centerX,centerY+radius-arrowRadius);
        arrowPath.lineTo(centerX-arrowRadius/2,centerY+radius);
        arrowPath.lineTo(centerX+arrowRadius/2,centerY+radius);
        arrowPath.close();
        canvas.drawPath(arrowPath,circlePaint);
        canvas.restore();
    }
    public float getTextOffset(Paint paint, String text){
        Rect bounds=new Rect();
        paint.getTextBounds(text,0,text.length(),bounds);
        float offset=(bounds.top+bounds.bottom)/2;
        return offset;
    }
    private void drawDataArc(Canvas canvas) {
        if (pieRotateViewModelList!=null&&pieRotateViewModelList.size()>0){
            float hasDregee=0;
            for (int i=0;i<pieRotateViewModelList.size();i++){
                piePaint.setColor(pieRotateViewModelList.get(i).getColor());
                switch (i){
                    case 0:
                        hasDregee=90f-pieRotateViewModelList.get(i).getNum()/sum*360f/2f;
                        break;
                    default:
                        hasDregee=hasDregee+  pieRotateViewModelList.get(i-1).getNum()/sum*360f  ;
                }
                pieRotateViewModelList.get(i).setCenterDregee(getDregee(hasDregee+moveRotateDregee+(pieRotateViewModelList.get(i).getNum()/sum*360f/2f)));
                pieRotateViewModelList.get(i).setSelfDregee(pieRotateViewModelList.get(i).getNum()/sum*360f);
                if (pieRotateViewModelList.get(i).getPath()!=null){
                    Path path=pieRotateViewModelList.get(i).getPath();
                    path.reset();
                    path.moveTo(centerX,centerY);
                    path.arcTo(new RectF(centerX-radius, centerY-radius,
                            centerX+radius, centerY+radius),hasDregee+moveRotateDregee, pieRotateViewModelList.get(i).getSelfDregee());
                }else {
                    Path path=new Path();
                    path.moveTo(centerX,centerY);
                    path.arcTo(new RectF(centerX-radius, centerY-radius,
                            centerX+radius, centerY+radius),hasDregee+moveRotateDregee, pieRotateViewModelList.get(i).getSelfDregee());
                    pieRotateViewModelList.get(i).setPath(path);
                }
                 canvas.drawPath(pieRotateViewModelList.get(i).getPath(),piePaint);
            }
        }
    }
private float getDregee(float dregee){
        return dregee<0?(dregee+360f)%360f:dregee%360f;
}
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch (event.getAction()& MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId=event.getPointerId(0);
                downX= event.getX();
                downY= event.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //如果有新的手指按下，就直接把它当作当前活跃的指针
                final int index = event.getActionIndex();
                mActivePointerId = event.getPointerId(index);
                //并且刷新上一次记录的旧坐标值
                downX=(int) event.getX(index);
                downY=(int) event.getY(index);
                break;
            case MotionEvent.ACTION_MOVE:
                int activePointerIndex = event.findPointerIndex(mActivePointerId);
                if (activePointerIndex == INVALID_POINTER) {
                    break;
                }
                rotateDregee=degree(event.getX(activePointerIndex),event.getY(activePointerIndex))-lastRotateDregee;
                moveRotateDregee=moveRotateDregee+getDregee(rotateDregee);
                selectRecover(true);
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                selectRecover(false);
                break;
        }
        if (mActivePointerId != INVALID_POINTER) {
            lastRotateDregee = degree(event.getX(event.findPointerIndex(mActivePointerId)),event.getY(event.findPointerIndex(mActivePointerId)));
        }else {
            lastRotateDregee = degree(event.getX(),event.getY()) ;
        }
        return true;
    }

    //恢复到指针指向的部分的中心
    private void selectRecover(boolean isMove){
        for (int i=0;i<pieRotateViewModelList.size();i++){
            Region re=new Region();
            RectF rf=new RectF();
            pieRotateViewModelList.get(i).getPath().computeBounds(rf,true);
            re.setPath(pieRotateViewModelList.get(i).getPath(),new Region((int)rf.left,(int)rf.top,(int)rf.right,(int)rf.bottom));
            if (re.contains((int) centerX,(int)(centerY+radius/2))){
                selectPosition=i;
                if (onSelectionListener!=null){
                    onSelectionListener.onSelect(selectPosition);
                }
                if (!isMove){
                    Log.v("xixi=",moveRotateDregee+"");
//                    if (pieRotateViewModelList.get(i).getCenterDregee()>270f){
//                        float degree=360f-pieRotateViewModelList.get(i).getCenterDregee()+90f;
//                        recover(degree,0);
//
//                    }else {
//                        recover(0,pieRotateViewModelList.get(i).getCenterDregee()-90f);
//                    }
                }
                break;
            }
        }
    }
    private void recover(final float start, final float end){
        recoverStartValue=0;
        if (anim==null) {
            anim = new ValueAnimator();
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(200);
            anim.setFloatValues(start, end);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float currentValue = (float) animation.getAnimatedValue()-recoverStartValue;
                        moveRotateDregee=moveRotateDregee+currentValue;
                    invalidate();
                    recoverStartValue=(float) animation.getAnimatedValue();
                }
            });
            anim.start();
        }else {
            if (!anim.isRunning()){
                anim.setDuration(200);
                anim.setFloatValues(start, end);
                anim.start();
            }
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = ev.getActionIndex();
        int pointerId = ev.getPointerId(pointerIndex);
        //如果抬起的那根手指，刚好是当前活跃的手指，那么
        if (pointerId == mActivePointerId) {
            //另选一根手指，并把它标记为活跃
            int newPointerIndex =  pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
            //把上一次记录的坐标，更新为新手指的当前坐标
            downX = (int) ev.getX(newPointerIndex);
            downY =(int)  ev.getY(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }
    /**获得相对于X轴正方向的夹角*/

    private float degree(float x, float y){
        float detaX = x - centerX;
        float detaY = y - centerY;
        float degree=0;
        if (detaX==0&&detaY!=0){
            //在Y轴上
            if (detaY>0){
                degree=90;
            }else{
                degree=270;
            }
        }else if(detaY==0&&detaX!=0){
            //在X轴上
            if (detaX>0){
                degree=0;
            }else{
                degree=180;
            }
        }else if (detaX>0&&detaY>0){
            //第一象限内
            float tan = Math.abs(detaY / detaX);
            degree=(float)Math.toDegrees(Math.atan(tan));
        }else if (detaX<0&&detaY>0){
            //第二象限内
            float tan = Math.abs(detaX / detaY);
            degree=90+(float)Math.toDegrees(Math.atan(tan));
        }else if (detaX<0&&detaY<0){
            //第三象限内
            float tan = Math.abs(detaY / detaX);
            degree=180+(float)Math.toDegrees(Math.atan(tan));
        }else if (detaX>0&&detaY<0){
            //第四象限内
            float tan = Math.abs(detaX / detaY);
            degree=270+(float)Math.toDegrees(Math.atan(tan));
        }else{
            //在原点位置
            degree=0;
        }
        return degree;
    }

    public static int trimFloat(float value) {
        DecimalFormat df   =new   DecimalFormat("#.00");
        return (int)(Float.parseFloat(df.format(value))*100f);

    }
    public interface onSelectionListener{
        void onSelect(int position);
    }
}