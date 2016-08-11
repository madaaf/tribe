package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 06/05/2016.
 */
public class PendingType extends LabelType implements Serializable {

    @StringDef({RESEND, DELETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PendingTypeDef {
    }

    public static final String RESEND = "resend";
    public static final String DELETE = "delete";

    private List<TribeMessage> pending;
    private @PendingTypeDef String pendingType;

    public PendingType(List<TribeMessage> pending, String label, @PendingTypeDef String pendingType) {
        super(label);
        this.pending = new ArrayList<>(pending);
        this.pendingType = pendingType;
    }

    public String getPendingType() {
        return pendingType;
    }

    public List<TribeMessage> getPending() {
        return pending;
    }
}
