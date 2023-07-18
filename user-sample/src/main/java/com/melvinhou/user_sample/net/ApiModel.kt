package com.melvinhou.user_sample.net

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
                HttpConstants.SERVER_URL,
                false
            )
        }
        return mService!!
    }

}