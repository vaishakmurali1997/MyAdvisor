package `in`.grdtech.myadvisor

import com.bumptech.glide.Glide
import android.view.ViewGroup
import android.widget.TextView
import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import `in`.grdtech.myadvisor.ChatMessageViewHolder




/**
 * Created by Gammy on 17/12/17.
 */
internal class ChatMessageAdapter(private val activity: Activity) : RecyclerView.Adapter<ChatMessageViewHolder>() {
    var messages: MutableList<ChatMessage> = ArrayList()

    fun addMessage(chat: ChatMessage) {
        messages.add(chat)
        notifyItemInserted(messages.size)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        return ChatMessageViewHolder(activity, activity.layoutInflater.inflate(android.R.layout.two_line_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    companion object {
        private val TAG = "ChatMessageAdapter"
    }
}
class ChatMessageViewHolder(private val activity: Activity, itemView: View) : RecyclerView.ViewHolder(itemView) {

    var name: TextView
    var message: TextView
    var image: ImageView

    init {
        name = itemView.findViewById(android.R.id.text1)
        message = itemView.findViewById(android.R.id.text2)
        image = ImageView(activity)
        (itemView as ViewGroup).addView(image)

    }

    fun bind(chat: ChatMessage) {
        name.text = chat.name
        if (chat.message!!.startsWith("https://firebasestorage.googleapis.com/") || chat.message.startsWith("content://")) {
            message.visibility = View.INVISIBLE
            image.visibility = View.VISIBLE
            Glide.with(activity)
                    .load(chat.message)
                    .into(image)
            image.maxHeight = 300
            image.maxWidth = 200
        } else {
            message.visibility = View.VISIBLE
            image.visibility = View.GONE
            message.text = chat.message
        }
    }

    companion object {
        private val TAG = "ChatMessageViewHolder"
    }
}

data class ChatMessage(val name: String?, val message: String?){
    constructor(): this("", "")
}