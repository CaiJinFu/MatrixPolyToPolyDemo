# MatrixPolyToPolyDemo
折叠布局Demo

# 前言
在查看Matrix相关资料的时候，发现了setPolyToPoly这个方法，可以实现折叠布局，觉得挺有意思的，就查看了相关资料。发现鸿洋大神写的一篇很不错，[Android FoldingLayout 折叠布局 原理及实现（一）](https://blog.csdn.net/lmj623565791/article/details/44278417)，这篇文章是对鸿洋的这篇文章的一些补充吧，由于鸿洋的文章里的代码是在CSDN的，需要下载，所以我将代码上传至GitHub，[MatrixPolyToPolyDemo](https://github.com/CaiJinFu/MatrixPolyToPolyDemo)，有兴趣的可以看看。还有一篇文章也不错，鸿洋的文章里也提到了，[Android Folding View（折叠视图、控件）](https://blog.csdn.net/wangjinyu501/article/details/24289861)，可以参考一下。先上个效果图![图1](https://img-blog.csdnimg.cn/20200521102422151.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L01yX0ppbmdGdQ==,size_16,color_FFFFFF,t_70)
 这个效果就是使用Matrix的setPolyToPoly实现的。下面就来讲一下具体的实现。

## Matrix的setPolyToPoly使用

想要实现折叠，最重要的就是其核心的原理了，那么第一步我们要了解的就是，如何能把一张正常显示的图片，让它能够进行偏移显示。

其实精髓就在于Matrix的setPolyToPoly的方法。
 
```java
public boolean setPolyToPoly(float[] src, int srcIndex,  float[] dst, int dstIndex,int pointCount) 
```

简单看一下该方法的参数，src代表变换前的坐标；dst代表变换后的坐标；从src到dst的变换，可以通过srcIndex和dstIndex来制定第一个变换的点，一般可能都设置位0。pointCount代表支持的转换坐标的点数，最多支持4个（取值范围是: 0到4）。其实也就是你定义的float[] src这个数组除以2的数字。也可以这么理解：
0 相当于reset
1 相当于translate
2 可以进行 缩放、旋转、平移 变换
3 可以进行 缩放、旋转、平移、错切 变换
4 可以进行 缩放、旋转、平移、错切以及任何形变

我的理解是，一个点的情况下，除了平移是做不了别的操作的，两个点就是一条直线，三个点，四个点就是三角形跟四边形。

用一段简单的代码看下怎么使用：

```java
package com.example.matrixpolytopolydemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
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
      mMatrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      canvas.drawBitmap(mBitmap, mMatrix, null);
    }
  }
}

```
我们编写了一个PolyToPolyView作为我们的Activity的主视图。

在PolyToPolyView中，我们加载了一张图片，初始化我们的Matrix，注意src和dst两个数组，src就是正常情况下图片的4个顶点。dst将图片右侧两个点的y坐标做了些许的修改。srcIndex和 dstIndex都设置为0，pointCount为src.length >> 1，也就是src.length 除以2。

大家可以在纸上稍微标一下src和dst的四个点的位置。

最后我们在onDraw的时候进行图像的绘制，效果为：

![图2](https://img-blog.csdnimg.cn/20200521104557303.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L01yX0ppbmdGdQ==,size_16,color_FFFFFF,t_70)
可以看到我们通过matrix.setPolyToPoly实现了图片的倾斜，那么引入到折叠的情况，假设折叠两次，大家有思路么，考虑一下，没有的话，继续往下看。

## 引入阴影

其实阴影应该在实现初步的折叠以后来说，这样演示其实比较方便，但是为了降低其理解的简单性，我们先把阴影抽取出来说。

假设我们现在要给上图加上阴影，希望的效果图是这样的：![图3](https://img-blog.csdnimg.cn/20200521105955537.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L01yX0ppbmdGdQ==,size_16,color_FFFFFF,t_70)

```java
package com.example.matrixpolytopolydemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MatrixPolyToPolyWithShadowActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(new PolyToPolyView(this));
  }

  class PolyToPolyView extends View {

    private Bitmap mBitmap;
    private Matrix mMatrix;

    private Paint mShadowPaint;
    private Matrix mShadowGradientMatrix;
    private LinearGradient mShadowGradientShader;

    public PolyToPolyView(Context context) {
      super(context);
      mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lufei);
      mMatrix = new Matrix();

      mShadowPaint = new Paint();
      mShadowPaint.setStyle(Paint.Style.FILL);
      mShadowGradientShader =
          new LinearGradient(0, 0, 0.5f, 0, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
      mShadowPaint.setShader(mShadowGradientShader);

      mShadowGradientMatrix = new Matrix();
      mShadowGradientMatrix.setScale(mBitmap.getWidth(), 1);
      mShadowGradientShader.setLocalMatrix(mShadowGradientMatrix);
      mShadowPaint.setAlpha((int) (0.9 * 255));
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      canvas.save();
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
      mMatrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);

      canvas.concat(mMatrix);
      canvas.drawBitmap(mBitmap, 0, 0, null);
      // 绘制阴影
      canvas.drawRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), mShadowPaint);
      canvas.restore();
    }
  }
}

```
重点看mShadowPaint，mShadowGradientShader，mShadowGradientMatrix一个是画笔，我们为画笔设置了一个渐变的Shader，这个Shader的参数为
new LinearGradient(0, 0, 0.5f, 0,Color.BLACK, Color.TRANSPARENT, TileMode.CLAMP);

它的构造方法为：

```java
public LinearGradient(float x0, float y0, float x1, float y1, int color0, int color1, TileMode tile)
```

第一个参数为线性起点的x坐标
第二个参数为线性起点的y坐标
第三个参数为线性终点的x坐标
第四个参数为线性终点的y坐标
第五个参数为渐变起始颜色
第六个参数为渐变终止颜色
第七个参数为渲染器平铺的模式，一共有三种
-CLAMP
边缘拉伸
-REPEAT
在水平和垂直两个方向上重复，相邻图像没有间隙
-MIRROR
以镜像的方式在水平和垂直两个方向上重复，相邻图像有间隙
关于LinearGradient其中的设置用法等，可以参考这篇文章[Android中的LinearGradient](https://www.jianshu.com/p/a9d09cb7577f)。
所以这里代表起点（0，0）、终点（0.5f，0）；颜色从BLACK到透明；模式为CLAMP，也就是拉伸最后一个像素。

```java
mShadowGradientMatrix.setScale(mBitmap.getWidth(), 1);
```
这里是一个缩放的操作，

构造参数为：

```java
setScale(float sx,float sy)
```
设置Matrix进行缩放，sx,sy控制X,Y方向上的缩放比例；

这里你可能会问，这才为0.5个像素的区域设置了渐变，不对呀，恩，是的，继续看接下来我们使用了setLocalMatrix(mShadowGradientMatrix);，而这个

mShadowGradientMatrix将横坐标扩大了mBitmap.getWidth()倍，也就是说现在设置渐变的区域为（0.5f*mBitmap.getWidth()，0）半张图的大小，那么后半张图呢？

后半张应用CLAMP模式，拉伸的透明。
 
 关于Shader、setLocalMatrix等用法也可以参考：[Android BitmapShader 实战 实现圆形、圆角图片](https://blog.csdn.net/lmj623565791/article/details/41967509)

## 初步实现折叠

了解了原理以及阴影的绘制以后，接下来要开始学习真正的去折叠了，我们的目标效果为：

![图4](https://img-blog.csdnimg.cn/20200521134410757.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L01yX0ppbmdGdQ==,size_16,color_FFFFFF,t_70)
图片折叠成了8份，且阴影的范围为：每个沉下去夹缝的左右两侧，左侧黑色半透明遮盖，右侧短距离的黑色到透明阴影（大家可以仔细看）。

现在其实大家以及会将图片简单倾斜和添加阴影了，那么唯一的难点就是怎么将一张图分成很多快，我相信每块的折叠大家都会。

其实我们可以通过绘制该图多次，比如第一次绘制往下倾斜；第二次绘制网上倾斜；这样就和我们图2的实现类似了，只需要利用setPolyToPoly。

那么绘制多次，每次显示肯定不是一整张图，比如第一次，我只想显示第一块，所以我们还需要clipRect的配合，说到这，应该以及揭秘了~~~

```java
package com.example.matrixpolytopolydemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class FoldActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(new PolyToPolyView(this));
  }

  class PolyToPolyView extends View {

    private static final int NUM_OF_POINT = 8;
    /** 图片的折叠后的总宽度 */
    private int mTranslateDis;

    /** 折叠后的总宽度与原图宽度的比例 */
    private float mFactor = 0.8f;
    /** 折叠块的个数 */
    private int mNumOfFolds = 8;

    private Matrix[] mMatrices = new Matrix[mNumOfFolds];

    private Bitmap mBitmap;

    /** 绘制黑色透明区域 */
    private Paint mSolidPaint;

    /** 绘制阴影 */
    private Paint mShadowPaint;

    private Matrix mShadowGradientMatrix;
    private LinearGradient mShadowGradientShader;

    /** * 原图每块的宽度 */
    private int mFlodWidth;
    /** 折叠时，每块的宽度 */
    private int mTranslateDisPerFlod;

    public PolyToPolyView(Context context) {
      super(context);
      mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lufei);

      // 折叠后的总宽度
      mTranslateDis = (int) (mBitmap.getWidth() * mFactor);
      // 原图每块的宽度
      mFlodWidth = mBitmap.getWidth() / mNumOfFolds;
      // 折叠时，每块的宽度
      mTranslateDisPerFlod = mTranslateDis / mNumOfFolds;

      // 初始化matrix
      for (int i = 0; i < mNumOfFolds; i++) {
        mMatrices[i] = new Matrix();
      }

      mSolidPaint = new Paint();
      int alpha = (int) (255 * mFactor * 0.8f);
      mSolidPaint.setColor(Color.argb((int) (alpha * 0.8F), 0, 0, 0));

      mShadowPaint = new Paint();
      mShadowPaint.setStyle(Paint.Style.FILL);
      mShadowGradientShader =
          new LinearGradient(0, 0, 0.5f, 0, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
      mShadowPaint.setShader(mShadowGradientShader);
      mShadowGradientMatrix = new Matrix();
      mShadowGradientMatrix.setScale(mFlodWidth, 1);
      mShadowGradientShader.setLocalMatrix(mShadowGradientMatrix);
      mShadowPaint.setAlpha(alpha);

      // 纵轴减小的那个高度，用勾股定理计算下
      int depth =
          (int) Math.sqrt(mFlodWidth * mFlodWidth - mTranslateDisPerFlod * mTranslateDisPerFlod)
              / 2;

      // 转换点
      float[] src = new float[NUM_OF_POINT];
      float[] dst = new float[NUM_OF_POINT];

      /** 原图的每一块，对应折叠后的每一块，方向为左上、右上、右下、左下，大家在纸上自己画下 */
      for (int i = 0; i < mNumOfFolds; i++) {
        src[0] = i * mFlodWidth;
        src[1] = 0;
        src[2] = src[0] + mFlodWidth;
        src[3] = 0;
        src[4] = src[2];
        src[5] = mBitmap.getHeight();
        src[6] = src[0];
        src[7] = src[5];

        boolean isEven = i % 2 == 0;

        dst[0] = i * mTranslateDisPerFlod;
        dst[1] = isEven ? 0 : depth;
        dst[2] = dst[0] + mTranslateDisPerFlod;
        dst[3] = isEven ? depth : 0;
        dst[4] = dst[2];
        dst[5] = isEven ? mBitmap.getHeight() - depth : mBitmap.getHeight();
        dst[6] = dst[0];
        dst[7] = isEven ? mBitmap.getHeight() : mBitmap.getHeight() - depth;

        // setPolyToPoly
        mMatrices[i].setPolyToPoly(src, 0, dst, 0, src.length >> 1);
      }
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      // 绘制mNumOfFolds次
      for (int i = 0; i < mNumOfFolds; i++) {

        canvas.save();
        // 将matrix应用到canvas
        canvas.concat(mMatrices[i]);
        // 控制显示的大小
        canvas.clipRect(mFlodWidth * i, 0, mFlodWidth * i + mFlodWidth, mBitmap.getHeight());
        // 绘制图片
        canvas.drawBitmap(mBitmap, 0, 0, null);
        // 移动绘制阴影
        canvas.translate(mFlodWidth * i, 0);
        if (i % 2 == 0) {
          // 绘制黑色遮盖
          canvas.drawRect(0, 0, mFlodWidth, mBitmap.getHeight(), mSolidPaint);
        } else {
          // 绘制阴影
          canvas.drawRect(0, 0, mFlodWidth, mBitmap.getHeight(), mShadowPaint);
        }
        canvas.restore();
      }
    }
  }
}

```

简单讲解下，不去管绘制阴影的部分，其实折叠就是：
1、初始化转换点，这里注释说的很清楚，大家最好在纸上绘制下，标一下每个变量。

2、为matrix.setPolyToPoly

3、绘制时使用该matrix，且clipRect控制显示区域（这个区域也很简单，原图的第一块到最后一块），最后就是绘制bitmap了。

需要注意的是，需先调用一下`canvas.save();`再调用`canvas.restore();`，否则会出现不一样的效果，可以看[Android中canvas.save()和canvas.restore()的使用](https://www.cnblogs.com/lcchuguo/p/5117220.html)。
阴影这里大家可以换个明亮点的图片去看看~~
对于类似这种效果的，一定要拿出稿纸笔去画一画，否则很难弄明白。
我个人建议可以写个demo，对着敲一遍，虽然没什么难度，但是这样印象会比较深刻，代码除了看还要敲，增强码感。谁让我们是码农呢，哈哈！
最后附上代码连接[MatrixPolyToPolyDemo](https://github.com/CaiJinFu/MatrixPolyToPolyDemo)

