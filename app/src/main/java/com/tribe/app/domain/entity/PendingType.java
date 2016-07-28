package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tiago on 06/05/2016.
 */
public class PendingType implements Serializable {

    @StringDef({RESEND, DELETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PendingTypeDef {
    }

    public static final String RESEND = "resend";
    public static final String DELETE = "delete";

    private String label;
    private @PendingTypeDef String pendingType;

    public PendingType(String label, @PendingTypeDef String pendingType) {
        this.label = label;
        this.pendingType = pendingType;
    }

    public String getLabel() {
        return label;
    }

    public String getPendingType() {
        return pendingType;
    }
}
