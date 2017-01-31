package com.tribe.app.presentation.view.widget.avatar;

import com.tribe.app.domain.entity.Recipient;

/**
 * Created by tiago on 06/01/2017.
 */

public interface Avatar {

    public void load(Recipient recipient);

    public void load(String url);
}
