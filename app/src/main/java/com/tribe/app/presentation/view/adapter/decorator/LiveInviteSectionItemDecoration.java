package com.tribe.app.presentation.view.adapter.decorator;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.adapter.SectionCallback;
import com.tribe.app.presentation.view.utils.ScreenUtils;

public class LiveInviteSectionItemDecoration extends BaseSectionItemDecoration {

  private TextView txtLabel;

  public LiveInviteSectionItemDecoration(int headerHeight, boolean sticky,
      @NonNull SectionCallback sectionCallback, ScreenUtils screenUtils) {
    super(headerHeight, sticky, sectionCallback, screenUtils);
  }

  @Override protected void populateHeader(Context context, int headerType,
      GradientDrawable gradientDrawable) {
    switch (headerType) {
      case LIVE_CHAT_MEMBERS:
        txtLabel.setText(R.string.live_invite_section_chat_members);
        break;

      case LIVE_ADD_FRIENDS_IN_CALL:
        txtLabel.setText(R.string.live_invite_section_add_friends);
        break;
    }
  }

  @Override protected View inflateHeaderView(RecyclerView parent) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_live_invite_list_header, parent, false);
    txtLabel = ButterKnife.findById(view, R.id.txtLabel);
    return view;
  }
}