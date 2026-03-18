package com.stone.persistent.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stone.persistent.R
import com.stone.persistent.adapter.MenuGridAdapter

class MenuGridFragment : Fragment(R.layout.fragment_menu_grid) {

    var page : Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuRecyclerView = view.findViewById<RecyclerView>(R.id.menu_recyclerview)
        menuRecyclerView.layoutManager = GridLayoutManager(activity, 5)
        menuRecyclerView.adapter = MenuGridAdapter(requireActivity(), page)
    }
}