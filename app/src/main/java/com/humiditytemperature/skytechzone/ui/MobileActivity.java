package com.humiditytemperature.skytechzone.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.humiditytemperature.skytechzone.utils.BitmapsDrawable;
import com.humiditytemperature.skytechzone.R;
import com.humiditytemperature.skytechzone.databinding.ActivityMobileBinding;
import com.humiditytemperature.skytechzone.db.PreferencesOffline;

public class MobileActivity extends AppCompatActivity {
    private ActivityMobileBinding activityMobileBinding;
    private float temperature;
    PreferencesOffline prefs = null;
    private FrameLayout adContainerView;
    private AdView adView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        activityMobileBinding = DataBindingUtil.setContentView(this, R.layout.activity_mobile);
        if (this.prefs == null) {
            this.prefs = new PreferencesOffline(this);

        }


        switchBackground(BitmapsDrawable.getRoundedRectangleDrawableForColor(prefs.getBackgroundColor(), MobileActivity.this), prefs.getBackgroundColor());

        adContainerView = findViewById(R.id.ad_view_container);

        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        adContainerView.post(new Runnable() {
            @Override
            public void run() {
                loadBanner();
            }
        });

        activityMobileBinding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        updateBattery();
    }

    private void updateBattery() {
        float intExtra = (float) registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED")).getIntExtra("temperature", 0);
        this.temperature = intExtra;
        this.temperature = intExtra / 10.0f;
        updateDegrees();
    }

    private void updateDegrees() {
        try {
            activityMobileBinding.speedviewHumidity.updateSpeed((int) temperature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
        if (this.prefs == null) {
            this.prefs = new PreferencesOffline(this);

        }
        switchBackground(BitmapsDrawable.getRoundedRectangleDrawableForColor(prefs.getBackgroundColor(), MobileActivity.this), prefs.getBackgroundColor());

    }


    public void switchBackground(final Drawable newBg, final int baseColor) {

        activityMobileBinding.bgBottom.setBackgroundDrawable(newBg);

        ObjectAnimator a1 = ObjectAnimator.ofFloat(activityMobileBinding.bgTop, "alpha", 1.0f, 0.0f);
        a1.setDuration(500L);
        a1.addListener(new Animator.AnimatorListener() {


            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                activityMobileBinding.bgTop.setBackgroundDrawable(newBg);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        ObjectAnimator a2 = ObjectAnimator.ofFloat(activityMobileBinding.bgBottom, "alpha", 0.0f, 1.0f);
        a2.setDuration(500L);
        a1.start();
        a2.start();


    }
    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }



    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void loadBanner() {
        // Create an ad request.
        adView = new AdView(this);
        adView.setAdUnitId(this.getResources().getString(R.string.admob_banner_ad_unit_id));
        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);

        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = outMetrics.density;

        float adWidthPixels = adContainerView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }

        int adWidth = (int) (adWidthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
}