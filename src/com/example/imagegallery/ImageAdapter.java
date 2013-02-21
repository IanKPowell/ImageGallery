package com.example.imagegallery;

import java.net.URL;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


public class ImageAdapter extends BaseAdapter
{
	private Context mContext;
	private String[] imageURL;
	private ImageDownloader2 imageDownloader;
	// Constructor
	public ImageAdapter(Context context, String[] imageURL, ImageDownloader2 imageDownloader)
	{
		this.mContext = context;
		this.imageURL = imageURL;
		this.imageDownloader = imageDownloader;
	}

	@Override
	public int getCount() {	return this.imageURL.length; }

	@Override
	public Object getItem(int position) { return imageURL[position];	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ImageView imageView;
		
		if(convertView == null)	// Recycled View
		{
			imageView = new ImageView(mContext);
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setLayoutParams(new GridView.LayoutParams(220, 220));
		}
		else	// Re-use the view
		{
			imageView = (ImageView) convertView;
		}
		
		this.imageDownloader.download(this.imageURL[position], imageView);
		return imageView;
	}

}
