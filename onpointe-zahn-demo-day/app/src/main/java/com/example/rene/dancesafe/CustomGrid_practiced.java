package com.example.rene.dancesafe;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

// Get customized gridView adapter ready so it can be used in
// "column_practiced" class by calling CustomGrid_practiced( ?, ?, ?)

public class CustomGrid_practiced extends BaseAdapter {
    private Context mContext;

    private final ArrayList<String> web;
   // private final String[] web;
    private final ArrayList<Bitmap> ImageMap;

    public CustomGrid_practiced(Context c,ArrayList web, ArrayList<Bitmap> ImageMap ) {
        mContext = c;
        this.ImageMap = ImageMap;
        this.web = web;
    }

    /*
    public CustomGrid_practiced(Context c,String[] web,int[] Imageid ) {
        mContext = c;
        this.Imageid = Imageid;
        this.web = web;
    }*/

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return web.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_single_practiced, null);
            TextView textView = (TextView) grid.findViewById(R.id.grid_text_practiced);
            ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image_practiced);
           // textView.setText(web[position]);
            textView.setText(web.get(position));
            //imageView.setImageResource(Imageid[position]);

            imageView.setImageBitmap(ImageMap.get(position));
        } else {
            grid = (View) convertView;
        }
        return grid;
    }

}