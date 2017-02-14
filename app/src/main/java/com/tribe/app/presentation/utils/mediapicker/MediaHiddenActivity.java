package com.tribe.app.presentation.utils.mediapicker;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.activity.BaseActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

public class MediaHiddenActivity extends BaseActivity {

  private static final String KEY_CAMERA_PICTURE_URL = "cameraPictureUrl";

  public static final String IMAGE_SOURCE = "image_source";

  private static final int SELECT_PHOTO = 100;
  private static final int TAKE_PHOTO = 101;

  @Inject RxImagePicker rxImagePicker;

  // VARIABLES
  private Uri cameraPictureUrl;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    init();
    initDependencyInjector();

    if (savedInstanceState == null) {
      handleIntent(getIntent());
    }
  }

  @Override protected void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    outState.putParcelable(KEY_CAMERA_PICTURE_URL, cameraPictureUrl);
    super.onSaveInstanceState(outState);
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    cameraPictureUrl = savedInstanceState.getParcelable(KEY_CAMERA_PICTURE_URL);
  }

  @Override protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
      CropImage.ActivityResult result = CropImage.getActivityResult(data);

      if (resultCode == RESULT_OK) {
        rxImagePicker.onImagePicked(result.getUri());
      } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
        // TODO HANDLE ERROR
      }

      finish();
    } else if (resultCode == RESULT_OK) {
      Uri uri = null;

      switch (requestCode) {
        case SELECT_PHOTO:
          uri = data.getData();
          break;
        case TAKE_PHOTO:
          uri = cameraPictureUrl;
          break;
      }

      startCropImageActivity(uri);
    } else {
      finish();
    }
  }

  private void init() {
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void handleIntent(Intent intent) {
    subscriptions.add(RxPermissions.getInstance(this)
        .request(PermissionUtils.PERMISSIONS_CAMERA)
        .subscribe(granted -> {
          Bundle bundle = new Bundle();
          bundle.putBoolean(TagManagerConstants.user_camera_enabled, granted);
          this.getTagManager().setProperty(bundle);

          if (granted) {
            Sources sourceType = Sources.values()[intent.getIntExtra(IMAGE_SOURCE, 0)];
            int chooseCode = 0;
            Intent pictureChooseIntent = null;

            switch (sourceType) {
              case CAMERA:
                cameraPictureUrl = createImageUri();
                pictureChooseIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                pictureChooseIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPictureUrl);
                chooseCode = TAKE_PHOTO;
                break;
              case GALLERY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                  pictureChooseIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                  pictureChooseIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                  pictureChooseIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                } else {
                  pictureChooseIntent = new Intent(Intent.ACTION_GET_CONTENT);
                }
                pictureChooseIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                pictureChooseIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pictureChooseIntent.setType("image/*");
                chooseCode = SELECT_PHOTO;
                break;
            }

            startActivityForResult(pictureChooseIntent, chooseCode);
          } else {
            finish();
          }
        }));
  }

  private Uri createImageUri() {
    ContentResolver contentResolver = getContentResolver();
    ContentValues cv = new ContentValues();
    String timeStamp =
        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    cv.put(MediaStore.Images.Media.TITLE, timeStamp);
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
  }

  private void startCropImageActivity(Uri imageUri) {
    CropImage.activity(imageUri)
        .setGuidelines(CropImageView.Guidelines.ON)
        .setRequestedSize(500, 500)
        .setAspectRatio(1, 1)
        .setMultiTouchEnabled(true)
        .setActivityMenuIconColor(Color.BLACK)
        .setBorderLineColor(getResources().getColor(R.color.black_opacity_40))
        .setBorderCornerColor(getResources().getColor(R.color.black_opacity_40))
        .start(this);
    overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
  }
}