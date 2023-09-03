import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import elfak.mosis.rmas18203.R
import elfak.mosis.rmas18203.data.Place
import elfak.mosis.rmas18203.data.PlacePurpose
import elfak.mosis.rmas18203.data.PlaceType

class PlacesAdapter : ListAdapter<Place, PlacesAdapter.PlaceViewHolder>(PlaceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.place_item, parent, false)
        return PlaceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = getItem(position)

        holder.nameTextView.text = place.name
        holder.descriptionTextView.text = place.description
        holder.ratingTextView.text = place.rating.toString()
        holder.dateTextView.text = place.dateCreated
        holder.aktTextView.text = place.purpose
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nazivMestaTV)
        val descriptionTextView: TextView = itemView.findViewById(R.id.opisMestaTV)
        val ratingTextView: TextView = itemView.findViewById(R.id.ocenaMestaTV)
        val dateTextView: TextView = itemView.findViewById(R.id.datumMestaTV)
        val aktTextView: TextView = itemView.findViewById(R.id.aktMestaTV)
    }

    private class PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            // Check if items have the same ID (e.g., unique identifier for places)
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            // Check if the contents of the items are the same (e.g., all fields)
            return oldItem == newItem
        }
    }
}

class PlaceTypeAdapter(context: Context) : ArrayAdapter<PlaceType>(context, android.R.layout.simple_spinner_item, PlaceType.values()) {
    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
}

class PlacePurposeAdapter(context: Context) : ArrayAdapter<PlacePurpose>(context, android.R.layout.simple_spinner_item, PlacePurpose.values()) {
    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
}

