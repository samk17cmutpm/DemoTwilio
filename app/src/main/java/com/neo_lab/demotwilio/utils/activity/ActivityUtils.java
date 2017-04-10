package com.neo_lab.demotwilio.utils.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by sam_nguyen on 10/04/2017.
 */

public class ActivityUtils {
    public static void addFragmentToActivity (FragmentManager fragmentManager, Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public static void startActivity (Activity from, Class to) {
        Intent intent = new Intent(from, to);
        from.startActivity(intent);
    }

    public static void startActivity (Activity from, Class to, boolean finish) {
        Intent intent = new Intent(from, to);
        from.startActivity(intent);
        if (finish)
            from.finish();
    }

    public static void startActivity (Activity from, Class to, String key, Parcelable parcelable) {
        Intent intent = new Intent(from, to);
        intent.putExtra(key, parcelable);
        from.startActivity(intent);
    }
}
