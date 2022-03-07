package com.ados.myfanclub.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelperReport(context: Context)
    : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "MyFanClub"
        private const val DB_VERSION = 1

        private const val TABLE_NAME = "t_report"
        private const val COL_DOC_NAME = "DocName"
        private const val COL_IS_BLOCK = "IsBlock"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE $TABLE_NAME" +
                    "($COL_DOC_NAME TEXT PRIMARY KEY," +
                    "$COL_IS_BLOCK INTEGER DEFAULT 0)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    private fun isUpdate(docName: String) : Boolean {
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $COL_DOC_NAME = '$docName'"
        val cursor = db.rawQuery(selectALLQuery, null)
        var isUpdate = false
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isUpdate = true
            }
        }
        cursor.close()
        db.close()

        return isUpdate
    }

    fun updateBlock(docName: String, isBlock: Int): Boolean {
        return if (isUpdate(docName)) { // 값이 있으면 update
            val db = this.writableDatabase
            val values = ContentValues()
            values.put(COL_DOC_NAME, docName)
            values.put(COL_IS_BLOCK, isBlock)
            val success = db.update(TABLE_NAME, values, "$COL_DOC_NAME=?", arrayOf(docName))
            db.close()
            (Integer.parseInt("$success") != -1)
        } else { // 없으면 insert
            insertData(docName, isBlock)
        }
    }

    private fun insertData(docName: String, isBlock: Int) : Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_DOC_NAME, docName)
        values.put(COL_IS_BLOCK, isBlock)

        var success = db.insert(TABLE_NAME, null, values)
        db.close()

        return (Integer.parseInt("$success") != -1)
    }

    fun getBlock(docName: String) : Boolean {
        val db = readableDatabase
        val selectALLQuery = "SELECT * FROM $TABLE_NAME WHERE $COL_DOC_NAME = '$docName'"
        val cursor = db.rawQuery(selectALLQuery, null)
        var isBlock = 0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val value = cursor.getColumnIndex(COL_IS_BLOCK)
                isBlock = cursor.getInt(value)
            }
        }
        cursor.close()
        db.close()

        return isBlock == 1
    }
}
