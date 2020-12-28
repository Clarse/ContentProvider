package com.example.contentprovider

import android.content.UriMatcher
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.provider.Telephony
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_verify_code.*
import java.util.regex.Pattern

class VerifyCodeActivity : AppCompatActivity() {

    lateinit var phoneNum: String
    lateinit var verifyCode: String
    private val TAG = "VerifyCodeActivity"

    companion object {
        val MATCH_CODE = 1
        var uriMath = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI("sms", "#", MATCH_CODE)
        }
    }

    //倒计时
    private val countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            count_down_btn.isEnabled = false
            count_down_btn.text = String.format("重新获取(%d)", millisUntilFinished / 1000)
        }

        override fun onFinish() {
            count_down_btn.isEnabled = true
            count_down_btn.text = "获取验证码"
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)
    }

    private fun getSms() {
        val uri = Telephony.Sms.CONTENT_URI
        val handler = Handler()
        contentResolver.registerContentObserver(uri, true, object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                if (uriMath.match(uri) == MATCH_CODE) {
                    Log.d(TAG, "selfChange" + "--------" + selfChange)
                    Log.d(TAG, "uri" + "-------" + uri)
                    val query = contentResolver.query(uri!!, arrayOf("body"), null, null, null)
                    while (query?.moveToNext() == true) {
                        val body = query.getString(0)
                        Log.d(TAG, "body" + "------" + body)
                        handlerBody(body)
                    }
                    query?.close()
                }
            }
        }) //true包含所有uri后缀
    }

    private fun handlerBody(body: String) {
        if (!TextUtils.isEmpty(body) && body.startsWith("")) {//确定短信的开始模板规则
            val pattern = Pattern.compile("(?<![0-9])([0-9]{4})(?![0-9])")
            val matcher = pattern.matcher(body)
            val contain = matcher.find()
            if (contain) {
                val group = matcher.group()
                Log.d(TAG, "verifyCode" + "------" + group)
                verify_code_et.setText(group)
                verify_code_et.isFocusable = true
            }
        }
    }

    fun getCode(view: View) {
        phoneNum = phone_num_et.text.toString().trim()
        if (phoneNum.isEmpty()) {
            Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show()
            return
        } else {
            //开始倒计时，并向服务器请求验证码
            countDownTimer.start()
        }
    }

    fun submit(view: View) {
        phoneNum = phone_num_et.text.toString().trim()
        verifyCode = verify_code_et.text.toString().trim()
        if (phoneNum.isEmpty() || verifyCode.isEmpty()) {
            Toast.makeText(this, "手机号码和验证码都不能为空", Toast.LENGTH_SHORT).show()
            return
        } else {
            getSms()
        }
    }

}