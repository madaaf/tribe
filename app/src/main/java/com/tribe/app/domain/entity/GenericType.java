package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tiago on 11/30/16.
 */
public class GenericType extends LabelType implements Serializable {

    @StringDef({SET_AS_ADMIN, REMOVE_FROM_GROUP, REMOVE_FROM_ADMIN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GenericTypeDef {
    }

    public static final String SET_AS_ADMIN = "setAsAdmin";
    public static final String REMOVE_FROM_GROUP = "removeFromGroup";
    public static final String REMOVE_FROM_ADMIN = "removeFromAdmin";

    private @GenericTypeDef String typeDef;

    public GenericType(String label, @GenericTypeDef String cameraTypeDef) {
        super(label);
        this.typeDef = cameraTypeDef;
    }

    public String getLabel() {
        return label;
    }

    public @GenericTypeDef String getTypeDef() {
        return typeDef;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (typeDef != null ? typeDef.hashCode() : 0);
        return result;
    }
}
