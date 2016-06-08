package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScreenUtils {

	private Context context;

	@Inject
	public ScreenUtils(Context context) {
		this.context = context;
	}

	public int dpToPx(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	public int getWidth() {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.widthPixels;
	}

	public int getHeight() {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.heightPixels;
	}

	public float getDensity() {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.densityDpi;
	}

	public int getOrientation() {
		return context.getResources().getConfiguration().orientation;
	}
}
