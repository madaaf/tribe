package com.tribe.app.presentation.mvp.view;

import com.tribe.app.domain.entity.TribeMessage;

public interface SendTribeView extends LoadDataView {

    void setCurrentTribe(TribeMessage tribe);
}
