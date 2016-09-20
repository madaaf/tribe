package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import com.google.android.exoplayer.C;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by horatiothomas on 9/20/16.
 */
public class GroupType extends LabelType implements Serializable {
    @StringDef({ADD_FRIEND, ADD_ADMIN, REMOVE_ADMIN, REMOVE_MEMBER, BLOCK, UNFRIEND, CANCEL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GroupTypeDef {

    }

    public static final String ADD_FRIEND = "addFriend";
    public static final String ADD_ADMIN = "addAdmin";
    public static final String REMOVE_ADMIN = "removeAdmin";
    public static final String REMOVE_MEMBER = "removeMember";
    public static final String BLOCK = "block";
    public static final String UNFRIEND = "unfriend";
    public static final String CANCEL = "cancel";

    private @GroupTypeDef String groupTypeDef;

    public GroupType(String label, @GroupTypeDef String groupTypeDef) {
        super(label);
        this.groupTypeDef = groupTypeDef;
    }

    public String getLabel() {
        return label;
    }

    public @GroupTypeDef String getGroupTypeDef() {
        return groupTypeDef;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (groupTypeDef != null ? groupTypeDef.hashCode() : 0);
        return result;
    }

}
