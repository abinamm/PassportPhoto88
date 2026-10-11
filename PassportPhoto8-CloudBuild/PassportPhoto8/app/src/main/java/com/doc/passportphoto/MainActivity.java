package com.doc.passportphoto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE = 10, CROP_IMAGE = 11, CREATE_PDF = 12;
    private Bitmap croppedBitmap;
    private ImageView preview;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(26), dp(22), dp(20));
        root.setBackgroundColor(Color.rgb(248, 250, 249));

        TextView title = new TextView(this); title.setText("Passport Photo 8"); title.setTextSize(27); title.setTextColor(Color.rgb(25, 45, 41)); title.setTypeface(null, 1);
        root.addView(title);
        TextView subtitle = new TextView(this); subtitle.setText("Make 8 passport photos on one 10 × 15 cm sheet"); subtitle.setTextSize(15); subtitle.setTextColor(Color.rgb(87, 101, 97)); subtitle.setPadding(0, dp(8), 0, dp(18)); root.addView(subtitle);

        preview = new ImageView(this); preview.setBackgroundColor(Color.WHITE); preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(-1, 0, 1f); imageParams.bottomMargin = dp(16); root.addView(preview, imageParams);

        Button choose = button("1  ·  Select and crop photo");
        choose.setOnClickListener(v -> { Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("image/*"); startActivityForResult(Intent.createChooser(i, "Select a photo"), PICK_IMAGE); });
        root.addView(choose);
        Button export = button("2  ·  Save print-ready PDF"); export.setOnClickListener(v -> {
            if (croppedBitmap == null) { Toast.makeText(this, "Select and crop a photo first", Toast.LENGTH_SHORT).show(); return; }
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("application/pdf"); i.putExtra(Intent.EXTRA_TITLE, "passport-photos-10x15cm.pdf"); startActivityForResult(i, CREATE_PDF);
        }); LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1, dp(52)); ep.topMargin = dp(10); root.addView(export, ep);

        status = new TextView(this); status.setText("Photo size: 3.5 × 4.5 cm  •  Sheet: 10 × 15 cm"); status.setTextColor(Color.rgb(87,101,97)); status.setTextSize(12); status.setGravity(Gravity.CENTER); status.setPadding(0, dp(15), 0, 0); root.addView(status);
        setContentView(root);
    }

    private Button button(String text) { Button b = new Button(this); b.setText(text); b.setAllCaps(false); b.setTextSize(15); b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(23,107,91))); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52)); b.setLayoutParams(p); return b; }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        try {
            if (requestCode == PICK_IMAGE) {
                Intent i = new Intent(this, CropActivity.class); i.setData(data.getData()); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivityForResult(i, CROP_IMAGE);
            } else if (requestCode == CROP_IMAGE) {
                String path = data.getStringExtra("crop_path");
                if (path != null) { croppedBitmap = BitmapFactory.decodeFile(path); preview.setImageBitmap(croppedBitmap); status.setText("Crop ready  •  8 copies  •  300 DPI PDF"); }
            } else if (requestCode == CREATE_PDF) {
                createPdf(data.getData()); Toast.makeText(this, "PDF saved", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) { Toast.makeText(this, "Could not process file: " + e.getMessage(), Toast.LENGTH_LONG).show(); }
    }

    private void createPdf(Uri destination) throws Exception {
        // PDF points are 1/72 inch. 15 x 10 cm, landscape.
        final int pageW = 425, pageH = 283;
        PdfDocument pdf = new PdfDocument();
        PdfDocument.Page page = pdf.startPage(new PdfDocument.PageInfo.Builder(pageW, pageH, 1).create());
        Canvas c = page.getCanvas(); c.drawColor(Color.WHITE);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        // 4 columns x 2 rows: each photo is 3.5 x 4.5 cm. Centered on a 15 x 10 cm sheet.
        float photoW = pageW * (3.5f / 15f), photoH = pageH * (4.5f / 10f);
        float startX = (pageW - 4 * photoW) / 2f, startY = (pageH - 2 * photoH) / 2f;
        for (int row = 0; row < 2; row++) for (int col = 0; col < 4; col++) {
            float x = startX + col * photoW, y = startY + row * photoH;
            c.drawBitmap(croppedBitmap, null, new RectF(x, y, x + photoW, y + photoH), p);
        }
        pdf.finishPage(page);
        try (OutputStream out = getContentResolver().openOutputStream(destination)) { if (out == null) throw new Exception("Cannot open output file"); pdf.writeTo(out); }
        pdf.close();
    }
}
