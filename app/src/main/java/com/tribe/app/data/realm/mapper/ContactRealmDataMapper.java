package com.tribe.app.data.realm.mapper;

import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.data.realm.ContactInterface;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.ContactAB;
import com.tribe.app.domain.entity.ContactFB;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton
public class ContactRealmDataMapper {

    private final UserRealmDataMapper userRealmDataMapper;
    private final PhoneRealmDataMapper phoneRealmDataMapper;

    @Inject
    public ContactRealmDataMapper(UserRealmDataMapper userRealmDataMapper, PhoneRealmDataMapper phoneRealmDataMapper) {
        this.userRealmDataMapper = userRealmDataMapper;
        this.phoneRealmDataMapper = phoneRealmDataMapper;
    }

    /**
     * Transform a {@link com.tribe.app.data.realm.ContactInterface} into an {@link com.tribe.app.domain.entity.Contact}.
     *
     * @param contactI Object to be transformed.
     * @return {@link com.tribe.app.domain.entity.Contact} if valid {@link ContactInterface} otherwise null.
     */
    public Contact transform(ContactInterface contactI) {
        if (contactI instanceof ContactABRealm) {
            ContactABRealm contactABRealm = (ContactABRealm) contactI;
            ContactAB contact = new ContactAB(contactABRealm.getId());
            contact.setPhones(phoneRealmDataMapper.transform(contactABRealm.getPhones()));
            contact.setLastTimeContacted(contactABRealm.getLastTimeContacted());
            contact.setVersion(contactABRealm.getVersion());
            contact.setName(contactABRealm.getName());
            contact.setUserList(userRealmDataMapper.transform(contactABRealm.getUsers()));
            contact.setHowManyFriends(contactABRealm.getHowManyFriends());
            return contact;
        } else {
            ContactFBRealm contactFBRealm = (ContactFBRealm) contactI;
            ContactFB contact = new ContactFB(contactFBRealm.getId());
            contact.setName(contactFBRealm.getName());
            contact.setUserList(userRealmDataMapper.transform(contactFBRealm.getUserList()));
            contact.setHowManyFriends(contactFBRealm.getHowManyFriends());
            return contact;
        }
    }

    /**
     * Transform a {@link Contact} into an {@link ContactInterface}.
     *
     * @param contact Object to be transformed.
     * @return {@link ContactInterface} if valid {@link Contact} otherwise null.
     */
    public ContactInterface transform(Contact contact) {
        if (contact instanceof ContactAB) {
            ContactAB contactAB = (ContactAB) contact;
            ContactABRealm contactABRealm = new ContactABRealm();
            contactABRealm.setId(contactAB.getId());
            contactABRealm.setPhones(phoneRealmDataMapper.transform(contactAB.getPhones()));
            contactABRealm.setLastTimeContacted(contactAB.getLastTimeContacted());
            contactABRealm.setVersion(contactAB.getVersion());
            contactABRealm.setName(contactAB.getName());
            contactABRealm.setUserList(userRealmDataMapper.transformList(contactAB.getUserList()));
            contactABRealm.setHowManyFriends(contactAB.getHowManyFriends());
            return contactABRealm;
        } else {
            ContactFB contactFB = (ContactFB) contact;
            ContactFBRealm contactFBRealm = new ContactFBRealm();
            contactFBRealm.setId(contactFB.getId());
            contactFBRealm.setName(contactFB.getName());
            contactFBRealm.setUserList(userRealmDataMapper.transformList(contactFB.getUserList()));
            contactFBRealm.setHowManyFriends(contactFB.getHowManyFriends());
            return contactFBRealm;
        }
    }

    /**
     * Transform a List of {@link ContactInterface} into a Collection of {@link Contact}.
     *
     * @param contactRealmCollection Object Collection to be transformed.
     * @return {@link List<Contact>} if valid {@link Collection<ContactInterface>} otherwise null.
     */
    public List<Contact> transform(Collection<ContactInterface> contactRealmCollection) {
        List<Contact> contactList = new ArrayList<>();
        Contact contact;
        for (ContactInterface contactI : contactRealmCollection) {
            contact = transform(contactI);
            if (contact != null) {
                contactList.add(contact);
            }
        }

        return contactList;
    }

    /**
     * Transform a List of {@link Contact} into a Collection of {@link ContactInterface}.
     *
     * @param contactCollection Object Collection to be transformed.
     * @return {@link List<ContactInterface>} if valid {@link Collection<Contact>} otherwise null.
     */
    public List<ContactInterface> transform(List<Contact> contactCollection) {
        List<ContactInterface> contactInterfaceList = new ArrayList<>();
        ContactInterface contactI;
        for (Contact contact : contactCollection) {
            contactI = transform(contact);
            if (contactI != null) {
                contactInterfaceList.add(contactI);
            }
        }

        return contactInterfaceList;
    }
}
