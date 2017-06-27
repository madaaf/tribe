package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tarek360.instacapture.InstaCapture;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.BitmapUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 13/06/2017.
 */

public class ScreenshotView extends FrameLayout {

  public static final int FLASH_DURATION = 500;
  private final int CORNER_SCREENSHOT = 5;
  private final int SCREENSHOT_DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @Inject Navigator navigator;

  @BindView(R.id.txtShareScreenshot) TextViewFont txtShareScreenshot;

  @BindView(R.id.btnShareScreenshot) ImageView btnShareScreenshot;

  @BindView(R.id.btnCloseScreenshot) ImageView btnCloseScreenshot;

  @BindView(R.id.viewScreenShot) ImageView viewScreenShot;

  @BindView(R.id.viewBGScreenshot) View viewBGScreenshot;

  @BindView(R.id.viewFlash) FrameLayout viewFlash;

  @BindView(R.id.layoutScreenShotControls) LinearLayout layoutScreenShotControls;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private boolean takeScreenshotEnable = true;
  private Bitmap bitmapWatermarked;
  private Bitmap roundedBitmap;
  private Context context;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public ScreenshotView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public ScreenshotView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  ///////////////////////
  //       PUBLIC      //
  ///////////////////////

  public void takeScreenshot() {
    if (takeScreenshotEnable) {
      takeScreenshotEnable = false;
      subscriptions.add(InstaCapture.getInstance((Activity) getContext())
          .captureRx()
          .subscribe(new Subscriber<Bitmap>() {
            @Override public void onCompleted() {
            }

            @Override public void onError(Throwable e) {
            }

            @Override public void onNext(Bitmap bitmap) {
              bitmapWatermarked =
                  BitmapUtils.watermarkBitmap(screenUtils, getResources(), bitmap, context);

              roundedBitmap =
                  UIUtils.getRoundedCornerBitmap(bitmapWatermarked, Color.WHITE, CORNER_SCREENSHOT,
                      CORNER_SCREENSHOT * 2, getContext());

              BitmapUtils.saveScreenshotToDefaultDirectory(getContext(), bitmapWatermarked);

              viewScreenShot.setImageBitmap(roundedBitmap);
              viewScreenShot.setVisibility(View.VISIBLE);
              viewScreenShot.animate()
                  .alpha(1f)
                  .setDuration(SCREENSHOT_DURATION)
                  .setStartDelay(FLASH_DURATION)
                  .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                      viewScreenShot.animate().setListener(null).start();
                      setLayoutScreenshotControlsAnimation();
                    }
                  })
                  .start();

              viewFlash.animate()
                  .setDuration(FLASH_DURATION)
                  .alpha(1f)
                  .withEndAction(() -> viewFlash.animate()
                      .setDuration(FLASH_DURATION)
                      .alpha(0f)
                      .withEndAction(() -> viewFlash.animate().setListener(null).start()));

              viewBGScreenshot.setAlpha(1f);
              viewBGScreenshot.setVisibility(View.VISIBLE);
            }
          }));
    }
  }

  @OnClick(R.id.btnShareScreenshot) public void onShareScreenshotClick() {
    setIntent();
  }

  @OnClick(R.id.btnCloseScreenshot) public void onCloseScrenshotControls() {
    Animation slideToBottomAnimaton =
        AnimationUtils.loadAnimation(getContext(), R.anim.slide_to_bottom);
    slideToBottomAnimaton.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        layoutScreenShotControls.setVisibility(View.GONE);
      }
    });
    layoutScreenShotControls.startAnimation(slideToBottomAnimaton);

    Animation animation3 = AnimationUtils.loadAnimation(getContext(), R.anim.screenshot_anim3);
    animation3.setStartOffset(200);
    animation3.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        viewBGScreenshot.animate().alpha(0f).setDuration(1000).withEndAction(() -> {
          takeScreenshotEnable = true;
        });
      }
    });
    viewScreenShot.startAnimation(animation3);

    Toast.makeText(getContext(),
        EmojiParser.demojizedText(getContext().getString(R.string.live_screenshot_saved_toast)),
        Toast.LENGTH_SHORT).show();
  }

  ///////////////////////
  //       PRIVATE     //
  ///////////////////////

  private void setIntent() {
    String pathofBmp =
        MediaStore.Images.Media.insertImage(getContext().getContentResolver(), roundedBitmap, null,
            null);
    Uri bmpUri = Uri.parse(pathofBmp);
    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(Intent.EXTRA_STREAM, bmpUri);
    intent.setType("image/png");
    intent.putExtra(android.content.Intent.EXTRA_TEXT,
        EmojiParser.demojizedText(getContext().getString(R.string.live_share_screenshot_caption)));
    getContext().startActivity(Intent.createChooser(intent, null));
  }

  private void initView(Context context) {
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_screenshot, this, true);
    unbinder = ButterKnife.bind(this);
  }

  private void slideFromBottom(View v, float overshootTension, int startOffset) {
    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_bottom);
    animation.setInterpolator(new OvershootInterpolator(overshootTension));
    animation.setStartOffset(startOffset);
    v.startAnimation(animation);
  }

  private void setLayoutScreenshotControlsAnimation() {
    layoutScreenShotControls.setVisibility(VISIBLE);
    viewBGScreenshot.setVisibility(VISIBLE);

    slideFromBottom(txtShareScreenshot, 1.2f, 200);
    slideFromBottom(btnShareScreenshot, 1.1f, 300);
    slideFromBottom(btnCloseScreenshot, 1.3f, 400);

    Animation scaleAnim = AnimationUtils.loadAnimation(getContext(), R.anim.screenshot_anim2);
    scaleAnim.setFillAfter(true);
    scaleAnim.setAnimationListener(new AnimationListenerAdapter() {

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
      }
    });

    viewScreenShot.startAnimation(scaleAnim);
  }

  ///////////////////////
  //     CYCLE LIFE    //
  ///////////////////////
  @Override protected void onDetachedFromWindow() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    clearAnimation();
    super.onDetachedFromWindow();
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }
}
