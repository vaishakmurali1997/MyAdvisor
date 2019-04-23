package `in`.grdtech.myadvisor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_user_home.*
import java.text.DecimalFormat
import java.util.ArrayList

data class RecyclerViewDataClass(val titleText: String, val subTitleText: String?, val subSubTitleText: String?, val iconLink: String)

class MainRecyclerViewAdapter(private val activity: Activity): RecyclerView.Adapter<MainRecyclerViewAdapterViewHolder>() {
    private var data: MutableList<RecyclerViewDataClass> = ArrayList()
    private var uids: ArrayList<String> = ArrayList()

    fun addData(lawyer: RecyclerViewDataClass, uid: String){
        data.add(lawyer)
        uids.add(uid)
        notifyItemInserted(data.size)
    }

    override fun onBindViewHolder(holder: MainRecyclerViewAdapterViewHolder?, position: Int) {
        holder?.bind(data[position])

        // getting the activity name so that we can get the data accordingly to pass in intent
        val activityName = activity.localClassName

        // checking from which activity it is called
        if (activityName == "UserHomeActivity"){
            // sending the user to the chat window
            holder!!.itemView.setOnClickListener {
                val i = Intent(activity, ChatActivity::class.java)
                i.putExtra("lawyerUid", uids[position])
                activity.startActivity(i)
            }
        }else if (activityName == "LawyerHomeActivity"){
            // sending the user to the chat window
            holder!!.itemView.setOnClickListener {
                val i = Intent(activity, ChatActivity::class.java)
                i.putExtra("userUid", uids[position])
                activity.startActivity(i)
            }
        }


    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MainRecyclerViewAdapterViewHolder {
        return MainRecyclerViewAdapterViewHolder(activity, activity.layoutInflater.inflate(R.layout.drawer_item, parent, false))
    }

}

class MainRecyclerViewAdapterViewHolder(private val activity: Activity, itemView: View): RecyclerView.ViewHolder(itemView){

    private var title: TextView? = null
    private var subTitle: TextView? = null
    private var subSubTitle: TextView? = null
    private var icon: ImageView? = null

    init {
        title = itemView.findViewById<View>(R.id.title) as TextView
        subTitle = itemView.findViewById(R.id.subTitle) as TextView
        subSubTitle = itemView.findViewById(R.id.subSubTitle) as TextView
        icon = itemView.findViewById(R.id.icon) as ImageView
    }

    @SuppressLint("SetTextI18n")
    fun bind(data: RecyclerViewDataClass) {
        title!!.text = data.titleText
        subTitle!!.text = data.subTitleText
        if (data.subSubTitleText!!.startsWith("https://firebasestorage.googleapis.com/") || data.subSubTitleText.startsWith("content://")) {
            subSubTitle!!.text = "Image"
        }else {
            subSubTitle!!.text = data.subSubTitleText
        }

        Glide.with(activity)
                .load(data.iconLink)
                .into(icon)

    }

}
