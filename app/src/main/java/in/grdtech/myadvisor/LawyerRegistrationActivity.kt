package `in`.grdtech.myadvisor

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.android.synthetic.main.activity_lawyer_registration.*

class LawyerRegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lawyer_registration)



        val content = findViewById<ConstraintLayout>(R.id.content)
        content.visibility = View.INVISIBLE

        // progress
        val progress = ProgressDialog(this@LawyerRegistrationActivity)
        progress.setCancelable(false)
        progress.setTitle("Please Wait")
        progress.setMessage("We are loading your profile")
        progress.show()

        // firebase database reference
        val ref = FirebaseDatabase.getInstance().reference

        // user info
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val uid = currentUser.uid

        // UI Inits
        val educationInfo = findViewById<EditText>(R.id.educationInfo)
        val experience = findViewById<EditText>(R.id.experience)
        val casesRadioGroup = findViewById<RadioGroup>(R.id.casesRadioGroup)
        val genderRadioGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)
        val addDataBtn = findViewById<Button>(R.id.addDataBtn)
        val location = findViewById<Spinner>(R.id.location)

        // getting locations
        val locationArray = resources.getStringArray(R.array.location_array)
        val locationAdapter = ArrayAdapter(this@LawyerRegistrationActivity, android.R.layout.simple_spinner_item, locationArray)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        location.adapter = locationAdapter

        // Edit mode from lawyerSettingsActivity
        val intentData = intent.extras
        var by: String? = null

        // intent Data null check
        if (intentData != null){
            by = intentData.getString("By", null)
        }

        if (by != null && by == "Edit"){
            progress.dismiss()
            // Setting values fetched from lawyerSettingsActivity intent

            addDataBtn.text = "Update"

            educationInfo.setText(intent.extras.getString("EducationInfo"))
            experience.setText(intent.extras.getString("experience"))
            val case = intent.extras.getString("case")
            // Using select statement for optimization for cases that lawyer handles
            when(case){
                "Criminal" -> criminalRB.isChecked = true
                "Civil" -> civilRB.isChecked = true
                "Both" -> bothRB.isChecked = true
            }
            // Using select statement for optimization for gender

            val gender = intent.extras.getString("gender")
            when(gender){
                "Male" -> maleRB.isChecked = true
                "Female" -> femaleRB.isChecked = true
            }
            val intentLocation = intent.extras.getString("location")

            // getting the location position from the array
            val locationPosition = locationArray.indexOf(intentLocation)
            location.setSelection(locationPosition)
            content.visibility = View.VISIBLE


        }else{

            // checking if the user already exists
            ref.child("lawyer").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    progress.dismiss()
                }

                override fun onDataChange(p0: DataSnapshot?) {
                    if (p0!!.hasChildren()){
                        progress.dismiss()
                        val i = Intent(this@LawyerRegistrationActivity, LawyerHomeActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)
                        finish()
                    }else{
                        progress.dismiss()
                        content.visibility = View.VISIBLE
                    }
                }
            })
        }


        addDataBtn.setOnClickListener {

            // getting values and empty checks
            val gotLocation = location.selectedItem.toString().trim()
            if(gotLocation.isEmpty()){
                Toast.makeText(this, "Please enter Location.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val gotExperience = experience.text.toString().trim()
            if(gotExperience.isEmpty()){
                Toast.makeText(this, "Please enter experience.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else{
                if (gotExperience.toLong() < 0L){
                    Toast.makeText(this, "Experience cannot be less than 0.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }

            val gotEducationinfo = educationInfo.text.toString().trim()
            if(gotEducationinfo.isEmpty()){
                Toast.makeText(this, "Please enter Education info.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val caseSelectedId = casesRadioGroup.checkedRadioButtonId
            var caseType: String? = null
            for (i in 0 until casesRadioGroup.childCount){
                val radioButton = casesRadioGroup.getChildAt(i) as RadioButton
                if (radioButton.id == caseSelectedId){
                    caseType = radioButton.text.toString().trim()
                    break
                }
            }

            if (caseType == null){
                Toast.makeText(this, "Please enter case info.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val genderSelectedId = genderRadioGroup.checkedRadioButtonId
            var gender: String? = null
            for (i in 0 until genderRadioGroup.childCount){
                val radioButton = genderRadioGroup.getChildAt(i) as RadioButton
                if (radioButton.id == genderSelectedId){
                    gender = radioButton.text.toString().trim()
                    break
                }
            }

            if (gender == null){
                Toast.makeText(this, "Please enter gender info.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            progress.setMessage("Adding data to the database.")
            progress.setCancelable(false)
            progress.show()

            // getting the user info
            val name = currentUser.displayName!!
            val email = currentUser.email!!
            val photoUrl = currentUser.photoUrl.toString()
            val fcm = FirebaseInstanceId.getInstance().token!!

            // creating the Lawyer class object
            val lawyer = Lawyer(name, email, fcm, photoUrl, gotLocation, gotExperience,
                    gotEducationinfo, caseType, gender)

            // adding the lawyer to database
            ref.child("lawyer").child(uid).setValue(lawyer).addOnSuccessListener {
                Toast.makeText(this, "Profile Created.", Toast.LENGTH_LONG).show()
                val i = Intent(this@LawyerRegistrationActivity, LawyerHomeActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Was not able to create the file.", Toast.LENGTH_LONG).show()
            }.addOnCompleteListener {
                progress.dismiss()
            }

        }

    }

    // data claas for the lawyer registration
    data class Lawyer(val name: String, val email: String, val fcm: String,
                      val photoUrl: String, val location: String, val experience: String,
                      val educationInfo: String, val case: String, val gender: String){
        constructor(): this("", "", "", "", "",
                "", "", "", "")
    }

}
