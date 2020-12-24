package com.example.contentprovider

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkCalendarPermission();
        }
        queryCalendars()
    }

    private fun queryCalendars() {
        val contentResolver = contentResolver
        val uri = Uri.parse("content://" + "com.android.calendar" + "/calendars")
        val query = contentResolver.query(uri, null, null, null, null)
        val columnNames = query?.columnNames
        if (query?.moveToNext() == true) {
            Log.d(TAG, "====================")
            columnNames?.forEach {
                Log.d(TAG, it + "====" + query.getString(query.getColumnIndex(it)))
            }
            Log.d(TAG, "=====================")
        }
        query?.close()
    }

    private fun checkCalendarPermission() {
        val readCalendarPermission = checkSelfPermission(Manifest.permission.READ_CALENDAR)
        val writeCalendarPermission = checkSelfPermission(Manifest.permission.WRITE_CALENDAR)
        if (readCalendarPermission == PackageManager.PERMISSION_GRANTED
            && writeCalendarPermission == PackageManager.PERMISSION_GRANTED
        ) {
            //表明权限已申请
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR
                ), PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                //有权限
            } else {
                finish()
            }
        }
    }

}