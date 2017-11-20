package com.tribe.app.presentation.view.component;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class ProfileInfoView extends LinearLayout {

  private static final int DURATION = 100;

  @Inject RxImagePicker rxImagePicker;

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @BindView(R.id.imgAvatar) ImageView imgAvatar;

  @BindView(R.id.editDisplayName) EditTextFont editDisplayName;

  @BindView(R.id.imgDisplayNameInd) ImageView imgDisplayNameInd;

  @BindView(R.id.editUsername) EditTextFont editUsername;

  @BindView(R.id.imgUsernameInd) ImageView imgUsernameInd;

  @BindView(R.id.txtArobase) TextViewFont txtArobase;

  @BindView(R.id.circularProgressUsername) CircularProgressView circularProgressUsername;

  // VARIABLES
  private String usernameInit;
  private String displayNameInit;
  private boolean usernameSelected = false, displayNameSelected = false, displayNameChanged = false;
  private int avatarSize;
  private String imgUri;

  // OBSERVABLES
  Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<String> usernameInput = PublishSubject.create();
  private PublishSubject<String> displayNameInput = PublishSubject.create();
  private PublishSubject<Boolean> infoValid = PublishSubject.create();

  public ProfileInfoView(Context context) {
    super(context);
  }

  public ProfileInfoView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ProfileInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public ProfileInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_profile_info, this);
    unbinder = ButterKnife.bind(this);
    initDimens();
    initDependencyInjector();
    initUi();
  }

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();

    if (subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    super.onDetachedFromWindow();
  }

  public void loadAvatar(String url) {
    refactorInfosValid();

    new GlideUtils.Builder(getContext()).url(url)
        .size(avatarSize)
        .target(imgAvatar)
        .hasPlaceholder(false)
        .load();
  }

  public void setEditDisplayName(String displayName) {
    this.displayNameInit = displayName;
    editDisplayName.setText(displayName);
  }

  public void setEditUsername(String username) {
    this.usernameInit = username;
    editUsername.setText(username);
  }

  public String getDisplayName() {
    return editDisplayName.getText().toString();
  }

  public String getUsername() {
    return editUsername.getText().toString();
  }

  public boolean isDisplayNameSelected() {
    return displayNameSelected;
  }

  public boolean isUsernameSelected() {
    return usernameSelected;
  }

  private void initDimens() {
    avatarSize = getResources().getDimensionPixelSize(R.dimen.avatar_size_with_shadow);
  }

  private void initUi() {
    setOrientation(VERTICAL);
    setBackground(null);

    if (!StringUtils.isEmpty(user.getProfilePicture())) loadAvatar(user.getProfilePicture());
    if (!StringUtils.isEmpty(user.getDisplayName())) setEditDisplayName(user.getDisplayName());
    if (!StringUtils.isEmpty(user.getUsername())) setEditUsername(user.getUsername());

    ArrayList<InputFilter> inputFilters =
        new ArrayList<InputFilter>(Arrays.asList(editUsername.getFilters()));
    inputFilters.add(0, filterAlphanumeric);
    inputFilters.add(1, filterSpace);
    inputFilters.add(2, filterLowercase);
    InputFilter[] newInputFilters = inputFilters.toArray(new InputFilter[inputFilters.size()]);
    editUsername.setFilters(newInputFilters);

    editUsername.setOnFocusChangeListener((v, hasFocus) -> {
      if (hasFocus) editUsername.setSelection(editUsername.getText().length());
    });

    subscriptions.add(
        RxTextView.textChanges(editUsername)
            .map(CharSequence::toString)
            .doOnNext(s -> {
              if (!StringUtils.isEmpty(s)) {
                editUsername.setHint("");
              } else {
                editUsername.setHint(R.string.onboarding_user_username_placeholder);
              }
            })
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(s -> {
              if (!StringUtils.isEmpty(s)) {
                if (s.equals(usernameInit)) {
                  setUsernameValid(true);
                } else if (!StringUtils.isEmpty(s)) {
                  showUsernameProgress();
                }
              } else {
                setUsernameValid(false);
              }
            })
            .subscribe(s -> {
              if (!StringUtils.isEmpty(s)) usernameInput.onNext(s);
            }));

    subscriptions.add(RxTextView.textChanges(editDisplayName)
        .doOnNext(s -> displayNameSelected = false)
        .subscribe(s -> {
          if (s.length() > 1) {
            setDisplayNameValid(true);
          } else if (displayNameChanged) {
            displayNameChanged = true;
            setDisplayNameValid(false);
          } else {
            displayNameChanged = true;
          }
        }));

    subscriptions.add(RxView.clicks(imgAvatar)
        .doOnNext(aVoid -> {
          imgAvatar.setEnabled(false);
          if (editDisplayName.hasFocus()) {
            screenUtils.hideKeyboard(editDisplayName);
          } else if (editUsername.hasFocus()) screenUtils.hideKeyboard(editUsername);
        })
        .delay(200, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .flatMap(aVoid -> {
          imgAvatar.setEnabled(true);
          return DialogFactory.showBottomSheetForCamera(getContext());
        }, ((aVoid, labelType) -> {
          if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA).subscribe(uri -> {
              loadUri(uri);
            }));
          } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY).subscribe(uri -> {
              loadUri(uri);
            }));
          }

          return null;
        }))
        .subscribe());
  }

  public void setInfoFromFacebook(FacebookEntity facebookEntity) {
    new GlideUtils.Builder(getContext()).url(facebookEntity.getProfilePicture())
        .size(avatarSize)
        .target(imgAvatar)
        .load();

    Glide.with(getContext())
        .load(facebookEntity.getProfilePicture())
        .asBitmap()
        .override(ImageUtils.IMG_SIZE, ImageUtils.IMG_SIZE)
        .into(new SimpleTarget<Bitmap>() {
          @Override public void onResourceReady(Bitmap resource,
              GlideAnimation<? super Bitmap> glideAnimation) {
            imgUri =
                Uri.fromFile(FileUtils.bitmapToFile("avatar", resource, getContext())).toString();
          }
        });

    editDisplayName.setText(facebookEntity.getName());

    if ((editUsername.getText() == null || editUsername.getText().length() == 0) &&
        facebookEntity.getEmail() != null) {
      editUsername.setText(StringUtils.usernameFromEmail(facebookEntity.getEmail()));
    }
  }

  public void setUsernameValid(boolean valid) {
    hideUsernameProgress();

    if (valid) {
      usernameSelected = true;
      imgUsernameInd.setImageResource(R.drawable.picto_valid);
    } else {
      usernameSelected = false;
      imgUsernameInd.setImageResource(R.drawable.picto_wrong);
    }

    if (editUsername.getText().length() > 0) {
      showUsernameInd();
    } else {
      hideUsernameInd();
    }

    refactorInfosValid();
  }

  public void showUsernameInd() {
    imgUsernameInd.clearAnimation();
    AnimationUtils.scaleUp(imgUsernameInd, DURATION, new DecelerateInterpolator());
  }

  public void hideUsernameInd() {
    imgUsernameInd.clearAnimation();
    AnimationUtils.scaleDown(imgUsernameInd, DURATION, new DecelerateInterpolator());
  }

  public void showUsernameProgress() {
    usernameSelected = false;
    refactorInfosValid();
    circularProgressUsername.clearAnimation();

    hideUsernameInd();
    AnimationUtils.scaleUp(circularProgressUsername, DURATION, new DecelerateInterpolator());
  }

  public void hideUsernameProgress() {
    circularProgressUsername.clearAnimation();
    AnimationUtils.scaleDown(circularProgressUsername, DURATION, new DecelerateInterpolator());
  }

  public void setDisplayNameValid(boolean valid) {
    if (valid) {
      displayNameSelected = true;
      imgDisplayNameInd.setImageResource(R.drawable.picto_valid);
    } else {
      displayNameSelected = false;
      imgDisplayNameInd.setImageResource(R.drawable.picto_wrong);
    }

    if (editDisplayName.getText().length() > 0) {
      showDisplayNameInd();
    } else {
      hideDisplayNameInd();
    }

    refactorInfosValid();
  }

  public void showDisplayNameInd() {
    if (imgDisplayNameInd.getScaleX() == 0) {
      imgDisplayNameInd.clearAnimation();
      AnimationUtils.scaleUp(imgDisplayNameInd, DURATION, new DecelerateInterpolator());
    }
  }

  public void hideDisplayNameInd() {
    if (imgDisplayNameInd.getScaleX() == 1) {
      imgDisplayNameInd.clearAnimation();
      AnimationUtils.scaleDown(imgDisplayNameInd, DURATION, new DecelerateInterpolator());
    }
  }

  public String getImgUri() {
    return imgUri;
  }

  public void shakeAvatar() {
    shake(imgAvatar);
  }

  public void shakeDisplayName() {
    shake(editDisplayName);
  }

  public void shakeUsername() {
    shake(editUsername);
  }

  private void shake(View v) {
    ValueAnimator valueAnimator =
        ValueAnimator.ofFloat(screenUtils.dpToPx(-3f), screenUtils.dpToPx(3f),
            screenUtils.dpToPx(-3f), screenUtils.dpToPx(3f), screenUtils.dpToPx(-1f),
            screenUtils.dpToPx(1f), 0);
    valueAnimator.setInterpolator(new LinearInterpolator());
    valueAnimator.setDuration(800);
    valueAnimator.addUpdateListener(animation -> {
      float value = (Float) animation.getAnimatedValue();
      v.setTranslationX(value);
    });
    valueAnimator.start();
  }

  private void refactorInfosValid() {
    infoValid.onNext(displayNameSelected && usernameSelected);
  }

  @OnClick(R.id.layoutUsername) void clickLayoutUsername() {
    editUsername.requestFocus();
    editUsername.setSelection(editUsername.getText().length());
    editUsername.postDelayed(() -> {
      InputMethodManager keyboard =
          (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      keyboard.showSoftInput(editUsername, 0);
    }, 200);
  }

  private static InputFilter filterSpace = (source, start, end, dest, dstart, dend) -> {
    for (int i = start; i < end; i++) {
      if (Character.isSpaceChar(source.charAt(i))) {
        return "";
      }
    }

    return null;
  };

  private static InputFilter filterAlphanumeric = (source, start, end, dest, dstart, dend) -> {
    StringBuilder builder = new StringBuilder();
    for (int i = start; i < end; i++) {
      char c = source.charAt(i);
      if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
        builder.append(c);
      }
    }

    // If all characters are valid, return null, otherwise only return the filtered characters
    boolean allCharactersValid = (builder.length() == end - start);
    return allCharactersValid ? null : builder.toString();
  };

  private static InputFilter filterLowercase = (source, start, end, dest, dstart, dend) -> {
    for (int i = start; i < end; i++) {
      if (!Character.isLowerCase(source.charAt(i))) {
        char[] v = new char[end - start];
        TextUtils.getChars(source, start, end, v, 0);
        String s = new String(v).toLowerCase();

        if (source instanceof Spanned) {
          SpannableString sp = new SpannableString(s);
          TextUtils.copySpansFrom((Spanned) source, start, end, null, sp, 0);
          return sp;
        } else {
          return s;
        }
      }
    }

    return null; // keep original
  };

  public void loadUri(Uri uri) {
    imgUri = uri.toString();
    loadAvatar(uri.toString());
  }

  /**
   * DEPENDENCIES
   */
  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  /**
   * OBSERVABLES
   */
  public Observable<String> onUsernameInput() {
    return usernameInput;
  }

  public Observable<String> onDisplayNameInput() {
    return displayNameInput;
  }

  public Observable<Boolean> onInfoValid() {
    return infoValid;
  }
}
