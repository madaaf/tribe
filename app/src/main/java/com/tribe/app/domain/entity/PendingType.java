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
public class PendingType implements Serializable {

    @StringDef({RESEND, DELETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PendingTypeDef {
    }

    public static final String RESEND = "resend";
    public static final String DELETE = "delete";

    private List<Tribe> pending;
    private String label;
    private @PendingTypeDef String pendingType;

    public PendingType(List<Tribe> pending, String label, @PendingTypeDef String pendingType) {
        this.pending = new ArrayList<>(pending);
        this.label = label;
        this.pendingType = pendingType;
    }

    public String getLabel() {
        return label;
    }

    public String getPendingType() {
        return pendingType;
    }

    public List<Tribe> getPending() {
        return pending;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (pendingType != null ? pendingType.hashCode() : 0);
        return result;
    }
}
