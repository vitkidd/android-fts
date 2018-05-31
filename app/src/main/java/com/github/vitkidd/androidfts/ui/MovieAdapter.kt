package com.github.vitkidd.androidfts.ui

import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.vitkidd.androidfts.R
import com.github.vitkidd.androidfts.db.Movie
import kotlinx.android.synthetic.main.item.view.*

class MovieAdapter(private val movies: MutableList<Movie>) : RecyclerView.Adapter<MovieHolder>() {

    override fun getItemCount() = movies.size

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        holder.itemView?.title_tv?.text = Html.fromHtml(movies[position].title)
        holder.itemView?.desc_tv?.text = Html.fromHtml(movies[position].overview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        return MovieHolder(LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false))
    }

    fun updateMovies(newMovies: ArrayList<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }
}

class MovieHolder (view: View) : RecyclerView.ViewHolder(view)