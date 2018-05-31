package com.github.vitkidd.androidfts.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.*

fun Cursor.getInt(columnName: String): Int = this.getInt(this.getColumnIndexOrThrow(columnName))
fun Cursor.getString(columnName: String): String = this.getString(this.getColumnIndexOrThrow(columnName))

data class Movie(val id: Int, val title: String, val overview: String)

class DbHelper(context: Context) : SQLiteOpenHelper(context, "movies.db", null, 1) {

    companion object {
        const val TABLE = "movies"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_OVERVIEW = "overview"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE VIRTUAL TABLE $TABLE USING fts3($COLUMN_ID, $COLUMN_TITLE, $COLUMN_OVERVIEW);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun populate(context: Context) {
        var movies: List<Movie>? = null

        context.assets.open("movies.json").use {
            val typeToken = object : TypeToken<List<Movie>>() {}.type
            movies = Gson().fromJson<List<Movie>>(InputStreamReader(it), typeToken)
        }

        movies?.let {
            try {
                writableDatabase.beginTransaction()

                it.forEach { movie ->
                    val values = ContentValues().apply {
                        put(COLUMN_ID, movie.id)
                        put(COLUMN_TITLE, movie.title)
                        put(COLUMN_OVERVIEW, movie.overview)
                    }

                    writableDatabase.insert(TABLE, null, values)
                }

                writableDatabase.setTransactionSuccessful()
            } finally {
                writableDatabase.endTransaction()
            }
        }
    }

    fun search(searchString: String): ArrayList<Movie> {
        val words = searchString
                .replace("\"(\\[\"]|.*)?\"".toRegex(), " ")
                .split("[^\\p{Alpha}]+".toRegex())
                .filter { it.isNotBlank() }
                .map { Porter.stem(it) }
                .filter { it.length > 2 }
                .joinToString(separator = " OR ", transform = { "$it*" })

        val query = "SELECT $COLUMN_ID, snippet($TABLE, '<b>', '</b>', '...', 1, 15) $COLUMN_TITLE, " +
                "snippet($TABLE, '<b>', '</b>', '...', 2, 15) $COLUMN_OVERVIEW FROM $TABLE WHERE $TABLE MATCH '$words'"
        val cursor = readableDatabase.rawQuery(query, null)
        val result = ArrayList<Movie>()

        try {
            if (cursor == null || !cursor.moveToFirst()) return result

            while (!cursor.isAfterLast) {
                result.add(Movie(
                        cursor.getInt(COLUMN_ID),
                        cursor.getString(COLUMN_TITLE),
                        cursor.getString(COLUMN_OVERVIEW)
                ))

                cursor.moveToNext()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return result
    }

    fun search_two(searchString: String): ArrayList<Movie> {
        val query = "SELECT $COLUMN_ID, snippet($TABLE, '<b>', '</b>', '...', 1, 15) $COLUMN_TITLE, " +
                "snippet($TABLE, '<b>', '</b>', '...', 2, 15) $COLUMN_OVERVIEW FROM $TABLE WHERE $TABLE MATCH '$searchString'"
        val cursor = readableDatabase.rawQuery(query, null)
        val result = ArrayList<Movie>()

        try {
            if (cursor == null || !cursor.moveToFirst()) return result

            while (!cursor.isAfterLast) {
                result.add(Movie(
                        cursor.getInt(COLUMN_ID),
                        cursor.getString(COLUMN_TITLE),
                        cursor.getString(COLUMN_OVERVIEW)
                ))

                cursor.moveToNext()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return result
    }

    fun search_one(searchString: String): ArrayList<Movie> {
        val query = "SELECT * FROM $TABLE WHERE $TABLE MATCH '$searchString'"
        val cursor = readableDatabase.rawQuery(query, null)
        val result = ArrayList<Movie>()

        try {
            if (cursor == null || !cursor.moveToFirst()) return result

            while (!cursor.isAfterLast) {
                result.add(Movie(
                        cursor.getInt(COLUMN_ID),
                        cursor.getString(COLUMN_TITLE),
                        cursor.getString(COLUMN_OVERVIEW)
                ))

                cursor.moveToNext()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return result
    }
}