package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tiago on 06/05/2016.
 */
public class MoreType extends LabelType implements Serializable {

    @StringDef({CLEAR_MESSAGES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MoreTypeDef {
    }

    public static final String CLEAR_MESSAGES = "clearMessages";

    private @MoreTypeDef String moreTypeDef;

    public MoreType(String label, @MoreTypeDef String moreTypeDef) {
        super(label);
        this.moreTypeDef = moreTypeDef;
    }

    public String getLabel() {
        return label;
    }

    public @MoreTypeDef String getMoreType() {
        return moreTypeDef;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (moreTypeDef != null ? moreTypeDef.hashCode() : 0);
        return result;
    }
}
