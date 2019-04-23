package `in`.grdtech.myadvisor

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.*
import android.support.v7.widget.LinearLayoutManager
import android.content.Intent
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.app.ProgressDialog
import android.util.Log
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference






class ChatActivity : AppCompatActivity() {

    // UI INIT
    private lateinit var header: RelativeLayout
    private lateinit var userIcon: ImageView
    private lateinit var usernameTxt: TextView
    private lateinit var seeProfile: Button
    private lateinit var messagesList: RecyclerView
    private lateinit var imageBtn: ImageButton
    private lateinit var messageTxt: EditText
    private lateinit var sendBtn: Button

    private val adapter = ChatMessageAdapter(this@ChatActivity)
    val name = FirebaseAuth.getInstance().currentUser!!.displayName

    // firebase database
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // added the line so that the ui moves up when keyboard is open
        window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        // getting the info from the intent
        val intentData = intent.extras
        val lawyerUid = intentData.getString("lawyerUid", null)
        val userUid = intentData.getString("userUid", null)

        // getting the main uid to get the data from
        val mainUid = lawyerUid ?: userUid

        header = findViewById(R.id.header)
        userIcon = findViewById(R.id.userIcon)
        usernameTxt = findViewById(R.id.usernameTxt)
        seeProfile = findViewById(R.id.seeProfile)
        messagesList = findViewById(R.id.messagesList)
        imageBtn = findViewById(R.id.imageBtn)
        messageTxt = findViewById(R.id.messageTxt)
        sendBtn = findViewById(R.id.sendBtn)

        // get the opposite user information in the header
        getOtherUserProfile(lawyerUid, userUid)

        // getting the current user uid
        val uid =  FirebaseAuth.getInstance().currentUser!!.uid

        // creating the firebase database reference path
        val chatRefPath = if (lawyerUid != null){
            lawyerUid + "_$uid"
        }else{
            "${uid}_" + userUid
        }

        // firebase database reference with full and functional path
        ref = FirebaseDatabase.getInstance().reference!!.child("chat").child(chatRefPath)

        // button on click listener
        sendBtn.setOnClickListener {

            // empty check
            val message = messageTxt.text.toString().trim()
            if (message.isEmpty()){
                return@setOnClickListener
            }

            val chat = ChatMessage(name, message)
            // Push the chat message to the database
            ref.push().setValue(chat)
            messageTxt.setText("")
        }
        imageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER)
        }

        val layoutManager = LinearLayoutManager(this)
        // stack from end take us to at the end of the recycler view
        layoutManager.stackFromEnd = true
        messagesList.setHasFixedSize(false)
        messagesList.layoutManager = layoutManager

        messagesList.adapter = adapter
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                messagesList.smoothScrollToPosition(adapter.itemCount)
            }
        })

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                if (p0 != null){
                    if (p0.hasChildren()){
                        val chat = p0.getValue(ChatMessage::class.java)
                        adapter.addMessage(chat!!)
                    }else{
                        Toast.makeText(this@ChatActivity, "p0 no children", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this@ChatActivity, "p0 is null", Toast.LENGTH_LONG).show()
                }
            }

            override fun onChildRemoved(p0: DataSnapshot?) {

            }

        })
    }

    // function to get the other user data
    private fun getOtherUserProfile(lawyerUid: String?, userUid: String?) {
        val otherUserRef = FirebaseDatabase.getInstance().reference

        var userProfilePath: String? = null
        var gotUid: String? = null
        val profileIntent: Intent

        // null check
        if (lawyerUid != null){
            userProfilePath = "lawyer"
            gotUid = lawyerUid
            // TODO add the destination intent
            profileIntent = Intent(this@ChatActivity, UserSeeLawyerProfileActivity::class.java)
        }else{
            userProfilePath = "user"
            gotUid = userUid
            // TODO add the destination intent
            profileIntent = Intent(this@ChatActivity, UserSettingsActivity::class.java)
        }
        // see profile button on click listener... It is over here so that we could get to know that
        // which profile has to be seen lawyer's or user's
        seeProfile.setOnClickListener {
            profileIntent.putExtra("uid", gotUid)
            startActivity(profileIntent)
        }



        otherUserRef.child(userProfilePath).child(gotUid).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(this@ChatActivity, "Error while loading other person profile.", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.hasChildren()){
                    val name = p0.child("name").value.toString()
                    usernameTxt.text = name
                    val photoUrl = p0.child("photoUrl").value.toString()

                    // calling the function to load the  image
                    loadPhoto(photoUrl)
                }else{
                    Toast.makeText(this@ChatActivity, "No profile found.", Toast.LENGTH_LONG).show()
                }
            }
        })

    }

    // function to the image of the user
    private fun loadPhoto(photoUrl: String) {
        Glide.with(this@ChatActivity)
                .load(photoUrl)
                .into(userIcon)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_PHOTO_PICKER && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data

            // null check
            if (selectedImageUri != null) {

                val progress = ProgressDialog(this@ChatActivity)
                progress.setCancelable(false)
                progress.setMessage("Uploading picture")
                progress.show()

                Toast.makeText(this@ChatActivity, selectedImageUri.toString(), Toast.LENGTH_LONG).show()
                // Get a reference to the location where we'll store our photos
                val storage = FirebaseStorage.getInstance().reference
                // Get a reference to store file at chat_photos/<FILENAME>
                val photoRef = storage.child("chatting").child(selectedImageUri.lastPathSegment)

                // Upload file to Firebase Storage
                photoRef.putFile(selectedImageUri)
                        .addOnSuccessListener(this, { taskSnapshot ->
                            // When the image has successfully uploaded, we get its download URL
                            val downloadUrl = taskSnapshot.downloadUrl
                            // Set the download URL to the message box, so that the user can send it to the database
                            val chat = ChatMessage(name, downloadUrl.toString())
                            // Push the chat message to the database
                            ref.push().setValue(chat)

                        })
                        .addOnProgressListener { taskSnapshot ->
                            val percentUploaded = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                            progress.setMessage("${percentUploaded.toInt()} % uploaded")
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this@ChatActivity, exception.message, Toast.LENGTH_LONG).show()
                            Log.e("addOnFailureListener", exception.message)
                        }.addOnCompleteListener {
                            progress.dismiss()
                        }
            }
        }
    }

    companion object {
        val RC_PHOTO_PICKER = 13
    }
}
