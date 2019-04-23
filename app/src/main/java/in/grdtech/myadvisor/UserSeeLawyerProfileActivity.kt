package `in`.grdtech.myadvisor

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_user_see_lawyer_profile.*

class UserSeeLawyerProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_see_lawyer_profile)

        // progress dialog
        val progress = ProgressDialog(this@UserSeeLawyerProfileActivity)
        progress.setMessage("Loading the profile")
        progress.show()

        content.visibility = View.INVISIBLE

        // getting the intent data
        val intentData = intent.extras
        if (intentData != null){
            val lawyerUid = intentData.getString("uid", null)
            if (lawyerUid != null){

                // firebase database reference
                val ref = FirebaseDatabase.getInstance().reference
                ref.child("lawyer").child(lawyerUid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        progress.dismiss()
                        Toast.makeText(this@UserSeeLawyerProfileActivity, "Error occurred while loading the profile.", Toast.LENGTH_LONG).show()
                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        // checking if exists
                        if (p0!!.hasChildren()){
                            val lawyer = p0.getValue(LawyerRegistrationActivity.Lawyer::class.java)
                            showImage(lawyer!!.photoUrl)
                            name.text = lawyer.name
                            email.text = lawyer.email
                            gender.text = lawyer.gender
                            experience.text = "Experience: \n${lawyer.experience} years."
                            location.text = "Operational location: \n${lawyer.location}"
                            educationInfo.text = "Education information:\n${lawyer.educationInfo}"
                            cases.text = "Type cases be handle:\n${lawyer.case}"
                            content.visibility = View.VISIBLE
                            progress.dismiss()
                        }else{
                            progress.dismiss()
                            Toast.makeText(this@UserSeeLawyerProfileActivity, "Did not get the user id.", Toast.LENGTH_LONG).show()
                        }
                    }
                })

            }else{
                progress.dismiss()
                Toast.makeText(this@UserSeeLawyerProfileActivity, "Did not get the user id.", Toast.LENGTH_LONG).show()
            }
        }else{
            progress.dismiss()
            Toast.makeText(this@UserSeeLawyerProfileActivity, "Intent is null.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showImage(photoUrl: String){
        Glide.with(this).load(photoUrl).asBitmap().centerCrop().into(object : BitmapImageViewTarget(dp) {
            override fun setResource(resource: Bitmap) {
                val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                circularBitmapDrawable.isCircular = true
                dp.setImageDrawable(circularBitmapDrawable)
            }
        })
    }
}
