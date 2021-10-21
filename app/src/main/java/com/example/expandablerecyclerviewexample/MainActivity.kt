package com.example.expandablerecyclerviewexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var itemsData = ArrayList<DataModel>()
    private var expandedSize = ArrayList<Int>()

    private lateinit var adapter: RVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = RVAdapter(itemsData, expandedSize)
        val llm = LinearLayoutManager(this)

        itemsrv.setHasFixedSize(true)
        itemsrv.layoutManager = llm
        getData()
        itemsrv.adapter = adapter
    }

    private fun getData() {
        itemsData = ArrayList()
        itemsData = Data.items

        setCellSize()

        adapter.notifyDataSetChanged()
        adapter = RVAdapter(itemsData, expandedSize)
    }

    // Set the expanded view size to 0, because all expanded views are collapsed at the beginning
    private fun setCellSize() {
        expandedSize = ArrayList()
        for (i in 0 until itemsData.count()) {
            expandedSize.add(0)
        }
    }
}
