package com.example.myandroidapp.data

import android.provider.BaseColumns

object TenantContract {

    // To prevent someone from accidentally instantiating the contract class,
    // the constructor is not needed for an object declaration.

    /* Inner object that defines the table contents */
    object TenantEntry : BaseColumns {
        const val TABLE_NAME = "tenants"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_APARTMENT_NUMBER = "apartment_number"
        const val COLUMN_NAME_PHONE_NUMBER = "phone_number"
        const val COLUMN_NAME_OTHER_CONTACT_INFO = "other_contact_info"
    }

    object InvoiceEntry : BaseColumns {
        const val TABLE_NAME = "invoices"
        const val COLUMN_NAME_TENANT_ID = "tenant_id"
        const val COLUMN_NAME_INVOICE_DATE = "invoice_date" // TEXT as ISO8601 string or INTEGER for timestamp
        const val COLUMN_NAME_DUE_DATE = "due_date"         // TEXT as ISO8601 string or INTEGER for timestamp
        const val COLUMN_NAME_AMOUNT_DUE = "amount_due"     // REAL for currency
        const val COLUMN_NAME_STATUS = "status"             // TEXT e.g., "unpaid", "partially_paid", "paid"
    }

    object PaymentEntry : BaseColumns {
        const val TABLE_NAME = "payments"
        const val COLUMN_NAME_TENANT_ID = "tenant_id"       // INTEGER, Foreign Key to tenants._id
        const val COLUMN_NAME_INVOICE_ID = "invoice_id"     // INTEGER, Foreign Key to invoices._id (nullable)
        const val COLUMN_NAME_PAYMENT_DATE = "payment_date" // TEXT as ISO8601 string or INTEGER for timestamp
        const val COLUMN_NAME_PAYMENT_METHOD = "payment_method" // TEXT e.g., "cash", "bank_transfer"
        const val COLUMN_NAME_AMOUNT_PAID = "amount_paid"   // REAL for currency
    }
}
