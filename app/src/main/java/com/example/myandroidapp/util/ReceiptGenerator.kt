package com.example.myandroidapp.util

import android.content.Context
import android.database.Cursor
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.myandroidapp.data.TenantContract
import com.example.myandroidapp.data.TenantDbHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptGenerator {

    fun generateReceiptPdf(context: Context, paymentId: Long, dbHelper: TenantDbHelper): File? {
        val paymentCursor = dbHelper.getPaymentById(paymentId)
        if (!paymentCursor.moveToFirst()) {
            paymentCursor.close()
            return null
        }

        val tenantId = paymentCursor.getLong(paymentCursor.getColumnIndexOrThrow(TenantContract.PaymentEntry.COLUMN_NAME_TENANT_ID))
        val invoiceId = paymentCursor.getLong(paymentCursor.getColumnIndexOrThrow(TenantContract.PaymentEntry.COLUMN_NAME_INVOICE_ID)) // Might be 0 if not set (nullable)
        val paymentDateStr = paymentCursor.getString(paymentCursor.getColumnIndexOrThrow(TenantContract.PaymentEntry.COLUMN_NAME_PAYMENT_DATE))
        val paymentMethod = paymentCursor.getString(paymentCursor.getColumnIndexOrThrow(TenantContract.PaymentEntry.COLUMN_NAME_PAYMENT_METHOD))
        val amountPaid = paymentCursor.getDouble(paymentCursor.getColumnIndexOrThrow(TenantContract.PaymentEntry.COLUMN_NAME_AMOUNT_PAID))
        paymentCursor.close()

        val tenantCursor = dbHelper.getTenantById(tenantId)
        if (!tenantCursor.moveToFirst()) {
            tenantCursor.close()
            return null // Should not happen if DB integrity is maintained
        }
        val tenantName = tenantCursor.getString(tenantCursor.getColumnIndexOrThrow(TenantContract.TenantEntry.COLUMN_NAME_NAME))
        val apartmentNumber = tenantCursor.getString(tenantCursor.getColumnIndexOrThrow(TenantContract.TenantEntry.COLUMN_NAME_APARTMENT_NUMBER))
        tenantCursor.close()

        var invoiceDateStr: String? = null
        var invoiceAmountDue: Double? = null
        var invoiceStatus: String? = null
        var invoiceRemainingAmount: Double? = null
        if (invoiceId > 0) { // Check if invoiceId is valid
            val invoiceCursor = dbHelper.getInvoiceById(invoiceId)
            if (invoiceCursor.moveToFirst()) {
                invoiceDateStr = invoiceCursor.getString(invoiceCursor.getColumnIndexOrThrow(TenantContract.InvoiceEntry.COLUMN_NAME_INVOICE_DATE))
                invoiceAmountDue = invoiceCursor.getDouble(invoiceCursor.getColumnIndexOrThrow(TenantContract.InvoiceEntry.COLUMN_NAME_AMOUNT_DUE))
                invoiceStatus = invoiceCursor.getString(invoiceCursor.getColumnIndexOrThrow(TenantContract.InvoiceEntry.COLUMN_NAME_STATUS))
                invoiceRemainingAmount = invoiceCursor.getDouble(invoiceCursor.getColumnIndexOrThrow(TenantContract.InvoiceEntry.COLUMN_NAME_REMAINING_AMOUNT))
            }
            invoiceCursor.close()
        }

        val document = PdfDocument()
        val pageWidth = 595 // A4 width in points (approx 210mm * 72/25.4)
        val pageHeight = 842 // A4 height in points (approx 297mm * 72/25.4)
        val pageNumber = 1
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }
        val smallTextPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
        }

        var yPosition = 60f
        val xMargin = 40f
        val contentWidth = pageWidth - 2 * xMargin

        // Title
        canvas.drawText("RECEIPT", (pageWidth / 2).toFloat(), yPosition, titlePaint)
        yPosition += 40f

        // Property Info & Date (Right Aligned)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentDate = sdf.format(Date())
        canvas.drawText("Property Address: [Your Property Address Here]", xMargin, yPosition, textPaint)
        yPosition += 20f
        canvas.drawText("Receipt Date: $currentDate", xMargin, yPosition, textPaint)
        yPosition += 20f
        canvas.drawText("Payment Date: $paymentDateStr", xMargin, yPosition, textPaint)
        yPosition += 40f

        // Tenant Info
        canvas.drawText("Tenant Name: $tenantName", xMargin, yPosition, headerPaint)
        yPosition += 20f
        canvas.drawText("Apartment No: $apartmentNumber", xMargin, yPosition, textPaint)
        yPosition += 40f

        // Payment Details
        canvas.drawText("Amount Paid: $${String.format(Locale.US, "%.2f", amountPaid)} ($paymentMethod)", xMargin, yPosition, headerPaint)
        yPosition += 30f

        if (invoiceId > 0 && invoiceDateStr != null && invoiceAmountDue != null && invoiceStatus != null && invoiceRemainingAmount != null) {
            canvas.drawText("Details for Invoice #$invoiceId:", xMargin, yPosition, textPaint)
            yPosition += 15f
            canvas.drawText("Invoice Date: $invoiceDateStr", xMargin + 10f, yPosition, smallTextPaint)
            yPosition += 15f
            canvas.drawText("Invoice Status: $invoiceStatus", xMargin + 10f, yPosition, smallTextPaint)
            yPosition += 15f
            canvas.drawText("Original Amount Due: $${String.format(Locale.US, "%.2f", invoiceAmountDue)}", xMargin + 10f, yPosition, smallTextPaint)
            yPosition += 15f
            canvas.drawText("Amount Paid (this payment): $${String.format(Locale.US, "%.2f", amountPaid)}", xMargin + 10f, yPosition, smallTextPaint)
            yPosition += 15f
            canvas.drawText("Remaining Balance on Invoice (after this payment): $${String.format(Locale.US, "%.2f", invoiceRemainingAmount)}", xMargin + 10f, yPosition, smallTextPaint)
            yPosition += 15f
        }
        yPosition += 40f

        // Footer
        canvas.drawText("Thank you for your payment!", (pageWidth / 2).toFloat(), yPosition, titlePaint.apply { textSize = 16f; isFakeBoldText = false; })
        yPosition += 20f
        canvas.drawText("-- End of Receipt --", (pageWidth / 2).toFloat(), yPosition, smallTextPaint)

        document.finishPage(page)

        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Receipts")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val fileName = "Receipt_Payment_${paymentId}_Tenant_${tenantId}_${System.currentTimeMillis()}.pdf"
        val file = File(directory, fileName)

        return try {
            document.writeTo(FileOutputStream(file))
            document.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            document.close()
            null
        }
    }
}
