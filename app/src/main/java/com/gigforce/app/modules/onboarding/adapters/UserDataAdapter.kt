package com.gigforce.app.modules.onboarding.adapters

import android.content.Context
import android.content.Intent
//import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.onboarding.models.UserData
import kotlinx.android.synthetic.main.recview_item.view.*

class UserDataAdapter(val context: Context?, private var users: List<UserData>) : RecyclerView.Adapter<UserDataAdapter.MyViewHolder>() {

//    private val mAdapter=RecyclerView.Adapter<UserDataAdapter.MyViewHolder>()
    companion object {
        val TAG: String = UserDataAdapter::class.java.simpleName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recview_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = users[position]
        holder.setData(user, position)
    }

    fun update(modelList:ArrayList<UserData>){
        users = modelList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var currentUser: UserData? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                currentUser?.let {
                    //context.showToast(currentUser!!.name + " Clicked !")
                    Toast.makeText(context,currentUser!!.name+" Clicked!", currentPosition)
                }
            }

            itemView.imgShare.setOnClickListener {
                currentUser?.let {
                    val message: String = "My hobby is: " + currentUser!!.name

                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_TEXT, message)
                    intent.type = "text/plain"

                    context!!.startActivity(Intent.createChooser(intent, "Please select app: "))
                }
            }
        }

        fun setData(user: UserData?, pos: Int) {
            user?.let {
                itemView.txvTitle.text = user.name
            }

            this.currentUser = user
            this.currentPosition = pos
        }
    }
}