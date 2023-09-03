package elfak.mosis.rmas18203.fragments

import PlacesAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import elfak.mosis.rmas18203.R
import elfak.mosis.rmas18203.models.PlaceViewModel


class PlacesFragment : Fragment() {

    private  val placeViewModel: PlaceViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private val adapter = PlacesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_places, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewplaces)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        //
        placeViewModel.placesLiveData.observe(viewLifecycleOwner, {places->
            adapter.submitList(places)
        })

        placeViewModel.fetchPlaces()
    }

}