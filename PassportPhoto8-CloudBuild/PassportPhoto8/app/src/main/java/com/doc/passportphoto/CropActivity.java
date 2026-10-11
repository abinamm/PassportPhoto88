package com.doc.passportphoto;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CropActivity extends Activity {
    private CropView cropView;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            Uri uri = getIntent().getData(); if (uri == null) throw new Exception("No photo selected");
            Bitmap bitmap; try (InputStream in = getContentResolver().openInputStream(uri)) { bitmap = BitmapFactory.decodeStream(in); }
            if (bitmap == null) throw new Exception("Unsupported image");
            LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16), dp(18), dp(16), dp(16)); root.setBackgroundColor(Color.rgb(248,250,249));
            TextView title = new TextView(this); title.setText("Crop your photo"); title.setTextSize(23); title.setTextColor(Color.rgb(25,45,41)); title.setTypeface(null,1); root.addView(title);
            TextView hint = new TextView(this); hint.setText("Drag the image to position the face inside the portrait frame. Use zoom buttons below."); hint.setTextSize(14); hint.setTextColor(Color.rgb(87,101,97)); hint.setPadding(0,dp(8),0,dp(12)); root.addView(hint);
            cropView = new CropView(bitmap); root.addView(cropView, new LinearLayout.LayoutParams(-1, 0, 1f));
            LinearLayout controls = new LinearLayout(this); controls.setOrientation(LinearLayout.HORIZONTAL);
            Button minus = button("− Zoom"); minus.setOnClickListener(v -> cropView.zoom(0.9f)); controls.addView(minus, new LinearLayout.LayoutParams(0,dp(48),1f));
            Button plus = button("+ Zoom"); plus.setOnClickListener(v -> cropView.zoom(1.1f)); LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(0,dp(48),1f); pp.leftMargin=dp(8); controls.addView(plus,pp); root.addView(controls);
            Button done = button("Use this crop"); done.setOnClickListener(v -> saveCrop()); LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(-1,dp(50)); dp.topMargin=dp(10); root.addView(done,dp);
            setContentView(root);
        } catch (Exception e) { Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show(); finish(); }
    }
    private int dp(float v) { return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    private Button button(String s) { Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(23,107,91))); return b; }
    private void saveCrop() {
        try { Bitmap result = cropView.getCroppedBitmap(); File f = new File(getCacheDir(), "passport_crop.jpg"); try(FileOutputStream out = new FileOutputStream(f)){ result.compress(Bitmap.CompressFormat.JPEG, 96, out); } IntentResult(); }
        catch(Exception e){ Toast.makeText(this,"Crop failed: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
    }
    private void IntentResult() { android.content.Intent result = new android.content.Intent(); result.putExtra("crop_path", new File(getCacheDir(), "passport_crop.jpg").getAbsolutePath()); setResult(RESULT_OK,result); finish(); }

    private class CropView extends View {
        private final Bitmap bitmap; private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG); private float scale=1f, dx=0, dy=0, lastX,lastY; private boolean moved;
        private RectF frame = new RectF(); private float baseScale;
        CropView(Bitmap b){super(CropActivity.this); bitmap=b; setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas); canvas.drawColor(Color.rgb(30,36,35));
            float fw=getWidth()*0.72f, fh=fw*9f/7f; if(fh>getHeight()*0.78f){fh=getHeight()*0.78f;fw=fh*7f/9f;}
            frame.set((getWidth()-fw)/2f,(getHeight()-fh)/2f,(getWidth()+fw)/2f,(getHeight()+fh)/2f);
            baseScale=Math.max(fw/bitmap.getWidth(),fh/bitmap.getHeight()); if(scale<1f) scale=1f;
            float drawScale=baseScale*scale; float bw=bitmap.getWidth()*drawScale,bh=bitmap.getHeight()*drawScale;
            float cx=frame.centerX()+dx, cy=frame.centerY()+dy;
            RectF dest=new RectF(cx-bw/2,cy-bh/2,cx+bw/2,cy+bh/2); canvas.save(); canvas.clipRect(frame); canvas.drawBitmap(bitmap,null,dest,paint); canvas.restore();
            Paint overlay=new Paint(); overlay.setColor(0x99000000); canvas.drawRect(0,0,getWidth(),frame.top,overlay); canvas.drawRect(0,frame.bottom,getWidth(),getHeight(),overlay); canvas.drawRect(0,frame.top,frame.left,frame.bottom,overlay); canvas.drawRect(frame.right,frame.top,getWidth(),frame.bottom,overlay);
            Paint border=new Paint(Paint.ANTI_ALIAS_FLAG); border.setColor(Color.WHITE); border.setStyle(Paint.Style.STROKE); border.setStrokeWidth(dp(2)); canvas.drawRect(frame,border);
            border.setStrokeWidth(dp(1)); border.setColor(0xAAFFFFFF); canvas.drawLine(frame.left,frame.centerY(),frame.right,frame.centerY(),border); canvas.drawLine(frame.centerX(),frame.top,frame.centerX(),frame.bottom,border);
        }
        void zoom(float factor){scale=Math.max(1f,Math.min(4f,scale*factor));invalidate();}
        @Override public boolean onTouchEvent(MotionEvent e){switch(e.getAction()){case MotionEvent.ACTION_DOWN:lastX=e.getX();lastY=e.getY();moved=true;return true;case MotionEvent.ACTION_MOVE:if(moved){dx+=e.getX()-lastX;dy+=e.getY()-lastY;lastX=e.getX();lastY=e.getY();invalidate();}return true;case MotionEvent.ACTION_UP:moved=false;return true;}return true;}
        Bitmap getCroppedBitmap(){
            float fw=frame.width(),fh=frame.height(); float drawScale=baseScale*scale; float centerX=frame.centerX()+dx,centerY=frame.centerY()+dy;
            float left=(frame.left-(centerX-bitmap.getWidth()*drawScale/2f))/drawScale;
            float top=(frame.top-(centerY-bitmap.getHeight()*drawScale/2f))/drawScale;
            float cropW=fw/drawScale,cropH=fh/drawScale;
            left=Math.max(0,Math.min(bitmap.getWidth()-cropW,left)); top=Math.max(0,Math.min(bitmap.getHeight()-cropH,top));
            int l=(int)left,t=(int)top,w=(int)Math.min(bitmap.getWidth()-l,cropW),h=(int)Math.min(bitmap.getHeight()-t,cropH);
            Bitmap raw=Bitmap.createBitmap(bitmap,l,t,Math.max(1,w),Math.max(1,h)); return Bitmap.createScaledBitmap(raw,700,900,true);
        }
    }
}
