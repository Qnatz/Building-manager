package com.example.myandroidapp

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroidapp.data.TenantContract
import com.example.myandroidapp.data.TenantDbHelper

class AddTenantActivity : AppCompatActivity() {

    private lateinit var mTenantNameEditText: EditText
    private lateinit var mApartmentNumberEditText: EditText
    private lateinit var mPhoneNumberEditText: EditText
    private lateinit var mOtherContactInfoEditText: EditText
    private lateinit var mSaveTenantButton: Button

    private lateinit var mDbHelper: TenantDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tenant)

        // Initialize UI elements
        mTenantNameEditText = findViewById(R.id.et_tenant_name)
        mApartmentNumberEditText = findViewById(R.id.et_apartment_number)
        mPhoneNumberEditText = findViewById(R.id.et_phone_number)
        mOtherContactInfoEditText = findViewById(R.id.et_email) // Mapped to other_contact_info

        mSaveTenantButton = findViewById(R.id.btn_save_tenant)

        // Instantiate TenantDbHelper
        mDbHelper = TenantDbHelper(this)

        // Set OnClickListener for the Save Tenant button
        mSaveTenantButton.setOnClickListener {
            saveTenant()
        }
    }

    private fun saveTenant() {
        // Clear previous errors
        mTenantNameEditText.error = null
        mApartmentNumberEditText.error = null
        mPhoneNumberEditText.error = null

        // Get text from EditText fields
        val nameString = mTenantNameEditText.text.toString().trim()
        val apartmentNumberString = mApartmentNumberEditText.text.toString().trim()
        val phoneNumberString = mPhoneNumberEditText.text.toString().trim()
        val otherContactInfoString = mOtherContactInfoEditText.text.toString().trim()

        var isValid = true

        // Validate Tenant Name
        if (nameString.isEmpty()) {
            mTenantNameEditText.error = "Name is required"
            isValid = false
        }

        // Validate Apartment Number
        if (apartmentNumberString.isEmpty()) {
            mApartmentNumberEditText.error = "Apartment number is required"
            isValid = false
        }

        // Validate Phone Number (if not empty)
        if (phoneNumberString.isNotEmpty()) {
            if (!phoneNumberString.matches("^[\\d\\s()+-]*$".toRegex()) || phoneNumberString.length < 7 || phoneNumberString.length > 15) {
                mPhoneNumberEditText.error = "Invalid phone number format (7-15 chars, digits, spaces, +, -, ())"
                isValid = false
            }
        }

        if (!isValid) {
            Toast.makeText(this@AddTenantActivity, "Please correct the errors.", Toast.LENGTH_SHORT).show()
            return // Stop before saving
        }

        // Get writable database
        val db: SQLiteDatabase = mDbHelper.writableDatabase

        // Create ContentValues object
        val cv = ContentValues().apply {
            put(TenantContract.TenantEntry.COLUMN_NAME_NAME, nameString)
            put(TenantContract.TenantEntry.COLUMN_NAME_APARTMENT_NUMBER, apartmentNumberString)
            put(TenantContract.TenantEntry.COLUMN_NAME_PHONE_NUMBER, phoneNumberString)
            put(TenantContract.TenantEntry.COLUMN_NAME_OTHER_CONTACT_INFO, otherContactInfoString)
        }

        // Insert new row
        val newRowId = db.insert(TenantContract.TenantEntry.TABLE_NAME, null, cv)

        // Check newRowId and show Toast message
        if (newRowId == -1L) {
            Toast.makeText(this, "Error saving tenant.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Tenant saved successfully.", Toast.LENGTH_SHORT).show()
            finish() // Optionally finish activity after successful save
        }
    }

    override fun onDestroy() {
        mDbHelper.close() // Close the database helper when the activity is destroyed
        super.onDestroy()
    }
}
