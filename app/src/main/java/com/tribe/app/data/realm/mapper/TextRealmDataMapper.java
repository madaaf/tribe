package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.TextRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Text;
import com.tribe.app.domain.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton
public class TextRealmDataMapper {

    @Inject
    public TextRealmDataMapper() {

    }

    /**
     * Transform a {@link com.tribe.app.data.realm.TextRealm} into an {@link com.tribe.app.domain.entity.Text}.
     *
     * @param textRealm Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.Text} if valid {@link com.tribe.app.data.realm.TextRealm} otherwise null.
     */
    public Text transform(TextRealm textRealm) {
        Text text = null;

        if (textRealm != null) {
            text = new Text(textRealm.getId());
            text.setCreatedAt(textRealm.getCreatedAt());
            text.setUpdatedAt(textRealm.getUpdatedAt());
            text.setText(textRealm.getText());
        }

        return text;
    }

    /**
     * Transform a List of {@link TextRealm} into a Collection of {@link Text}.
     *
     * @param textRealmCollection Object Collection to be transformed.
     * @return {@link Text} if valid {@link TextRealm} otherwise null.
     */
    public List<Text> transform(Collection<TextRealm> textRealmCollection) {
        List<Text> textList = new ArrayList<>();
        Text text;
        for (TextRealm textRealm : textRealmCollection) {
            text = transform(textRealm);
            if (text != null) {
                textList.add(text);
            }
        }

        return textList;
    }
}
