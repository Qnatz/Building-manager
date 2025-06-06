package com.example.myandroidapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure this matches the new layout

        val btnViewTenants: Button = findViewById(R.id.btn_view_tenants)
        val btnAddTenant: Button = findViewById(R.id.btn_add_tenant)
        val btnRecordPayment: Button = findViewById(R.id.btn_record_payment)
        val btnGenerateInvoicesBulk: Button = findViewById(R.id.btn_generate_invoices_bulk)

        btnViewTenants.setOnClickListener {
            val intent = Intent(this@MainActivity, ViewTenantsActivity::class.java)
            startActivity(intent)
        }

        btnAddTenant.setOnClickListener {
            val intent = Intent(this@MainActivity, AddTenantActivity::class.java)
            startActivity(intent)
        }

        btnRecordPayment.setOnClickListener {
            val intent = Intent(this@MainActivity, RecordPaymentActivity::class.java)
            startActivity(intent)
        }

        btnGenerateInvoicesBulk.setOnClickListener {
            // Placeholder dates and amount for now
            val invoiceDate = "2024-07-01" // Ideally get current date formatted
            val dueDate = "2024-07-15"   // Ideally calculate based on current date
            val amount = 1000.00 // Example amount

            val dbHelper = TenantDbHelper(this@MainActivity)
            val count = dbHelper.generateMonthlyInvoices(invoiceDate, dueDate, amount)
            dbHelper.close() // Close helper after use

            if (count > 0) {
                Toast.makeText(this@MainActivity, getString(R.string.invoices_generated_toast, count), Toast.LENGTH_LONG).show()
            } else if (count == 0 && dbHelper.getAllTenantsCursor().count > 0) { // Check if tenants exist
                 Toast.makeText(this@MainActivity, "Invoices for $invoiceDate might already exist or no new invoices needed.", Toast.LENGTH_LONG).show()
            }
            else if (dbHelper.getAllTenantsCursor().count == 0) {
                 Toast.makeText(this@MainActivity, getString(R.string.no_tenants_found), Toast.LENGTH_LONG).show()
            }
            else {
                 Toast.makeText(this@MainActivity, getString(R.string.error_generating_invoices), Toast.LENGTH_LONG).show()
            }
        }
    }
}
