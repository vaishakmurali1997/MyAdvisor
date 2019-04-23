package `in`.grdtech.myadvisor

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_lawyer_registration.*

class UserSettingsActivity : AppCompatActivity() {
    private lateinit var ref: DatabaseReference
    private lateinit var uid: String
    private lateinit var progress: ProgressDialog


    // sign out
    private var mAuth: FirebaseAuth? = null
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    // UI ints
    private lateinit var circularImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_settings)

        // UI init
        circularImage = findViewById<ImageView>(R.id.imageView)
        val username = findViewById<TextView>(R.id.username)
        val changeLacation = findViewById<Button>(R.id.changeLocation)
        val signOut = findViewById<Button>(R.id.signOut)

        // progress init
        progress = ProgressDialog(this@UserSettingsActivity)
        progress.setMessage("Loading profile")
        progress.show()

        // firebase database
        ref = FirebaseDatabase.getInstance().reference

        // getting the intent
        val intentData = intent.extras
        if (intentData != null){
            val intentUid = intentData.getString("uid", null)
            if (intentUid != null){
                signOut.visibility = View.GONE
                changeLacation.visibility = View.GONE

                ref.child("user").child(intentUid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        Toast.makeText(this@UserSettingsActivity, "Error while loading the user profile.", Toast.LENGTH_LONG).show()
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        if (p0!!.hasChildren()){
                            val user = p0.getValue(UserHomeActivity.User::class.java)
                            username.text = user!!.name
                            val photoUrl = user.photoUrl
                            showImage(photoUrl)
                        }else{
                            Toast.makeText(this@UserSettingsActivity, "Was not able to find the user.", Toast.LENGTH_LONG).show()
                        }
                    }
                })

            }
        }else{
            // getting the current info of the user
            val photoUrl = FirebaseAuth.getInstance().currentUser!!.photoUrl
            val fullName = FirebaseAuth.getInstance().currentUser!!.displayName

            // setting the name of the user
            username.text = fullName

            // getting the photo
            showImage(photoUrl.toString())

            // sign out vars
            mAuth = FirebaseAuth.getInstance()

            mAuthStateListener = FirebaseAuth.AuthStateListener {
                if (mAuth!!.currentUser == null){
                    val i = Intent(this@UserSettingsActivity, LoginActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                    finish()
                }
            }

            // Configure Google Sign In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .requestProfile()
                    .build()


            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .enableAutoManage(this, GoogleApiClient.OnConnectionFailedListener {
                        Toast.makeText(applicationContext, "Network Error.", Toast.LENGTH_SHORT).show()
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build()

            signOut.setOnClickListener {
                mAuth!!.signOut()
                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                val i = Intent(this@UserSettingsActivity, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
                finish()
            }

            changeLacation.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser!!
                // getting user uid
                uid = currentUser.uid
                val input = Spinner(this@UserSettingsActivity)

                // spinner adapter init
                val locationArray = resources.getStringArray(R.array.location_array)
                val spinnerAdapter = ArrayAdapter(this@UserSettingsActivity, android.R.layout.simple_spinner_item, locationArray)
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                input.adapter = spinnerAdapter

                val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                input.layoutParams = lp
                val alertDialog = AlertDialog.Builder(this@UserSettingsActivity)
                alertDialog.setTitle("Choose location")
                        .setMessage("Choose the location from where you want to find the lawyers.")
                        .setView(input)
                        .setPositiveButton("YES", { _, _ ->
                            // spinner selected item
                            val location = input.selectedItem.toString()
                            ref.child("user").child(uid).child("location").setValue(location).addOnCompleteListener(OnCompleteListener {
                                Snackbar.make(changeLacation,"Location has been updated",Snackbar.LENGTH_LONG).show()
                            })
                        }).show()


            }
        }
    }

    private fun showImage(photoUrl: String){
        Glide.with(this).load(photoUrl).asBitmap().centerCrop().into(object : BitmapImageViewTarget(circularImage) {
            override fun setResource(resource: Bitmap) {
                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                circularBitmapDrawable.isCircular = true
                circularImage.setImageDrawable(circularBitmapDrawable)
                progress.dismiss()
            }
        })
    }
}
