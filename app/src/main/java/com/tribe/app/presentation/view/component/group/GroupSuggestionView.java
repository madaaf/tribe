package com.tribe.app.presentation.view.component.group;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.widget.TextViewFont;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by tiago on 11/19/2016.
 */
public class GroupSuggestionView extends FrameLayout {

  @IntDef({ BEST_FRIENDS, TEAMMATES, CLASSMATES, ROOMIES, WORKTEAM, FAMILY })
  public @interface GroupSuggestion {
  }

  public static final int BEST_FRIENDS = 0;
  public static final int TEAMMATES = 1;
  public static final int CLASSMATES = 2;
  public static final int ROOMIES = 3;
  public static final int WORKTEAM = 4;
  public static final int FAMILY = 5;

  @BindView(R.id.imageView) ImageView imageView;

  @BindView(R.id.txtLabel) TextViewFont txtLabel;

  // VARIABLES
  private int drawableId;
  private int type;
  private String label;

  // RESOURCES
  private int paddingSmall;
  private int paddingMedium;

  // OBSERVABLES
  private Unbinder unbinder;

  public GroupSuggestionView(Context context) {
    this(context, null);
    init(context, null);
  }

  public GroupSuggestionView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_group_suggestion, this, true);
    unbinder = ButterKnife.bind(this);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    paddingSmall = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
    paddingMedium = getResources().getDimensionPixelSize(R.dimen.vertical_margin);
    setPadding(paddingSmall, paddingSmall, paddingMedium, paddingSmall);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GroupSuggestionView);

    setType(a.getInt(R.styleable.GroupSuggestionView_groupType, BEST_FRIENDS));
    drawableId = a.getResourceId(R.styleable.GroupSuggestionView_groupDrawable, 0);

    if (a.hasValue(R.styleable.GroupSuggestionView_groupDrawable)) {
      imageView.getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
              imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
              setDrawableResource(drawableId);
            }
          });
    }

    a.recycle();
  }

  public void setType(int type) {
    this.type = type;
    setLabel(getLabel());
  }

  public void setDrawableResource(int res) {
    this.drawableId = res;

    Glide.with(getContext())
        .load(drawableId)
        .override(imageView.getWidth(), imageView.getHeight())
        .bitmapTransform(new CropCircleTransformation(getContext()))
        .crossFade()
        .into(imageView);
  }

  public void setLabel(String str) {
    label = str;
    txtLabel.setText(label);
  }

  public String getLabel() {
    if (type == BEST_FRIENDS) {
      return getContext().getString(R.string.group_bffs_title);
    } else if (type == TEAMMATES) {
      return getContext().getString(R.string.group_teammates_title);
    } else if (type == CLASSMATES) {
      return getContext().getString(R.string.group_classmates_title);
    } else if (type == ROOMIES) {
      return getContext().getString(R.string.group_roomies_title);
    } else if (type == WORKTEAM) {
      return getContext().getString(R.string.group_work_title);
    } else {
      return getContext().getString(R.string.group_family_title);
    }
  }

  public int getDrawableId() {
    return drawableId;
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }
}
