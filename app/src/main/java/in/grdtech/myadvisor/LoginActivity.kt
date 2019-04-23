package `in`.grdtech.myadvisor

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private var RC_SIGN_IN = 1

    private val TAG = "Error:"

    private var mGoogleApiClient: GoogleApiClient? = null

    private var mAuth: FirebaseAuth? = null

    private var sign_in_button: SignInButton? = null

    private var mAuthStateListener: FirebaseAuth.AuthStateListener? = null

    private lateinit var progress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progress = ProgressDialog(this)
        progress.setMessage("Logging you in.")

        mAuth = FirebaseAuth.getInstance()

        mAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                if (isNetworkAvailable) {
                    val i = Intent(this@LoginActivity, SelectionActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(i)
                    finish()
                }else{
                    Toast.makeText(this@LoginActivity, "No internet connection found", Toast.LENGTH_LONG).show()
                    progress.dismiss()
                }
            }else{
                progress.dismiss()
            }
        }

        val termsConditionsBtn = findViewById<Button>(R.id.TermsConditionsBtn)
        termsConditionsBtn.setOnClickListener {
            val j = Intent(this@LoginActivity, termsandconditionsActivity::class.java)
            j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(j)
        }
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()


        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this) { Toast.makeText(applicationContext, "Network Error.", Toast.LENGTH_SHORT).show() }
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        sign_in_button = findViewById<SignInButton>(R.id.sign_in_button)


        sign_in_button!!.setOnClickListener {
            if (isNetworkAvailable)
                signIn()
            else
                Toast.makeText(this@LoginActivity, "No internet connection found.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        mAuth!!.addAuthStateListener(mAuthStateListener!!)
        super.onStart()
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                progress.dismiss()
                // Google Sign In was successful, authenticate with Firebase
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(applicationContext, result.toString(), Toast.LENGTH_LONG).show()
                progress.dismiss()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful) {
                        Log.w(TAG, "signInWithCredential", task.exception)
                        Toast.makeText(applicationContext, task.exception!!.toString() + "", Toast.LENGTH_SHORT).show()
                        progress.dismiss()
                    } else {
                        // code if logged in
                        // intent waal code likho
                        //checkIfInDatabase();

                    }
                }
    }

    private val isNetworkAvailable: Boolean
        get() {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo !=
                    null && activeNetworkInfo.isConnected
        }
}
