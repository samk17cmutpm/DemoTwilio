package com.neo_lab.demotwilio.ui.base;

import android.util.Log;

import com.neo_lab.demotwilio.domain.error.APIError;
import com.neo_lab.demotwilio.domain.utils.DomainUtils;

import retrofit2.Response;
import rx.Subscriber;

/**
 * Created by sam_nguyen on 12/04/2017.
 */

public abstract class BaseSubscriber<T> extends Subscriber<Response<T>> {

    private String TAG = this.getClass().getName();

    protected BaseSubscriber() {
        super();
    }

    @Override
    public void onCompleted() {

        Log.d(TAG, "Finished");

    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        handleViewOnConnectSeverError();
    }

    @Override
    public void onNext(Response<T> tResponse) {
        if (!tResponse.isSuccessful()) {
            APIError apiError = DomainUtils.parseError(tResponse);
            handleViewOnRequestError(apiError);
        } else {
            handleViewOnRequestSuccess(tResponse.body());
        }

    }

    public abstract void handleViewOnRequestSuccess(T data);

    public abstract void handleViewOnRequestError(APIError apiError);

    public abstract void handleViewOnConnectSeverError();


}
