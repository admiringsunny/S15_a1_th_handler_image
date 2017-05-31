package com.acadgild.assignments.session15.assignment1;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    DownloadManager mDManager;
    DownloadCompleteReceiver mReceiver;
    Handler mHandler;
    ImageView mImgShow;
    TextView mTvDetails;
    EditText mEtUrl;
    Button mBtnDownload;
    private ProgressBar mProgressBar;

    String mLocalImageUri;
    String mTitle;
    String mUri;
    String mMediaType;
    String mTotalSize;


    private final static int SET_PROGRESS_BAR_VISIBILITY = 0;
    private final static int PROGRESS_UPDATE = 1;
    private final static int SET_BITMAP = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnDownload = (Button) findViewById(R.id.btn_download);
        mProgressBar = (ProgressBar) findViewById(R.id.p_bar);
        mImgShow = (ImageView) findViewById(R.id.img_show);
        mTvDetails = (TextView) findViewById(R.id.tv_details);
        mEtUrl = (EditText) findViewById(R.id.et_url);
        mDManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

/** savedInstanceState will not be null, when the activity
 * is re created by configuration changes
 */
        if(savedInstanceState!=null){
            mLocalImageUri =  savedInstanceState.getString("local_image_uri");
            mTitle = savedInstanceState.getString("title");
            mUri = savedInstanceState.getString("uri");
            mMediaType = savedInstanceState.getString("media_type");
            mTotalSize = savedInstanceState.getString("total_size");

            Uri local_uri = Uri.parse(mLocalImageUri);
            mImgShow.setImageURI(local_uri);




        }

        /** Creating an instance of Handler class,
         * which draws the image and download image details
         * in the MainActivity
         */
        mHandler = new Handler(){
            @Override
            /** This callback method is invoked when sendMessage() is
             * invoked on this handler
             */
            public void handleMessage(Message msg) {

                mLocalImageUri = msg.getData().getString("local_image_uri");
                mTitle = msg.getData().getString("title");
                mUri = msg.getData().getString("uri");
                mMediaType = msg.getData().getString("media_type");
                mTotalSize = msg.getData().getString("total_size");

                Uri local_uri = Uri.parse(mLocalImageUri);
                mImgShow.setImageURI(local_uri);

                mTvDetails.setText(
                        "Title : " + mTitle + "\n" +
                        "File Size : " + mTotalSize + " Bytes "

                );
                super.handleMessage(msg);
            }
        };

        /** Defining a button click listener for the Download button */
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new LoadIconTask( handler))
                        .start();

                Uri uri = Uri.parse(mEtUrl.getText().toString());
                DownloadManager.Request req = new DownloadManager.Request(uri);
                mDManager.enqueue(req);
            }
        };

        mReceiver = new DownloadCompleteReceiver();
        IntentFilter filter = new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE");
        registerReceiver(mReceiver, filter);

        /** Setting click event listener for the button */
        mBtnDownload.setOnClickListener(onClickListener);

    }

    /** This callback method is called when the configuration
     * change occurs
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("local_image_uri", mLocalImageUri);
        outState.putString("title", mTitle);
        outState.putString("uri", mUri);
        outState.putString("media_type", mMediaType);
        outState.putString("total_size", mTotalSize);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /** Defining a broadcast receiver */
    private class DownloadCompleteReceiver extends BroadcastReceiver{

        /** Will be executed when the download is completed */
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)){
                Bundle data = intent.getExtras();
                long download_id = data.getLong(DownloadManager.EXTRA_DOWNLOAD_ID );

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(download_id);

                Cursor c = mDManager.query(query);

                if(c.moveToFirst()){

                    Bundle d = new Bundle();
                    d.putString("title", c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE )));
                    d.putString("uri", c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI )));
                    d.putString("media_type", c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE )));
                    d.putString("total_size", c.getString(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES )));
                    d.putString("local_image_uri", c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI )));

                    Message msg = new Message();
                    msg.setData(d);

                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    static class UIHandler extends Handler {
        WeakReference<MainActivity> mParent;

        public UIHandler(WeakReference<MainActivity> parent) {
            mParent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity parent = mParent.get();
            if (null != parent) {
                switch (msg.what) {
                    case SET_PROGRESS_BAR_VISIBILITY: {
                        parent.getProgressBar().setVisibility((Integer) msg.obj);
                        break;
                    }
                    case PROGRESS_UPDATE: {
                        parent.getProgressBar().setProgress((Integer) msg.obj);
                        break;
                    }
                    case SET_BITMAP: {
                        parent.getImageView().setImageBitmap((Bitmap) msg.obj);
                        break;
                    }
                }
            }
        }

    }

    Handler handler = new UIHandler(new WeakReference<>(
            this));

    public ImageView getImageView() {
        return mImgShow;
    }

    public ProgressBar getProgressBar() {
        return mProgressBar;
    }

    private class LoadIconTask implements Runnable {
        private final Handler handler;

        LoadIconTask(Handler handler) {
            this.handler = handler;
        }

        public void run() {

            Message msg = handler.obtainMessage(SET_PROGRESS_BAR_VISIBILITY,
                    ProgressBar.VISIBLE);
            handler.sendMessage(msg);

            final Bitmap tmp = BitmapFactory.decodeResource(getResources(), 100);

            for (int i = 1; i < 11; i++) {
                sleep();
                msg = handler.obtainMessage(PROGRESS_UPDATE, i * 10);
                handler.sendMessage(msg);
            }

            msg = handler.obtainMessage(SET_BITMAP, tmp);
            handler.sendMessage(msg);

            msg = handler.obtainMessage(SET_PROGRESS_BAR_VISIBILITY,
                    ProgressBar.INVISIBLE);
            handler.sendMessage(msg);
        }

        private void sleep() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
