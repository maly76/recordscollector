package com.example.leistungssammler.persistence

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteQueryBuilder

class AppContentProvider: ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val builder = SupportSQLiteQueryBuilder
            .builder("Record")
            .columns(projection)
            .orderBy(sortOrder)
        if(URI_MATCHER.match(uri) == RECORD_ID){
            builder.selection("id = ?", arrayOf(uri.lastPathSegment))
        }

        return AppDatabase
            .getDb(context!!)
            .openHelper
            .readableDatabase
            .query(builder.create())
    }

    override fun getType(uri: Uri): String? = when(URI_MATCHER.match(uri)) {
        RECORDS   -> "vnd.android.cursor.dir/vnd.example.leistungssammler.record"
        RECORD_ID -> "vnd.android.cursor.item/vnd.example.leistungssammler.record"
        else      -> null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        TODO("Not yet implemented")
    }

    companion object{
        private const val AUTHORITY = "com.example.leistungssammler"
        private const val RECORD_PATH = "records"
        private const val RECORDS = 1
        private const val RECORD_ID = 2

        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            // content://com.example.leistungssammler/records
            addURI(AUTHORITY, RECORD_PATH, RECORDS);
            // content://com.example.leistungssammler/records/# (# Nummernplatzhalter)
            addURI(AUTHORITY, "$RECORD_PATH/#", RECORD_ID)
        }
    }
}