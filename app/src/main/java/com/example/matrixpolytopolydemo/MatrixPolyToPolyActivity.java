package com.example.matrixpolytopolydemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MatrixPolyToPolyActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(new PolyToPolyView(this));
  }

  class PolyToPolyView extends View {

    private Bitmap mBitmap;
    private Matrix mMatrix;

    public PolyToPolyView(Context context) {
      super(context);
      mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lufei);
      mMatrix = new Matrix();
      float[] src = {
        0,
        0, //
        mBitmap.getWidth(),
        0, //
        mBitmap.getWidth(),
        mBitmap.getHeight(), //
        0,
        mBitmap.getHeight()
      };
      float[] dst = {
        0,
        0, //
        mBitmap.getWidth(),
        100, //
        mBitmap.getWidth(),
        mBitmap.getHeight() - 100, //
        0,
        mBitmap.getHeight()
      };
      int i = src.length >> 1;
      Log.i(TAG, "PolyToPolyView: "+i);
      mMatrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
    }

    private static final String TAG = "PolyToPolyView";

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      canvas.drawBitmap(mBitmap, mMatrix, null);
    }
  }
}
