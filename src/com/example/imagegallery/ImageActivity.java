package com.example.imagegallery;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.View.OnClickListener;

public class ImageActivity extends Activity implements OnClickListener
{
	private ProgressDialog mProgress;
    View.OnTouchListener gestureListener;
    private int position;
    String[] imageurls;
    private DownloadFile downloadFile;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_image);
		
		// Get intent data
		Intent i = getIntent();
		
		// Get Image ID from the passed intent
		String url = i.getExtras().getString("standard");
		imageurls = i.getExtras().getStringArray("URLarray");
		position = i.getExtras().getInt("currentPosition");
		
		mProgress = new ProgressDialog(ImageActivity.this);
		/*gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };*/

		
		downloadFile = new DownloadFile();
		downloadFile.execute(url);
	}
	
	private class DownloadFile extends AsyncTask<String, Integer, Bitmap>{
		@Override
		protected Bitmap doInBackground(String...sUrl){
			try{
				URL url = new URL(sUrl[0]);
				URLConnection conn= url.openConnection();
				conn.connect();
				InputStream input= new BufferedInputStream(url.openStream());
				publishProgress(50);
				publishProgress(100);
				
				return BitmapFactory.decodeStream(input);
				
			}
			catch(Exception e){
				Log.e("Error: ", e.getMessage());
			}
			return null;
	}
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			mProgress = ProgressDialog.show(ImageActivity.this, "", "Loading Image...");
		}
		@Override
		protected void onProgressUpdate(Integer...progress){
			super.onProgressUpdate(progress);
			mProgress.setProgress(progress[0]);
		}
		@Override
		protected void onPostExecute(Bitmap bitmap){
			mProgress.cancel();
			TouchImageView view = (TouchImageView) findViewById(R.id.full_image_view);
			view.setImageBitmap(bitmap);
			
			view.setOnClickListener(ImageActivity.this); 
			//view.setOnTouchListener(gestureListener);
			
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}