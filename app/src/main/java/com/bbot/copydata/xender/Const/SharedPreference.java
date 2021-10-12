package com.bbot.copydata.xender.Const;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    public static final String MyPREFERENCES = "File Sharing";
    public static String LOGIN = "LOGIN";

    public static boolean getLogin(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        boolean ans = sharedpreferences.getBoolean(LOGIN, false);
        return ans;
    }

    public static void setLogin(Context c1, boolean value) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(LOGIN, value);
        editor.apply();
    }
}
