package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Membership;

public interface HomeView extends LoadDataView {
    void onDeepLink(String url);
    void onMembershipCreated(Membership membership);
}
