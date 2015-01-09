package com.example.scanner_test4;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class adjustPic extends Activity implements OnTouchListener{
	
	final int dst_width = 1000;
    final int dst_height = 600;
     
	Button confirm, rotateLeft, rotateRight;
	ImageView LT, LB, RT, RB;
	ImageView imageView, imageViewResult;
	ProgressBar pb;
	private Point leftTop = new Point(0,0);
	private Point leftBot = new Point(0,600);
	private Point rightTop = new Point(1000,0);
	private Point rightBot = new Point(1000,600);
	private Point new_leftTop = new Point(0,0);
	private Point new_leftBot = new Point(0,0);
	private Point new_rightTop = new Point(0,0);
	private Point new_rightBot = new Point(0,0);
	Bitmap processBitmap, resultBitmap;
	
	double screenWidth=0, screenHeight=0, pictureRatio=0.6;
	 
	float x,y = 0.0f;
	boolean moving=false;
	
	
	RelativeLayout rl;
	
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adjust_pic);
        
        Display display = getWindowManager().getDefaultDisplay(); 
        screenWidth = display.getWidth();  // deprecated
        screenHeight = screenWidth * pictureRatio;
        
        rl = (RelativeLayout) findViewById(R.id.relative_layout);
        confirm = (Button) findViewById(R.id.confirm);
        rotateLeft = (Button) findViewById(R.id.rotate_left);
        rotateRight = (Button) findViewById(R.id.rotate_right);
        
        imageView = (ImageView) findViewById(R.id.image);
        imageViewResult = (ImageView) findViewById(R.id.image_result);
		imageView.getLayoutParams().height = (int) screenHeight;
		imageView.getLayoutParams().width = (int) screenWidth;
		imageViewResult.getLayoutParams().height = (int) screenHeight;
		imageViewResult.getLayoutParams().width = (int) screenWidth;
		
        pb = (ProgressBar) findViewById(R.id.progressBar);
        LT = (ImageView) findViewById(R.id.left_up);
        LB = (ImageView) findViewById(R.id.left_down);
        RT = (ImageView) findViewById(R.id.right_up);
        RB = (ImageView) findViewById(R.id.right_down);
        LT.setOnTouchListener(this);
        LB.setOnTouchListener(this);
        RT.setOnTouchListener(this);
        RB.setOnTouchListener(this);
        
        
        MyAsyncTaskHelper async = new MyAsyncTaskHelper(getApplicationContext());
        async.execute();
        
        confirm.setOnClickListener(new ConfirmListerner());
        rotateLeft.setOnClickListener(new RotateLeftListerner());
        rotateRight.setOnClickListener(new RotateRightListerner());
	}
	

	private class ConfirmListerner implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			new_leftTop = new Point((LT.getX()/(screenWidth*1.0)*dst_width)+LT.getLayoutParams().width/2f,
					(LT.getY()/(screenHeight*1.0)*dst_height)+LT.getLayoutParams().height/2f);
			new_leftBot = new Point((LB.getX()/(screenWidth*1.0)*dst_width)+LB.getLayoutParams().width/2f,
					(LB.getY()/(screenHeight*1.0)*dst_height)+LB.getLayoutParams().height/2f);
			new_rightTop = new Point((RT.getX()/(screenWidth*1.0)*dst_width)+RT.getLayoutParams().width/2f,
					(RT.getY()/(screenHeight*1.0)*dst_height)+RT.getLayoutParams().height/2f);
			new_rightBot = new Point((RB.getX()/(screenWidth*1.0)*dst_width)+RB.getLayoutParams().width/2f,
					(RB.getY()/(screenHeight*1.0)*dst_height)+RB.getLayoutParams().height/2f);
			
            Mat src_mat=new Mat(4,1,CvType.CV_32FC2);
            Mat dst_mat=new Mat(4,1,CvType.CV_32FC2);
            
            if(distance(new_leftTop,new_rightTop)>distance(new_leftTop,new_leftBot)){
                src_mat.put(0,0,new_leftTop.x,new_leftTop.y,
                		new_rightTop.x, new_rightTop.y, 
                		new_leftBot.x, new_leftBot.y, 
                		new_rightBot.x, new_rightBot.y );
            }else{
                src_mat.put(0,0,new_rightTop.x,new_rightTop.y,
                		new_rightBot.x, new_rightBot.y, 
                		new_leftTop.x, new_leftTop.y, 
                		new_leftBot.x, new_leftBot.y);
            }
            
            Mat rgbMat = new Mat();  
            Utils.bitmapToMat(processBitmap, rgbMat);
            Imgproc.resize(rgbMat, rgbMat, new Size(dst_width, dst_height));
            resultBitmap = Bitmap.createBitmap((int)dst_width,(int)dst_height, Config.RGB_565);
            
            dst_mat.put(0,0, 0,0,dst_width,0, 0,dst_height, dst_width,dst_height);
            Mat tempMat = Imgproc.getPerspectiveTransform(src_mat, dst_mat);
            Mat dstMat=rgbMat.clone();
            Imgproc.warpPerspective(rgbMat, dstMat, tempMat, new Size(dst_width,dst_height));
            Utils.matToBitmap(dstMat, resultBitmap);
			
            // flip is not done
			rotateLeft.setVisibility(View.VISIBLE);
			rotateRight.setVisibility(View.VISIBLE);
			imageViewResult.setImageBitmap(resultBitmap);
			imageViewResult.setVisibility(View.VISIBLE);
		}
		
	}
	private class RotateLeftListerner implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			resultBitmap = RotateBitmap(resultBitmap,-90);
			if(resultBitmap.getWidth()>resultBitmap.getHeight()){
				imageViewResult.getLayoutParams().height = (int) screenHeight;
				imageViewResult.getLayoutParams().width = (int) screenWidth;
			}else{
				imageViewResult.getLayoutParams().width = (int) screenHeight;
				imageViewResult.getLayoutParams().height = (int) screenWidth;
			}
			imageViewResult.setImageBitmap(resultBitmap);
		}
		
	}
	private class RotateRightListerner implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			resultBitmap = RotateBitmap(resultBitmap,90);
			if(resultBitmap.getWidth()>resultBitmap.getHeight()){
				imageViewResult.getLayoutParams().height = (int) screenHeight;
				imageViewResult.getLayoutParams().width = (int) screenWidth;
			}else{
				imageViewResult.getLayoutParams().width = (int) screenHeight;
				imageViewResult.getLayoutParams().height = (int) screenWidth;
			}
			imageViewResult.setImageBitmap(resultBitmap);
		}
		
		
	}
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			moving = true;
			break;
		case MotionEvent.ACTION_MOVE:
			if(moving){
				x=event.getRawX() - v.getLayoutParams().width;
				y=event.getRawY() - v.getLayoutParams().height*2;
				if(x<=screenWidth-v.getLayoutParams().width/2 && y<=screenHeight-v.getLayoutParams().height/2){
					if(x<0-v.getLayoutParams().width/2){
						v.setX(v.getLayoutParams().width/2);
					}else{
						v.setX(x);
					}
					if(y<0-v.getLayoutParams().height/2){
						v.setY(0-v.getLayoutParams().height/2);
					}else{
						v.setY(y);	
					}	
				}else{
					if(x>screenWidth-v.getLayoutParams().width/2){
						v.setX((float) (screenWidth - v.getLayoutParams().width/2));
					}else{
						v.setX(x);
					}
					if(y>screenHeight-v.getLayoutParams().height/2){
						v.setY((float) (screenHeight - v.getLayoutParams().height/2));		
					}else{
						v.setY(y);
					}

				}
				imageView.setImageBitmap(processBitmap);
				Bitmap temp = processBitmap.copy(processBitmap.getConfig(), true);;
				Canvas canvas = new Canvas(temp);
			    Paint paint = new Paint();
			    paint.setColor(Color.rgb(102, 204, 255));
			    paint.setStrokeWidth(5);
			    canvas.drawLine((float)(LT.getX()/(screenWidth*1.0)*dst_width)+LT.getLayoutParams().width/2f, 
			    		(float)(LT.getY()/(screenHeight*1.0)*dst_height)+LT.getLayoutParams().height/2f,
			    		(float)(RT.getX()/(screenWidth*1.0)*dst_width)+RT.getLayoutParams().width/2f, 
			    		(float)(RT.getY()/(screenHeight*1.0)*dst_height)+RT.getLayoutParams().height/2f, paint);
			    
			    canvas.drawLine((float)(RT.getX()/(screenWidth*1.0)*dst_width)+RT.getLayoutParams().width/2f, 
			    		(float)(RT.getY()/(screenHeight*1.0)*dst_height)+RT.getLayoutParams().height/2f,
			    		(float)(RB.getX()/(screenWidth*1.0)*dst_width)+RB.getLayoutParams().width/2f, 
			    		(float)(RB.getY()/(screenHeight*1.0)*dst_height)+RB.getLayoutParams().height/2f, paint);
			    
			    canvas.drawLine((float)(RB.getX()/(screenWidth*1.0)*dst_width)+RB.getLayoutParams().width/2f, 
			    		(float)(RB.getY()/(screenHeight*1.0)*dst_height)+RB.getLayoutParams().height/2f,
			    		(float)(LB.getX()/(screenWidth*1.0)*dst_width)+LB.getLayoutParams().width/2f, 
			    		(float)(LB.getY()/(screenHeight*1.0)*dst_height)+LB.getLayoutParams().height/2f, paint);
			    
			    canvas.drawLine((float)(LB.getX()/(screenWidth*1.0)*dst_width)+LB.getLayoutParams().width/2f, 
			    		(float)(LB.getY()/(screenHeight*1.0)*dst_height)+LB.getLayoutParams().height/2f,
			    		(float)(LT.getX()/(screenWidth*1.0)*dst_width)+LT.getLayoutParams().width/2f, 
			    		(float)(LT.getY()/(screenHeight*1.0)*dst_height)+LT.getLayoutParams().height/2f, paint);
			    imageView.setImageBitmap(temp);
				
			}
			break;
		case MotionEvent.ACTION_UP:
			moving = false;
			break;
		}
		return true;
	}
	
	private class MyAsyncTaskHelper extends AsyncTask<Void, Void, Void>{

		private Context context;
		
		MyAsyncTaskHelper(Context c){
			context = c;
		}
		
		protected void onPreExecute(){
			pb.setVisibility(View.VISIBLE);
		}
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			findCard();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v){
	        pb.setVisibility(View.GONE);
	        confirm.setVisibility(View.VISIBLE);
	        imageView.setVisibility(View.VISIBLE);
			imageView.setImageBitmap(processBitmap);
			
			
			
			LT.setX((float) (leftTop.x/(double)dst_width * (int)screenWidth)-(float)LT.getLayoutParams().width/2f);
			LT.setY((float) (leftTop.y/(double)dst_height * (int)screenHeight)-(float)LT.getLayoutParams().height/2f);
	        LT.setVisibility(View.VISIBLE);
	        
			LB.setX((float) (leftBot.x/(double)dst_width * (int)screenWidth)-(float)LB.getLayoutParams().width/2f);
			LB.setY((float) (leftBot.y/(double)dst_height * (int)screenHeight)-(float)LB.getLayoutParams().height/2f);
	        LB.setVisibility(View.VISIBLE);
	        
			RT.setX((float) (rightTop.x/(double)dst_width * (int)screenWidth)-(float)RT.getLayoutParams().width/2f);
			RT.setY((float) (rightTop.y/(double)dst_height * (int)screenHeight)-(float)RT.getLayoutParams().height/2f);
	        RT.setVisibility(View.VISIBLE);
	        
	        RB.setX((float) (rightBot.x/(double)dst_width * (int)screenWidth)-(float)RB.getLayoutParams().width/2f);
	        RB.setY((float) (rightBot.y/(double)dst_height * (int)screenHeight)-(float)RB.getLayoutParams().height/2f);
	        RB.setVisibility(View.VISIBLE);
	        
	        //draw the first four lines
			Bitmap temp = processBitmap.copy(processBitmap.getConfig(), true);;
			Canvas canvas = new Canvas(temp);
		    Paint paint = new Paint();
		    paint.setColor(Color.rgb(102, 204, 255));
		    paint.setStrokeWidth(5);
		    canvas.drawLine((float)(LT.getX()/(screenWidth*1.0)*dst_width)+LT.getLayoutParams().width/2f, 
		    		(float)(LT.getY()/(screenHeight*1.0)*dst_height)+LT.getLayoutParams().height/2f,
		    		(float)(RT.getX()/(screenWidth*1.0)*dst_width)+RT.getLayoutParams().width/2f, 
		    		(float)(RT.getY()/(screenHeight*1.0)*dst_height)+RT.getLayoutParams().height/2f, paint);
		    
		    canvas.drawLine((float)(RT.getX()/(screenWidth*1.0)*dst_width)+RT.getLayoutParams().width/2f, 
		    		(float)(RT.getY()/(screenHeight*1.0)*dst_height)+RT.getLayoutParams().height/2f,
		    		(float)(RB.getX()/(screenWidth*1.0)*dst_width)+RB.getLayoutParams().width/2f, 
		    		(float)(RB.getY()/(screenHeight*1.0)*dst_height)+RB.getLayoutParams().height/2f, paint);
		    
		    canvas.drawLine((float)(RB.getX()/(screenWidth*1.0)*dst_width)+RB.getLayoutParams().width/2f, 
		    		(float)(RB.getY()/(screenHeight*1.0)*dst_height)+RB.getLayoutParams().height/2f,
		    		(float)(LB.getX()/(screenWidth*1.0)*dst_width)+LB.getLayoutParams().width/2f, 
		    		(float)(LB.getY()/(screenHeight*1.0)*dst_height)+LB.getLayoutParams().height/2f, paint);
		    
		    canvas.drawLine((float)(LB.getX()/(screenWidth*1.0)*dst_width)+LB.getLayoutParams().width/2f, 
		    		(float)(LB.getY()/(screenHeight*1.0)*dst_height)+LB.getLayoutParams().height/2f,
		    		(float)(LT.getX()/(screenWidth*1.0)*dst_width)+LT.getLayoutParams().width/2f, 
		    		(float)(LT.getY()/(screenHeight*1.0)*dst_height)+LT.getLayoutParams().height/2f, paint);

		    		
		    imageView.setImageBitmap(temp);
		}
		
		private void findCard() {
			Mat rgbMat = new Mat();  
			Bitmap srcBitmap = BitmapFactory.decodeFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
	        		+"/scanner_test4_pic.jpg");
			if(srcBitmap.getWidth() < srcBitmap.getHeight()){
				Mat rotate = new Mat();
				Utils.bitmapToMat(srcBitmap, rotate);
				Bitmap tempBitmap = RotateBitmap(srcBitmap,90);
				srcBitmap = null;
				srcBitmap = tempBitmap.copy(tempBitmap.getConfig(), true);
			}
	        Utils.bitmapToMat(srcBitmap, rgbMat);
			Imgproc.resize(rgbMat, rgbMat, new Size(dst_width, dst_height));
	        processBitmap = Bitmap.createBitmap((int)dst_width,(int)dst_height, Config.RGB_565);
	         
	        ArrayList<List<Point>> squares = new ArrayList<List<Point>>();
	        ArrayList<List<Point>> largest_square = new ArrayList<List<Point>>();
	        squares = (ArrayList<List<Point>>) find_squares(rgbMat).clone();
	        largest_square = (ArrayList<List<Point>>) find_largest_square(squares).clone();
	        
	        
	        
	        if(largest_square.size()==0){
	        	Utils.matToBitmap(rgbMat, processBitmap);
	        }else{
	        	double maxSum=Double.NEGATIVE_INFINITY, 
	        			minSum = Double.POSITIVE_INFINITY,
	        			maxDiff = Double.NEGATIVE_INFINITY, 
	        			minDiff = Double.POSITIVE_INFINITY;
	        	
	        	for(int i=0; i<largest_square.get(0).size();i++){
	              	if(largest_square.get(0).get(i).x+largest_square.get(0).get(i).y <= minSum){
	              		minSum = largest_square.get(0).get(i).x+largest_square.get(0).get(i).y;
	              		leftTop.x = largest_square.get(0).get(i).x+5;
	              		leftTop.y = largest_square.get(0).get(i).y+5;
	              	}
	              	if(largest_square.get(0).get(i).x+largest_square.get(0).get(i).y >= maxSum){
	              		maxSum = largest_square.get(0).get(i).x+largest_square.get(0).get(i).y;
	              		rightBot.x = largest_square.get(0).get(i).x-5;
	              		rightBot.y = largest_square.get(0).get(i).y+5;
	              	}
	              	if(largest_square.get(0).get(i).x-largest_square.get(0).get(i).y <= minDiff){
	              		minDiff = largest_square.get(0).get(i).x-largest_square.get(0).get(i).y;
	              		leftBot.x = largest_square.get(0).get(i).x+5;
	              		leftBot.y = largest_square.get(0).get(i).y-5;
	              	}
	              	if(largest_square.get(0).get(i).x-largest_square.get(0).get(i).y >= maxDiff){
	              		maxDiff = largest_square.get(0).get(i).x-largest_square.get(0).get(i).y;
	              		rightTop.x = largest_square.get(0).get(i).x-5;
	              		rightTop.y = largest_square.get(0).get(i).y-5;
	              	}
	        	}        	
	            Utils.matToBitmap(rgbMat, processBitmap);
	            
	        }
	        
	        
		}
		
		private ArrayList<List<Point>> find_largest_square(ArrayList<List<Point>> squares) {
			// TODO Auto-generated method stub
			ArrayList<List<Point>> largest_squares = new ArrayList<List<Point>>();
		    if (squares.size()==0)
		    {
		        // no squares detected
		        return largest_squares;
		    }else{
		        int max_width = 0;
		        int max_height = 0;
		        int max_square_idx = 0;
		        for (int i = 0; i < squares.size(); i++){

					Rect rectangle = Imgproc.boundingRect(new MatOfPoint(squares.get(i).get(0),
							squares.get(i).get(1),squares.get(i).get(2),squares.get(i).get(3)));
					
					if ((rectangle.width >= max_width) && (rectangle.height >= max_height))
			        {
			            max_width = rectangle.width;
			            max_height = rectangle.height;
			            max_square_idx = i;
			        }
		        }
		        largest_squares.add(squares.get(max_square_idx));
				return largest_squares;
		    }
		}

		private ArrayList<List<Point>> find_squares(Mat rgbMat) {
			// TODO Auto-generated method stub
			ArrayList<List<Point>> squares = new ArrayList<List<Point>>();
			
			Mat blurred_1 = new Mat(); 
			Mat gray_0 = new Mat();
			Mat gray = new Mat();
			Imgproc.medianBlur(rgbMat, blurred_1, 9);
			Imgproc.cvtColor(blurred_1, gray_0, Imgproc.COLOR_RGB2GRAY);
			
			List<Mat> blurred=new ArrayList<Mat>();
			List<Mat> gray0=new ArrayList<Mat>();
			blurred.add(blurred_1);
			gray0.add(gray_0);
			
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			
		    // find squares in every color plane of the image
		    for (int c = 0; c < 3; c++){
		    	int ch[] = {c, 0};
		    	MatOfInt fromto = new MatOfInt(ch);
		    	Core.mixChannels(blurred, gray0, fromto);
		    	
			    int threshold_level = 2;
			    for (int l = 0; l < threshold_level; l++){
			    	if (l == 0){
			    		Imgproc.Canny(gray0.get(0), gray, 10, 20, 3, false);
			    		Imgproc.dilate(gray, gray, new Mat(), new Point(-1,-1),1);
			    	}else{
			    		//gray = gray0.get(0) >= (l+1) * 255 / threshold_level;
			    	}
			    	Mat hierarchy = new Mat();
			    	Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
			    	
			    	//List<MatOfPoint> approx = new ArrayList<MatOfPoint>();
		    		MatOfPoint2f approx = new MatOfPoint2f();
			    	for (int i = 0; i < contours.size(); i++){
						Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx, 
								Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true)*0.02, true);
						
						if(approx.toList().size()==4 && 
								Math.abs(Imgproc.contourArea(approx)) > 1000 &&
								Imgproc.isContourConvex(new MatOfPoint(approx.toArray()))){
							double maxCosine = 0;
						
							for (int j = 2; j < 5; j++){
								double cosine = Math.abs(angle(approx.toList().get(j%4), approx.toList().get(j-2), approx.toList().get(j-1)));
								maxCosine = Math.max(maxCosine, cosine);
							}
							if (maxCosine < 0.46){
		                        squares.add(approx.toList());
							}
						}
			    	} 
			    }
		    }	
			
			return squares;
		}
		    
		double angle( Point pt1, Point pt2, Point pt0 ) {
		    double dx1 = pt1.x - pt0.x;
		    double dy1 = pt1.y - pt0.y;
		    double dx2 = pt2.x - pt0.x;
		    double dy2 = pt2.y - pt0.y;
		    return (dx1*dx2 + dy1*dy2)/Math.sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
		}
		
		double distance(Point pt1, Point pt2){
			return (Math.sqrt((pt1.x-pt2.x)*(pt1.x-pt2.x)+(pt1.y-pt2.y)*(pt1.y-pt2.y)));
		}
	}

	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}
	public static float convertPixelsToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}
	double distance(Point pt1, Point pt2){
		return (Math.sqrt((pt1.x-pt2.x)*(pt1.x-pt2.x)+(pt1.y-pt2.y)*(pt1.y-pt2.y)));
	}
	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

}
