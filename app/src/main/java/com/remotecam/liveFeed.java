package com.remotecam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Environment;
import android.widget.VideoView;


public class liveFeed extends ActionBarActivity {

    String dstAddress;
    Button buttonConnect;
    int dstPort;
    File file;
    int counter;
    int playcounter;
    static Socket  socket;
    VideoView mVideoView;
    MediaMetadataRetriever mediaMetadataRetriever;
    MediaController myMediaController;
    Bitmap bmFrame;
    Bitmap bitmap;
    ImageView capturedImageView;
    private File mydir;
    boolean connect;

    static final int SocketServerPORT = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_feed);
        Intent intent = new Intent();
        //dstAddress = intent.getStringExtra("ip");//
        dstAddress = "192.168.43.1";
        //buttonConnect = (Button) findViewById(R.id.connect);
        dstPort = SocketServerPORT;
        mVideoView = (VideoView)findViewById(R.id.videoView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        File mydir = new File("/sdcard/RemoteCam/");
        if(!mydir.exists())
            mydir.mkdirs();

        FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
        frame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (counter == 0)
                    startReceiving();
                else {
                    int currentPosition = mVideoView.getCurrentPosition(); //in millisecond

                    bmFrame = mediaMetadataRetriever
                            .getFrameAtTime(currentPosition * 1000); //unit in microsecond

                    if (bmFrame == null) {
                        Toast.makeText(liveFeed.this,
                                "bmFrame == null!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialog.Builder myCaptureDialog =
                                new AlertDialog.Builder(liveFeed.this);
                         capturedImageView = new ImageView(liveFeed.this);
                        capturedImageView.setImageBitmap(bmFrame);
                        ViewGroup.LayoutParams capturedImageViewLayoutParams =
                                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT);
                        capturedImageView.setLayoutParams(capturedImageViewLayoutParams);

                        myCaptureDialog.setView(capturedImageView);
                        myCaptureDialog.show();
                        BitmapDrawable drawable = (BitmapDrawable) capturedImageView.getDrawable();
                        bitmap = drawable.getBitmap();
                    }
                    saveFile();
                }
            }
        });


    }

    public void saveFile()
    {
        FileOutputStream outStream = null;
        File file = new File("/sdcard/"+(System.currentTimeMillis()%1000)+".JPEG");
        try {
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

            Toast.makeText(liveFeed.this, "Saved", Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(liveFeed.this, e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(liveFeed.this, e.toString(), Toast.LENGTH_LONG).show();
        }


    }

    public void startReceiving()
    {
        if(counter==0) {
            //buttonConnect.setText("Stop Streaming");
            getdata();
        }
        else {
            try {
                socket.close();
                Intent intent = new Intent(liveFeed.this,MainActivity.class);
                startActivity(intent);
                counter = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    void getdata()
    {
        if(counter == 10)
        {
            counter = 1;
        }
        counter++;
        file = new File("/sdcard/RemoteCam/"+counter+".3gp");
        dstPort = SocketServerPORT;
        new GetVideo().execute(file);

    }
    protected void VideoPlay(){
        MediaPlayer mediaPlayer;
        SurfaceView surfaceView;
        SurfaceHolder surfaceHolder;
        boolean pausing = false;;

        getWindow().setFormat(PixelFormat.UNKNOWN);
        playcounter++;

        String Path = "/sdcard/RemoteCam/"+counter+".3gp";
        mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(Path);
        //mVideoView.setVideoURI(Uri.parse(Path));
        //myMediaController = new MediaController(liveFeed.this);

        //mVideoView.setMediaController(myMediaController);

        mVideoView.setOnCompletionListener(myVideoViewCompletionListener);
        mVideoView.setOnPreparedListener(MyVideoViewPreparedListener);
        mVideoView.setOnErrorListener(myVideoViewErrorListener);


        mVideoView.setVideoPath(Path);
        mVideoView.requestFocus();
        mVideoView.start();



    }

    MediaPlayer.OnCompletionListener myVideoViewCompletionListener =
            new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer arg0) {
                }
            };

    MediaPlayer.OnPreparedListener MyVideoViewPreparedListener =
            new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    //    long duration = mVideoView.getDuration(); //in millisecond
                }
            };

    MediaPlayer.OnErrorListener myVideoViewErrorListener =
            new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    return true;
                }
            };



    public class GetVideo extends AsyncTask<File,Integer,File>{

        @Override
        protected File doInBackground(File... params) {


            try {
                if(counter==1){
                    socket = null;
                    socket = new Socket(dstAddress, dstPort);
                }
                connect = true;
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                byte[] bytes;
                FileOutputStream fos = null;
                try {
                    bytes = (byte[])ois.readObject();
                    fos = new FileOutputStream(params[0]);
                    fos.write(bytes);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if(fos!=null){
                        fos.close();
                    }

                }

                //socket.close();

            } catch (IOException e) {

                e.printStackTrace();

                //final String eMsg = "Something wrong: " + e.getMessage();
            } finally {
                if(!connect) {
                    return file;
                }
            }
            return file;
        }
        protected void onPostExecute(File result)
        {
            if(connect)
            {
                VideoPlay();
                getdata();
            }
            else
            {
                changetext();
            }
        }
    }

    public void changetext()
    {
        //buttonConnect.setText("Start Live Feed");
        counter = 0;
        Toast.makeText(liveFeed.this, "Start streaming on Camera Side", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed()
    {

    }
/*   public void onBackPressed() {
        super.onBackPressed();

        if (socket != null) {
            try {
                socket.close();
                Intent intent = new Intent(liveFeed.this,Splash.class);
               startActivity(intent);
                finish();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }*/
    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        //deleteDirectory(mydir);
        if (socket != null) {
            try {
                socket.close();
                Intent intent = new Intent(liveFeed.this,AvailableConnections.class);
               startActivity(intent);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }*/
    /*@Override
    protected void onStop()
    {
        super.onStop();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        deleteDirectory(mydir);
    }*/
    /*public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }*/
    }



