package com.proveedoradeclimas.sacalmacen.clases;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Administrador on 02/11/2016.
 */
public class DrawingView extends View {

    private String coordenadas ="";
    ArrayList<Integer> xs = new ArrayList<Integer>();
    ArrayList<Integer> ys = new ArrayList<Integer>();

    ArrayList<Integer> smoothX = new ArrayList<Integer>();
    ArrayList<Integer> smoothY = new ArrayList<Integer>();

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = Color.BLACK;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    //brush sizes
    private float brushSize;
    //is smoother activated
    private boolean isSmooth;
    //is traslation activated
    private boolean isTraslation;

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    //setup drawing
    private void setupDrawing(){

        //prepare for drawing and setup paint stroke properties
        brushSize = 2;
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    //size assigned to view
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    //draw the view - will be called after touch event
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
        //Log.i("TAG", "Canvas Height: "+ Integer.toString(canvas.getHeight()) + ", Width: " + Integer.toString(canvas.getWidth()));
    }

    //touch event
    private float mX, mY;
    private int divisor = 1;
    private float TOUCH_TOLERANCE = 4;
    private void touch_start(float x, float y) {
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            //save points
            //coordenadas = coordenadas + Integer.toString((int)Math.round(mX)) + " " + Integer.toString((int)Math.round(mY)) + " " + Integer.toString((int)Math.round((x + mX)/2)) + " " + Integer.toString((int)Math.round((y + mY)/2)) + "\n";
            xs.add((int)Math.round( mX ));
            ys.add((int)Math.round( mY ));
            xs.add((int)Math.round( (x + mX)/2 ));
            ys.add((int)Math.round( (y + mY)/2 ));
            mX = x;
            mY = y;
        }
    }
    private void touch_up() {
//		xs.add(-1);
//		ys.add(-1);
        xs.add((int)Math.round(mX));
        ys.add((int)Math.round(mY));
        drawPath.lineTo(mX, mY);
        // commit the path to our offscreen
        drawCanvas.drawPath(drawPath, drawPaint);

        if(xs.size()>1) {
            if(!isSmooth) {
                smoothX.addAll(xs);
                smoothY.addAll(ys);
                smoothX.add(-1);
                smoothY.add(-1);
            }
            else {
                smoothX.addAll(smoothTheLine(xs));
                smoothY.addAll(smoothTheLine(ys));
                smoothX.add(-1);
                smoothY.add(-1);
                reDrawSmoothLine();
            }
        }
        xs.clear();
        ys.clear();

        // kill this so we don't double draw
        drawPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    //start new drawing
    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    //return the coordinates of the new drawing (signature)
    public String getCoordenadas() {
        int minimoX = 0;
        int minimoY = 0;
        if(isTraslation) {
            minimoX = smoothX.get(0);
            minimoY = smoothY.get(0);
            for(int i = 0; i < smoothX.size(); i++) {
                if(smoothX.get(i) != -1 && smoothX.get(i) < minimoX ) {
                    minimoX = smoothX.get(i);
                }
                if(smoothY.get(i) != -1 && smoothY.get(i) < minimoY ) {
                    minimoY = smoothY.get(i);
                }
            }
            minimoX = minimoX / 2;
            minimoY = minimoY / 2;
        }
        for(int i = 0; i<smoothX.size(); i++) {
            if(smoothX.get(i) != -1) {
                if(smoothX.get(i + 1) != -1) {
                    coordenadas = coordenadas
                            + Integer.toString(smoothX.get(i) / divisor - minimoX) + " "
                            + Integer.toString(smoothY.get(i) / divisor - minimoY) + " "
                            + Integer.toString(smoothX.get(i + 1) / divisor - minimoX) + " "
                            + Integer.toString(smoothY.get(i + 1) / divisor - minimoY)
                            + "\n";
                }
                else {
                    i++;
                }
            }
        }
        return coordenadas;
//		}
//		return "Ups!";
    }

    //clear the contents of the arrays (coordinates)
    public void setCoordenadas() {
        xs.clear();
        ys.clear();
        smoothX.clear();
        smoothY.clear();
        coordenadas = "";
    }

    public void setBrushSize( float size ) {
        brushSize = size;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setTouchTolerance( float size ) {
        TOUCH_TOLERANCE = size;
    }

    public void setDivisor( int size ) {
        divisor = size;
    }

    public void setSmoothActive(boolean activado) { isSmooth = activado; }

    public void setTranslationActive(boolean activado) { isTraslation = activado; }

    public ArrayList<Integer> smoothTheLine( ArrayList<Integer> line ) {

        ArrayList<Integer> smoothLine = new ArrayList<Integer>();

        for(int i = 0; i<line.size(); i++) {
            if( i == 0 ) {
                smoothLine.add( line.get(i) );
            }
            else if( i == line.size()-1 ) {
                smoothLine.add( line.get(line.size()-1) );
            }
            else if( i == 1 ) {
                smoothLine.add( ( line.get(i-1) + line.get(i) + line.get(i+1) )/3 );
            }
            else if( i == line.size()-2 ) {
                smoothLine.add( ( line.get(line.size()-3) + line.get(line.size()-2) + line.get(line.size()-1) )/3 );
            }
            else if( i == 2 ) {
                smoothLine.add( ( line.get(i-2) + line.get(i-1) + line.get(i) + line.get(i+1) + line.get(i+2) )/5 );
            }
            else if( i == line.size()-3 ) {
                smoothLine.add( ( line.get(line.size()-5) + line.get(line.size()-4) + line.get(line.size()-3) + line.get(line.size()-2) + line.get(line.size()-1) )/5 );
            }
            else {
                smoothLine.add( ( line.get(i-3) + line.get(i-2) + line.get(i-1) + line.get(i) + line.get(i+1) + line.get(i+2) + line.get(i+3) )/7 );
            }
        }

        return smoothLine;

    }

    public void reDrawSmoothLine() {

        // redraw the bucking smoother signature
        startNew();

        int r=0;
        for(int i = 0; i<smoothX.size(); i++) {
            if(smoothX.get(i)!=-1) {
                if(smoothX.get(i+1)!=-1) {
                    if( r==0 ) {
                        //start
                        drawPath.reset();
                        drawPath.moveTo(smoothX.get(i), smoothY.get(i));
                        r=1;
                    }
                    else {
                        // moveto
                        drawPath.quadTo(smoothX.get(i), smoothY.get(i), (smoothX.get(i+1) + smoothX.get(i))/2, (smoothY.get(i+1) + smoothY.get(i))/2);
                    }
                    //coordenadas = coordenadas + Integer.toString(smoothX.get(i)) + " " +  Integer.toString(smoothY.get(i)) + " " + Integer.toString(smoothX.get(i+1)) + " " + Integer.toString(smoothY.get(i+1)) + "\n";
                }
                else {
                    // end
                    r=0;
                    drawPath.lineTo(smoothX.get(i), smoothY.get(i));
                    // commit the path to our offscreen
                    drawCanvas.drawPath(drawPath, drawPaint);
                    // kill this so we don't double draw
                    drawPath.reset();
                    i++;
                }
            }
        }

    }
}
