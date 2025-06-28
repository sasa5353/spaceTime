package com.instacart.library.sample;

import static java.lang.Math.PI;
import static java.lang.Math.pow;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.TrueTimeRx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

//import static com.instacart.library.sample.App.TAG;


public class MainActivity
        extends AppCompatActivity {

    @BindView(R.id.tt_btn_refresh) Button refreshBtn;
//    @BindView(R.id.tt_time_gmt) TextView timeGMT;
    @BindView(R.id.tt_time_pst) TextView timePST;
//    @BindView(R.id.tt_time_device) TextView timeDeviceTime;
    @BindView(R.id.tt_time_delta) TextView timeDelta;
    @BindView(R.id.tt_time_priv) TextView timePriveos;
    @BindView(R.id.tt_time_lmst) TextView timeLMST;

    final String LOG_TAG = "myLogs";
    final String DIR_SD = "Documents";
    final String FILENAME_SD = "MyOwnFile[LMST].txt";
    public Long LocalMeanSiderialTime;
    private static final String TAG = MainActivity.class.getSimpleName();

    public String ntp = "time.google.com";
    public double dolgota = 30.2948791667;

    public final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 5000);


//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        getSupportActionBar().setTitle("Хронометрист");
//
//        ButterKnife.bind(this);
//        refreshBtn.setEnabled(TrueTimeRx.isInitialized());
//    }



    private CheckBox mSingleShotCheckBox;
    //    private TextView mCounterTextView;

    private Timer mTimer;
    private Timer mGlobalTimer;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_ntpserver:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("NTP Server");
                View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.ntpserver, null, false);
                final EditText input = (EditText) viewInflated.findViewById(R.id.input_text);
                builder.setView(viewInflated);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (input.getText().length()!=0) {
                            ntp=input.getText().toString();
                        } else {
                            ntp = "time.google.com";
                        }


                        timeDelta.setText(ntp);
                        initRxTrueTime();
//                        refreshBtn.setEnabled(TrueTimeRx.isInitialized());
                    }
                });
                builder.setNegativeButton(R.string.clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TrueTime.clearCachedInfo();
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            case R.id.action_search:
                refreshBtn.setEnabled(TrueTimeRx.isInitialized());
                return true;


            case R.id.action_dolgota:
                AlertDialog.Builder ad= new AlertDialog.Builder(MainActivity.this);
                ad.setTitle("Долгота");
                View newViewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.dolgota, null, false);
                final EditText inputText = (EditText) newViewInflated.findViewById(R.id.input_text);
                ad.setView(newViewInflated);

                ad.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (inputText.getText().length()!=0) {
                            dolgota=Double.valueOf(inputText.getText().toString());
                        } else {
                            dolgota = 30.2948791667;
                        }
//                        timeDelta.setText(ntp);
                        initRxTrueTime();
//                        refreshBtn.setEnabled(TrueTimeRx.isInitialized());
                    }
                });
                ad.setNegativeButton(R.string.clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TrueTime.clearCachedInfo();
                        dialog.cancel();
                    }
                });
                ad.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                ad.show();
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        initRxTrueTime();

        getSupportActionBar().setTitle("Хронометрист");

        ButterKnife.bind(this);

        mSingleShotCheckBox = (CheckBox) findViewById(R.id.checkBoxSingleShot);
        Button mCancelButton = (Button) findViewById(R.id.buttonCancel);
        Button mRemoveFileButton = (Button) findViewById(R.id.buttonDeleteAll);
//        mCounterTextView = (TextView) findViewById(R.id.textViewCounter);
        /*
        <TextView
            android:id="@+id/textViewCounter"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textSize="24sp"
            android:textStyle="bold"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/buttonCancel"
            tools:layout_constraintBottom_creator="1"
            tools:layout_constraintLeft_creator="1"
            tools:layout_constraintRight_creator="1"
            tools:layout_constraintTop_creator="1"/>
         */


        mCancelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mGlobalTimer != null) {
                    mGlobalTimer.cancel();
                    mGlobalTimer = null;
                }

                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
            }
        });

        mRemoveFileButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final String path = Environment.getExternalStorageDirectory() + "/" + DIR_SD + "/" + FILENAME_SD;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("ACHTUNG!!!");
                builder.setMessage("Вы  уверены, что хотите удалить файл")
                        .setCancelable(true)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        delete(path);
                                    }
                                })
                        .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        refreshBtn.setEnabled(TrueTimeRx.isInitialized());
    }

    public static void delete(String path) {
        File file = new File(path); //Создаем файловую переменную
        if (file.exists()) { //Если файл или директория существует
            String deleteCmd = "rm -r " + path; //Создаем текстовую командную строку
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd); //Выполняем системные команды
            } catch (IOException e) {
            }
        }
    }

    class MyTimerTask extends TimerTask {

//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
//                "dd:MMMM:yyyy HH:mm:ss a", Locale.getDefault());
//        final String strDate = simpleDateFormat.format(calendar.getTime());


        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    updateTime();
//                    updateTime(false);
//                    mCounterTextView.setText(strDate);
//                    Toolkit.getDefaultToolkit().beep();
                    tg.startTone(ToneGenerator.TONE_PROP_BEEP);

                }
            });
        }
    }


    class MyGlobalTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    updateTime(false);

                    if (mTimer != null) {
                        mTimer.cancel();
                    }

                    // re-schedule timer here
                    // otherwise, IllegalStateException of
                    // "TimerTask is scheduled already"
                    // will be thrown
                    mTimer = new Timer();
                    MyTimerTask mMyTimerTask = new MyTimerTask();

