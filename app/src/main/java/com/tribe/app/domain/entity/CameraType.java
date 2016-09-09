package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by horatiothomas on 9/9/16.
 */
public class CameraType extends LabelType implements Serializable {
    @StringDef({OPEN_CAMERA, OPEN_PHOTOS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface  CameraTypeDef {
    }

    public static final String OPEN_CAMERA = "openCamera";
    public static final String OPEN_PHOTOS = "openPhotos";

    private @CameraTypeDef String cameraTypeDef;

    public CameraType(String label, @CameraTypeDef String cameraTypeDef) {
        super(label);
        this.cameraTypeDef = cameraTypeDef;
    }

    public String getLabel() {
        return label;
    }

    public @CameraTypeDef String getCameraTypeDef() {
        return cameraTypeDef;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (cameraTypeDef != null ? cameraTypeDef.hashCode() : 0);
        return result;
    }
}
