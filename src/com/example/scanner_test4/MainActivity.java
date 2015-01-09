package com.example.scanner_test4;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity {
	
	private Uri imageUri;
	private static int TAKE_PICTURE = 1;

	/*
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS ) {
            	Log.i("~~~~~~~", "Load successful!");
            } else {
                super.onManagerConnected(status);
                Log.i("~~~~~~~", "Load failed!");
            }
        }
    };*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if(!OpenCVLoader.initDebug()){
        	
        }
        
        Button camera = (Button) findViewById(R.id.camera);
        camera.setOnClickListener(cameraListener);
        
    }
	@Override
	protected void onResume(){
	    Log.i("~~~~", "Called onResume");
	    super.onResume();
	    
	    Log.i("~~~~", "Trying to load OpenCV library");
	    /*
	    if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mLoaderCallback))
	    {
	        Log.e("~~~~", "Cannot connect to OpenCV Manager");
	    }*/
	      
	}
    private OnClickListener cameraListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			takePhoto(v);
		}
    };
    
	protected void takePhoto(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/scanner_test4_pic.jpg");
		imageUri = Uri.fromFile(photo);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(intent, TAKE_PICTURE);
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // call the parent
        super.onActivityResult(requestCode, resultCode, data);
		Intent i = new Intent(MainActivity.this, adjustPic.class);
	    startActivity(i); 
    }
}
