package com.example.myandroidapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myandroidapp.data.TenantContract.TenantEntry
import com.example.myandroidapp.data.TenantContract.InvoiceEntry
import com.example.myandroidapp.data.TenantContract.PaymentEntry

class TenantDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "TenantManagement.db"
        const val DATABASE_VERSION = 2 // Incremented version

        private const val SQL_CREATE_TENANTS =
            "CREATE TABLE ${TenantEntry.TABLE_NAME} (" +
                    "${TenantEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${TenantEntry.COLUMN_NAME_NAME} TEXT NOT NULL," +
                    "${TenantEntry.COLUMN_NAME_APARTMENT_NUMBER} TEXT NOT NULL," +
                    "${TenantEntry.COLUMN_NAME_PHONE_NUMBER} TEXT," +
                    "${TenantEntry.COLUMN_NAME_OTHER_CONTACT_INFO} TEXT);"

        private const val SQL_CREATE_INVOICES =
            "CREATE TABLE ${InvoiceEntry.TABLE_NAME} (" +
                    "${InvoiceEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${InvoiceEntry.COLUMN_NAME_TENANT_ID} INTEGER NOT NULL," +
                    "${InvoiceEntry.COLUMN_NAME_INVOICE_DATE} TEXT NOT NULL," +
                    "${InvoiceEntry.COLUMN_NAME_DUE_DATE} TEXT NOT NULL," +
                    "${InvoiceEntry.COLUMN_NAME_AMOUNT_DUE} REAL NOT NULL," +
                    "${InvoiceEntry.COLUMN_NAME_STATUS} TEXT NOT NULL," +
                    "FOREIGN KEY(${InvoiceEntry.COLUMN_NAME_TENANT_ID}) REFERENCES ${TenantEntry.TABLE_NAME}(${TenantEntry._ID}));"

        private const val SQL_CREATE_PAYMENTS =
            "CREATE TABLE ${PaymentEntry.TABLE_NAME} (" +
                    "${PaymentEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${PaymentEntry.COLUMN_NAME_TENANT_ID} INTEGER NOT NULL," +
                    "${PaymentEntry.COLUMN_NAME_INVOICE_ID} INTEGER," + // Nullable Foreign Key
                    "${PaymentEntry.COLUMN_NAME_PAYMENT_DATE} TEXT NOT NULL," +
                    "${PaymentEntry.COLUMN_NAME_PAYMENT_METHOD} TEXT," +
                    "${PaymentEntry.COLUMN_NAME_AMOUNT_PAID} REAL NOT NULL," +
                    "FOREIGN KEY(${PaymentEntry.COLUMN_NAME_TENANT_ID}) REFERENCES ${TenantEntry.TABLE_NAME}(${TenantEntry._ID})," +
                    "FOREIGN KEY(${PaymentEntry.COLUMN_NAME_INVOICE_ID}) REFERENCES ${InvoiceEntry.TABLE_NAME}(${InvoiceEntry._ID}));"

        private const val SQL_DELETE_TENANTS = "DROP TABLE IF EXISTS ${TenantEntry.TABLE_NAME}"
        private const val SQL_DELETE_INVOICES = "DROP TABLE IF EXISTS ${InvoiceEntry.TABLE_NAME}"
        private const val SQL_DELETE_PAYMENTS = "DROP TABLE IF EXISTS ${PaymentEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TENANTS)
        db.execSQL(SQL_CREATE_INVOICES)
        db.execSQL(SQL_CREATE_PAYMENTS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_PAYMENTS)    // Drop payments first due to FK on invoices
        db.execSQL(SQL_DELETE_INVOICES)   // Drop invoices next due to FK on tenants
        db.execSQL(SQL_DELETE_TENANTS)    // Drop tenants last
        onCreate(db)
        // If you had a more complex schema evolution, you would implement
        // specific migration steps here, checking oldVersion and newVersion.
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion) // Simple downgrade is same as upgrade for now
    }

    fun getAllTenantsCursor(): Cursor {
        val db = this.readableDatabase
        return db.query(
            TenantEntry.TABLE_NAME,
            arrayOf(TenantEntry._ID, TenantEntry.COLUMN_NAME_NAME), // Projections
            null, // Selection
            null, // Selection args
            null, // Group by
            null, // Having
            TenantEntry.COLUMN_NAME_NAME + " ASC" // Order by
        )
    }

    fun getInvoicesForTenantCursor(tenantId: Long): Cursor {
        val db = this.readableDatabase
        val selection = "${InvoiceEntry.COLUMN_NAME_TENANT_ID} = ?"
        val selectionArgs = arrayOf(tenantId.toString())
        return db.query(
            InvoiceEntry.TABLE_NAME,
            null, // Projection - null means all columns
            selection,
            selectionArgs,
            null, // Group by
            null, // Having
            InvoiceEntry.COLUMN_NAME_INVOICE_DATE + " DESC" // Order by
        )
    }

    fun getPaymentsForTenantCursor(tenantId: Long): Cursor {
        val db = this.readableDatabase
        val selection = "${PaymentEntry.COLUMN_NAME_TENANT_ID} = ?"
        val selectionArgs = arrayOf(tenantId.toString())
        return db.query(
            PaymentEntry.TABLE_NAME,
            null, // Projection - null means all columns
            selection,
            selectionArgs,
            null, // Group by
            null, // Having
            PaymentEntry.COLUMN_NAME_PAYMENT_DATE + " DESC" // Order by
        )
    }

    // Basic method to generate monthly invoices.
    // NOTE: Date handling is very simplified (no specific month check, just existence).
    // A more robust solution would involve proper date parsing and comparison for the current month.
    fun generateMonthlyInvoices(invoiceDate: String, dueDate: String, amount: Double): Int {
        val db = this.writableDatabase
        var invoicesGenerated = 0
        val tenantCursor = getAllTenantsCursor() // Reuse existing method to get all tenants

        if (tenantCursor.moveToFirst()) {
            do {
                val tenantId = tenantCursor.getLong(tenantCursor.getColumnIndexOrThrow(TenantEntry._ID))

                // Simplified check: Does an invoice exist for this tenant already?
                // This doesn't check for a specific month, just any invoice.
                // For a real app, you'd check for an invoice for the *current billing period*.
                val invoiceCheckCursor = db.query(
                    InvoiceEntry.TABLE_NAME,
                    arrayOf(InvoiceEntry._ID),
                    "${InvoiceEntry.COLUMN_NAME_TENANT_ID} = ? AND ${InvoiceEntry.COLUMN_NAME_INVOICE_DATE} = ?", // Simple check for this exact invoice date to avoid exact duplicates
                    arrayOf(tenantId.toString(), invoiceDate),
                    null, null, null, "1"
                )

                if (invoiceCheckCursor.count == 0) {
                    val values = ContentValues().apply {
                        put(InvoiceEntry.COLUMN_NAME_TENANT_ID, tenantId)
                        put(InvoiceEntry.COLUMN_NAME_INVOICE_DATE, invoiceDate) // Use provided invoiceDate
                        put(InvoiceEntry.COLUMN_NAME_DUE_DATE, dueDate)
                        put(InvoiceEntry.COLUMN_NAME_AMOUNT_DUE, amount)
                        put(InvoiceEntry.COLUMN_NAME_STATUS, "unpaid")
                    }
                    db.insert(InvoiceEntry.TABLE_NAME, null, values)
                    invoicesGenerated++
                }
                invoiceCheckCursor.close()
            } while (tenantCursor.moveToNext())
        }
        tenantCursor.close()
        return invoicesGenerated
    }
}
