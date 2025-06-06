package com.example.myandroidapp.adapter

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CursorAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import android.content.ActivityNotFoundException
import android.content.Intent
import com.example.myandroidapp.R
import com.example.myandroidapp.data.TenantContract
import com.example.myandroidapp.data.TenantDbHelper
import com.example.myandroidapp.util.ReceiptGenerator
import java.io.File

class PaymentCursorAdapter(
    private val activityContext: Context, // Renamed from 'context' to avoid conflict
    cursor: Cursor?,
    private val dbHelper: TenantDbHelper // Pass dbHelper for receipt generation
) : CursorAdapter(activityContext, cursor, 0) {

    private val receiptGenerator = ReceiptGenerator()

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.list_item_payment, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val textViewPaymentDate = view.findViewById<TextView>(R.id.textViewPaymentDate)
        val textViewPaymentAmount = view.findViewById<TextView>(R.id.textViewPaymentAmount)
        val textViewPaymentMethod = view.findViewById<TextView>(R.id.textViewPaymentMethod)
        val textViewPaymentInvoiceId = view.findViewById<TextView>(R.id.textViewPaymentInvoiceId)
        val layoutPaymentInvoiceId = view.findViewById<View>(R.id.layoutPaymentInvoiceId) // Get the parent LinearLayout
        val buttonViewReceipt = view.findViewById<Button>(R.id.buttonViewReceipt)
        val buttonShareReceipt = view.findViewById<Button>(R.id.buttonShareReceipt)

        val paymentDate = cursor.getString(cursor.getColumnIndexOrThrow(TenantContract.PaymentEntry.COLUMN_NAME_PAYMENT_DATE))
        val amountPaid = cursor.getDouble(cursor.getColumnIndexOrThrow(TenantContract.PaymentEntry.COLUMN_NAME_AMOUNT_PAID))
        val paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(TenantContract.PaymentEntry.COLUMN_NAME_PAYMENT_METHOD))
        val invoiceId = cursor.getLong(cursor.getColumnIndexOrThrow(TenantContract.PaymentEntry.COLUMN_NAME_INVOICE_ID))

        val paymentId = cursor.getLong(cursor.getColumnIndexOrThrow(TenantContract.PaymentEntry._ID))

        textViewPaymentDate.text = paymentDate
        textViewPaymentAmount.text = String.format(java.util.Locale.US, "%.2f", amountPaid)
        textViewPaymentMethod.text = paymentMethod
        if (invoiceId > 0) {
            textViewPaymentInvoiceId.text = invoiceId.toString()
            layoutPaymentInvoiceId.visibility = View.VISIBLE
        } else {
            textViewPaymentInvoiceId.text = "N/A"
            layoutPaymentInvoiceId.visibility = View.GONE
        }


        buttonViewReceipt.setOnClickListener {
            val generatedFile: File? = receiptGenerator.generateReceiptPdf(activityContext, paymentId, dbHelper)
            if (generatedFile != null) {
                // Toast.makeText(activityContext, "Receipt saved to: ${generatedFile.absolutePath}", Toast.LENGTH_LONG).show()
                val uri = FileProvider.getUriForFile(activityContext, "${activityContext.packageName}.provider", generatedFile)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/pdf")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    activityContext.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(activityContext, "No PDF viewer app found.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(activityContext, "Error generating receipt.", Toast.LENGTH_LONG).show()
            }
        }

        buttonShareReceipt.setOnClickListener {
            val generatedFile: File? = receiptGenerator.generateReceiptPdf(activityContext, paymentId, dbHelper)
            if (generatedFile != null) {
                val shareUri = FileProvider.getUriForFile(activityContext, "${activityContext.packageName}.provider", generatedFile)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    activityContext.startActivity(Intent.createChooser(intent, "Share Receipt Via"))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(activityContext, "No app can handle sharing.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(activityContext, "Error generating receipt for sharing.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
