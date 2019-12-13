package com.example.expandablerecyclerviewexample

import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    var itemsData = ArrayList<DataModel>()
    lateinit var adapter: RVAdapter
    lateinit var mcontext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mcontext = this.baseContext

        adapter = RVAdapter(itemsData)
        val llm = LinearLayoutManager(this)

        itemsrv.setHasFixedSize(true)
        itemsrv.layoutManager = llm
        getData()
        itemsrv.adapter = adapter
    }

    private fun getData() {
        itemsData = ArrayList()
        itemsData = Data.items
        adapter.notifyDataSetChanged()
        adapter = RVAdapter(itemsData)
    }
}
