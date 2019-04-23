package `in`.grdtech.myadvisor

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection)

        val lawyer = findViewById<Button>(R.id.lawyer)
        val normalUser = findViewById<Button>(R.id.normalUser)

        lawyer.setOnClickListener {
            val i = Intent(this@SelectionActivity, LawyerRegistrationActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
        }

        normalUser.setOnClickListener {
            val j = Intent(this@SelectionActivity, UserHomeActivity::class.java)
            j.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(j)
            finish()
        }

    }
}
