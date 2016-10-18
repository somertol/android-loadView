package com.example.somer.loadview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by somer on 16/10/16.
 */

public class LoadView extends LinearLayout {
    public final int HEAD_STATE=0;
    public final int HEAD_MOVE_STATE=3;
    public final int HEAD_MOVED_STATE=5;
    public final int BODY_STATE=1;
    public final int FOOT_STATE=2;
    public final int FOOT_MOVE_STATE=4;
    public final int FOOT_MOVED_STATE=6;
    View head,foot;
    ArrayList<View> bodys=new ArrayList<>();
    int headHeightPX,footHeightPX;
    int moveState=HEAD_STATE;
    int downY,moveDistance;
    boolean isGoback=false,isInit=false,isHeadLoader=true,isFootLoader=true;
    Handler goBackHandler=new Handler();
    BackStateRunnable backStateRunnable=new BackStateRunnable();
    BackToLoadRunnable backToLoadRunnable=new BackToLoadRunnable();
    int labelHeight,moveStep=3,startMoveStep=10;
    public LoadView(Context context) {
        super(context);
    }

    public LoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(){
        Activity activity=(Activity)getContext();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int contentTop=activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();//获取标题栏高度
        int titleBarHeight = contentTop - statusBarHeight;
        int actionBarHeight =0;
        if(activity.getActionBar()!=null)
            actionBarHeight=activity.getActionBar().getHeight();//获取actionbar高度
        labelHeight=Math.abs(titleBarHeight)+actionBarHeight;
    }
    protected void onLayout(boolean changed, int left, int top, int right, int bottom){
        super.onLayout(changed, left, top, right, bottom);
        int childCount = this.getChildCount();
        int t = 0;
        if(isInit==false) {
            for (int i = 0; i < childCount; i++) {
                View view = this.getChildAt(i);
                if (i == 0 && childCount>1) {
                    int height =headHeightPX= view.getHeight();
                    LinearLayout.LayoutParams headParams = (LayoutParams) view.getLayoutParams();//new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                    headParams.topMargin = -height;
                    headParams.bottomMargin = 0;
                    view.setLayoutParams(headParams);
                    //view.setTop(-view.getHeight());
                    //view.setBottom(0);
                    head = view;
                } else if (i == childCount - 1 && childCount>2) {
                    view.setTop(getHeight()+labelHeight);
                    view.setBottom(getHeight()+labelHeight+view.getHeight());
                    /*int height =footHeightPX=  view.getHeight();
                    LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                    params.topMargin = getMeasuredHeight();
                    params.bottomMargin = getMeasuredHeight() + height;
                    view.setLayoutParams(params);*/
                    foot = view;
                } else
                    bodys.add(view);
            }
            isInit=true;
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 测量子控件的大小
        // 子控件的个数
        /*int childCount = this.getChildCount();
        for (int i = 0; i < childCount; i++) {
            // 得到子控件
            View view = this.getChildAt(i);
            view.measure(widthMeasureSpec, heightMeasureSpec);
        }*/
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        return true;
    }
    public boolean dispatchTouchEvent(MotionEvent ev){
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY=(int) ev.getY();
                switch (moveState){
                    case HEAD_STATE:
                    case FOOT_STATE:
                        return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (y - downY);
                downY = y;
                switch (moveState){
                    case HEAD_STATE:
                        if(moveY<0)
                            return false;
                        moveView(moveY);
                        return true;
                    case FOOT_STATE:
                        if(moveY>0)
                            return false;
                        moveView(moveY);
                        return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                int moveD = Math.abs(moveDistance);
                switch (moveState){
                    case HEAD_STATE:
                        if (moveDistance > 0) {
                           setHeadMove();
                            if(head!=null) {
                                if (moveD < head.getHeight())
                                    goBackHandler.post(backStateRunnable);
                                else
                                    goBackHandler.post(backToLoadRunnable);
                            }else
                                goBackHandler.post(backStateRunnable);
                        }else
                            moveState=BODY_STATE;
                        break;
                    case FOOT_STATE:
                        if (moveDistance < 0) {
                            setFootMove();
                            if(foot!=null) {
                                if (moveD < foot.getHeight())
                                    goBackHandler.post(backStateRunnable);
                                else
                                    goBackHandler.post(backToLoadRunnable);
                            }else
                                goBackHandler.post(backStateRunnable);
                        }else
                            moveState=BODY_STATE;
                        return true;
                }
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
    private void setHeadMove(){
        backStateRunnable.moveSp = -startMoveStep;
        backToLoadRunnable.moveSp = -startMoveStep;
        backToLoadRunnable.moveTo = 0;
        if(head!=null) {
            //LayoutParams layoutParams = (LayoutParams) head.getLayoutParams();
            backStateRunnable.moveDistance = head.getTop();
            backStateRunnable.moveTo = -head.getHeight();
            backToLoadRunnable.moveDistance = head.getTop();
        }else{
            backStateRunnable.moveDistance =bodys.get(0).getTop();
            backStateRunnable.moveTo = 0;
            backToLoadRunnable.moveDistance = bodys.get(0).getTop();
        }
    }
    private void setFootMove(){
        backStateRunnable.moveSp = startMoveStep;
        backToLoadRunnable.moveSp = startMoveStep;
        backStateRunnable.moveTo = getHeight();
        if(foot!=null) {
            //LayoutParams layoutParams = (LayoutParams) foot.getLayoutParams();
            backStateRunnable.moveDistance = foot.getTop();
            backToLoadRunnable.moveDistance = foot.getTop();
            backToLoadRunnable.moveTo = getHeight() - foot.getHeight();
        }else{
            backStateRunnable.moveDistance = bodys.get(bodys.size()-1).getBottom();
            backToLoadRunnable.moveDistance = bodys.get(bodys.size()-1).getBottom();
            backToLoadRunnable.moveTo = getHeight() - bodys.get(bodys.size()-1).getBottom();
        }
    }
    public boolean onInterceptTouchEvent(MotionEvent ev){
        int y=(int)ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downY=(int)ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                switch (moveState){
                    case HEAD_STATE:
                        if(y-downY<0)
                            return false;
                    case FOOT_STATE:
                        if(y-downY>0)
                            return false;
                        return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (moveState){
                    case HEAD_STATE:
                        if(y-downY<0) {
                            return false;
                        }
                    case FOOT_STATE:
                        if(y-downY>0) {
                            return false;
                        }
                        return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
    private void moveView(int y){
        if(y>0&&moveDistance>getMeasuredHeight()/2)
            return;
        if(y<0&&moveDistance<-getMeasuredHeight()/2)
            return;
        if(head!=null)
            moveView(y,head);
        for(View body :bodys)
            moveView(y,body);
        if(foot!=null)
            moveView(y,foot);
        moveDistance+=y;
    }
    private void moveView(int y,View view){
        view.setTop(view.getTop()+y);
        view.setBottom(view.getBottom()+y);
    }
    public void setState(int state){
        this.moveState=state;
    }
    class BackStateRunnable implements Runnable{
        public int moveSp=0;
        public int moveDistance;
        public int moveTo;
        @Override
        public void run() {
            if(moveSp!=0) {
                if (moveSp > 0) {
                    if (moveDistance + moveSp < moveTo) {
                        moveDistance += moveSp;
                        moveView(moveSp);
                        moveSp+=moveStep;
                    } else {
                        moveSp=moveTo-moveDistance;
                        moveView(moveSp);
                        moveSp=0;
                        moveState=BODY_STATE;
                    }
                } else{
                    if (moveDistance+moveSp> moveTo) {
                        moveDistance += moveSp;
                        moveView(moveSp);
                        moveSp-=moveStep;

                    } else {
                        moveSp=moveTo-moveDistance;
                        moveView(moveSp);
                        moveSp=0;
                        moveState=BODY_STATE;
                    }
                }
                goBackHandler.postDelayed(this,40);
            }else
                goBackHandler.removeCallbacks(this);
        }
    }
    class BackToLoadRunnable implements Runnable{
        public int moveSp=0;
        public int moveDistance;
        public int moveTo;
        @Override
        public void run() {
            if(moveSp!=0) {
                if (moveSp < 0) {
                    if (moveDistance + moveSp > moveTo) {
                        moveDistance += moveSp;
                        moveView(moveSp);
                        moveSp-=moveStep;

                    } else {
                        moveSp=moveTo-moveDistance;
                        moveView(moveSp);
                        moveSp=0;
                        moveState=BODY_STATE;
                    }

                } else{
                    if (moveDistance + moveSp < moveTo) {
                        moveDistance += moveSp;
                        moveView(moveSp);
                        moveSp+=moveStep;

                    } else {
                        moveSp=moveTo-moveDistance;
                        moveView(moveSp);
                        moveSp=0;
                        moveState=BODY_STATE;
                    }
                }
                goBackHandler.postDelayed(this,40);
            }else
                goBackHandler.removeCallbacks(this);
        }
    }
    public void setLoadOver(){
        switch (moveState){
            case HEAD_STATE:
                setHeadMove();
                break;
            case FOOT_STATE:
                setFootMove();
                break;
        }
        goBackHandler.post(backStateRunnable);
    }
    public void setHeaderLoader(Boolean isHeadLoader){
        this.isHeadLoader=isHeadLoader;
    }
    public void setFootLoader(Boolean isFootLoader){
        this.isFootLoader=isFootLoader;
    }
}
