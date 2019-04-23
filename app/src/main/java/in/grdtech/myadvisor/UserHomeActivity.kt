package `in`.grdtech.myadvisor

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import android.R.string.cancel
import android.content.DialogInterface
import android.content.Intent
import android.R.attr.password
import android.R.attr.key
import android.app.ProgressDialog
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*


class UserHomeActivity : AppCompatActivity() {

    // UI Init
    private lateinit var criminalLayout: LinearLayout
    private lateinit var criminalList: RecyclerView
    private lateinit var civilLayout: LinearLayout
    private lateinit var civilList: RecyclerView

    // firebase database reference
    private lateinit var ref: DatabaseReference

    // counter to check for cases
    private var caseCounter = 0

    // progress dialog
    private lateinit var progress: ProgressDialog

    // user uid
    private lateinit var uid: String

    // message list adapter
    private lateinit var messageListAdapter: MainRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        // firebase database
        ref = FirebaseDatabase.getInstance().reference

        // progress dialog init
        progress = ProgressDialog(this@UserHomeActivity)
        progress.setMessage("Checking profile")
        progress.show()

        // checking if user is registered or not
        checkUserRegistered()

    }

    // function checking if user is registered or not
    private fun checkUserRegistered() {
        // getting the current user variable
        val currentUser = FirebaseAuth.getInstance().currentUser!!

        // getting user uid
        uid = currentUser.uid

        ref.child("user").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                // checking if the record exists
                if (!p0!!.hasChildren()){

                    progress.dismiss()

                    // spinner init
                    val input = Spinner(this@UserHomeActivity)

                    // spinner adapter init
                    val locationArray= resources.getStringArray(R.array.location_array)
                    val spinnerAdapter = ArrayAdapter(this@UserHomeActivity, android.R.layout.simple_spinner_item, locationArray)
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    input.adapter = spinnerAdapter

                    val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT)
                    input.layoutParams = lp
                    val alertDialog = AlertDialog.Builder(this@UserHomeActivity)
                    alertDialog.setTitle("Choose location")
                            .setMessage("Choose the location from where you want to find the lawyers.")
                            .setView(input)
                            .setPositiveButton("YES", { _, _ ->
                                // spinner selected item
                                val location = input.selectedItem.toString()
                                // getting user information
                                val name = currentUser.displayName!!
                                val email = currentUser.email!!
                                val photoUrl = currentUser.photoUrl.toString()
                                val fcm = FirebaseInstanceId.getInstance().token!!

                                val user = User(name, email, photoUrl, fcm, location)

                                // now setting the value in database
                                ref.child("user").child(uid).setValue(user).addOnSuccessListener {
                                    Toast.makeText(this@UserHomeActivity, "Profile created.", Toast.LENGTH_LONG).show()
                                    loadUI(user)
                                }.addOnFailureListener {
                                    Toast.makeText(this@UserHomeActivity, "Error while creating the profile.", Toast.LENGTH_LONG).show()
                                }.addOnCompleteListener {
                                    progress.dismiss()
                                }
                                Toast.makeText(this@UserHomeActivity, location, Toast.LENGTH_LONG).show()
                            })
                            .setCancelable(false)
                            .create()
                            .show()
                }else{
                    val user = p0.getValue(User::class.java)
                    // calling function to load the UI according to user profile
                    loadUI(user)
                }
            }
        })
    }

    private fun loadUI(user: User?) {

        progress.setMessage("Getting the UI")
        progress.show()

        // UI init
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)
        val messagesLayout = findViewById<LinearLayout>(R.id.messagesLayout)
        val messageList = findViewById<RecyclerView>(R.id.messageList)
        val seeAllMessages = findViewById<Button>(R.id.seeAllMessages)
        criminalLayout = findViewById(R.id.criminalLayout)
        criminalList = findViewById(R.id.criminalList)
        val seeAllCriminal = findViewById<Button>(R.id.seeAllCriminal)
        civilLayout = findViewById(R.id.civilLayout)
        civilList = findViewById(R.id.civilList)
        val seeAllCivil = findViewById<Button>(R.id.seeAllCivil)

        // Buttons init

        // intent to see all page
        val intent = Intent(this@UserHomeActivity, SeeAllActivity::class.java)
        seeAllMessages.setOnClickListener {
            intent.putExtra("data", "message")
            startActivity(intent)
        }
        seeAllCriminal.setOnClickListener {
            intent.putExtra("data", "Criminal")
            startActivity(intent)
        }
        seeAllCivil.setOnClickListener {
            intent.putExtra("data", "Civil")
            startActivity(intent)
        }

        // adapters for lawyers list
        criminalList.setHasFixedSize(false)
        criminalList.layoutManager = LinearLayoutManager(this@UserHomeActivity)
        val criminalAdapter = MainRecyclerViewAdapter(this@UserHomeActivity)
        criminalList.adapter = criminalAdapter
        criminalAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                messageList.smoothScrollToPosition(criminalAdapter.itemCount)
            }
        })
        civilList.setHasFixedSize(false)
        civilList.layoutManager = LinearLayoutManager(this@UserHomeActivity)
        val civilAdapter = MainRecyclerViewAdapter(this@UserHomeActivity)
        civilList.adapter = civilAdapter
        criminalAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                messageList.smoothScrollToPosition(civilAdapter.itemCount)
            }
        })

        var counter = 0

        // getting the messages from the lawyer
        ref.child("chat").orderByKey().endAt(uid).limitToLast(3).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(this@UserHomeActivity, "Error occurred while loading chats.", Toast.LENGTH_LONG).show()
                counter += 1
                if (counter == 2)
                    progress.dismiss()
            }

            override fun onDataChange(p0: DataSnapshot?) {
                // checking is any record exists
                if (p0!!.hasChildren()){
                    messagesLayout.visibility = View.VISIBLE
                    val layoutManager =  LinearLayoutManager(this@UserHomeActivity)
                    messageList.setHasFixedSize(false)
                    messageList.layoutManager = layoutManager
                    // message recycler adapter
                    messageListAdapter = MainRecyclerViewAdapter(this@UserHomeActivity)
                    messageList.adapter = messageListAdapter
                    messageListAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                            messageList.smoothScrollToPosition(messageListAdapter.itemCount)
                        }
                    })
                    for (data in p0.children){
                        val key = data.key
                        val lastMessage = data.children.last().child("message").value.toString()
                        addMessageToRecyclerView(key, lastMessage)
                    }
                    counter += 1
                    if (counter == 2)
                        progress.dismiss()

                }else{
                    messagesLayout.visibility = View.GONE
                    Log.i("GETTING CHATS", "NO CHATS")
                    messageList.adapter = null
                    counter += 1
                    if (counter == 2)
                        progress.dismiss()
                }
            }
        })

        // getting the user location
        val location = user?.location

        // getting lawyers
        ref.child("lawyer").orderByChild("location").equalTo(location).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(this@UserHomeActivity, "Error occurred while loading lawyers.", Toast.LENGTH_LONG).show()
                counter += 1
                if (counter == 2)
                    progress.dismiss()
            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0!!.hasChildren()){
                    // counter for number of lawyers added in the array list
                    var criminalCounter = 0
                    var civilCounter = 0

                    for (data in p0.children){
                        val lawyer = data.getValue(LawyerRegistrationActivity.Lawyer::class.java)
                        val lawyerCase = lawyer!!.case
                        var handleCase = lawyerCase
                        if (handleCase == "Both"){
                            handleCase = "Criminal & Civil"
                        }
                        handleCase = "$handleCase lawyer"
                        val recyclerViewDataClass = RecyclerViewDataClass(lawyer.name, handleCase, lawyer.gender, lawyer.photoUrl)

                        when (lawyerCase){
                            "Both" -> {
                                if (criminalCounter < 3){
                                    criminalAdapter.addData(recyclerViewDataClass, data.key)
                                    criminalCounter += 1
                                }
                                if (civilCounter < 3){
                                    civilAdapter.addData(recyclerViewDataClass, data.key)
                                    civilCounter += 1
                                }
                            }
                            "Civil" -> {
                                if (civilCounter < 3){
                                    civilAdapter.addData(recyclerViewDataClass, data.key)
                                    civilCounter += 1
                                }
                            }
                            "Criminal" -> {
                                if (criminalCounter < 3){
                                    criminalAdapter.addData(recyclerViewDataClass, data.key)
                                    criminalCounter += 1
                                }
                            }
                        }
                        if (criminalCounter == 3 && civilCounter == 3){
                            break
                        }
                    }


                }else{
                    Toast.makeText(this@UserHomeActivity, "No lawyer found in $location location", Toast.LENGTH_LONG).show()
                }

                counter += 1
                if (counter == 2)
                    progress.dismiss()
            }
        })

    }

    // function which adds the item to recycler view
    private fun addMessageToRecyclerView(key: String?, lastMessage: String) {
        val lawyerUid = key!!.split("_")[0]

        ref.child("lawyer").child(lawyerUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(this@UserHomeActivity, "Error loading the message.", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot?) {
                // exists check
                if (p0!!.hasChildren()){
                    val lawyer = p0.getValue(LawyerRegistrationActivity.Lawyer::class.java)
                    var handleCase = lawyer!!.case
                    if (handleCase == "Both"){
                        handleCase = "Criminal & Civil"
                    }
                    handleCase = "$handleCase lawyer"
                    val recyclerViewDataClass = RecyclerViewDataClass(lawyer.name, handleCase, lastMessage, lawyer.photoUrl)
                    messageListAdapter.addData(recyclerViewDataClass, lawyerUid)
                }else{

                }
            }
        })

    }

    // adding the settings option
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.settings, menu)
        return true
    }

    // click listener on the settings button
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.settings ){
            startActivity(Intent(this@UserHomeActivity, UserSettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Data class for users
    data class User(val name: String, val email: String, val photoUrl: String, val fcm: String, val location: String){
        constructor(): this(", ", ", ", "", "", "")
    }
}
