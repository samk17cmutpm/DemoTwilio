package com.neo_lab.demotwilio.utils.toolbar;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.neo_lab.demotwilio.R;

/**
 * Created by sam_nguyen on 10/04/2017.
 */

public class ToolbarUtils {

    public static void initialize(Toolbar toolbar, Activity activity, int title) {
        ((AppCompatActivity) activity).setSupportActionBar(toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);

        if (title != 0) {
            TextView titleToolbar = (TextView) toolbar.findViewById(R.id.toolbar_title);
            titleToolbar.setText(title);
        }
        ((AppCompatActivity)activity).getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public static void initialize(Toolbar toolbar, Activity activity, String title, int icon) {
        ((AppCompatActivity) activity).setSupportActionBar(toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        if (icon != 0) {
            ((AppCompatActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(icon);
        }

        if (!title.isEmpty()) {
            TextView titleToolbar = (TextView) toolbar.findViewById(R.id.toolbar_title);
            titleToolbar.setText(title);
        }
        ((AppCompatActivity)activity).getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public static void initialize(Toolbar toolbar, Activity activity, int title, int icon) {
        ((AppCompatActivity) activity).setSupportActionBar(toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        if (icon != 0) {
            ((AppCompatActivity) activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(icon);
        }

        if (title != 0) {
            TextView titleToolbar = (TextView) toolbar.findViewById(R.id.toolbar_title);
            titleToolbar.setText(title);
        }
        ((AppCompatActivity)activity).getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
}
