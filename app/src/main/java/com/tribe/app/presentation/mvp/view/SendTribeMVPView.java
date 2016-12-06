package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.TribeMessage;

public interface SendTribeMVPView extends LoadDataMVPView {

    void setCurrentTribe(TribeMessage tribe);
}
