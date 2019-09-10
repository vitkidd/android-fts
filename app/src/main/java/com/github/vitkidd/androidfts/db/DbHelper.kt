package com.github.vitkidd.androidfts.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

fun Cursor.getInt(columnName: String): Int = this.getInt(this.getColumnIndexOrThrow(columnName))
fun Cursor.getString(columnName: String): String = this.getString(this.getColumnIndexOrThrow(columnName))

data class Movie(val id: Int, val title: String, val overview: String)

class DbHelper(context: Context) : SQLiteOpenHelper(context, "movies.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE VIRTUAL TABLE movies USING fts4(id, title, overview);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun populate(context: Context) {
        val movies: MutableList<Movie> = mutableListOf()

        context.assets.open("movies.json").use {
            val typeToken = object : TypeToken<List<Movie>>() {}.type
            movies.addAll(Gson().fromJson(InputStreamReader(it), typeToken))
        }

        try {
            writableDatabase.beginTransaction()

            movies.forEach { movie ->
                val values = ContentValues().apply {
                    put("id", movie.id)
                    put("title", movie.title)
                    put("overview", movie.overview)
                }

                writableDatabase.insert("movies", null, values)
            }

            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun thirdSearch(searchString: String): List<Movie> {
        val words = searchString
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alpha}]+".toRegex())
                .filter { it.isNotBlank() }
                .map(Porter::stem)
                .filter { it.length > 2 }
                .joinToString(separator = " OR ", transform = { "$it*" })

        val query = "SELECT id, snippet(movies, '<b>', '</b>', '...', 1, 15) title, " +
                "snippet(movies, '<b>', '</b>', '...', 2, 15) overview FROM movies WHERE movies MATCH '$words'"
        val cursor = readableDatabase.rawQuery(query, null)
        val result = mutableListOf<Movie>()

        cursor?.use {
            if (!cursor.moveToFirst()) return result

            while (!cursor.isAfterLast) {
                val id = cursor.getInt("id")
                val title = cursor.getString("title")
                val overview = cursor.getString("overview")

                result.add(Movie(id, title, overview))

                cursor.moveToNext()
            }
        }

        return result
    }

    fun secondSearch(searchString: String): List<Movie> {
        val query = "SELECT id, snippet(movies, '<b>', '</b>', '...', 1, 15) title, " +
                "snippet(movies, '<b>', '</b>', '...', 2, 15) overview FROM movies WHERE movies MATCH '$searchString'"
        val cursor = readableDatabase.rawQuery(query, null)
        val result = mutableListOf<Movie>()

        cursor?.use {
            if (!cursor.moveToFirst()) return result

            while (!cursor.isAfterLast) {
                val id = cursor.getInt("id")
                val title = cursor.getString("title")
                val overview = cursor.getString("overview")

                result.add(Movie(id, title, overview))

                cursor.moveToNext()
            }
        }

        return result
    }

    fun firstSearch(searchString: String): List<Movie> {
        val query = "SELECT * FROM movies WHERE movies MATCH '$searchString'"
        val cursor = readableDatabase.rawQuery(query, null)
        val result = mutableListOf<Movie>()

        cursor?.use {
            if (!cursor.moveToFirst()) return result

            while (!cursor.isAfterLast) {
                val id = cursor.getInt("id")
                val title = cursor.getString("title")
                val overview = cursor.getString("overview")

                result.add(Movie(id, title, overview))

                cursor.moveToNext()
            }
        }

        return result
    }
}