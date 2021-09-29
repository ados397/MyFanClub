package com.ados.myfanclub.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelperSchedule(context: Context)
    : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "MyFanClub"
        private const val DB_VERSION = 1

        private const val TABLE_NAME = "cheering_board_like"
        private const val POST_ID = "PostID"
        private const val IS_LIKE = "IsLike"
        private const val IS_DISLIKE = "IsDislike"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE $TABLE_NAME" +
                    "($POST_ID TEXT PRIMARY KEY," +
                    "$IS_LIKE INTEGER DEFAULT 0," +
                    "$IS_DISLIKE INTEGER DEFAULT 0)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun isRow(docname: String) : Boolean {
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $POST_ID = '$docname'"
        val cursor = db.rawQuery(selectALLQuery, null)
        var isRow = false
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isRow = true
            }
        }
        cursor.close()
        db.close()

        return isRow
    }

    fun updateLike(docname: String, like: Int): Boolean {
        if (isRow(docname) == true) { // 값이 있으면 update
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(POST_ID, docname)
            values.put(IS_LIKE, like)
            val _success = db.update(TABLE_NAME, values, "$POST_ID=?", arrayOf(docname))
            db.close()
            return (Integer.parseInt("$_success") != -1)
        } else { // 없으면 insert
            return insertData(docname, like, 0)
        }
    }

    fun updateDislike(docname: String, dislike: Int): Boolean {
        if (isRow(docname) == true) { // 값이 있으면 update
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(POST_ID, docname)
            values.put(IS_DISLIKE, dislike)
            val _success = db.update(TABLE_NAME, values, "$POST_ID=?", arrayOf(docname))
            db.close()
            return (Integer.parseInt("$_success") != -1)
        } else { // 없으면 insert
            return insertData(docname, 0, dislike)
        }
    }

    fun insertData(docname: String, like: Int, dislike: Int) : Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(POST_ID, docname)
        values.put(IS_LIKE, like)
        values.put(IS_DISLIKE, dislike)

        var _success = db.insert(TABLE_NAME, null, values)
        db.close()

        return (Integer.parseInt("$_success") != -1)
    }


    /*fun updateLike(item: BoardDTO, like: Int) {
        var query = "INSERT OR REPLACE INTO $TABLE_NAME ($POST_ID, $IS_LIKE) VALUES ('${item.docname}', $like)"
        val db = this.writableDatabase
        db?.execSQL(query)
        db.close()
    }

    fun updateDislike(item: BoardDTO, dislike: Int) {
        var query = "INSERT OR REPLACE INTO $TABLE_NAME ($POST_ID, $IS_DISLIKE) VALUES ('${item.docname}', $dislike)"
        val db = this.writableDatabase
        db?.execSQL(query)
        db.close()
    }*/

    fun getlike(docname: String) : Boolean {
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $POST_ID = '$docname'"
        val cursor = db.rawQuery(selectALLQuery, null)
        var isLike = 0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isLike = cursor.getInt(cursor.getColumnIndex(IS_LIKE))
            }
        }
        cursor.close()
        db.close()

        if (isLike == 1) return true
        else return false
    }

    fun getdislike(docname: String) : Boolean {
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $POST_ID = '$docname'"
        val cursor = db.rawQuery(selectALLQuery, null)
        var isDislike = 0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isDislike = cursor.getInt(cursor.getColumnIndex(IS_DISLIKE))

                println("싫어요 결과 : $isDislike")
            }
        }
        cursor.close()
        db.close()

        if (isDislike == 1) return true
        else return false
    }
}
