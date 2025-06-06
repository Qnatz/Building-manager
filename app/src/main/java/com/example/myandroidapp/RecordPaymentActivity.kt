package com.example.myandroidapp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroidapp.data.TenantContract
import com.example.myandroidapp.data.TenantDbHelper

class RecordPaymentActivity : AppCompatActivity() {

    private lateinit var tenantSpinner: Spinner
    private lateinit var paymentDateEditText: EditText
    private lateinit var paymentMethodEditText: EditText
    private lateinit var amountPaidEditText: EditText
    private lateinit var invoiceSpinner: Spinner // Placeholder for now
    private lateinit var savePaymentButton: Button

    private lateinit var dbHelper: TenantDbHelper
    private var selectedTenantId: Long = -1
    private var selectedInvoiceId: Long? = null // To store the ID of the selected invoice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_payment)

        dbHelper = TenantDbHelper(this)

        tenantSpinner = findViewById(R.id.spinner_tenant)
        paymentDateEditText = findViewById(R.id.et_payment_date)
        paymentMethodEditText = findViewById(R.id.et_payment_method)
        amountPaidEditText = findViewById(R.id.et_amount_paid)
        invoiceSpinner = findViewById(R.id.spinner_invoice) // Placeholder
        savePaymentButton = findViewById(R.id.btn_save_payment)

        loadTenantSpinner()

        savePaymentButton.setOnClickListener {
            savePayment()
        }
    }

    private fun loadTenantSpinner() {
        val tenantCursor = dbHelper.getAllTenantsCursor()

        val fromColumns = arrayOf(TenantContract.TenantEntry.COLUMN_NAME_NAME)
        val toViews = intArrayOf(android.R.id.text1) // Use a default Android layout for simple text

        val adapter = SimpleCursorAdapter(
            this,
            android.R.layout.simple_spinner_item, // Use a default Android layout
            tenantCursor,
            fromColumns,
            toViews,
            0
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tenantSpinner.adapter = adapter

        tenantSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTenantId = id
                loadInvoiceSpinner(selectedTenantId) // Load invoices for the selected tenant
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedTenantId = -1
                invoiceSpinner.adapter = null // Clear invoice spinner
                selectedInvoiceId = null
            }
        }
    }

    private fun savePayment() {
        paymentDateEditText.error = null
        amountPaidEditText.error = null

        val paymentDate = paymentDateEditText.text.toString().trim()
        val paymentMethod = paymentMethodEditText.text.toString().trim()
        val amountPaidStr = amountPaidEditText.text.toString().trim()

        var isValid = true

        if (selectedTenantId == -1L) {
            Toast.makeText(this, getString(R.string.tenant_not_selected), Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (TextUtils.isEmpty(paymentDate)) {
            paymentDateEditText.error = getString(R.string.date_not_valid)
            isValid = false
        }
        // Basic date format validation (YYYY-MM-DD) - can be improved with DatePicker
        // else if (!paymentDate.matches("^\\d{4}-\\d{2}-\\d{2}$".toRegex())) {
        //     paymentDateEditText.error = "Invalid date format (YYYY-MM-DD)"
        //     isValid = false
        // }


        val amountPaid = amountPaidStr.toDoubleOrNull()
        if (amountPaid == null || amountPaid <= 0) {
            amountPaidEditText.error = getString(R.string.amount_not_valid)
            isValid = false
        }

        if (!isValid) {
            Toast.makeText(this, getString(R.string.please_correct_errors), Toast.LENGTH_SHORT).show()
            return
        }

        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(TenantContract.PaymentEntry.COLUMN_NAME_TENANT_ID, selectedTenantId)
            put(TenantContract.PaymentEntry.COLUMN_NAME_PAYMENT_DATE, paymentDate)
            put(TenantContract.PaymentEntry.COLUMN_NAME_PAYMENT_METHOD, paymentMethod)
            put(TenantContract.PaymentEntry.COLUMN_NAME_AMOUNT_PAID, amountPaid)
            selectedInvoiceId?.let { // Add invoice_id if an invoice was selected
                put(TenantContract.PaymentEntry.COLUMN_NAME_INVOICE_ID, it)
            }
        }

        val newRowId = db.insert(TenantContract.PaymentEntry.TABLE_NAME, null, values)

        if (newRowId == -1L) {
            Toast.makeText(this, getString(R.string.error_saving_payment), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.payment_saved_successfully), Toast.LENGTH_SHORT).show()
            // Update invoice if one was selected
            selectedInvoiceId?.let { invId ->
                amountPaid?.let { paidAmount -> // Ensure amountPaid is not null
                    updateInvoiceAfterPayment(invId, paidAmount)
                }
            }
            finish()
        }
    }

    private fun loadInvoiceSpinner(tenantId: Long) {
        if (tenantId == -1L) {
            invoiceSpinner.adapter = null
            selectedInvoiceId = null
            return
        }
        val invoiceCursor = dbHelper.getUnpaidInvoicesForTenantCursor(tenantId)

        val fromColumns = arrayOf(TenantContract.InvoiceEntry.COLUMN_NAME_INVOICE_DATE, TenantContract.InvoiceEntry.COLUMN_NAME_REMAINING_AMOUNT) // What to show in spinner
        val toViews = intArrayOf(android.R.id.text1, android.R.id.text2) // Default layout for two lines

        // Custom adapter might be better for formatting, but SimpleCursorAdapter can work
        // You might need a custom ViewBinder for SimpleCursorAdapter to format date and amount.
        val invoiceAdapter = SimpleCursorAdapter(
            this,
            android.R.layout.simple_spinner_item, // Or android.R.layout.simple_list_item_2 for two lines
            invoiceCursor,
            fromColumns,
            toViews,
            0
        )
        invoiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        invoiceSpinner.adapter = invoiceAdapter
        invoiceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedInvoiceId = id
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedInvoiceId = null
            }
        }
    }

    private fun updateInvoiceAfterPayment(invoiceId: Long, paymentAmount: Double) {
        val invoiceCursor = dbHelper.getInvoiceById(invoiceId)
        if (invoiceCursor.moveToFirst()) {
            val currentAmountDue = invoiceCursor.getDouble(invoiceCursor.getColumnIndexOrThrow(TenantContract.InvoiceEntry.COLUMN_NAME_AMOUNT_DUE))
            val currentRemainingAmount = invoiceCursor.getDouble(invoiceCursor.getColumnIndexOrThrow(TenantContract.InvoiceEntry.COLUMN_NAME_REMAINING_AMOUNT))
            invoiceCursor.close()

            val newRemainingAmount = currentRemainingAmount - paymentAmount
            val newStatus = when {
                newRemainingAmount <= 0 -> "paid"
                newRemainingAmount < currentAmountDue -> "partially_paid"
                else -> "unpaid" // Should ideally not happen if payment doesn't exceed remaining
            }
            dbHelper.updateInvoiceStatusAndRemainingAmount(invoiceId, newStatus, newRemainingAmount)
        } else {
            invoiceCursor.close()
        }
    }

    override fun onDestroy() {
        // The cursor used by SimpleCursorAdapter is managed by the adapter itself
        // and will be closed when the activity is destroyed if it's from a ContentProvider.
        // However, since we are creating it directly from dbHelper, it's good practice
        // to close it if the adapter is holding onto it and the activity is finishing.
        // But SimpleCursorAdapter often handles this.
        // For safety, dbHelper.close() is the most critical.
        dbHelper.close()
        super.onDestroy()
    }
}
