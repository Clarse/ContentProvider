package com.example.contentprovider

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkCalendarPermission();
        }
//        queryCalendars()
    }

    //向日历里面添加事件
    fun addTips(view: View) {
        //_id====1
        val calId = 1
        //开始时间
        val beginTime = Calendar.getInstance()
        //年月(从0开始)日时分
        beginTime.set(2021, 0, 1, 0, 0)
        val beginTimeMills = beginTime.timeInMillis
        //结束时间
        val endTime = Calendar.getInstance()
        endTime.set(2021, 0, 2, 0, 0)
        val endTimeMills = endTime.timeInMillis
        //时区
        val timeZone = TimeZone.getDefault().id

        val values = ContentValues()
        values.put(CalendarContract.Events.CALENDAR_ID, calId)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone)
        values.put(CalendarContract.Events.DTSTART, beginTimeMills)
        values.put(CalendarContract.Events.DTEND, endTimeMills)
        values.put(CalendarContract.Events.TITLE, "2021新年祝福")
        values.put(CalendarContract.Events.DESCRIPTION, "新的一年要更勇敢地去实现自己的目标")
        values.put(CalendarContract.Events.EVENT_LOCATION, "绍兴")

        val eventUri = CalendarContract.Events.CONTENT_URI
        val contentResolver = contentResolver
        val resultUri = contentResolver.insert(eventUri, values)
        Log.d(TAG, "resultUri------" + resultUri)

        val eventId = resultUri?.lastPathSegment
        //设置提醒
        val reminderUri = CalendarContract.Reminders.CONTENT_URI
        val reminderValues = ContentValues()
        reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId)
        reminderValues.put(CalendarContract.Reminders.MINUTES, 15)
        reminderValues.put(
            CalendarContract.Reminders.METHOD,
            CalendarContract.Reminders.METHOD_ALERT
        )

        contentResolver.insert(reminderUri, reminderValues)

    }

    //获取通讯录数据
    fun getContacts(view: View) {
        val contentResolver = contentResolver
        val uri = ContactsContract.RawContacts.CONTENT_URI
        val rawContacts = contentResolver.query(
            uri, arrayOf("contact_id", "display_name"), null,
            null, null
        )
        val userInfos = arrayListOf<UserInfo>()
        while (rawContacts?.moveToNext() == true) {
            val userInfo = UserInfo(
                rawContacts.getString(rawContacts.getColumnIndex("contact_id")),
                rawContacts.getString(rawContacts.getColumnIndex("display_name")), ""
            )
            userInfos.add(userInfo)
        }
        rawContacts?.close()
        //获取手机号码
        val phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        userInfos.forEach {
            val phoneCursor = contentResolver.query(
                phoneUri, arrayOf("data1"), "raw_contact_id=?", arrayOf(it.id), null
            )
            if (phoneCursor?.moveToNext() == true) {
                it.phoneNum = phoneCursor.getString(0)
            }
            phoneCursor?.close()
            Log.d(TAG, "userInfo---->" + it)
        }
    }

    private fun queryCalendars() {
        val contentResolver = contentResolver
//        val uri = Uri.parse("content://" + "com.android.calendar" + "/calendars")
        val uri = CalendarContract.Calendars.CONTENT_URI
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
        val readContactsPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS)
        val readCalendarPermission = checkSelfPermission(Manifest.permission.READ_CALENDAR)
        val writeCalendarPermission = checkSelfPermission(Manifest.permission.WRITE_CALENDAR)
        if (readCalendarPermission == PackageManager.PERMISSION_GRANTED
            && writeCalendarPermission == PackageManager.PERMISSION_GRANTED
            && readContactsPermission == PackageManager.PERMISSION_GRANTED
        ) {
            //表明权限已申请
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.READ_CONTACTS
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
            if (grantResults.size == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {
                //有权限
            } else {
                finish()
            }
        }
    }

}