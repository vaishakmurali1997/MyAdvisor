package `in`.grdtech.myadvisor

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LawyerHomeActivity : AppCompatActivity() {

    // firebase database reference
    private lateinit var ref: DatabaseReference

    // counter to check for cases
    private var caseCounter = 0

    // progress
    private lateinit var progress: ProgressDialog

    // message list adapter
    private lateinit var messageListAdapter: MainRecyclerViewAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lawyer_home)

        // progress
        progress = ProgressDialog(this@LawyerHomeActivity)
        progress.setMessage("Loading home")
        progress.setCancelable(false)
        progress.show()

        // UI init
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)
        val messagesLayout = findViewById<LinearLayout>(R.id.messagesLayout)
        val messageList = findViewById<RecyclerView>(R.id.messageList)

        // creating firebase database reference
        ref = FirebaseDatabase.getInstance().reference

        // getting the uid of the user
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        // getting the messages for the lawyer
        ref.child("chat").orderByKey().startAt(uid).endAt(uid+"\uf8ff").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(this@LawyerHomeActivity, "Error occurred while loading chats.", Toast.LENGTH_LONG).show()
                progress.dismiss()
            }

            override fun onDataChange(p0: DataSnapshot?) {
                // checking is any record exists
                if (p0!!.hasChildren()) {
                    // TODO build up the logic for how to sore the chat
                    messagesLayout.visibility = View.VISIBLE
                    val layoutManager = LinearLayoutManager(this@LawyerHomeActivity)
                    messageList.setHasFixedSize(false)
                    messageList.layoutManager = layoutManager
                    // message recycler adapter
                    messageListAdapter = MainRecyclerViewAdapter(this@LawyerHomeActivity)
                    messageList.adapter = messageListAdapter
                    messageListAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                            messageList.smoothScrollToPosition(messageListAdapter.itemCount)
                        }
                    })
                    for (data in p0.children) {
                        val key = data.key
                        val lastMessage = data.children.last().child("message").value.toString()
                        addMessageToRecyclerView(key, lastMessage)
                    }

                    progress.dismiss()
                } else {
                    Log.i("GETTING CHATS", "NO CHATS")
                    progress.dismiss()
                }
            }
        })

    }
        // function which adds the item to recycler view
        private fun addMessageToRecyclerView(key: String?, lastMessage: String) {
            val userUid = key!!.split("_")[1]

            ref.child("user").child(userUid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    Toast.makeText(this@LawyerHomeActivity, "Error loading the message.", Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    // exists check
                    if (p0!!.hasChildren()){
                        val user = p0.getValue(UserHomeActivity.User::class.java)
                        val recyclerViewDataClass = RecyclerViewDataClass(user!!.name, "", lastMessage, user.photoUrl)
                        messageListAdapter.addData(recyclerViewDataClass, userUid)
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
            startActivity(Intent(this@LawyerHomeActivity, LawyerSettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}
