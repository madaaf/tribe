package com.tribe.app.presentation.view.widget.header;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.widget.TextViewFont;

/**
 * Created by tiago on 09/05/17.
 */
public class HomeListViewHeader extends LinearLayout {

  @IntDef({ ONGOING, ONLINE, RECENT }) public @interface HeaderType {
  }

  public static final int ONGOING = 0;
  public static final int ONLINE = 1;
  public static final int RECENT = 2;

  @BindView(R.id.imgPicto) ImageView imgPicto;
  @BindView(R.id.txtLabel) TextViewFont txtLabel;

  private int type = RECENT;
  private Unbinder unbinder;

  public HomeListViewHeader(Context context) {
    super(context);
    init();
  }

  public HomeListViewHeader(Context context, AttributeSet attrs) {
    super(context, attrs);

    init();
  }

  public void init() {
    initResources();
    initDependencyInjector();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  private void initResources() {

  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_home_list_header, this);
    unbinder = ButterKnife.bind(this);

    //setOrientation(HORIZONTAL);
    //setGravity(Gravity.CENTER_VERTICAL);
  }

  public void setHeaderType(@HeaderType int headerType) {
    type = headerType;

    switch (type) {
      case ONGOING:
        txtLabel.setText(R.string.home_section_ongoing);
        imgPicto.setImageResource(R.drawable.picto_header_ongoing);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.violet_home));

      case ONLINE:
        txtLabel.setText(R.string.home_section_online);
        imgPicto.setImageResource(R.drawable.picto_header_online);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue_new));

      case RECENT:
        txtLabel.setText(R.string.home_section_recent);
        imgPicto.setImageResource(R.drawable.picto_recent);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.orange_recent));
    }
  }
}
