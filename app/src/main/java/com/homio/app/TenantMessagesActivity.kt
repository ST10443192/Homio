package com.homio.app

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class TenantMessagesActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var llMessagesList: LinearLayout
    private lateinit var scrollMessages: ScrollView
    private lateinit var etMessageInput: EditText
    private lateinit var btnSendMessage: ImageButton
    private lateinit var btnRecordAudio: ImageButton
    private var listener: ListenerRegistration? = null

    private val myRole = "tenant"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tenant_messages)

        db = FirebaseFirestore.getInstance()
        llMessagesList = findViewById(R.id.llMessagesList)
        scrollMessages = findViewById(R.id.scrollMessages)


        etMessageInput = findViewById(R.id.etMessage)
        btnSendMessage = findViewById(R.id.btnSendMessage)
        btnRecordAudio = findViewById(R.id.btnRecordAudio)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavHelper.setupTenantNav(this, bottomNav, R.id.nav_messages)

        btnSendMessage.setOnClickListener { sendMessage() }
        btnRecordAudio.setOnClickListener {
            // Audio recording hook
        }
    }

    override fun onStart() {
        super.onStart()
        loadMessages()
    }

    override fun onStop() {
        super.onStop()
        listener?.remove()
        listener = null
    }

    private fun sendMessage() {
        val text = etMessageInput.text.toString().trim()
        if (text.isEmpty()) return

        val message = hashMapOf(
            "text" to text,
            "senderRole" to myRole,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("messages").add(message)
            .addOnSuccessListener {
                etMessageInput.setText("")
            }
    }

    private fun loadMessages() {
        listener?.remove()
        listener = db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                llMessagesList.removeAllViews()

                if (snapshot.isEmpty) {
                    val empty = TextView(this)
                    empty.text = getString(R.string.no_messages)
                    empty.setPadding(0, 12, 0, 12)
                    llMessagesList.addView(empty)
                    return@addSnapshotListener
                }

                for (doc in snapshot.documents) {
                    val text = doc.getString("text") ?: continue
                    val senderRole = doc.getString("senderRole") ?: "landlord"
                    val timestamp = doc.getTimestamp("timestamp")
                    val timeStr = if (timestamp != null) {
                        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(timestamp.toDate())
                    } else ""

                    val isMine = senderRole == myRole
                    val layoutRes = if (isMine) R.layout.item_message_sent else R.layout.item_message_received
                    val itemView = LayoutInflater.from(this).inflate(layoutRes, llMessagesList, false)

                    itemView.findViewById<TextView>(R.id.tvMessageText).text = text
                    itemView.findViewById<TextView>(R.id.tvMessageTime).text = timeStr

                    if (!isMine) {
                        itemView.findViewById<TextView>(R.id.tvSenderLabel)?.text =
                            getString(R.string.role_landlord)
                    }

                    llMessagesList.addView(itemView)
                }

                scrollMessages.post { scrollMessages.fullScroll(ScrollView.FOCUS_DOWN) }
            }
    }
}