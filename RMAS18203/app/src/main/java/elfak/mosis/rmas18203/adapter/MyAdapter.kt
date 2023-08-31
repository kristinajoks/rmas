package elfak.mosis.rmas18203.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import elfak.mosis.rmas18203.data.User
import elfak.mosis.rmas18203.R

class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val userList = ArrayList<User>()

    class MyViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView){

        val firstName: TextView = itemView.findViewById(R.id.tvFirstName)
        val lastName: TextView = itemView.findViewById(R.id.tvLastName)
        val username: TextView = itemView.findViewById(R.id.tvUserName)
        val points : TextView = itemView.findViewById(R.id.tvPoints)

        val rank : TextView = itemView.findViewById(R.id.tvRank)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = userList[position]

        holder.rank.text = (position + 1).toString()

        holder.firstName.text = currentItem.firstName
        holder.lastName.text = currentItem.lastName
        holder.username.text = currentItem.username
        holder.points.text = currentItem.points.toString()
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUserList(newUserList :List<User>){
        this.userList.clear()
        this.userList.addAll(newUserList)
        this.userList.sortByDescending { it.points }
        notifyDataSetChanged()
    }
}