package com.gigforce.app.modules.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import kotlinx.android.synthetic.main.fragment_create_init_profile.*

/**
 * A simple [Fragment] subclass.
 * Use the [CreateInitProfile.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateInitProfile : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_init_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRV()
    }

    fun setupRV() {

        this.rv_ob_chats.layoutManager = LinearLayoutManager(this.context)
        val adapter = CreateInitProfileRVAdapter()

        // some initial data
        adapter.data.add(ObChatLogItem("in", "What's your name?"))
        adapter.data.add(ObChatLogItem("out", "Chirag Mittal"))
        adapter.data.add(ObChatLogItem("in", "What's your date of birth?"))

        this.rv_ob_chats.adapter = adapter
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CreateInitProfile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            CreateInitProfile().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

class CreateInitProfileRVAdapter: RecyclerView.Adapter<CreateInitProfileRVAdapter.ViewHolder>() {

    var data:ArrayList<ObChatLogItem> = ArrayList<ObChatLogItem>()

    override fun getItemViewType(position: Int): Int {
        if(data.get(position).flow_direction == "in") {
            return 101
        }else
        {
            return  102
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = (if(viewType == 101)
            R.layout.item_ob_chat_in
        else
            R.layout.item_ob_chat_out)

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size;
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data.get(position))
    }

    class ViewHolder(val view:View): RecyclerView.ViewHolder(view)
    {
        fun bind(item:ObChatLogItem) {
            view.findViewById<TextView>(R.id.txt).setText(item.text)
            // chanage profile_icon based in item.profile_icon_path
        }
    }
}

data class ObChatLogItem(
    val flow_direction:String, // in, out
    val text: String,
    val timestamp: String = "", // ideally a date format
    val profile_icon_path: String = "", // leave it blank
    val required_input_type: String = "text"   // text, date, time, options
){

}