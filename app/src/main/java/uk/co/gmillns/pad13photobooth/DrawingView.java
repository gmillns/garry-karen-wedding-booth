package uk.co.gmillns.pad13photobooth;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;

public class DrawingView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<Path> paths = new ArrayList<>();
    private final ArrayList<Path> undone = new ArrayList<>();
    private Path current;
    private boolean eraser = false;

    public DrawingView(Context c, AttributeSet a) { super(c,a); init(); }
    public DrawingView(Context c) { super(c); init(); }
    private void init(){
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        paint.setColor(Color.rgb(25,35,70)); paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND); paint.setStrokeWidth(6f);
        setBackgroundColor(Color.rgb(255,253,247));
    }
    @Override protected void onDraw(Canvas c){ super.onDraw(c); for(Path p:paths)c.drawPath(p,paint); }
    @Override public boolean onTouchEvent(MotionEvent e){
        float x=e.getX(),y=e.getY();
        if(eraser && (e.getAction()==MotionEvent.ACTION_DOWN || e.getAction()==MotionEvent.ACTION_MOVE)){ eraseNear(x,y); return true; }
        switch(e.getAction()){
            case MotionEvent.ACTION_DOWN: current=new Path(); current.moveTo(x,y); paths.add(current); undone.clear(); invalidate(); return true;
            case MotionEvent.ACTION_MOVE: if(current!=null){current.lineTo(x,y);invalidate();} return true;
            case MotionEvent.ACTION_UP: if(current!=null){current.lineTo(x,y);invalidate();current=null;} return true;
        } return false;
    }
    private void eraseNear(float x,float y){
        // Simple whole-stroke eraser: remove the newest nearby stroke using bounds.
        for(int i=paths.size()-1;i>=0;i--){ RectF r=new RectF(); paths.get(i).computeBounds(r,true); r.inset(-35,-35); if(r.contains(x,y)){paths.remove(i);invalidate();break;} }
    }
    public void undo(){ if(!paths.isEmpty()){undone.add(paths.remove(paths.size()-1));invalidate();} }
    public void redo(){ if(!undone.isEmpty()){paths.add(undone.remove(undone.size()-1));invalidate();} }
    public void clearAll(){paths.clear();undone.clear();invalidate();}
    public void setEraser(boolean on){eraser=on;}
    public boolean isEmpty(){return paths.isEmpty();}
    public Bitmap snapshot(){ Bitmap b=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888); Canvas c=new Canvas(b); draw(c); return b; }
}
