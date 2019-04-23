package `in`.grdtech.myadvisor

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AlertDialog
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LawyerSettingsActivity : AppCompatActivity() {

    //private initialization goes here
    private lateinit var ref: DatabaseReference
    private lateinit var uid: String
    private lateinit var progress: ProgressDialog

    // sign out
    private var mAuth: FirebaseAuth? = null
    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lawyer_settings)

        // All initialization goes here.
        val imageView = findViewById<ImageView>(R.id.imageView)
        val username = findViewById<TextView>(R.id.username)
        val editLegalProfile = findViewById<Button>(R.id.editLegalProfile)
        val signOut = findViewById<Button>(R.id.signOut)
        val photoUrl = FirebaseAuth.getInstance().currentUser!!.photoUrl
        progress = ProgressDialog(this)

        // Fetching image of user from google+ account
        progress.setTitle("Please Wait")
        progress.setMessage("We are loading your profile")
        progress.show()
        Glide.with(this).load(photoUrl).asBitmap().centerCrop().into(object : BitmapImageViewTarget(imageView) {
            override fun setResource(resource: Bitmap) {
                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                circularBitmapDrawable.isCircular = true
                imageView.setImageDrawable(circularBitmapDrawable)
                progress.dismiss()
                 }
            })

                // Displaying username
                username.text = FirebaseAuth.getInstance().currentUser!!.displayName



                // Action for edit legal profile
                editLegalProfile.setOnClickListener {
                    progress.setTitle("Please Wait")
                    progress.setMessage("We are loading your profile")
                    progress.show()
                    ref = FirebaseDatabase.getInstance().reference
                    val currentUser = FirebaseAuth.getInstance().currentUser!!
                    uid = currentUser.uid

                    ref.child("lawyer").child(uid).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            val lawyer = p0!!.getValue(LawyerRegistrationActivity.Lawyer::class.java)
                            val i = Intent(this@LawyerSettingsActivity,LawyerRegistrationActivity::class.java)
                            i.putExtra("By","Edit")
                            i.putExtra("EducationInfo",lawyer!!.educationInfo)
                            i.putExtra("experience",lawyer.experience)
                            i.putExtra("location",lawyer.location)
                            i.putExtra("case",lawyer.case)
                            i.putExtra("gender",lawyer.gender)
                            progress.dismiss()
                            startActivity(i)

                        }

                    })
                }

        // sign out vars
        mAuth = FirebaseAuth.getInstance()

        mAuthStateListener = FirebaseAuth.AuthStateListener {
            if (mAuth!!.currentUser == null){
                val i = Intent(this@LawyerSettingsActivity, LoginActivity::class.java)
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
            val i = Intent(this@LawyerSettingsActivity, LoginActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
        }
    }
}


