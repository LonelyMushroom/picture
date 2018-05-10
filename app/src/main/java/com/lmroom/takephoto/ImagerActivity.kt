package com.lmroom.takephoto

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.lmroom.takephotolib.model.TImage
import org.jetbrains.anko.find

class ImagerActivity : AppCompatActivity() {

    lateinit var recycle: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imager_recycle)

        val images = intent.getSerializableExtra("images") as ArrayList<TImage>
        recycle = find(R.id.recycle)

        recycle.layoutManager = LinearLayoutManager(this)
        recycle.adapter = MyAdapter(images,Glide.with(this))

    }


    class MyAdapter(val datas: ArrayList<TImage>, val glideWith: RequestManager) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_imager, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = datas.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            glideWith.load(datas[position].compressPath).into(holder.imageView)
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var imageView: ImageView = view.find(R.id.img)
        }
    }
}