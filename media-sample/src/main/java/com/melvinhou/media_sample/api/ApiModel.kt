package com.melvinhou.media_sample.api

import com.melvinhou.kami.net.proxy.RetrofitFactory

class ApiModel {

    companion object {
        val instance = ApiModel()
    }

    private var mService: ApiService? = null


    fun Api(): ApiService {
        if (mService == null) {
            mService = RetrofitFactory.getInstence().create(
                ApiService::class.java,
                "https://api.bilibili.com",
                false
            )
        }
        return mService!!
    }

}