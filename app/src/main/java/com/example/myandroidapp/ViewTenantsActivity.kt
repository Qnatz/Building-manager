package com.example.myandroidapp

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroidapp.data.TenantContract
import com.example.myandroidapp.data.TenantDbHelper

class ViewTenantsActivity : AppCompatActivity() {

    private lateinit var dbHelper: TenantDbHelper
    private lateinit var tenantListView: ListView
    private var adapter: SimpleCursorAdapter? = null // Nullable because cursor might be null initially
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_tenants)

        dbHelper = TenantDbHelper(this)
        tenantListView = findViewById(R.id.tenantListView)
        searchEditText = findViewById(R.id.searchEditText)

        displayTenants(null) // Display all tenants initially

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                displayTenants(s?.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        tenantListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@ViewTenantsActivity, TenantDetailActivity::class.java)
            intent.putExtra(TenantDetailActivity.EXTRA_TENANT_ID, id) // id is the _ID of the selected tenant
            startActivity(intent)
        }
    }

    private fun displayTenants(query: String?) {
        val db: SQLiteDatabase = dbHelper.readableDatabase

        val projection = arrayOf(
            TenantContract.TenantEntry._ID,
            TenantContract.TenantEntry.COLUMN_NAME_NAME,
            TenantContract.TenantEntry.COLUMN_NAME_APARTMENT_NUMBER,
            TenantContract.TenantEntry.COLUMN_NAME_PHONE_NUMBER
        )

        val selection: String?
        val selectionArgs: Array<String>?

        if (query != null && query.isNotEmpty()) {
            selection = "${TenantContract.TenantEntry.COLUMN_NAME_NAME} LIKE ?"
            selectionArgs = arrayOf("%$query%")
        } else {
            selection = null
            selectionArgs = null
        }

        val cursor: Cursor = db.query(
            TenantContract.TenantEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            "${TenantContract.TenantEntry.COLUMN_NAME_NAME} ASC"
        )

        val fromColumns = arrayOf(
            TenantContract.TenantEntry.COLUMN_NAME_NAME,
            TenantContract.TenantEntry.COLUMN_NAME_APARTMENT_NUMBER,
            TenantContract.TenantEntry.COLUMN_NAME_PHONE_NUMBER
        )

        val toViews = intArrayOf(
            R.id.textViewTenantName,
            R.id.textViewApartmentNumber,
            R.id.textViewPhoneNumber
        )

        // If an old adapter exists, close its cursor by changing it to null
        // SimpleCursorAdapter's changeCursor method handles closing the old cursor
        adapter?.changeCursor(null)

        adapter = SimpleCursorAdapter(
            this,
            R.layout.list_item_tenant,
            cursor,
            fromColumns,
            toViews,
            0
        )
        tenantListView.adapter = adapter
    }

    override fun onDestroy() {
        adapter?.cursor?.close() // Close the cursor if it exists and is open
        dbHelper.close()
        super.onDestroy()
    }
}
