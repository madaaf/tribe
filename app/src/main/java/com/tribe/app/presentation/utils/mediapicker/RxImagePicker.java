package com.tribe.app.presentation.utils.mediapicker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.subjects.PublishSubject;

@Singleton public class RxImagePicker {

  private Context context;
  private PublishSubject<Uri> publishSubject;
  private Uri uri;

  @Inject public RxImagePicker(Context context) {
    this.context = context;
  }

  public Observable<Uri> getActiveSubscription() {
    return publishSubject;
  }

  public Observable<Uri> requestImage(Sources imageSource) {
    publishSubject = PublishSubject.create();
    startImagePickHiddenActivity(imageSource.ordinal());
    return publishSubject;
  }

  void onImagePicked(Uri uri) {
    this.uri = uri;

    if (publishSubject != null) {
      publishSubject.onNext(uri);
      publishSubject.onCompleted();
    }
  }

  private void startImagePickHiddenActivity(int imageSource) {
    Intent intent = new Intent(context, MediaHiddenActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(MediaHiddenActivity.IMAGE_SOURCE, imageSource);
    context.startActivity(intent);
  }

  public Uri getUri() {
    Uri temp = uri;
    uri = null;
    return temp;
  }
}

