package com.instacart.library.sample;

import android.app.Application;

//import android.os.AsyncTask;
//import com.instacart.library.truetime.TrueTime;
//import java.io.IOException;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();
//    public static String ntp = "time.google.com";

    @Override
    public void onCreate() {
        super.onCreate();
//        initRxTrueTime();

//        initTrueTime();
    }

//    /**
//     * init the TrueTime using a AsyncTask.
//     */
//    private void initTrueTime() {
//        new InitTrueTimeAsyncTask().execute();
//    }
//
//    // a little part of me died, having to use this
//    private class InitTrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {
//
//        protected Void doInBackground(Void... params) {
//            try {
//                TrueTime.build()
//                        //.withSharedPreferences(SampleActivity.this)
//                        .withNtpHost("time.google.com")
//                        .withLoggingEnabled(false)
//                        .withSharedPreferencesCache(App.this)
//                        .withConnectionTimeout(3_1428)
//                        .initialize();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.e(TAG, "something went wrong when trying to initialize TrueTime", e);
//            }
//            return null;
//        }
//    }

    /**
     * Initialize the TrueTime using RxJava.
     */
//    private void initRxTrueTime() {
//        DisposableSingleObserver<Date> disposable = TrueTimeRx.build()
//                .withConnectionTimeout(31_428)
//                .withRetryCount(100)
//                .withSharedPreferencesCache(this)
//                .withLoggingEnabled(true)
//                .initializeRx(ntp)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeWith(new DisposableSingleObserver<Date>() {
//                    @Override
//                    public void onSuccess(Date date) {
//                        Log.d(TAG, "Success initialized TrueTime :" + date.toString());
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", e);
//                    }
//                });
//    }


}
