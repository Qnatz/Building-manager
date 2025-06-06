package com.example.myandroidapp

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroidapp.data.TenantContract
import com.example.myandroidapp.data.TenantDbHelper

class TenantDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: TenantDbHelper
    private var tenantId: Long = -1

    private lateinit var textViewDetailTenantName: TextView
    private lateinit var textViewDetailApartmentNumber: TextView
    private lateinit var textViewDetailPhoneNumber: TextView
    private lateinit var textViewDetailOtherInfo: TextView
    private lateinit var listViewTenantInvoices: ListView
    private lateinit var listViewTenantPayments: ListView
    private lateinit var buttonAddPaymentForTenant: Button
    private lateinit var buttonGenerateInvoiceForTenant: Button


    private var invoiceAdapter: SimpleCursorAdapter? = null
    private var paymentAdapter: SimpleCursorAdapter? = null

    companion object {
        const val EXTRA_TENANT_ID = "com.example.myandroidapp.TENANT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_detail)

        dbHelper = TenantDbHelper(this)
        tenantId = intent.getLongExtra(EXTRA_TENANT_ID, -1)

        if (tenantId == -1L) {
            Toast.makeText(this, "Error: Tenant ID not found.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        textViewDetailTenantName = findViewById(R.id.textViewDetailTenantName)
        textViewDetailApartmentNumber = findViewById(R.id.textViewDetailApartmentNumber)
        textViewDetailPhoneNumber = findViewById(R.id.textViewDetailPhoneNumber)
        textViewDetailOtherInfo = findViewById(R.id.textViewDetailOtherInfo)
        listViewTenantInvoices = findViewById(R.id.listViewTenantInvoices)
        listViewTenantPayments = findViewById(R.id.listViewTenantPayments)
        buttonAddPaymentForTenant = findViewById(R.id.buttonAddPaymentForTenant)
        buttonGenerateInvoiceForTenant = findViewById(R.id.buttonGenerateInvoiceForTenant)

        loadTenantDetails()
        loadInvoices()
        loadPayments()

        buttonAddPaymentForTenant.setOnClickListener {
            val intent = Intent(this, RecordPaymentActivity::class.java)
            // Optionally, pre-fill tenant in RecordPaymentActivity by passing tenantId
            // intent.putExtra(RecordPaymentActivity.EXTRA_PREFILLED_TENANT_ID, tenantId)
            startActivity(intent) // May need to refresh lists after payment
        }

        buttonGenerateInvoiceForTenant.setOnClickListener {
             val tenantName = textViewDetailTenantName.text.toString()
             Toast.makeText(this, getString(R.string.invoice_generation_placeholder, tenantName), Toast.LENGTH_LONG).show()
        }
    }

    private fun loadTenantDetails() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TenantContract.TenantEntry.TABLE_NAME,
            null, // All columns
            "${TenantContract.TenantEntry._ID} = ?",
            arrayOf(tenantId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            textViewDetailTenantName.text = cursor.getString(cursor.getColumnIndexOrThrow(TenantContract.TenantEntry.COLUMN_NAME_NAME))
            textViewDetailApartmentNumber.text = cursor.getString(cursor.getColumnIndexOrThrow(TenantContract.TenantEntry.COLUMN_NAME_APARTMENT_NUMBER))
            textViewDetailPhoneNumber.text = cursor.getString(cursor.getColumnIndexOrThrow(TenantContract.TenantEntry.COLUMN_NAME_PHONE_NUMBER))
            textViewDetailOtherInfo.text = cursor.getString(cursor.getColumnIndexOrThrow(TenantContract.TenantEntry.COLUMN_NAME_OTHER_CONTACT_INFO))
        }
        cursor.close()
    }

    private fun loadInvoices() {
        val invoiceCursor = dbHelper.getInvoicesForTenantCursor(tenantId)
        val fromInvoiceColumns = arrayOf(
            TenantContract.InvoiceEntry._ID,
            TenantContract.InvoiceEntry.COLUMN_NAME_DUE_DATE,
            TenantContract.InvoiceEntry.COLUMN_NAME_AMOUNT_DUE,
            TenantContract.InvoiceEntry.COLUMN_NAME_STATUS
        )
        val toInvoiceViews = intArrayOf(
            R.id.textViewInvoiceId,
            R.id.textViewInvoiceDueDate,
            R.id.textViewInvoiceAmountDue,
            R.id.textViewInvoiceStatus
        )
        invoiceAdapter?.changeCursor(null) // Close old cursor
        invoiceAdapter = SimpleCursorAdapter(this, R.layout.list_item_invoice, invoiceCursor, fromInvoiceColumns, toInvoiceViews, 0)
        listViewTenantInvoices.adapter = invoiceAdapter
    }

    private fun loadPayments() {
        val paymentCursor = dbHelper.getPaymentsForTenantCursor(tenantId)
        val fromPaymentColumns = arrayOf(
            TenantContract.PaymentEntry.COLUMN_NAME_PAYMENT_DATE,
            TenantContract.PaymentEntry.COLUMN_NAME_AMOUNT_PAID,
            TenantContract.PaymentEntry.COLUMN_NAME_PAYMENT_METHOD,
            TenantContract.PaymentEntry.COLUMN_NAME_INVOICE_ID
        )
        val toPaymentViews = intArrayOf(
            R.id.textViewPaymentDate,
            R.id.textViewPaymentAmount,
            R.id.textViewPaymentMethod,
            R.id.textViewPaymentInvoiceId
        )
        paymentAdapter?.changeCursor(null) // Close old cursor
        paymentAdapter = SimpleCursorAdapter(this, R.layout.list_item_payment, paymentCursor, fromPaymentColumns, toPaymentViews, 0)
        listViewTenantPayments.adapter = paymentAdapter
    }

    override fun onResume() {
        super.onResume()
        // Refresh lists in case data changed (e.g. a payment was added for this tenant)
        loadInvoices()
        loadPayments()
    }

    override fun onDestroy() {
        invoiceAdapter?.cursor?.close()
        paymentAdapter?.cursor?.close()
        dbHelper.close()
        super.onDestroy()
    }
}
