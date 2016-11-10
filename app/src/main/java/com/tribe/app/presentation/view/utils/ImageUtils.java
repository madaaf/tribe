package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Pair;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.camera.utils.BitmapFactoryUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Utility methods for manipulating images.
 * Created by horatiothomas on 8/25/16.
 */
public class ImageUtils {

    public static final int IMG_SIZE = 500;

    private static ScreenUtils screenUtils;

    @Inject
    public ImageUtils(Context context, ScreenUtils screenUtils) {
        this.screenUtils = screenUtils;
    }

    public static Bitmap loadFromPath(String imagePath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / IMG_SIZE, photoH / IMG_SIZE);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        return (bitmap);
    }

    public static Bitmap loadFromInputStream(InputStream inputStream) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 2;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);
        return bitmap;
    }

    public static Bitmap formatForUpload(Bitmap srcBmp) {
        srcBmp = centerCropBitmap(srcBmp);
        srcBmp = Bitmap.createScaledBitmap(srcBmp, IMG_SIZE, IMG_SIZE, true);
        return srcBmp;
    }

    public static Bitmap centerCropBitmap(Bitmap srcBmp) {
        Bitmap dstBmp;

        if (srcBmp.getWidth() >= srcBmp.getHeight()) {
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );
        } else {
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }

        srcBmp.recycle();

        return dstBmp;
    }

    public static Observable<Bitmap> createGroupAvatar(Context context, String id, List<String> urls, int avatarSize) {
        List<Pair<Integer, String>> positionUrls = new ArrayList<>();

        int count = 0;
        for (String url : urls) {
            positionUrls.add(Pair.create(count, url));
            count++;
        }

        int halfSize = avatarSize >> 1;

        return Observable
                .from(positionUrls)
                .flatMap(pair -> {
                    int finalSize = urls.size() > 3 ? (avatarSize) : (pair.first == 0 ? avatarSize : halfSize);
                    FutureTarget<File> futureTarget =
                            Glide.with(context)
                                    .load(pair.second)
                                    .downloadOnly(finalSize, finalSize);
                    File file = null;
                    try {
                        file = futureTarget.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    Pair<Integer, File> pairFile = Pair.create(pair.first, file);

                    return Observable.just(pairFile);
                })
                .toList()
                .map(filesPair -> {
                    List<Pair<Integer, Bitmap>> bitmapList = new ArrayList<>();

                    for (Pair<Integer, File> pair : filesPair) {
                        int finalSize = urls.size() > 3 ? (halfSize) : (pair.first == 0 ? avatarSize : halfSize);

                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inMutable = true;
                        Bitmap temp = BitmapFactoryUtils.decodeFile(pair.second.getAbsolutePath());
                        bitmapList.add(Pair.create(pair.first, BitmapFactoryUtils.scale(temp, finalSize, true)));
                    }

                    return bitmapList;
                })
                .map(bitmapPairList -> {
                    boolean isFull = bitmapPairList.size() > 3;
                    Bitmap base = isFull
                            ? Bitmap.createBitmap(avatarSize, avatarSize, Bitmap.Config.ARGB_8888) : bitmapPairList.get(0).second;
                    Canvas canvas = new Canvas(base);

                    Paint drawPaint = new Paint();
                    drawPaint.setAntiAlias(false);
                    drawPaint.setFilterBitmap(false);

                    int dividerSize = context.getResources().getDimensionPixelSize(R.dimen.divider_size);

                    for (int i = isFull ? 0 : 1; i < bitmapPairList.size(); i++) {
                        int top = 0;
                        int left = 0;

                        switch (bitmapPairList.get(i).first) {
                            case 1: {
                                left = halfSize;
                                top = halfSize;
                                break;
                            }

                            case 2: {
                                left = halfSize;
                                top = 0;
                                break;
                            }

                            case 3: {
                                left = 0;
                                top = halfSize;
                            }
                        }

                        canvas.drawBitmap(
                                bitmapPairList.get(i).second,
                                left,
                                top,
                                drawPaint
                        );

                        bitmapPairList.get(i).second.recycle();
                        bitmapPairList.set(i, null);
                    }

                    Paint drawLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    drawLinePaint.setStrokeWidth(dividerSize);
                    drawLinePaint.setColor(Color.WHITE);

                    if (bitmapPairList.size() == 2) {
                        canvas.drawLine(halfSize, halfSize, avatarSize, halfSize, drawLinePaint);
                        canvas.drawLine(halfSize, halfSize, halfSize, avatarSize, drawLinePaint);
                    } else if (bitmapPairList.size() == 3) {
                        canvas.drawLine(halfSize, halfSize, avatarSize, halfSize, drawLinePaint);
                        canvas.drawLine(halfSize, 0, halfSize, avatarSize, drawLinePaint);
                    } else if (bitmapPairList.size() == 4) {
                        canvas.drawLine(0, halfSize, avatarSize, halfSize, drawLinePaint);
                        canvas.drawLine(halfSize, 0, halfSize, avatarSize, drawLinePaint);
                    }

                    System.out.println("CREATING AVATAR FOR : " + id);

                    File finalFile = FileUtils.getAvatarForGroupId(context, id, FileUtils.PHOTO);
                    FileUtils.bitmapToFile(base, finalFile);
                    return base;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }
}
