package com.remotecam;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.zip.Inflater;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


public class CameraServer extends ActionBarActivity {

    TextView infoIp, infoPort;
    static final int SocketServerPORT = 8080;
    static public Socket socket;
    static ServerSocket serverSocket;
    private Camera myCamera;
    boolean finished;
    public MyCameraSurfaceView myCameraSurfaceView;
    SurfaceHolder surfaceHolder;
    public MediaRecorder mediaRecorder;
    private int counter = 1;
    private File mydir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_server);
        myCamera = getCameraInstance();
        if(myCamera == null){
            Toast.makeText(CameraServer.this,                    "Fail to get Camera",Toast.LENGTH_LONG).show();
        }

        myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera);
        FrameLayout myCameraPreview = (FrameLayout)findViewById(R.id.videoview);
        myCameraPreview.addView(myCameraSurfaceView);
        mydir = new File("/sdcard/RemoteCam/");
        if(!mydir.exists())
            mydir.mkdirs();
        RunVideo Runvideothread = new RunVideo();
        Runvideothread.start();


    }

    public class RunVideo extends Thread{
        @Override
        public void run()
        {
            CameraServer.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recordVideo();
                }
            });
        }
    }

    public void recordVideo()
    {
        if(counter==1) {

            releaseMediaRecorder();
            releaseCamera();
            finished = true;


            if (!prepareMediaRecorder()) {
                Toast.makeText(CameraServer.this,
                        "Fail in prepareMediaRecorder()!\n - Ended -",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            mediaRecorder.start();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            mediaRecorder.stop();
            new ConnectSocket().execute();
        }
        else
        {
            if(socket.isConnected())
            {
                releaseMediaRecorder();
                releaseCamera();
                finished = true;


                if (!prepareMediaRecorder()) {
                    Toast.makeText(CameraServer.this,
                            "Fail in prepareMediaRecorder()!\n - Ended -",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                mediaRecorder.start();
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                mediaRecorder.stop();
                new ConnectSocket().execute();
            }
            else
            {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(CameraServer.this,MainActivity.class);
                deleteDirectory(mydir);
                startActivity(intent);
            }
        }
    }
    public static boolean deleteDirectory(File path) {
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
    }




   /* @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }*/

    @Override
    public void onBackPressed() {

        Context context = CameraServer.this;
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, false);

        } catch (Exception e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(CameraServer.this,MainActivity.class);
        startActivity(intent);
        return;
    }

    public class ConnectSocket extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            if(counter==1) {
                try {
                    serverSocket = new ServerSocket(SocketServerPORT);
                    socket = serverSocket.accept();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {

                }
            }
            return null;
        }

        protected void onPostExecute(String string) {
            File file = new File("/sdcard/RemoteCam/" + counter + ".3gp");
            new SendFile().execute(file);
        }
    }



    public class SendFile extends AsyncTask<File, Integer, String> {

        @Override
        protected String doInBackground(File... params) {
            byte[] bytes = new byte[(int) params[0].length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(params[0]));
                bis.read(bytes, 0, bytes.length);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(bytes);
                oos.flush();

                //final String sentMsg = "File sent to: " + socket.getInetAddress();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                // socket.close();
                //socket = null;
                // serverSocket = null;
            }
            return null;
        }
        protected void onPostExecute(String result)
        {
            counter++;
            recordVideo();
        }
    }
    private Camera getCameraInstance(){
// TODO Auto-generated method stub
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean prepareMediaRecorder(){

        myCamera = getCameraInstance();
        myCamera.setDisplayOrientation(90);
        myCamera.enableShutterSound(false);
        mediaRecorder = new MediaRecorder();

        myCamera.unlock();
        mediaRecorder.setCamera(myCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        mediaRecorder.setMaxDuration(6000); // Set max duration 60 sec.
        mediaRecorder.setMaxFileSize(5000000); // Set max file size 5M
        mediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());
        mediaRecorder.setOutputFile("/sdcard/RemoteCam/" + counter + ".3gp");


        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            myCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (myCamera != null){
            myCamera.release();        // release the camera for other applications
            myCamera = null;
        }
    }

    public class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{

        private SurfaceHolder mHolder;
        private Camera mCamera;

        public MyCameraSurfaceView(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int weight,
                                   int height) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // make any resize, rotate or reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
        mCamera.setPreviewDisplay(holder);
        mCamera.setDisplayOrientation(90);
        mCamera.startPreview();
    } catch (IOException e) {
    }
}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    }

}


