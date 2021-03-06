package com.vicky.red.redpackets.view.meteorshower;/**
 * Created by lenovo on 2016/12/1.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.vicky.red.redpackets.R;

import java.util.Random;

/**
 * create by yao.cui at 2016/12/1
 */
public class MeteorShowerSurface extends SurfaceView implements SurfaceHolder.Callback,View.OnTouchListener{
    private final String TAG="MeteorShowerSurface";

    private Thread mDrawThread;
    private Thread mAddThread;
    private Thread mAddLineThread;

    private boolean isGameOver = false;
    private SurfaceHolder mHolder;

    private int mHeight;
    private int mWidth;
    private Paint mPaint;
    private Bitmap[] mBmps;
    private Random mRandom;
    private Bitmap mScoreBmp;
    private Rect mTargetRect;
    private Rect mOrgRect;
    private int mScore ;
    private Paint mScorePaint;
    private Rect mScoreBounds = new Rect();

    private SpriteManager mSpriteManager;
    private Context mContext;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what ==1){
                mSpriteManager.addMeteorSprite();
            } else if(msg.what==2){
                mSpriteManager.addLine();
            } else if (msg.what ==3){
               mScore++;
            }
        }
    };

    public MeteorShowerSurface(Context context) {
        super(context);
        init(context);
    }

    public MeteorShowerSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MeteorShowerSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.mContext = context;
        setZOrderOnTop(true);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mDrawThread = new Thread(new DrawThread());
        mAddThread = new Thread(new AddMeteorThread());
        mAddLineThread = new Thread(new AddLineThread());

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mBmps = new Bitmap[5];
        mBmps[0]= Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hongb0),150,200,false);
        mBmps[1]= Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hongb1),150,200,false);
        mBmps[2]= Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hongb2),150,200,false);
        mBmps[3]= Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hongb3),150,200,false);
        mBmps[4]= Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hongb4),150,200,false);
        mRandom = new Random();
        setOnTouchListener(this);
        mScoreBmp = BitmapFactory.decodeResource(getResources(),R.drawable.hbs);
        mScorePaint = new Paint();
        mScorePaint.setAntiAlias(true);
        mScorePaint.setColor(Color.RED);
        mScorePaint.setTextSize(80);
        mScorePaint.getTextBounds("000",0,1,mScoreBounds);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mWidth = getMeasuredWidth();
        this.mHeight = getMeasuredHeight();
        mSpriteManager = SpriteManager.getInstance(mWidth,mHeight);
        mSpriteManager.init(mContext);
        mTargetRect = new Rect((int)(mWidth/1.5f),100,mWidth-20,230);
        mOrgRect = new Rect(0,0,mScoreBmp.getWidth(),mScoreBmp.getHeight());
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        isGameOver = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isGameOver = true;
        mHandler.removeCallbacksAndMessages(null);
        mSpriteManager.stop();
    }

    /**
     * 开始绘制线程
     */
    public void start(){
        if (mDrawThread!= null){
            mDrawThread.start();
        }
        if (mAddThread!= null){
            mAddThread.start();
        }
        if (mAddLineThread != null){
            mAddLineThread.start();
        }
    }

    public void stop(){
        isGameOver = true;
    }

    private void clean(){
        mSpriteManager.cleanData();
    }
    private void recycle(){
        Log.i(TAG,"=====recycle");
        mSpriteManager.recycle();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                BaseSpite sprite = mSpriteManager.isContains(x,y);
                if (sprite != null){
                    mSpriteManager.addBoom((int)x,(int)y);
                    mHandler.sendEmptyMessage(3);
                    sprite.stop();
                }

                break;
        }
        return true;
    }

    class DrawThread implements Runnable{
        @Override
        public void run() {
            while(!isGameOver){
                clean();//清除滚出去的数据
                Canvas canvas = null;
                synchronized (mHolder){
                    canvas = mHolder.lockCanvas();

                    if (canvas== null){
                        isGameOver = true;
                        return;
                    }

                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mSpriteManager.draw(canvas);
                    canvas.drawBitmap(mScoreBmp,mOrgRect,mTargetRect,mPaint);
                    canvas.drawText(mScore+" ",mTargetRect.centerX()-mScoreBounds.width()/2,
                            mTargetRect.centerY()+mScoreBounds.height()/2,mScorePaint);
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    class AddMeteorThread implements Runnable{
        @Override
        public void run() {
            while (!isGameOver){
                try {
                    Thread.sleep(300);
                } catch (Exception e){
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    class AddLineThread implements Runnable{
        @Override
        public void run() {
            while (!isGameOver){
                try{
                    Thread.sleep(200);
                }catch (Exception e){
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(2);
            }
        }
    }

}
