package elfak.mosis.rmas18203.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import elfak.mosis.rmas18203.adapter.MyAdapter
import elfak.mosis.rmas18203.models.UserViewModel
import elfak.mosis.rmas18203.R

class LeaderboardFragment : Fragment() {

    private lateinit var viewModel : UserViewModel
    private lateinit var userRecyclerView: RecyclerView
    lateinit var adapter : MyAdapter

    override fun onCreate(savedInstanceState:   Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userRecyclerView = view.findViewById(R.id.recyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(context)
        userRecyclerView.setHasFixedSize(true)
        adapter = MyAdapter()
        userRecyclerView.adapter = adapter

        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        viewModel.allUsers.observe(viewLifecycleOwner, Observer {

            adapter.updateUserList(it)
        })

        //dodato
        val repository = viewModel.getRepository()
        repository.loadUsers(viewModel.allUsers)
    }
}