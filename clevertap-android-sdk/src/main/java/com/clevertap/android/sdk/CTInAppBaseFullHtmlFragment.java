package com.clevertap.android.sdk;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import java.net.URLDecoder;

public abstract class CTInAppBaseFullHtmlFragment extends CTInAppBaseFullFragment {
    private CTInAppWebView webView;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return displayHTMLView(inflater,container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reDrawInApp();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View displayHTMLView(LayoutInflater inflater, ViewGroup container){
        View inAppView;
        try {
            inAppView = inflater.inflate(R.layout.inapp_html_full, container, false);
            RelativeLayout rl = inAppView.findViewById(R.id.inapp_html_full_relative_layout);
            RelativeLayout.LayoutParams webViewLp = new RelativeLayout
                    .LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            webViewLp.addRule(RelativeLayout.CENTER_IN_PARENT);

            initWebViewLayoutParams(webViewLp);
            webView = new CTInAppWebView(getActivity().getBaseContext(), inAppNotification.getWidth(), inAppNotification.getHeight(), inAppNotification.getWidthPercentage(), inAppNotification.getHeightPercentage());
            InAppWebViewClient webViewClient = new InAppWebViewClient();
            webView.setWebViewClient(webViewClient);

            if (isDarkenEnabled())
                rl.setBackgroundDrawable(new ColorDrawable(0xBB000000));
            else
                rl.setBackgroundDrawable(new ColorDrawable(0x00000000));

            rl.addView(webView, webViewLp);

            if (isCloseButtonEnabled()) {
                closeImageView = new CloseImageView(getActivity().getBaseContext());
                RelativeLayout.LayoutParams closeIvLp = new RelativeLayout
                        .LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
                        RelativeLayout.LayoutParams.FILL_PARENT);
                // Position it at the top right corner
                closeIvLp.addRule(RelativeLayout.ABOVE, webView.getId());
                closeIvLp.addRule(RelativeLayout.RIGHT_OF, webView.getId());


                int sub = getScaledPixels(Constants.INAPP_CLOSE_IV_WIDTH) / 2;
                closeIvLp.setMargins(-sub, 0, 0, -sub);

                closeImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        didDismiss(null);
                    }
                });
                rl.addView(closeImageView, closeIvLp);
            }

        }catch (Throwable t){
            config.getLogger().verbose(config.getAccountId(),"Fragment view not created",t);
            return null;
        }
        return inAppView;
    }

    private boolean isCloseButtonEnabled() {
        return inAppNotification.isShowClose();
    }

    private boolean isDarkenEnabled() {
        return inAppNotification.isDarkenScreen();
    }

    private class InAppWebViewClient extends WebViewClient {
        InAppWebViewClient() {
            super();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            final Bundle formData;
            try {
                formData = UriHelper.getAllKeyValuePairs(url, false);

                if (formData != null && formData.containsKey("wzrk_c2a")) {
                    final String c2a = formData.getString("wzrk_c2a");
                    if (c2a != null) {
                        final String[] parts = c2a.split("__dl__");
                        if (parts.length == 2) {
                            // Decode it here as wzrk_c2a is not decoded by UriHelper
                            formData.putString("wzrk_c2a", URLDecoder.decode(parts[0], "UTF-8"));
                            url = parts[1];
                        }
                    }
                }

                didClick(formData);
                Logger.d("Executing call to action for in-app: " + url);
                fireUrlThroughIntent(url,formData);
            } catch (Throwable t) {
                Logger.v("Error parsing the in-app notification action!", t);
            }
            return true;
        }
    }

    private void initWebViewLayoutParams(RelativeLayout.LayoutParams params) {
        char pos = inAppNotification.getPosition();
        switch (pos) {
            case Constants.INAPP_POSITION_TOP:
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case Constants.INAPP_POSITION_LEFT:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                break;
            case Constants.INAPP_POSITION_BOTTOM:
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case Constants.INAPP_POSITION_RIGHT:
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                break;
            case Constants.INAPP_POSITION_CENTER:
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                break;
        }
        params.setMargins(0, 0, 0, 0);
    }

    private void reDrawInApp() {
        webView.updateDimension();

        int mHeight = webView.dim.y;
        int mWidth = webView.dim.x;

        float d = getResources().getDisplayMetrics().density;
        mHeight /= d;
        mWidth /= d;

        String html = inAppNotification.getHtml();

        String style = "<style>body{width:" + mWidth + "px; height: " + mHeight + "px}</style>";
        html = html.replaceFirst("<head>", "<head>" + style);
        Logger.v("Density appears to be " + d);

        webView.setInitialScale((int) (d * 100));
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reDrawInApp();
    }


}
