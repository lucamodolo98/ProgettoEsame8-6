package com.example.luca.progettoesame;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DrawerListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<NavItem> mNavItems;
    private MainActivity mMainActivity;

    public DrawerListAdapter(Context context, ArrayList<NavItem> navItems, MainActivity mainActivity) {
        mContext = context;
        mNavItems = navItems;
        mMainActivity = mainActivity;
    }

    @Override
    public int getCount() {
        return mNavItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mNavItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.drawer_item, null);
        }
        else {
            view = convertView;
        }

        TextView titleView = view.findViewById(R.id.title);
        TextView subtitleView = view.findViewById(R.id.subTitle);
        ImageView iconView = view.findViewById(R.id.icon);
        final NavItem item = mNavItems.get(position);
        titleView.setText( item.mTitle );
        subtitleView.setText( item.mSubtitle );
        iconView.setImageResource(item.mIcon);
        View.OnClickListener watch = v -> mMainActivity.changeCities(item.mCountryCode, mContext);
        view.setOnClickListener(watch);
       return view;
    }
}
