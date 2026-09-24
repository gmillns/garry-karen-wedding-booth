package uk.co.gmillns.pad13photobooth;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class DrawingView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final ArrayList<Path> paths = new ArrayList<>();
    private final ArrayList<Path> undone = new ArrayList<>();
    private Path current;
    private boolean eraser = false;
    private float lastX, lastY;
    private static final float TOUCH_TOLERANCE = 2.5f;

    public DrawingView(Context c, AttributeSet a) { super(c,a); init(); }
    public DrawingView(Context c) { super(c); init(); }

    private void init(){
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        paint.setColor(Color.rgb(25,35,70));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(7f);
        setBackgroundColor(Color.rgb(255,253,247));
    }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        for(Path p:paths) c.drawPath(p,paint);
    }

    @Override public boolean onTouchEvent(MotionEvent e){
        float x=e.getX(), y=e.getY();
        if(eraser && (e.getAction()==MotionEvent.ACTION_DOWN || e.getAction()==MotionEvent.ACTION_MOVE)){
            eraseNear(x,y); return true;
        }
        switch(e.getAction()){
            case MotionEvent.ACTION_DOWN:
                current=new Path(); current.moveTo(x,y); paths.add(current); undone.clear();
                lastX=x; lastY=y; invalidate(); return true;
            case MotionEvent.ACTION_MOVE:
                if(current!=null){
                    float dx=Math.abs(x-lastX), dy=Math.abs(y-lastY);
                    if(dx>=TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE){
                        current.quadTo(lastX,lastY,(x+lastX)/2f,(y+lastY)/2f);
                        lastX=x; lastY=y; invalidate();
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(current!=null){ current.lineTo(x,y); invalidate(); current=null; }
                performClick(); return true;
        }
        return false;
    }

    @Override public boolean performClick(){ super.performClick(); return true; }

    private void eraseNear(float x,float y){
        for(int i=paths.size()-1;i>=0;i--){
            RectF r=new RectF(); paths.get(i).computeBounds(r,true); r.inset(-45,-45);
            if(r.contains(x,y)){ paths.remove(i); invalidate(); break; }
        }
    }

    public void undo(){ if(!paths.isEmpty()){ undone.add(paths.remove(paths.size()-1)); invalidate(); } }
    public void redo(){ if(!undone.isEmpty()){ paths.add(undone.remove(undone.size()-1)); invalidate(); } }
    public void clearAll(){ paths.clear(); undone.clear(); current=null; invalidate(); }
    public void setEraser(boolean on){ eraser=on; }
    public boolean isEmpty(){ return paths.isEmpty(); }
    public Bitmap snapshot(){
        Bitmap b=Bitmap.createBitmap(Math.max(1,getWidth()),Math.max(1,getHeight()),Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b); draw(c); return b;
    }
}
