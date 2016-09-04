package com.tribe.app.presentation.view.contact;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.tribe.app.domain.entity.Contact;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.PhoneUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.provider.ContactsContract.CommonDataKinds;
import static android.provider.ContactsContract.Contacts;

public class ContactsHelper {

    private static final String TAG = "Contacts";

    public static boolean DEBUG = false;

    private static final String[] DATA_PROJECTION = {
            ContactsContract.Data.DATA1,    // email / phone
            ContactsContract.Data.MIMETYPE
    };

    private static final String[] DATA_PROJECTION_FULL = {
            ContactsContract.Data.DATA1,    // email / phone
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.PHOTO_THUMBNAIL_URI,
            ContactsContract.Data.STARRED,
            ContactsContract.Data.LAST_TIME_CONTACTED
    };

    private static final String[] CONTACTS_PROJECTION = new String[] {
            Contacts._ID,
            Contacts.DISPLAY_NAME_PRIMARY,
            Contacts.PHOTO_THUMBNAIL_URI,
            Contacts.HAS_PHONE_NUMBER,
            Contacts.STARRED,
            Contacts.LAST_TIME_CONTACTED
    };

    Context context;
    ContentResolver resolver;
    PhoneUtils phoneUtils;
    int countryCode;

    public ContactsHelper(Context ctx) {
        context = ctx;
        resolver = context.getContentResolver();
    }

    /**
     * Fetch device owner contact based on Google Account or 'Me' contact from address book
     * @return
     */
    @Nullable
    public Contact getProfileContact() {
        String filter = getAccountEmail();
        if (filter == null) {
            filter = getProfileName();
        }
        if (filter == null)
            return null;

        List<Contact> contacts = filter(filter, true, null, null);
        if (!contacts.isEmpty())
            return contacts.get(0);
        else
            return null;
    }

    /**
     * @param query      leave it null if you want all contacts
     * @return list with contacts data
     */
    @NonNull
    public List<Contact> filter(String query, boolean withPhones, Sorter sorter, Filter[] filter) {
        List<Contact> result = new ArrayList<>();

        Cursor c = getContactsCursor(query, sorter, filter);

        while (c.moveToNext()) {
            Contact contact = fetchContact(c, withPhones);
            result.add(contact);
        }
        c.close();

        if (DEBUG)
            log(result);

        return result;
    }

    public static String cleanPhone(String phone) {
        return phone.replaceAll("-|\\s|\\(|\\)|\\+", "");
    }

    @NonNull
    Contact fetchContact(Cursor c, boolean withPhones) {
        String id = c.getString(c.getColumnIndex(Contacts._ID));
        Contact contact = new Contact(id);
        contact.name = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY));

        // misc
        long lastTimeContacted = c.getLong(c.getColumnIndex(Contacts.LAST_TIME_CONTACTED));
        contact.lastTimeContacted = lastTimeContacted;

        // get data
        if (withPhones && c.getInt(c.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) > 0) {
            Set<String> phones = new HashSet<>();
            Cursor data = getDataCursor(id, withPhones);
            while (data.moveToNext()) {
                String value = data.getString(data.getColumnIndex(ContactsContract.Data.DATA1));
                switch (data.getString(data.getColumnIndex(ContactsContract.Data.MIMETYPE))) {
                    case CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                        String phoneNumberFormatted = phoneUtils.formatNumber(value, countryCode);
                        phones.add(!StringUtils.isEmpty(phoneNumberFormatted) ? phoneNumberFormatted : value.trim());

                        break;
                }
            }

            contact.phones = new ArrayList<String>(phones);
            data.close();
        }

        return contact;
    }

    Cursor getContactsCursor(String query, Sorter sorter, Filter[] filter) {
        Uri uri;
        if (query == null) {
            uri = Contacts.CONTENT_URI;
        } else {
            uri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, query);
        }

        String order = sorter != null ? sorter.raw : null;
        String where = filter != null ? TextUtils.join(" AND ", filter) : null;

        return resolver.query(
                uri,
                CONTACTS_PROJECTION,
                where,
                null,
                order
        );
    }


    Cursor getDataCursor(String contactId, boolean withPhones) {
        List<String> selections = new ArrayList<>();
        List<String> args = new ArrayList<>();
        args.add(contactId);
        String where = ContactsContract.Data.MIMETYPE + "=?";
        if (withPhones) {
            selections.add(where);
            args.add(CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        }

        Cursor data = resolver.query(
                ContactsContract.Data.CONTENT_URI,
                DATA_PROJECTION,
                String.format("%s=?" + " AND " + "(%s)", ContactsContract.Data.CONTACT_ID, TextUtils.join(" OR ", selections)),
                args.toArray(new String[args.size()]), null);

        return data;
    }

    Cursor getFastContactsCursor() {
        String where = ContactsContract.Data.MIMETYPE + "=?";
        String[] wheres = {where, where, where};
        String[] selectionArgs = {
                CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        };
        String selection = TextUtils.join(" OR ", wheres);
        Cursor data = resolver.query(
                ContactsContract.Data.CONTENT_URI,
                DATA_PROJECTION_FULL,
                selection,
                selectionArgs,
                ContactsContract.Data.CONTACT_ID
        );

        return data;
    }

    @NonNull
    Contact fetchContactFast(Cursor c) {
        String id = c.getString(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
        Contact contact = new Contact(id);

        long lastTimeContacted = c.getLong(c.getColumnIndex(ContactsContract.Data.LAST_TIME_CONTACTED));
        int starred = c.getInt(c.getColumnIndex(ContactsContract.Data.STARRED));
        contact.lastTimeContacted = lastTimeContacted;

        List<String> phones = new ArrayList<>();
        List<String> emails = new ArrayList<>();

        String nextId = id;
        while (id.equals(nextId)) {
            String value = c.getString(c.getColumnIndex(ContactsContract.Data.DATA1));
            switch (c.getString(c.getColumnIndex(ContactsContract.Data.MIMETYPE))) {
                case CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
                    contact.name = value;
                    break;
                case CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                    emails.add(value);
                    break;
                case CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                    phones.add(value);
                    break;

            }

            if (c.moveToNext())
                nextId = c.getString(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            else
                break;
        }

        contact.phones = phones;

        return contact;
    }

    private static void log(List<Contact> contacts) {
        Log.v(TAG, "=== contacts ===");
        for (Contact c : contacts) {
            Log.v(TAG, c.toString());
        }

    }

    /**
     * Utility method. Should'n rely on it by 100%
     * @return
     */
    @Nullable
    private String getAccountEmail() {
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");
        for (Account account : accounts) {
            Log.v(TAG, "account:" + account.name);
            if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                return account.name;
            }
        }

        return null;
    }

    private String getProfileName() {
        Cursor c = resolver.query(
                ContactsContract.Profile.CONTENT_URI,
                null,
                null,
                null,
                null);
        String name = null;
        if (c.moveToFirst()) {
            String id = c.getString(c.getColumnIndex(Contacts._ID));
            name = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY));
        }
        c.close();
        return name;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public void setPhoneUtils(PhoneUtils phoneUtils) {
        this.phoneUtils = phoneUtils;
    }
}
