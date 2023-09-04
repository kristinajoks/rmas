package elfak.mosis.rmas18203.data
import android.content.Context
import android.view.View
import android.widget.TextView
import elfak.mosis.rmas18203.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindow(
    marker: Marker,
    context: Context,
    private val dateCreated: String,
    private val purpose: String,
    private val rating: Number,
    private val mapView: MapView
) : InfoWindow(R.layout.place_item_window, mapView) {

    override fun onOpen(item: Any) {
        val layout = mView.findViewById<View>(R.id.placeItemLayout1)
        val titleView = mView.findViewById<TextView>(R.id.nazivMestaTV1)
        val dateCreatedView = mView.findViewById<TextView>(R.id.datumMestaTV1)
        val purposeView = mView.findViewById<TextView>(R.id.aktMestaTV1)
        val ratingView = mView.findViewById<TextView>(R.id.ocenaMestaTV1)
        val descriptionView = mView.findViewById<TextView>(R.id.opisMestaTV1)

        if (item is Marker) {
            val title = item.title
            val snippet = item.snippet

            // Populate the views with marker data
            titleView.text = title
            dateCreatedView.text = dateCreated
            purposeView.text = purpose
            ratingView.text = rating.toString()
            descriptionView.text = snippet


            layout.visibility = View.VISIBLE
        }
    }

    override fun onClose() {
        // Handle any cleanup when the info window is closed
    }
}
