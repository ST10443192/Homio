package com.homio.app

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.homio.app.models.Property

class AddPropertyActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_property)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etPropertyType = findViewById<EditText>(R.id.etPropertyType)
        val etRooms = findViewById<EditText>(R.id.etRooms)
        val etRentAmount = findViewById<EditText>(R.id.etRentAmount)
        val etDepositAmount = findViewById<EditText>(R.id.etDepositAmount)

        findViewById<android.widget.Button>(R.id.btnSaveProperty).setOnClickListener {
            val address = etAddress.text.toString().trim()
            val propertyType = etPropertyType.text.toString().trim()
            val rooms = etRooms.text.toString().trim().toIntOrNull() ?: 0
            val rentAmount = etRentAmount.text.toString().trim().toDoubleOrNull() ?: 0.0
            val depositAmount = etDepositAmount.text.toString().trim().toDoubleOrNull() ?: 0.0

            if (address.isEmpty() || propertyType.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_fill_address_type), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            val newDocRef = db.collection("properties").document()
            val property = Property(
                propertyId = newDocRef.id,
                landlordId = uid,
                address = address,
                propertyType = propertyType,
                rooms = rooms,
                rentAmount = rentAmount,
                depositAmount = depositAmount,
                status = "Vacant"
            )

            newDocRef.set(property)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.property_added), Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, getString(R.string.property_save_failed, e.message), Toast.LENGTH_LONG).show()
                }
        }
    }
}
