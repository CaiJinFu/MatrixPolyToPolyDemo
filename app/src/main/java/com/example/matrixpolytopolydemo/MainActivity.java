package com.example.matrixpolytopolydemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private TextView mTvTilt;
  private TextView mTvShadow;
  private TextView mTvFold;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
  }

  private void initView() {
    mTvTilt = (TextView) findViewById(R.id.tvTilt);
    mTvTilt.setOnClickListener(this);
    mTvShadow = (TextView) findViewById(R.id.tvShadow);
    mTvShadow.setOnClickListener(this);
    mTvFold = (TextView) findViewById(R.id.tvFold);
    mTvFold.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.tvTilt:
        // 图片倾斜
        startActivity(new Intent(this, MatrixPolyToPolyActivity.class));
        break;
      case R.id.tvShadow:
        // 图片加阴影
        startActivity(new Intent(this, MatrixPolyToPolyWithShadowActivity.class));
        break;
      case R.id.tvFold:
        // 折叠效果
        startActivity(new Intent(this, FoldActivity.class));
        break;
      default:
        break;
    }
  }
}
