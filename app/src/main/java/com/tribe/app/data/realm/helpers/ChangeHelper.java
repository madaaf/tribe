package com.tribe.app.data.realm.helpers;

import io.realm.RealmResults;

public class ChangeHelper<T extends RealmResults<? extends Changeable>> {

    private ChangeSet<T> previousChangeSet;

    public boolean filter(T t) {
        if (this.previousChangeSet == null) {
            this.previousChangeSet = new ChangeSet<>(t);
            return true;
        } else {
            ChangeSet<T> newChangeSet = new ChangeSet<>(t, this.previousChangeSet);

            if (newChangeSet.insertedItems > 0 ||
                newChangeSet.deletedItems > 0 ||
                newChangeSet.updatedItems.length > 0) {

                this.previousChangeSet = newChangeSet;

                return true;
            }
        }

        return false;
    }

    public void clear() {
        previousChangeSet = null;
    }
}
