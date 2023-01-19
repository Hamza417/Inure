//From the desk of Frank P. Westlake; public domain.
package app.simple.inure.terminal.shortcuts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.security.GeneralSecurityException;

import app.simple.inure.R;
import app.simple.inure.decorations.typeface.TypeFaceEditText;
import app.simple.inure.extensions.activities.TransparentBaseActivity;
import app.simple.inure.terminal.RemoteInterface;
import app.simple.inure.terminal.RunShortcut;
import app.simple.inure.terminal.TermDebug;
import app.simple.inure.terminal.compat.PRNGFixes;
import app.simple.inure.terminal.util.ShortcutEncryption;
import app.simple.inure.util.TypeFace;

public class AddShortcut extends TransparentBaseActivity {
    private final int OP_MAKE_SHORTCUT = 1;
    private final Context context = this;
    private final EditText[] editTexts = new EditText[5];
    private int ix = 0;
    private final int PATH = ix++, ARGS = ix++, NAME = ix++;
    private SharedPreferences sharedPreferences;
    private String path;
    private String name = "";
    private final String[] iconText = {"", null};
    
    //////////////////////////////////////////////////////////////////////
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String action = getIntent().getAction();
        if (action != null && action.equals("android.intent.action.CREATE_SHORTCUT")) {
            makeShortcut();
        } else {
            finish();
        }
    }
    
    //////////////////////////////////////////////////////////////////////
    void makeShortcut() {
        if (path == null) {
            path = "";
        }
        final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(25, 25, 25, 25);
        for (int i = 0, n = editTexts.length; i < n; i++) {
            editTexts[i] = new TypeFaceEditText(context);
            editTexts[i].setTypeface(TypeFace.INSTANCE.getRegularTypeFace(context));
            editTexts[i].setSingleLine(true);
        }
        if (!path.equals("")) {
            editTexts[0].setText(path);
        }
        editTexts[PATH].setHint(getString(R.string.addshortcut_command_hint));//"command");
        editTexts[NAME].setText(name);
        editTexts[ARGS].setHint(getString(R.string.addshortcut_example_hint));//"--example=\"a\"");
        editTexts[ARGS].setOnFocusChangeListener((view, focus) -> {
            if (!focus) {
                String s;
                if (editTexts[NAME].getText().toString().equals("") && !(s = editTexts[ARGS].getText().toString()).equals("")) {
                    editTexts[NAME].setText(s.split("\\s")[0]);
                }
            }
        });
    
        //        MaterialButton buttonPath = new MaterialButton(context);
        //        buttonPath.setText(getString(R.string.addshortcut_button_find_command));//"Find command");
        //        buttonPath.setTypeface(TypeFace.INSTANCE.getBoldTypeFace(context));
        //        buttonPath.setOnClickListener(p1 -> {
        //            String lastPath = sharedPreferences.getString("lastPath", null);
        //            File get = (lastPath == null) ? Environment.getExternalStorageDirectory() : new File(lastPath).getParentFile();
        //            Intent pickerIntent = new Intent();
        //            if (sharedPreferences.getBoolean("useInternalScriptFinder", false)) {
        //                pickerIntent.setClass(getApplicationContext(), FSNavigator.class)
        //                        .setData(Uri.fromFile(get))
        //                        .putExtra("title", getString(R.string.addshortcut_navigator_title));//"SELECT SHORTCUT TARGET")
        //            } else {
        //                pickerIntent
        //                        .putExtra("CONTENT_TYPE", "*/*")
        //                        .setAction(Intent.ACTION_PICK);
        //            }
        //            startActivityForResult(pickerIntent, OP_MAKE_SHORTCUT);
        //        });
    
        linearLayout.addView(layoutTextViewH(
                getString(R.string.addshortcut_command_window_instructions)//"Command window requires full path, no arguments. For other commands use Arguments window (ex: cd /sdcard)."
                , null
                , false));
    
        linearLayout.addView(layoutTextViewH(getString(R.string.addshortcut_command_hint), editTexts[PATH]));
        linearLayout.addView(layoutTextViewH(getString(R.string.addshortcut_arguments_label), editTexts[ARGS]));
        linearLayout.addView(layoutTextViewH(getString(R.string.addshortcut_shortcut_label), editTexts[NAME]));
    
        final ImageView img = new ImageView(context);
        img.setImageResource(R.mipmap.ic_terminal);
        img.setMaxHeight(100);
        img.setTag(0xFFFFFFFF);
        img.setMaxWidth(100);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    
        //        final MaterialButton btn_color = new MaterialButton(context);
        //        btn_color.setText(getString(R.string.addshortcut_button_text_icon));//"Text icon");
        //        btn_color.setTypeface(TypeFace.INSTANCE.getBoldTypeFace(context));
        //        btn_color.setOnClickListener(p1 -> new ColorValue(context, img, iconText));
        //        linearLayout.addView(layoutTextViewH(
        //                getString(R.string.addshortcut_text_icon_instructions)//"Optionally create a text icon:"
        //                , null
        //                , false));
        //        linearLayout.addView(layoutViewViewH(btn_color, img));
    
        final ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.addView(linearLayout);
    
        alert.setView(scrollView);
        alert.setTitle(getString(R.string.activity_shortcut_create));//"Term Shortcut");
        alert.setPositiveButton(android.R.string.yes, (dialog, which) -> buildShortcut(
                path,
                editTexts[ARGS].getText().toString(),
                editTexts[NAME].getText().toString(),
                iconText[1],
                (Integer) img.getTag()));
    
        alert.setNegativeButton(android.R.string.cancel, (dialog, which) -> finish());
        alert.show();
    }
    
    //////////////////////////////////////////////////////////////////////
    LinearLayout layoutTextViewH(String text, View vw) {
        return (layoutTextViewH(text, vw, true));
    }
    
    LinearLayout layoutTextViewH(String text, View view, boolean attributes) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        TextView textView = new TextView(context);
        textView.setText(text);
        if (attributes) {
            textView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (attributes) {
            textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }
        textView.setPadding(10, textView.getPaddingTop(), 10, textView.getPaddingBottom());
        LinearLayout lh = new LinearLayout(context);
        lh.setOrientation(LinearLayout.VERTICAL);
        lh.addView(textView, layoutParams);
        if (view != null) {
            lh.addView(view, layoutParams);
        }
        return (lh);
    }
    
    //////////////////////////////////////////////////////////////////////
    LinearLayout layoutViewViewH(View vw1, View vw2) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout lh = new LinearLayout(context);
        lh.setOrientation(LinearLayout.HORIZONTAL);
        lh.addView(vw1, lp);
        if (vw2 != null) {
            lh.addView(vw2, lp);
        }
        return (lh);
    }
    
    //////////////////////////////////////////////////////////////////////
    void buildShortcut(String path, String arguments, String shortcutName, String shortcutText, int shortcutColor) {
        // Apply workarounds for SecureRandom bugs in Android < 4.4
        PRNGFixes.apply();
        ShortcutEncryption.Keys keys = ShortcutEncryption.getKeys(context);
        if (keys == null) {
            try {
                keys = ShortcutEncryption.generateKeys();
            } catch (
                    GeneralSecurityException e) {
                Log.e(TermDebug.LOG_TAG, "Generating shortcut encryption keys failed: " + e);
                throw new RuntimeException(e);
            }
            ShortcutEncryption.saveKeys(context, keys);
        }
        StringBuilder cmd = new StringBuilder();
        if (path != null && !path.equals("")) {
            cmd.append(RemoteInterface.quoteForBash(path));
        }
        if (arguments != null && !arguments.equals("")) {
            cmd.append(" ").append(arguments);
        }
        String cmdStr = cmd.toString();
        String cmdEnc;
        try {
            cmdEnc = ShortcutEncryption.encrypt(cmdStr, keys);
        } catch (
                GeneralSecurityException e) {
            Log.e(TermDebug.LOG_TAG, "Shortcut encryption failed: " + e);
            throw new RuntimeException(e);
        }
        Intent target = new Intent().setClass(context, RunShortcut.class);
        target.setAction(RunShortcut.ACTION_RUN_SHORTCUT);
        target.putExtra(RunShortcut.EXTRA_SHORTCUT_COMMAND, cmdEnc);
        target.putExtra(RunShortcut.EXTRA_WINDOW_HANDLE, shortcutName);
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent wrapper = new Intent();
        wrapper.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        wrapper.putExtra(Intent.EXTRA_SHORTCUT_INTENT, target);
        if (shortcutName != null && !shortcutName.equals("")) {
            wrapper.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        }
        if (shortcutText != null && !shortcutText.equals("")) {
            wrapper.putExtra(
                    Intent.EXTRA_SHORTCUT_ICON
                    , TextIcon.getTextIcon(
                            shortcutText
                            , shortcutColor
                            , 96
                            , 96
                                          )
                            );
        } else {
            wrapper.putExtra(
                    Intent.EXTRA_SHORTCUT_ICON_RESOURCE
                    , Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_terminal)
                            );
        }
        setResult(RESULT_OK, wrapper);
        finish();
    }
    
    //////////////////////////////////////////////////////////////////////
    @SuppressLint ("ApplySharedPref")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        path = null;
        if (requestCode == OP_MAKE_SHORTCUT) {
            if (data != null && (uri = data.getData()) != null && (path = uri.getPath()) != null) {
                sharedPreferences.edit().putString("lastPath", path).commit();
                editTexts[PATH].setText(path);
                name = path.replaceAll(".*/", "");
                if (editTexts[NAME].getText().toString().equals("")) {
                    editTexts[NAME].setText(name);
                }
                if (iconText[0] != null && iconText[0].equals("")) {
                    iconText[0] = name;
                }
            } else {
                finish();
            }
        }
    }
    //////////////////////////////////////////////////////////////////////
}
