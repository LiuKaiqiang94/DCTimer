package com.dctimer.view;

import com.dctimer.util.Utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {
	private int mInitColor;	//��ʼ��ɫ
	private int mCrntColor;	//��ǰ��ɫ
	
	private Paint mPaint;	//�߲�ͼ����
	private Paint mShaderPaint;	//���ͶȻ���
	private Paint mRectPaint;	//���䷽�黭��
	private Paint tempPaint;
	
	private int[] mCircleColors;//����ͼ��ɫ
	private int[] mRectColors = {0xffffffff, 0, 0xff000000};//���䷽����ɫ
	
	private int mWidth;//View��
	private int hue; //ɫ��
	private double saturation; //���Ͷ�
	private double lum; //����
	
	private boolean downInCRect = true;//�����߲�ͼ��
	private boolean downInRect;//���ڽ��䷽����
	private int downInDef;	//����Ԥ�����
	private int[] defColor = {Color.RED, Color.YELLOW, 0xff009900, Color.WHITE, Color.BLUE, 0xffff8026};
	private RectF[] rects = new RectF[6];
	private Path path = new Path();
	private LinearGradient lg;

	public ColorPickerView(Context context, int width, int color) {
		super(context);
		this.mWidth = width;
		this.mInitColor = this.mCrntColor = color;
		setMinimumHeight((int)(width * 0.94));
		setMinimumWidth(width);
		
		//�߲�ͼ����
		mCircleColors = new int[] {0xffff0000, 0xffffff00, 0xff00ff00, 0xff00ffff,
	            0xff0000ff, 0xffff00ff, 0xffff0000};
		float[] op = {0, 0.16667f, 0.33333f, 0.5f, 0.66667f, 0.83333f, 1};
		lg = new LinearGradient((int)(width*0.02), 0, (int)(width*0.77), 0, mCircleColors, op, TileMode.MIRROR);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setShader(lg);
		lg = new LinearGradient(0, (int)(width*0.02), 0, (int)(width*0.62), 0x00808080, 0xff808080, TileMode.MIRROR);
		mShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mShaderPaint.setShader(lg);
		
		//�������
		mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		
		double[] hsl = Utils.rgbToHSL(mInitColor);
		hue = (int)hsl[0];
		saturation = hsl[1];
		lum = hsl[2];
		
		tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		tempPaint.setTextSize(mWidth/20);
		tempPaint.setTextAlign(Align.CENTER);
		for(int i=0; i<6; i++)
			rects[i] = new RectF((float)(mWidth*(i*0.164+0.02)), (float)(mWidth*0.8), (float)(mWidth*(i*0.164+0.16)), (float)(mWidth*0.94));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//���߲�ͼ
		canvas.drawRect((float)(mWidth*0.02), (float)(mWidth*0.02), (float)(mWidth*0.77), (float)(mWidth*0.62), mPaint);
		canvas.drawRect((float)(mWidth*0.02), (float)(mWidth*0.02), (float)(mWidth*0.77), (float)(mWidth*0.62), mShaderPaint);
		float dx = (float) (mWidth*(hue/480.0+0.02));
		float dy = (float) (mWidth*((1-saturation)*0.6+0.02));
		tempPaint.setColor(Color.BLACK);
		tempPaint.setStyle(Style.FILL);
		canvas.drawRect((float)(dx-mWidth*0.03), dy-1, (float)(dx-mWidth*0.01), dy+1, tempPaint);
		canvas.drawRect((float)(dx+mWidth*0.01), dy-1, (float)(dx+mWidth*0.03), dy+1, tempPaint);
		canvas.drawRect(dx-1, (float)(dy-mWidth*0.03), dx+1, (float)(dy-mWidth*0.01), tempPaint);
		canvas.drawRect(dx-1, (float)(dy+mWidth*0.01), dx+1, (float)(dy+mWidth*0.03), tempPaint);
		//��������
		mRectColors[1] = Utils.hslToRgb(hue, saturation, 0.5);
		final float[] op = new float[]{0, 0.5f, 1};
		lg = new LinearGradient(0, (float)(mWidth*0.02), 0, (float)(mWidth*0.62), mRectColors, op, TileMode.MIRROR);
		mRectPaint.setShader(lg);
		canvas.drawRect((float)(mWidth*0.82), (float)(mWidth*0.02), (float)(mWidth*0.98), (float)(mWidth*0.62), mRectPaint);
		dy = (float) (mWidth*((1-lum)*0.6+0.02));
		//tempPaint.setColor(Color.BLACK);
		path.reset();
		path.moveTo((float)(mWidth*0.8), (float)(dy-mWidth*0.02));
		path.lineTo((float)(mWidth*0.8), (float)(dy+mWidth*0.02));
		path.lineTo((float)(mWidth*0.82), dy);
		path.close();
		canvas.drawPath(path, tempPaint);
		
		int c = (mCrntColor>>>16) & 0xff;
		canvas.drawText("R: "+c, (float)(mWidth*0.3), (float)(mWidth*0.73), tempPaint);
		c = (mCrntColor>>>8) & 0xff;
		canvas.drawText("G: "+c, (float)(mWidth*0.58), (float)(mWidth*0.73), tempPaint);
		c = mCrntColor & 0xff;
		canvas.drawText("B: "+c, (float)(mWidth*0.86), (float)(mWidth*0.73), tempPaint);
		tempPaint.setColor(mCrntColor);
		canvas.drawRect((float)(mWidth*0.02), (float)(mWidth*0.64), (float)(mWidth*0.16), (float)(mWidth*0.71), tempPaint);
		tempPaint.setColor(mInitColor);
		canvas.drawRect((float)(mWidth*0.02), (float)(mWidth*0.71), (float)(mWidth*0.16), (float)(mWidth*0.78), tempPaint);
		
		//��Ԥ���
		float a = (float) (mWidth / 60D);
		for(int i=0; i<6; i++) {
			tempPaint.setColor(defColor[i]);
			canvas.drawRoundRect(rects[i], a, a, tempPaint);
    	}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		int inDef = inDefault(x, y);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downInCRect = inColorRect(x, y);
			downInRect = inRect(x, y);
			downInDef = inDef;
		case MotionEvent.ACTION_MOVE:
			if(downInCRect) { //down���ڽ���ɫ����
				hue = getHue(x);
				saturation = getSl(y);
				mCrntColor = Utils.hslToRgb(hue, saturation, lum);
			} else if(downInRect) { //down�ڽ��䷽����
				lum = getSl(y);
				mCrntColor = Utils.hslToRgb(hue, saturation, lum);
			} else if(downInDef >= 0 && inDef == downInDef) {
				double[] hsl = Utils.rgbToHSL(defColor[inDef]);
				hue = (int)hsl[0];
				saturation = hsl[1];
	    		lum = hsl[2];
	    		mCrntColor = Utils.hslToRgb(hue, saturation, lum);
			}
			if(downInDef >=0 && inDef < 0) {
				downInDef = -1;
			}
			invalidate();
        	break;
		case MotionEvent.ACTION_UP:
			break;
		}
		return true;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(mWidth, (int)(mWidth * 0.94));
	}
	
	/**
	 * �����Ƿ����߲�ͼ��
	 * @param x ����
	 * @param y ����
	 * @return
	 */
	private boolean inColorRect(float x, float y) {
		return x < mWidth * 0.78 && y < mWidth * 0.64;
	}
	
	/**
	 * �����Ƿ��ڽ���ɫ��
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean inRect(float x, float y) {
		return x > mWidth * 0.8 && y < mWidth * 0.64;
	}
	
	/**
	 * �����Ƿ���Ԥ�����
	 * @param x
	 * @param y
	 * @return color idx
	 */
	private int inDefault(float x, float y) {
		if(y < mWidth * 0.8) return -1;
		if(x < mWidth * 0.167) return 0;
		if(x < mWidth * 0.333) return 1;
		if(x < mWidth / 2) return 2;
		if(x < mWidth * 0.667) return 3;
		if(x < mWidth * 0.833) return 4;
		return 5;
	}
	
	/**
	 * ��ȡ�߲�ͼɫ��
	 * @param x
	 * @return
	 */
	private int getHue(float x) {
		int hue = (int) ((x / mWidth - 0.02) * 480);
		if(hue < 0) return 0;
		if(hue > 360) return 360;
		return hue;
    }
	
	/**
	 * ��ȡ�߲�ͼ���Ͷȡ�����
	 * @param y
	 * @return
	 */
	private double getSl(float y) {
		double sl = y / mWidth / 0.6 - 1 / 30D;
		if(sl < 0) return 1;
		if(sl > 1) return 0;
        return 1 - sl;
    }
	
	public int getColor() {
		return this.mCrntColor;
	}
}
