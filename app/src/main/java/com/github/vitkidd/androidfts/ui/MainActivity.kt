package com.github.vitkidd.androidfts.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.facebook.stetho.Stetho
import com.github.vitkidd.androidfts.R
import com.github.vitkidd.androidfts.db.DbHelper
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DbHelper
    private val movieAdapter: MovieAdapter = MovieAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Stetho.initializeWithDefaults(this)

        dbHelper = DbHelper(applicationContext)
        dbHelper.populate(applicationContext)

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            adapter = movieAdapter
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        }

        RxTextView.afterTextChangeEvents(findViewById(R.id.editText))
                .debounce(500, TimeUnit.MILLISECONDS)
                .map { it.editable().toString() }
                .filter { it.isNotEmpty() && it.length > 2 }
                .map(dbHelper::thirdSearch)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(movieAdapter::updateMovies)
    }
}