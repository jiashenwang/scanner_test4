package com.example.scanner_test4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class result extends Activity {
	
	ImageView result;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.result);
	    
	    result = (ImageView)findViewById(R.id.result);
	    
	    Intent intent = getIntent();
	    Bundle extras = getIntent().getExtras();
	    String path = extras.getString("PATH");
	    
	    try {
	        File f=new File(path, "profile.jpg");
	        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
	        result.setImageBitmap(b);
	    } 
	    catch (FileNotFoundException e) 
	    {
	        e.printStackTrace();
	    }
	}
}
