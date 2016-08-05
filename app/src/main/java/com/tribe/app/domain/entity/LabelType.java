package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 04/08/2016.
 */
public class LabelType implements Serializable {

    protected String label;

    public LabelType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}