//                    updateTime();
                    int delay = (int) ((500.1 -(LocalMeanSiderialTime%500))*365.25/366.25);

                    int period = (int)(500.1*365.25/366.25);
                    mTimer.schedule(mMyTimerTask, delay, period);
                }
            });
        }
    }




    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            //Обрабатываем нажатие кнопки гарнитуры:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                //Toast.makeText(this, "Нажата кнопка гарнитуры", Toast.LENGTH_SHORT).show();
                updateTime(true, false);
                return true;

            //Обрабатываем нажатие другой кнопки:
            default:
//                Toast.makeText(this, "Нажата кнопка номер: " + keyCode, Toast.LENGTH_SHORT).show();
                //updateTime(true);
                return super.onKeyDown(keyCode, event);

        }
//        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.tt_btn_refresh)
    public void onBtnRefresh() {
        if (mGlobalTimer != null) {
            mGlobalTimer.cancel();
        }

        // re-schedule timer here
        // otherwise, IllegalStateException of
        // "TimerTask is scheduled already"
        // will be thrown
        mGlobalTimer = new Timer();
        MyGlobalTimerTask mMyGlobalTimerTask = new MyGlobalTimerTask();

//        Double coeff = 366.2425/365.2425;
        updateTime(false);
//        int delay = 0;//(int) ((1000.1 -(LocalMeanSiderialTime%1000))*365.25/366.25);
        int delay = (int) ((1000.1 -(LocalMeanSiderialTime%1000))*365.25/366.25);

//        if (mSingleShotCheckBox.isChecked()) {
//            // singleshot delay 1000 ms
//            mTimer.schedule(mMyTimerTask, delay);
//        } else {
//            // delay 1000ms, repeat in 5000ms
//            int period = (int)(500.1*365.25/366.25);
//            mTimer.schedule(mMyTimerTask, delay, period);
//        }

        int period = (int)(5001.0*365.25/366.25);
        mGlobalTimer.schedule(mMyGlobalTimerTask, delay, period);
    }

    private void updateTime() {
        updateTime(true);
    }
    private void updateTime(Boolean visible) {
        updateTime(false, visible);
    }

    private void updateTime(Boolean writedown, Boolean visible) {
        if (!TrueTimeRx.isInitialized()) {
            Toast.makeText(this, "Sorry TrueTime not yet initialized.", Toast.LENGTH_SHORT).show();
            return;
        }
        refreshBtn.setEnabled(true);
        Date trueTime = TrueTimeRx.now();
        Date deviceTime = new Date();

        Log.d("kg",
                String.format(" [trueTime: %d] [devicetime: %d] [drift_sec: %f]",
                        trueTime.getTime(),
                        deviceTime.getTime(),
                        (trueTime.getTime() - deviceTime.getTime()) / 1000F));

//        timeDeviceTime.setText(getString(R.string.tt_time_device,
//                _formatDate(deviceTime, "HH:mm:ss.SS", TimeZone.getTimeZone("GMT+03:00"))));
//        timeGMT.setText(getString(R.string.tt_time_gmt,
//                _formatDate(trueTime, "HH:mm:ss.SS", TimeZone.getTimeZone("GMT"))));
        if (visible) {
            timePST.setText(getString(R.string.tt_time_pst,
                    _formatDate(trueTime, "HH:mm:ss.SS", TimeZone.getTimeZone("GMT+03:00"))));
        }
        long delt = trueTime.getTime() - deviceTime.getTime();

        Date datka = new Date();
        datka.setTime(delt);

        if (visible) {
            timeDelta.setText(getString(R.string.tt_time_delta,
                    _formatDate(datka, "HH:mm:ss.SSS", TimeZone.getTimeZone("GMT"))));
        }
//        long prev;
//        prev = trueTime.getTime();
//        prev-=prev%5000;
//        datka.setTime(prev);
//        timePriveos.setText(getString(R.string.tt_time_priv,
////                _formatDate(datka, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+03:00"))));
//                _formatDate(datka, "HH:mm:ss", TimeZone.getTimeZone("GMT+03:00"))));

        int pojas = 3;

        long UTC = trueTime.getTime();
        if (!mSingleShotCheckBox.isChecked()) {
            UTC = (new Date()).getTime();
        }
        long alfa = 0;//-116;  // -0.11568
        long UT1 = UTC + alfa;
        boolean flag = false;

        if (flag) {
            Date tempor = new Date();

            long tempory = tempor.getTime();
            tempory-=tempory%1000;
            tempor.setTime(tempory);

            tempor.setYear(119);
            tempor.setMonth(2);
            tempor.setDate(1);
            tempor.setHours(0);
            tempor.setMinutes(0);
            tempor.setSeconds(0);

            UT1 = tempor.getTime(); //+ alfa;
            pojas = 0;
        }

        long tempory2 = datka.getTime();
        tempory2-=tempory2%1000;
        datka.setTime(tempory2);

        datka.setYear(100);
        datka.setMonth(0);
        datka.setDate(1);
        datka.setHours(12+pojas);
        datka.setMinutes(0);
        datka.setSeconds(0);

        long Tu = UT1 - datka.getTime();
        double timeU = (double) Tu/(86400000.0);
        double ERA = 2 * PI *( (double) (Tu % 86400000)/(86400000.0) + 0.7790572732640 + 0.00273781191135448*timeU);

        double timeDecade = timeU/(36525.0);

        double Radsec = 1.0/206265.0;

        double GMST = ERA + 0.014506*Radsec + 4612.156534*Radsec*timeDecade + 1.3915817*Radsec*pow(timeDecade,2.0) -
                0.00000044*Radsec*pow(timeDecade,3.0) - 0.000029956*Radsec*pow(timeDecade,4.0) -
                0.0000000368*Radsec*pow(timeDecade,5.0);


//        =    ERA(UT1) + 0.014506′′+ 4612.156534′′t+ 1.3915817′′t2−0.00000044′′t3−0.000029956′′t4−0.0000000368′′t5
//        2π(UT1 Julian day fraction+0.7790572732640 + 0.00273781191135448Tu)
//        taken from IERS Technical Notes (2010)

        long GMSTime;
        GMSTime = (long) (GMST/PI*12*60*60*1000);
//        long LMSTime = GMSTime + ((2*60+1)*60+10)*1000+771;//(long) (29.82731*240000);//((2*60+1)*60+10)*1000+771;
        long LMSTime = GMSTime + (long) (dolgota*240000.0);

        Date newDatka = new Date();

        newDatka.setTime(LMSTime);
        String _s = getString(R.string.tt_time_lmst,
                _formatDate(newDatka, "HH:mm:ss", TimeZone.getTimeZone("GMT")));

        _s+="."+((LMSTime%1000)/100);

        if (visible && ((LMSTime%1000)/100)%5 != 0) {
            Toast.makeText(this, "Сейчас почему-то: " + ((LMSTime%1000)/100), Toast.LENGTH_SHORT).show();
        }
        if (visible) {
            timeLMST.setText(_s);
        }
        LocalMeanSiderialTime = LMSTime;

        long prev;
        prev = LMSTime;
        prev-=prev%5000;
        datka.setTime(prev);
        if (visible) {
            timePriveos.setText(getString(R.string.tt_time_priv,
//                _formatDate(datka, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+03:00"))));
                    _formatDate(datka, "HH:mm:ss", TimeZone.getTimeZone("GMT"))));
        }
        if (writedown) {

            String text = timeLMST.getText().toString() + "\n";

            isPermissionGrantedForStorage();

            // проверяем доступность SD
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
                return;
            }
            // получаем путь к SD
            File sdPath = Environment.getExternalStorageDirectory();

            //Environment.getExternalStorageDirectory() + "/" + DIR_SD + "/" + FILENAME_SD


            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
            // создаем каталог
            sdPath.mkdirs();
            // формируем объект File, который содержит путь к файлу
            File sdFile = new File(sdPath, FILENAME_SD);
            try {
                // открываем поток для записи
                BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile, true));
                // пишем данные
                bw.write(text);
                // закрываем поток
                bw.close();
                Log.d(LOG_TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public  boolean isPermissionGrantedForStorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG,"Permission is granted");
                return true;
            } else {

                Log.v(LOG_TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(LOG_TAG,"Permission is granted");
            return true;
        }
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    /**
     * Initialize the TrueTime using RxJava.
     */

    private void initRxTrueTime() {
        DisposableSingleObserver<Date> disposable = TrueTimeRx.build()
                .withConnectionTimeout(31_428)
                .withRetryCount(100)
                .withSharedPreferencesCache(this)
                .withLoggingEnabled(true)
                .initializeRx(ntp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Date>() {
                    @Override
                    public void onSuccess(Date date) {
                        Log.d(TAG, "Success initialized TrueTime :" + date.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", e);
                    }
                });
    }

}