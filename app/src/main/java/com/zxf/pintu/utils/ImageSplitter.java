/*
 * Copyright (C) 2015 备胎金服
 * 未经授权允许不得进行拷贝和修改
 *   http://www.btjf.com/
 */
package com.zxf.pintu.utils;

import android.graphics.Bitmap;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangman on 16/3/26 11:31.
 * Email: zhangman523@126.com
 */
public class ImageSplitter {
  public static final String TAG = "ImageSplitter";

  /**
   * 将图片切成，piece*piece
   */
  public static List<ImagePiece> split(Bitmap bitmap, int piece) {
    List<ImagePiece> pieces = new ArrayList<ImagePiece>(piece * piece);
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    int pieceWidth = Math.min(width, height) / piece;
    Log.e(TAG, "imagePiece.index" + (piece));
    for (int i = 0; i < piece; i++) {
      for (int j = 0; j < piece; j++) {
        ImagePiece imagePiece = new ImagePiece();
        int xValue = j * pieceWidth;
        int yValue = i * pieceWidth;
        imagePiece.index = j + i * piece;
        imagePiece.bitmap = Bitmap.createBitmap(bitmap, xValue, yValue, pieceWidth, pieceWidth);
        pieces.add(imagePiece);
      }
    }
    return pieces;
  }
}
