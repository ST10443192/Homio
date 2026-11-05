package com.homio.app

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.homio.app.data.api.ApiClient
import com.homio.app.data.api.ApiErrors
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubmitPaymentActivity : BaseActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etMonth: EditText
    private lateinit var etReference: EditText
    private lateinit var etNote: EditText
    private lateinit var btnChoosePhoto: Button
    private lateinit var btnSubmit: Button
    private lateinit var tvPhotoStatus: TextView
    private lateinit var ivPreview: ImageView
    private lateinit var progress: ProgressBar

    // The shrunken photo that will be uploaded
    private var photoBytes: ByteArray? = null

    // The system photo picker - no storage permission needed
    private val pickPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) loadPhoto(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_payment)

        etAmount = findViewById(R.id.etAmount)
        etMonth = findViewById(R.id.etMonth)
        etReference = findViewById(R.id.etReference)
        etNote = findViewById(R.id.etNote)
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto)
        btnSubmit = findViewById(R.id.btnSubmitPayment)
        tvPhotoStatus = findViewById(R.id.tvPhotoStatus)
        ivPreview = findViewById(R.id.ivProofPreview)
        progress = findViewById(R.id.progressSubmit)

        // Default to the current month
        etMonth.setText(SimpleDateFormat("yyyy-MM", Locale.US).format(Date()))

        btnChoosePhoto.setOnClickListener {
            pickPhoto.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        btnSubmit.setOnClickListener { submit() }
    }

    private fun loadPhoto(uri: Uri) {
        lifecycleScope.launch {
            val bytes = ImageUtil.compressForUpload(this@SubmitPaymentActivity, uri)
            if (bytes == null) {
                Toast.makeText(
                    this@SubmitPaymentActivity,
                    getString(R.string.error_photo_unreadable),
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            photoBytes = bytes
            val options = BitmapFactory.Options().apply { inSampleSize = 2 }
            ivPreview.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options))
            ivPreview.visibility = View.VISIBLE
            tvPhotoStatus.text = getString(R.string.photo_ready, bytes.size / 1024)
            btnChoosePhoto.setText(R.string.btn_change_photo)
        }
    }

    private fun submit() {
        val amount = etAmount.text.toString().trim().replace(',', '.').toDoubleOrNull()
        val month = etMonth.text.toString().trim()
        val photo = photoBytes

        if (amount == null || amount <= 0.0) {
            etAmount.error = getString(R.string.error_payment_amount)
            return
        }
        if (!MONTH_PATTERN.matches(month)) {
            etMonth.error = getString(R.string.error_payment_month)
            return
        }
        if (photo == null) {
            Toast.makeText(this, getString(R.string.error_payment_photo), Toast.LENGTH_SHORT).show()
            return
        }

        setBusy(true)
        lifecycleScope.launch {
            val response = try {
                ApiClient.service.submitPayment(
                    MultipartBody.Part.createFormData("amount", String.format(Locale.US, "%.2f", amount)),
                    MultipartBody.Part.createFormData("month", month),
                    MultipartBody.Part.createFormData("reference", etReference.text.toString().trim()),
                    MultipartBody.Part.createFormData("note", etNote.text.toString().trim()),
                    MultipartBody.Part.createFormData(
                        "proof", "proof.jpg", photo.toRequestBody("image/jpeg".toMediaType())
                    )
                )
            } catch (e: Exception) {
                null
            }
            setBusy(false)

            when {
                response == null -> toast(getString(R.string.error_no_connection))
                response.isSuccessful -> {
                    toast(getString(R.string.payment_submitted))
                    finish()
                }
                else -> toast(
                    getString(
                        R.string.payment_failed,
                        ApiErrors.message(response) ?: response.code().toString()
                    )
                )
            }
        }
    }

    private fun setBusy(busy: Boolean) {
        btnSubmit.isEnabled = !busy
        btnChoosePhoto.isEnabled = !busy
        progress.visibility = if (busy) View.VISIBLE else View.GONE
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private companion object {
        val MONTH_PATTERN = Regex("^\\d{4}-(0[1-9]|1[0-2])$")
    }
}