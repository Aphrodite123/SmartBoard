package com.aphrodite.smartboard.model.database.migration;

import com.aphrodite.smartboard.utils.LogUtils;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Aphrodite on 2018/7/31.
 */
public class GlobalRealmMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        LogUtils.i("Enter migrate method.oldVersion: " + oldVersion + ",newVersion: " + newVersion);
        RealmSchema schema = realm.getSchema();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GlobalRealmMigration;
    }
}
