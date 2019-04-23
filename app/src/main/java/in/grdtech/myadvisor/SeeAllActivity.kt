package `in`.grdtech.myadvisor

import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_see_all.*

class SeeAllActivity : AppCompatActivity() {

    // adapter for the recycler view
    private lateinit var messageListAdapter: MainRecyclerViewAdapter

    // firebase databse reference
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_all)

        // progress dialog
        val progress = ProgressDialog(this@SeeAllActivity)
        progress.setMessage("Loading the list")
        progress.show()

        // list
        val layoutManager =  LinearLayoutManager(this@SeeAllActivity)
        list.setHasFixedSize(false)
        list.layoutManager = layoutManager

        // getting the intent data
        val intentData = intent.extras

        // null check
        if (intentData != null){

            // getting the value from the intent
            val gotData = intentData.getString("data", null)

            // null check
            if (gotData != null) {
                // firebase database reference
                ref = FirebaseDatabase.getInstance().reference
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                if (gotData == "message") {
                    // getting the chat messages
                    ref.child("chat").orderByKey().endAt(uid).limitToLast(3).addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {
                            Toast.makeText(this@SeeAllActivity, "Error occurred while loading chats.", Toast.LENGTH_LONG).show()

                            progress.dismiss()
                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            // checking is any record exists
                            if (p0!!.hasChildren()) {
                                content.visibility = View.VISIBLE
                                // message recycler adapter
                                messageListAdapter = MainRecyclerViewAdapter(this@SeeAllActivity)
                                list.adapter = messageListAdapter
                                messageListAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                                        list.smoothScrollToPosition(messageListAdapter.itemCount)
                                    }
                                })
                                for (data in p0.children) {
                                    val key = data.key
                                    val lastMessage = data.children.last().child("message").value.toString()
                                    addMessageToRecyclerView(key, lastMessage)
                                }
                                progress.dismiss()

                            } else {
                                content.visibility = View.GONE
                                Log.i("GETTING CHATS", "NO CHATS")
                                list.adapter = null
                                progress.dismiss()
                            }
                        }
                    })

                }else{
                    // finding lawyers

                    // creating the adapter for the recycler view
                    val lawyerAdapter = MainRecyclerViewAdapter(this@SeeAllActivity)
                    list.adapter = lawyerAdapter
                    lawyerAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                            list.smoothScrollToPosition(lawyerAdapter.itemCount)
                        }
                    })

                    ref.child("lawyer").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {
                            Toast.makeText(this@SeeAllActivity, "Error occurred while loading lawyers.", Toast.LENGTH_LONG).show()
                            progress.dismiss()
                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            if (p0!!.hasChildren()) {
                                // counter for number of lawyers added in the array list

                                for (data in p0.children) {
                                    val lawyer = data.getValue(LawyerRegistrationActivity.Lawyer::class.java)
                                    val lawyerCase = lawyer!!.case
                                    if (lawyerCase == gotData || lawyerCase == "Both") {
                                        var handleCase = lawyerCase
                                        if (handleCase == "Both") {
                                            handleCase = "Criminal & Civil"
                                        }
                                        handleCase = "$handleCase lawyer"
                                        val recyclerViewDataClass = RecyclerViewDataClass(lawyer.name, handleCase, lawyer.gender, lawyer.photoUrl)
                                        lawyerAdapter.addData(recyclerViewDataClass, data.key)
                                    }
                                }

                                progress.dismiss()

                            } else {
                                progress.dismiss()
                                Toast.makeText(this@SeeAllActivity, "No criminal lawyer found.", Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                }
            }else{
                Toast.makeText(this@SeeAllActivity, "Data is null.", Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(this@SeeAllActivity, "Intent is null.", Toast.LENGTH_LONG).show()
        }
    }

    // function which adds the item to recycler view
    private fun addMessageToRecyclerView(key: String?, lastMessage: String) {
        val lawyerUid = key!!.split("_")[0]

        ref.child("lawyer").child(lawyerUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Toast.makeText(this@SeeAllActivity, "Error loading the message.", Toast.LENGTH_LONG).show()
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

}
