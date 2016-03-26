package com.zxf.pintu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.zxf.pintu.R;
import com.zxf.pintu.utils.ImagePiece;
import com.zxf.pintu.utils.ImageSplitter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhangman on 16/3/26 11:31.
 * Email: zhangman523@126.com
 */
public class PintuGameLayout extends RelativeLayout implements View.OnClickListener {
  private static final String TAG = "PintuGameLayout";

  /**
   * 设置item的数量n＊n 默认为3
   */
  private int mColumn = 3;
  /**
   * 布局的宽度
   */
  private int mWidth;
  /**
   * 布局的padding
   */
  private int mPadding;
  /**
   * 存放所有的Item
   */
  private ImageView[] mGamePintuItems;
  /**
   * Item的宽度
   */
  private int mItemWidth;
  /**
   * Item横向于纵向的边距
   */
  private int mMargin = 1;
  /**
   * 拼图的照片呢
   */
  private Bitmap mBitmap;
  /**
   * 存放切完以后的图片bean
   */
  private List<ImagePiece> mItemBitmaps;
  private boolean once;
  /**
   * 拼图类型
   */
  private boolean mGameType = false;
  private OnSuccessListener mOnSuccessListener;

  public PintuGameLayout(Context context) {
    this(context, null);
  }

  public PintuGameLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PintuGameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMargin,
        getResources().getDisplayMetrics());
    //设置Layout内边距,四边一致，设置为四内边距中的最小值
    mPadding = min(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    //获得游戏布局的边长
    mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
    if (!once) {
      initBitmap();
      initItem();
    }
    once = true;
    setMeasuredDimension(mWidth, mWidth);
  }

  public void setBitmap(Bitmap mBitmap) {
    this.mBitmap = mBitmap;
  }

  public void setOnSuccessListener(OnSuccessListener mListener) {
    this.mOnSuccessListener = mListener;
  }

  private int min(int... params) {
    int min = params[0];
    for (int param : params) {
      if (min > param) {
        min = param;
      }
    }
    return min;
  }

  /**
   * 初始化Bitmap
   */
  private void initBitmap() {
    if (mBitmap == null) {
      mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.aa);
    }
    mItemBitmaps = ImageSplitter.split(mBitmap, mColumn);
    if (mGameType) {
      mItemBitmaps.get(mItemBitmaps.size() - 1).bitmap = null;
    }
    //将图片切成n＊n份，并排序
    Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {
      @Override public int compare(ImagePiece lhs, ImagePiece rhs) {
        return Math.random() > 0.5 ? 1 : -1;
      }
    });
  }

  /**
   * 初始化Item
   */
  private void initItem() {
    //获得Item 的宽度
    int childWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;
    mItemWidth = childWidth;
    mGamePintuItems = new ImageView[mColumn * mColumn];
    //放置Item
    for (int i = 0; i < mGamePintuItems.length; i++) {
      ImageView item = new ImageView(getContext());
      item.setOnClickListener(this);
      item.setImageBitmap(mItemBitmaps.get(i).bitmap);
      mGamePintuItems[i] = item;
      item.setId(i + 1);
      item.setTag(i + "_" + mItemBitmaps.get(i).index);
      RelativeLayout.LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
      //设置横向边距,不是最后一列
      if ((i + 1) % mColumn != 0) {
        lp.rightMargin = mMargin;
      }
      //如果不是第一列
      if (i % mColumn != 0) {
        lp.addRule(RelativeLayout.RIGHT_OF, mGamePintuItems[i - 1].getId());
      }
      //如果不是第一行，设置纵向边距，非最后一行
      if ((i + 1) > mColumn) {
        lp.topMargin = mMargin;
        lp.addRule(RelativeLayout.BELOW, mGamePintuItems[i - mColumn].getId());
      }
      addView(item, lp);
    }
  }

  private ImageView mFirst;
  private ImageView mSecond;

  @Override public void onClick(View v) {
    if (mGameType) {
      switchPintu((ImageView) v);
    } else {
      //如果正在执行动画,则屏蔽
      if (isAniming) {
        return;
      }
      /**
       * 如果点击的是同一个
       */
      if (mFirst == v) {
        mFirst.setColorFilter(null);
        mFirst = null;
        return;
      }
      //点击第一个Item
      if (mFirst == null) {
        mFirst = (ImageView) v;
        mFirst.setColorFilter(Color.parseColor("#55FF0000"));
      } else {
        mSecond = (ImageView) v;
        exchangeView();
      }
    }
  }

  /**
   * 动画运动的标志
   */
  private boolean isAniming;
  /**
   * 动画层
   */
  private RelativeLayout mAnimaLayout;

  /**
   * 交换两个Item的图片
   */
  private void exchangeView() {
    mFirst.setColorFilter(null);
    setUpAnimLayout();
    //添加FirstView
    ImageView first = new ImageView(getContext());
    first.setImageBitmap(mItemBitmaps.get(getImageIndexByTag((String) mFirst.getTag())).bitmap);
    LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
    lp.leftMargin = mFirst.getLeft() - mPadding;
    lp.topMargin = mFirst.getTop() - mPadding;
    first.setLayoutParams(lp);
    mAnimaLayout.addView(first);
    //添加SecondView
    ImageView second = new ImageView(getContext());
    second.setImageBitmap(mItemBitmaps.get(getImageIndexByTag((String) mSecond.getTag())).bitmap);
    LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
    lp2.leftMargin = mSecond.getLeft() - mPadding;
    lp2.topMargin = mSecond.getTop() - mPadding;
    second.setLayoutParams(lp2);
    mAnimaLayout.addView(second);
    //设置动画
    TranslateAnimation anim = new TranslateAnimation(0, mSecond.getLeft() - mFirst.getLeft(), 0,
        mSecond.getTop() - mFirst.getTop());
    anim.setDuration(150);
    anim.setFillAfter(true);
    first.startAnimation(anim);
    TranslateAnimation animSecond =
        new TranslateAnimation(0, mFirst.getLeft() - mSecond.getLeft(), 0,
            mFirst.getTop() - mSecond.getTop());
    animSecond.setDuration(300);
    animSecond.setFillAfter(true);
    second.startAnimation(animSecond);
    //添加动画监听
    anim.setAnimationListener(new Animation.AnimationListener() {
      @Override public void onAnimationStart(Animation animation) {
        isAniming = true;
        mFirst.setVisibility(INVISIBLE);
        mSecond.setVisibility(INVISIBLE);
      }

      @Override public void onAnimationEnd(Animation animation) {
        String firsTag = (String) mFirst.getTag();
        String secondTag = (String) mSecond.getTag();
        String[] firstParams = firsTag.split("_");
        String[] secondParams = secondTag.split("_");
        mFirst.setImageBitmap(mItemBitmaps.get(Integer.parseInt(secondParams[0])).bitmap);
        mSecond.setImageBitmap(mItemBitmaps.get(Integer.parseInt(firstParams[0])).bitmap);

        mFirst.setTag(secondTag);
        mSecond.setTag(firsTag);

        mFirst.setVisibility(VISIBLE);
        mSecond.setVisibility(VISIBLE);
        mFirst = mSecond = null;
        mAnimaLayout.removeAllViews();
        checkSuccess();
        isAniming = false;
      }

      @Override public void onAnimationRepeat(Animation animation) {

      }
    });
  }

  private void checkSuccess() {
    boolean isSuccess = true;
    for (int i = 0; i < mGamePintuItems.length; i++) {
      ImageView first = mGamePintuItems[i];
      Log.e("TAG", getIndexByTag((String) first.getTag()) + " tag " + (String) first.getTag());
      if (getIndexByTag((String) first.getTag()) != i) {
        isSuccess = false;
      }
    }
    if (isSuccess) {
      if (mOnSuccessListener != null) mOnSuccessListener.onSucess();
    }
  }

  public void nextLevel() {
    this.removeAllViews();
    mAnimaLayout = null;
    mColumn++;
    initBitmap();
    initItem();
  }

  public void restartGame() {
    this.removeAllViews();
    mAnimaLayout = null;
    initBitmap();
    initItem();
  }

  public void changeType() {
    mGameType = !mGameType;
    mColumn = 3;//重置关卡
    restartGame();
  }

  /**
   * 获得图片的真正索引
   */
  private int getIndexByTag(String tag) {
    String[] split = tag.split("_");
    return Integer.parseInt(split[1]);
  }

  private int getImageIndexByTag(String tag) {
    String[] split = tag.split("_");
    return Integer.parseInt(split[0]);
  }

  private void setUpAnimLayout() {
    if (mAnimaLayout == null) {
      mAnimaLayout = new RelativeLayout(getContext());
      addView(mAnimaLayout);
    }
  }

  private void switchPintu(ImageView mImageView) {
    if (isAniming) return;
    //找出在 mGamePintuItems 中的第几个
    int mImageIndex = getIndexFromGamePintuItems(mImageView);
    Log.e(TAG, "this mImageIndex " + mImageIndex);
    if (canUp(mImageIndex)) {
      if (move(mImageView, mGamePintuItems[mImageIndex - mColumn],
          (String) mGamePintuItems[mImageIndex - mColumn].getTag(), "UP")) {
        return;
      }
    }
    if (canDown(mImageIndex)) {
      if (move(mImageView, mGamePintuItems[mImageIndex + mColumn],
          (String) mGamePintuItems[mImageIndex + mColumn].getTag(), "DOWN")) {
        return;
      }
    }
    if (canLeft(mImageIndex)) {
      if (move(mImageView, mGamePintuItems[mImageIndex - 1],
          (String) mGamePintuItems[mImageIndex - 1].getTag(), "LEFT")) {
        return;
      }
    }
    if (canRight(mImageIndex)) {
      if (move(mImageView, mGamePintuItems[mImageIndex + 1],
          (String) mGamePintuItems[mImageIndex + 1].getTag(), "RIGHT")) {
        return;
      }
    }
  }

  private boolean move(ImageView mImageView, ImageView mGamePintuItem, String tag, String down) {
    if (getIndexByTag(tag) == mGamePintuItems.length - 1) {
      Log.e(TAG, down);
      mFirst = mImageView;
      mSecond = mGamePintuItem;
      exchangeView();
      return true;
    } else {
      Log.e(TAG, "ERROR " + down);
      return false;
    }
  }

  private boolean canUp(int mIndex) {
    return mIndex >= mColumn; //除了第一行都可以向上
  }

  private boolean canDown(int mIndex) {
    return mIndex < mColumn * (mColumn - 1); //除了最后一行都可以向下
  }

  private boolean canLeft(int mIndex) {
    return mIndex % mColumn != 0; //除了第一列都可以向左
  }

  private boolean canRight(int mIndex) {
    return mIndex % mColumn != mColumn - 1; //除了最后一列都可以向右
  }

  private int getIndexFromGamePintuItems(ImageView mImageView) {
    for (int i = 0; i < mGamePintuItems.length; i++) {
      if (((String) mImageView.getTag()).equals((String) mGamePintuItems[i].getTag())) {
        return i;
      }
    }
    return 0;
  }

  public interface OnSuccessListener {
    void onSucess();
  }
}
