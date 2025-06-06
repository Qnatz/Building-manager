package com.example.myandroidapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myandroidapp.data.TenantContract.TenantEntry
import com.example.myandroidapp.data.TenantContract.InvoiceEntry
import com.example.myandroidapp.data.TenantContract.PaymentEntry

class TenantDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "TenantManagement.db"
        const val DATABASE_VERSION = 3 // Incremented version

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
                    "${InvoiceEntry.COLUMN_NAME_REMAINING_AMOUNT} REAL NOT NULL," + // Added new column
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
        // Simple drop/recreate for development. For production, use ALTER TABLE.
        // For this upgrade, we'd check if oldVersion < 3 and newVersion >= 3
        // and then execute ALTER TABLE for the invoices table if it exists.
        // But for now:
        db.execSQL(SQL_DELETE_PAYMENTS)
        db.execSQL(SQL_DELETE_INVOICES)
        db.execSQL(SQL_DELETE_TENANTS)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun getAllTenantsCursor(): Cursor {
        val db = this.readableDatabase
        return db.query(
            TenantEntry.TABLE_NAME,
            arrayOf(TenantEntry._ID, TenantEntry.COLUMN_NAME_NAME),
            null, null, null, null,
            "${TenantEntry.COLUMN_NAME_NAME} ASC"
        )
    }

    fun getInvoicesForTenantCursor(tenantId: Long): Cursor {
        val db = this.readableDatabase
        val selection = "${InvoiceEntry.COLUMN_NAME_TENANT_ID} = ?"
        val selectionArgs = arrayOf(tenantId.toString())
        return db.query(
            InvoiceEntry.TABLE_NAME, null, selection, selectionArgs, null, null,
            "${InvoiceEntry.COLUMN_NAME_INVOICE_DATE} DESC"
        )
    }

    fun getUnpaidInvoicesForTenantCursor(tenantId: Long): Cursor {
        val db = this.readableDatabase
        val selection = "${InvoiceEntry.COLUMN_NAME_TENANT_ID} = ? AND (${InvoiceEntry.COLUMN_NAME_STATUS} = ? OR ${InvoiceEntry.COLUMN_NAME_STATUS} = ?)"
        val selectionArgs = arrayOf(tenantId.toString(), "unpaid", "partially_paid")
        return db.query(
            InvoiceEntry.TABLE_NAME,
            arrayOf(InvoiceEntry._ID, InvoiceEntry.COLUMN_NAME_INVOICE_DATE, InvoiceEntry.COLUMN_NAME_REMAINING_AMOUNT), // Include relevant fields for display
            selection,
            selectionArgs,
            null, null,
            "${InvoiceEntry.COLUMN_NAME_INVOICE_DATE} ASC"
        )
    }


    fun getPaymentsForTenantCursor(tenantId: Long): Cursor {
        val db = this.readableDatabase
        val selection = "${PaymentEntry.COLUMN_NAME_TENANT_ID} = ?"
        val selectionArgs = arrayOf(tenantId.toString())
        return db.query(
            PaymentEntry.TABLE_NAME, null, selection, selectionArgs, null, null,
            "${PaymentEntry.COLUMN_NAME_PAYMENT_DATE} DESC"
        )
    }

    fun generateMonthlyInvoices(invoiceDate: String, dueDate: String, amount: Double): Int {
        val db = this.writableDatabase
        var invoicesGenerated = 0
        val tenantCursor = getAllTenantsCursor()

        if (tenantCursor.moveToFirst()) {
            do {
                val tenantId = tenantCursor.getLong(tenantCursor.getColumnIndexOrThrow(TenantEntry._ID))
                val invoiceCheckCursor = db.query(
                    InvoiceEntry.TABLE_NAME, arrayOf(InvoiceEntry._ID),
                    "${InvoiceEntry.COLUMN_NAME_TENANT_ID} = ? AND ${InvoiceEntry.COLUMN_NAME_INVOICE_DATE} = ?",
                    arrayOf(tenantId.toString(), invoiceDate),
                    null, null, null, "1"
                )

                if (invoiceCheckCursor.count == 0) {
                    val values = ContentValues().apply {
                        put(InvoiceEntry.COLUMN_NAME_TENANT_ID, tenantId)
                        put(InvoiceEntry.COLUMN_NAME_INVOICE_DATE, invoiceDate)
                        put(InvoiceEntry.COLUMN_NAME_DUE_DATE, dueDate)
                        put(InvoiceEntry.COLUMN_NAME_AMOUNT_DUE, amount)
                        put(InvoiceEntry.COLUMN_NAME_REMAINING_AMOUNT, amount) // Initialize remaining_amount
                        put(InvoiceEntry.COLUMN_NAME_STATUS, "unpaid")          // Initialize status
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

    fun getTenantById(tenantId: Long): Cursor {
        val db = this.readableDatabase
        return db.query(
            TenantEntry.TABLE_NAME, null, "${TenantEntry._ID} = ?", arrayOf(tenantId.toString()),
            null, null, null, "1"
        )
    }

    fun getInvoiceById(invoiceId: Long): Cursor {
        val db = this.readableDatabase
        return db.query(
            InvoiceEntry.TABLE_NAME, null, "${InvoiceEntry._ID} = ?", arrayOf(invoiceId.toString()),
            null, null, null, "1"
        )
    }

    fun getPaymentById(paymentId: Long): Cursor {
        val db = this.readableDatabase
        return db.query(
            PaymentEntry.TABLE_NAME, null, "${PaymentEntry._ID} = ?", arrayOf(paymentId.toString()),
            null, null, null, "1"
        )
    }

    fun updateInvoiceStatusAndRemainingAmount(invoiceId: Long, newStatus: String, newRemainingAmount: Double): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(InvoiceEntry.COLUMN_NAME_STATUS, newStatus)
            put(InvoiceEntry.COLUMN_NAME_REMAINING_AMOUNT, newRemainingAmount)
        }
        val selection = "${InvoiceEntry._ID} = ?"
        val selectionArgs = arrayOf(invoiceId.toString())
        return db.update(InvoiceEntry.TABLE_NAME, values, selection, selectionArgs)
    }
}
