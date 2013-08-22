package com.cookpadintern.twitdx.customize.fragment;

import com.cookpadintern.twitdx.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = (View)inflater.inflate(R.layout.about, container, false);
		return view;
	}
}
