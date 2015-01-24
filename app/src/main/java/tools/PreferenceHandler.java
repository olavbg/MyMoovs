package tools;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHandler {
    private static final String MyPREFERENCES = "MyPrefs";
    private final SharedPreferences sharedpreferences;

    public PreferenceHandler(Context context) {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    public void putData(String key, String value) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getData(String key) {
        return sharedpreferences.getString(key, "");
    }
}
