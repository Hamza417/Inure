package app.simple.inure.decorations.views;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.simple.inure.decorations.fastscroll.FastScrollWebView;

public final class MarkedView extends FastScrollWebView {
    
    private static final String TAG = MarkedView.class.getSimpleName();
    private static final String IMAGE_PATTERN = "!\\[(.*)]\\((.*)\\)";
    
    private String previewText;
    private boolean codeScrollDisable;
    
    public MarkedView(Context context) {
        this(context, null);
    }
    
    public MarkedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    @TargetApi (11)
    @SuppressLint ("SetJavaScriptEnabled")
    private void init() {
        // default browser is not called.
        setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                sendScriptAction();
            }
        });
        
        loadUrl("file:///android_asset/html/md_preview.html");
        
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAllowUniversalAccessFromFileURLs(true);
        getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }
    
    private void sendScriptAction() {
        evaluateJavascript(previewText, null);
    }
    
    /**
     * load Markdown text from file path.
     **/
    public void loadMDFilePath(String filePath) {
        loadMDFile(new File(filePath));
    }
    
    /**
     * load Markdown text from file.
     **/
    public void loadMDFile(File file) {
        String mdText = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            
            String readText = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((readText = bufferedReader.readLine()) != null) {
                stringBuilder.append(readText);
                stringBuilder.append("\n");
            }
            fileInputStream.close();
            mdText = stringBuilder.toString();
            
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException:" + e);
        } catch (IOException e) {
            Log.e(TAG, "IOException:" + e);
        }
        setMDText(mdText);
    }
    
    /**
     * set show the Markdown text.
     **/
    public void setMDText(String text) {
        text2Mark(text);
    }
    
    private void text2Mark(String mdText) {
        
        String bs64MdText = imgToBase64(mdText);
        String escMdText = escapeForText(bs64MdText);
        
        previewText = String.format("preview('%s', %b)", escMdText, isCodeScrollDisable());
        sendScriptAction();
    }
    
    private String escapeForText(String mdText) {
        String escText = mdText.replace("\n", "\\\\n");
        escText = escText.replace("'", "\\'");
        //in some cases the string may have "\r" and our view will show nothing,so replace it
        escText = escText.replace("\r", "");
        return escText;
    }
    
    private String imgToBase64(String mdText) {
        Pattern ptn = Pattern.compile(IMAGE_PATTERN);
        Matcher matcher = ptn.matcher(mdText);
        if (!matcher.find()) {
            return mdText;
        }
        
        String imgPath = matcher.group(2);
        if (isUrlPrefix(imgPath) || !isPathExChack(imgPath)) {
            return mdText;
        }
        String baseType = imgEx2BaseType(imgPath);
        if (baseType.equals("")) {
            // image load error.
            return mdText;
        }
        
        File file = new File(imgPath);
        byte[] bytes = new byte[(int) file.length()];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException:" + e);
        } catch (IOException e) {
            Log.e(TAG, "IOException:" + e);
        }
        String base64Img = baseType + Base64.encodeToString(bytes, Base64.NO_WRAP);
        
        return mdText.replace(imgPath, base64Img);
    }
    
    private boolean isUrlPrefix(String text) {
        return text.startsWith("http://") || text.startsWith("https://");
    }
    
    private boolean isPathExChack(String text) {
        return text.endsWith(".png")
                || text.endsWith(".jpg")
                || text.endsWith(".jpeg")
                || text.endsWith(".gif");
    }
    
    private String imgEx2BaseType(String text) {
        if (text.endsWith(".png")) {
            return "data:image/png;base64,";
        } else if (text.endsWith(".jpg") || text.endsWith(".jpeg")) {
            return "data:image/jpg;base64,";
        } else if (text.endsWith(".gif")) {
            return "data:image/gif;base64,";
        } else {
            return "";
        }
    }
    
    
    /* options */
    
    public void setCodeScrollDisable() {
        codeScrollDisable = true;
    }
    
    private boolean isCodeScrollDisable() {
        return codeScrollDisable;
    }
}
