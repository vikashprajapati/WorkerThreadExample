package com.app.workerthreadexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();
    AppCompatButton sendJobButton;
    private CustomHandler mCustomHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCustomHandler = new CustomHandler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sendJobButton = findViewById(R.id.sendJobButton);
        sendJobButton.setOnClickListener(v -> mCustomHandler.post(() -> {
            for(int i = 0; i < 5; i++){
                Log.i(TAG, "run: " + i);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomHandler.stop();
    }

    private static class CustomHandler{
        private BlockingQueue<Runnable> mQueue = new LinkedBlockingQueue<>();
        public CustomHandler() {
            initWorkerThread();
        }

        private void initWorkerThread() {
            new Thread(() -> {
                while(true){
                    Runnable runnable;
                    try {
                        runnable = mQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }

                    if(runnable == STOP){
                        return;
                    }
                    runnable.run();
                }
            }).start();
        }

        public void post(Runnable runnable){
            mQueue.add(runnable);
        }

        public void stop(){
            // mQueue is cleared earlier so as STOP runnable can be executed as soon as possible.
            mQueue.clear();
            mQueue.add(STOP);
        }

        private final Runnable STOP = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run: I will stop worker thread, but I wont be executed");
            }
        };
    }
}