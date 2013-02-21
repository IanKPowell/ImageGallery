package com.example.imagegallery;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity 
{
	private static int CONNECTION_TIMEOUT = 10000;
	private static int DATARETRIEVAL_TIMEOUT = 15000;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Get GridView from xml
		GridView gridView = (GridView) findViewById(R.id.grid_view);
		
		//Get List of Images and create an array of thumbnails
		final List<ImageMeta> images = findAllItems();
		final String[] imageurls = new String[images.size()];
		int count = 0;
		for(ImageMeta each: images){
			imageurls[count] = each.getThumbnailURL();
			count++;
		}
		
		ImageDownloader2 imageDownloader = new ImageDownloader2();
		
		gridView.setAdapter(new ImageAdapter(this, imageurls, imageDownloader));
		
		
		/**
		 * On Click event for Single GridView Item
		 * */
		gridView.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) 
			{
				// Create new intent
				Intent i = new Intent(MainActivity.this, ImageActivity.class);
				
				// Send Image ID to ImageActivity
				i.putExtra("standard", images.get(position).getImageURL());
				i.putExtra("URLarray", imageurls);
				i.putExtra("currentPosition", position);
				
				startActivity(i);
			}
		});
	}

	/**
	 * @return
	 */
	public List<ImageMeta> findAllItems(){
		JSONObject serviceResult = requestWebService("http://pages.cs.wisc.edu/~griepent/instagram.json");
		List<ImageMeta> foundItems = new ArrayList<ImageMeta>();
		
		try{
			JSONArray items = serviceResult.getJSONArray("data");
			
			for(int i=0; i<items.length();i++){
				JSONObject obj = items.getJSONObject(i); 
				String imageURL = obj.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
				String thumbnailURL= obj.getJSONObject("images").getJSONObject("thumbnail").getString("url");
				JSONObject captionobj = obj.getJSONObject("caption");
				String caption = captionobj.getString("text");
				String createdBy = captionobj.getJSONObject("from").getString("username");
				String profileURL = captionobj.getJSONObject("from").getString("profile_picture");
				
				//Comments
				List<String> comments = new ArrayList<String>();
				List<ProfileMeta> commentBy = new ArrayList<ProfileMeta>();
				JSONArray numOfComments = obj.getJSONObject("comments").getJSONArray("data");
				for(int j=0; j< numOfComments.length(); j++){
					JSONObject commentObj = numOfComments.getJSONObject(j);
					comments.add(commentObj.getString("text"));
					commentBy.add(new ProfileMeta(commentObj.getJSONObject("from").getString("username"), commentObj.getJSONObject("from").getString("profile_picture")));
				}
				
				//Likes
				List<ProfileMeta> likes= new ArrayList<ProfileMeta>();
				JSONArray JSONLikes = obj.getJSONObject("likes").getJSONArray("data");
				int numLikes = obj.getJSONObject("likes").getInt("count");
				for(int j=0; j< JSONLikes.length(); j++){
					likes.add(new ProfileMeta(JSONLikes.getJSONObject(j).getString("username"), JSONLikes.getJSONObject(j).getString("profile_picture")));
				}
				foundItems.add(new ImageMeta(imageURL, thumbnailURL, caption, new ProfileMeta(createdBy,profileURL), comments, commentBy, likes, numLikes));
				System.out.println(i);
			}
			
		} catch(JSONException e){
			
		}
		return foundItems;
	}

	private class ProfileMeta{
	
		private String username;
		private String profileURL;
		
		public ProfileMeta(String username,String profileURL)
		{
			this.username = username;
			this.profileURL = profileURL;
			
		}
		
		public String getUsername() {
			return username;
		}

		public String getProfileURL() {
			return profileURL;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public void setProfileURL(String profileURL) {
			this.profileURL = profileURL;
		}

	}
	
	/**
	 * @author Ian Powell
	 *
	 */
	private class ImageMeta {
		private List<ProfileMeta> commentedBy, likedBy;
		private String caption, imageURL, thumbnailURL;
		private ProfileMeta createdBy;
		private List<String> comments;
		private int numLikes;
		
		public ImageMeta(String URL, String Caption, ProfileMeta createdBy)
		{
			this.thumbnailURL = URL;
			this.caption = Caption;
			this.createdBy = createdBy;
			this.commentedBy = new ArrayList<ProfileMeta>();
			this.likedBy = new ArrayList<ProfileMeta>();
		}
		/**
		 * @param URL
		 * @param Caption
		 * @param createdBy
		 * @param comments
		 * @param commentedBy
		 * @param likedBy
		 * @param numLikes
		 */
		public ImageMeta(String URL, String thumbnailURL, String Caption, ProfileMeta createdBy, List<String> comments, List<ProfileMeta> commentedBy, List<ProfileMeta> likedBy, int numLikes ){
			
			this.caption = Caption;
			this.createdBy = createdBy;
			this.numLikes = numLikes;
			this.comments = comments;
			this.commentedBy = new ArrayList<ProfileMeta>();
			this.likedBy = new ArrayList<ProfileMeta>();
			this.imageURL = URL;
			this.thumbnailURL = thumbnailURL;
			
			for(int i=0; i< commentedBy.size();i++)
			{
				this.commentedBy.add(new ProfileMeta(commentedBy.get(i).getUsername(), commentedBy.get(i).getProfileURL()));
			}
			for(int i=0; i< likedBy.size();i++)
			{
				this.likedBy.add(new ProfileMeta(likedBy.get(i).getUsername(), likedBy.get(i).getProfileURL()));
			}	
		}

		
		public String getImageURL() {
			return imageURL;
		}
		public String getThumbnailURL() {
			return thumbnailURL;
		}
		public String getCaption() {
			return caption;
		}
		public String getCreatedBy() {
			return createdBy.getUsername();
		}
		public ArrayList<String> getComments() {
			
			ArrayList<String> comments = new ArrayList<String>();
			int counter = 0;
			for(ProfileMeta person: this.commentedBy)
			{
				comments.add(person.getUsername() + ": " + this.comments.get(counter));
				counter++;
			}
			return comments;
		}
		public int getNumLikes() {
			return numLikes;
		}
	}
	
	public static JSONObject requestWebService(String serviceURL){
		
		disableConnectionReuseIfNecessary();
		
		HttpURLConnection urlConnection = null;
		try {
			URL urlToRequest = new URL(serviceURL);
			urlConnection = (HttpURLConnection) urlToRequest.openConnection();
			urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
			urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);
			
			int statusCode = urlConnection.getResponseCode();
			if(statusCode == HttpURLConnection.HTTP_UNAUTHORIZED){
				//If there is need for username/password
				System.err.println("Please enter username/password first");
				System.exit(0);
			}
			else if(statusCode != HttpURLConnection.HTTP_OK)
			{
				//handle any other erros such as 404,500..
				System.err.println("An error has occured");
				System.exit(0);
			}
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			return new JSONObject(getResponseText(in));
			
		} catch(MalformedURLException e){
			//URL is invalid
		} catch(SocketTimeoutException e){
			//data retrieval or connection timed out
		} catch(IOException e){
			//Could not read response body
		} catch (JSONException e) {
			//response body is no valid JSON string
		} finally{
			if(urlConnection != null){
				urlConnection.disconnect();
			
			}
		}
		
		return null;
	}
	
	private JSONObject requestWebPage(String url){
		try {
			HttpClient client = new DefaultHttpClient(); 
			HttpGet get = new HttpGet( url );
			HttpResponse responseGet = client.execute(get); 
			HttpEntity resEntityGet = responseGet.getEntity(); 
			return new JSONObject(EntityUtils.toString(resEntityGet));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void disableConnectionReuseIfNecessary(){
		
		if(Integer.parseInt(Build.VERSION.SDK)< Build.VERSION_CODES.FROYO)
			System.setProperty("http.keepAlive", "false");
	}
	
	private static String getResponseText(InputStream inStream){
		return new Scanner(inStream).useDelimiter("\\A").next();
	}
}
