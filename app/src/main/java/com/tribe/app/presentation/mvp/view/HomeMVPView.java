package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.Membership;

public interface HomeMVPView extends LoadDataMVPView {
    void onDeepLink(String url);
    void onMembershipCreated(Membership membership);
}
