package com.melvinhou.dimension2.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<String> mBannerUrl;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void postText(String text){
        mText.setValue(text);
    }

    public LiveData<String> getBannerUrl() {
        return mBannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        mBannerUrl.setValue(bannerUrl);
    }


    public void toAR(){
    }
}