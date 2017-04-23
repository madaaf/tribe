package com.tribe.app.data.repository.user.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.tribe.app.data.realm.ContactABRealm;
import com.tribe.app.data.realm.PhoneRealm;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.provider.ContactsContract.CommonDataKinds;
import static android.provider.ContactsContract.Contacts;

public class ContactsHelper {

  private static final String TAG = "Contacts";

  public static boolean DEBUG = false;

  private static final String[] DATA_PROJECTION = {
      ContactsContract.Data.DATA1,    // email / phone
      ContactsContract.Data.MIMETYPE
  };

  private static final String[] DATA_PROJECTION_WITH_NAMES = {
      ContactsContract.RawContacts.CONTACT_ID, ContactsContract.Data.MIMETYPE,
      ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
      ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
      CommonDataKinds.StructuredName.DISPLAY_NAME
  };

  private static final String[] DATA_PROJECTION_FULL = {
      ContactsContract.Data.DATA1, ContactsContract.Data.DATA2,
      CommonDataKinds.StructuredName.DISPLAY_NAME, CommonDataKinds.StructuredName.GIVEN_NAME,
      CommonDataKinds.StructuredName.FAMILY_NAME, ContactsContract.Data.MIMETYPE,
      ContactsContract.Data.CONTACT_ID, ContactsContract.Data.DATA_VERSION
  };

  private static final String[] CONTACTS_PROJECTION = new String[] {
      Contacts._ID, Contacts.DISPLAY_NAME_PRIMARY, Contacts.HAS_PHONE_NUMBER,
      Contacts.LAST_TIME_CONTACTED
  };

  Context context;
  ContentResolver resolver;
  PhoneUtils phoneUtils;
  int countryCode;

  public ContactsHelper(Context ctx, PhoneUtils phoneUtils) {
    context = ctx;
    resolver = context.getContentResolver();
    this.phoneUtils = phoneUtils;
  }

  /**
   * @param query leave it null if you want all contacts
   * @return list with contacts data
   */
  @NonNull public List<ContactABRealm> filter(String query, boolean withPhones, Sorter sorter,
      Filter[] filter) {
    List<ContactABRealm> result = new ArrayList<>();

    Cursor c = getContactsCursor(query, sorter, filter);

    while (c.moveToNext()) {
      ContactABRealm contact = fetchContact(c, withPhones);
      result.add(contact);
    }
    c.close();

    return result;
  }

  @NonNull ContactABRealm fetchContact(Cursor c, boolean withPhones) {
    long timeStart = System.nanoTime();
    String id = c.getString(c.getColumnIndex(Contacts._ID));
    ContactABRealm contact = new ContactABRealm();
    contact.setId(id);
    contact.setName(c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY)));

    int countInternational = 0;

    // get data
    if (withPhones && c.getInt(c.getColumnIndex(Contacts.HAS_PHONE_NUMBER)) > 0) {
      HashMap<String, Pair<String, Boolean>> phonesPair = new HashMap<>();
      Cursor data = getDataCursor(id, withPhones);
      while (data.moveToNext()) {
        String value = data.getString(data.getColumnIndex(ContactsContract.Data.DATA1));
        switch (data.getString(data.getColumnIndex(ContactsContract.Data.MIMETYPE))) {
          case CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
            String phoneNumberFormatted =
                phoneUtils.formatMobileNumberForAddressBook(value, String.valueOf(countryCode));
            boolean isFormatted = !StringUtils.isEmpty(phoneNumberFormatted);
            phonesPair.put(isFormatted ? phoneNumberFormatted : value.trim(),
                new Pair<>(isFormatted ? phoneNumberFormatted : value.trim(), isFormatted));
            break;
        }
      }

      RealmList<PhoneRealm> realmList = new RealmList<>();

      if (phonesPair != null && phonesPair.size() > 0) {
        for (Pair<String, Boolean> phonePair : phonesPair.values()) {
          PhoneRealm phoneRealm = new PhoneRealm();
          phoneRealm.setPhone(phonePair.first);
          phoneRealm.setInternational(phonePair.second);
          realmList.add(phoneRealm);

          if (phonePair.second) countInternational++;
        }
      }

      contact.setPhones(realmList);
      data.close();
    }

    //long timeEnd = System.nanoTime();
    //Timber.d("time for contact " + (timeEnd - timeStart) / 1000000.0f + " ms");

    if (countInternational > 0) return contact;

    return null;
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

    return resolver.query(uri, CONTACTS_PROJECTION, where, null, order);
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

    Cursor data = resolver.query(ContactsContract.Data.CONTENT_URI, DATA_PROJECTION,
        String.format("%s=?" + " AND " + "(%s)", ContactsContract.Data.CONTACT_ID,
            TextUtils.join(" OR ", selections)), args.toArray(new String[args.size()]), null);

    return data;
  }

  Cursor getFastContactsCursor() {
    String where = ContactsContract.Data.HAS_PHONE_NUMBER + " != 0";

    where += " AND "
        + ContactsContract.Data.MIMETYPE
        + " IN ('"
        + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
        + "', '"
        + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
        + "')";

    Cursor data =
        resolver.query(ContactsContract.Data.CONTENT_URI, DATA_PROJECTION_WITH_NAMES, where, null,
            ContactsContract.Data.DISPLAY_NAME);

    return data;
  }

  @NonNull ContactABRealm fetchContactFast(Cursor c, ContactABRealm contact) {
    List<String> phones = new ArrayList<>();

    String mimeType = c.getString(c.getColumnIndex(ContactsContract.Data.MIMETYPE));
    if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
      String phone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
      phones.add(phone);
    } else if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
      String givenName =
          c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
      contact.setFirstName(givenName);

      String familyName = c.getString(
          c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
      contact.setLastName(familyName);

      String displayName =
          c.getString(c.getColumnIndex(CommonDataKinds.StructuredName.DISPLAY_NAME));
      contact.setName(displayName);
    }

    RealmList<PhoneRealm> realmList = new RealmList<>();

    if (phones != null && phones.size() > 0) {
      for (String phone : phones) {
        PhoneRealm phoneRealm = new PhoneRealm();
        phoneRealm.setPhone(phone);
        realmList.add(phoneRealm);
      }

      contact.setPhones(realmList);
      return contact;
    }

    return null;
  }

  private static void log(List<ContactABRealm> contacts) {
    Log.v(TAG, "=== contacts ===");
    for (ContactABRealm c : contacts) {
      Log.v(TAG, c.toString());
    }
  }

  public void setCountryCode(int countryCode) {
    this.countryCode = countryCode;
  }
}
