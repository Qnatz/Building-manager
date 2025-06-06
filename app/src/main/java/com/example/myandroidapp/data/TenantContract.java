package com.example.myandroidapp.data;

import android.provider.BaseColumns;

public final class TenantContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private TenantContract() {}

    /* Inner class that defines the table contents */
    public static class TenantEntry implements BaseColumns {
        public static final String TABLE_NAME = "tenants";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_APARTMENT_NUMBER = "apartment_number";
        public static final String COLUMN_NAME_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_NAME_OTHER_CONTACT_INFO = "other_contact_info";
        // Email was mentioned in layout but not in contract, adding here for completeness
        // public static final String COLUMN_NAME_EMAIL = "email"; // If needed later
        // Notes were mentioned in layout but not in contract, adding here for completeness
        // public static final String COLUMN_NAME_NOTES = "notes"; // If needed later
    }
}
