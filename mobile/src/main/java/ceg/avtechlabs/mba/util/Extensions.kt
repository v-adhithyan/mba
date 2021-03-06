package ceg.avtechlabs.mba.util

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.text.TextUtils
import android.util.Log
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import ceg.avtechlabs.mba.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.squareup.picasso.Picasso
import com.yarolegovich.lovelydialog.LovelyChoiceDialog
import com.yarolegovich.lovelydialog.LovelyInfoDialog
import kotlinx.android.synthetic.main.activity_notification_reader.*
import kotlinx.android.synthetic.main.activity_reader.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.microedition.khronos.opengles.GL

/**
 * Created by Adhithyan V on 23-05-2017.
 */

fun Context.internetAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null
}

fun getShareLinkIntent(context: Context, url: String): Intent? {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, context.resources.getString(R.string.text_share, url))
    return Intent.createChooser(intent,context.resources.getString(R.string.intent_share))
}

fun Context.showFeedPreferenceChooser() {
    val items = resources.getStringArray(ceg.avtechlabs.mba.R.array.Categories)
    LovelyChoiceDialog(this)
            .setTopColorRes(R.color.colorPrimary)

            .setTitle(R.string.input_choose_feed_preferences_title)
            .setMessage(R.string.input_choose_feed_preferences_message)
            .setItemsMultiChoice(items) { positions, items ->
                if(items.size == 0) {
                    val choice = TextUtils.join(",", items)
                    //Toast.makeText(this, choice, Toast.LENGTH_LONG).show()
                    storePreference(Globals.FEED_PREFERENCES, choice)
                    Toast.makeText(this,
                            "${getString(R.string.toast_notificatoon_opt)}" , Toast.LENGTH_LONG).show()
                    showFeedPreferenceChooser()
                } else {
                    val choice = TextUtils.join(",", items)
                    //Toast.makeText(this, choice, Toast.LENGTH_LONG).show()
                    storePreference(Globals.FEED_PREFERENCES, choice)
                    Toast.makeText(this, "${getString(R.string.toast_new_stories_notification_1)} " +
                            "${getPreference(Globals.FEED_PREFERENCES) as String} " +
                            "${getString(R.string.toast_new_stories_notification_2)}" , Toast.LENGTH_LONG).show()
                }

            }
            .setConfirmButtonText(getString(R.string.alert_confirm))
            .setCancelable(false)
            .show()
}

fun Context.storePreference(key: String, message: Any) {
    val preference = getSharedPreferences(Globals.DRONA_PREFERENCES, 0)
    val editor = preference.edit()

    if(message is Boolean) {
        editor.putBoolean(key, message)
    } else{
        editor.putString(key, message.toString())
    }

    editor.commit()
}


fun Context.getPreference(key: String): Any {
    val preference = getSharedPreferences(Globals.DRONA_PREFERENCES, 0)

    if(key == Globals.FIRST_RUN) {
        return preference.getBoolean(key, false)
    } else if(key == Globals.MAX_NOTIFICATIONS) {
        return preference.getInt(Globals.MAX_NOTIFICATIONS, 0)
    }

    return preference.getString(key, "")
}

fun Context.isNotificationAllowed(): Boolean {
    var count = this.getPreference(Globals.MAX_NOTIFICATIONS).toString().toInt()
    if (count < Globals.MAX_NOTIFICAITIONS_ALLOWED) {
        count++
        this.storePreference(Globals.MAX_NOTIFICATIONS, count)
        return true
    }

    return false
}

fun Context.decrementNotificationCount() {

    var count = this.getPreference(Globals.MAX_NOTIFICATIONS).toString().toInt()
    count--

    if(count < 0) {count = 0}
    this.storePreference(Globals.MAX_NOTIFICATIONS, count)
}

fun Context.loadInterstitialAd() {
    val interstitial = InterstitialAd(this)
    interstitial.adUnitId = getString(R.string.ad_unit_interstitial)
    val adRequest = AdRequest.Builder()
    interstitial.loadAd(adRequest.build())
    interstitial.adListener = object : AdListener() {
        override fun onAdLoaded() {
            // Call displayInterstitial() function
            if(interstitial.isLoaded) {
                interstitial.show()
            }

        }
    }
}

fun Context.showNoInternetDialog() {
    LovelyInfoDialog(this)
            .setTopColorRes(android.R.color.holo_blue_dark)
            //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
            .setTitle(R.string.alert_no_internet_title)
            .setMessage(R.string.alert_no_internet_message)
            .show();
}

fun Context.getReadingTime(content: String): String {
    val totalWords = content.trim().split(" ").size
    val wordsPerMinute = 275.toDouble()
    val wordsPerSecond = (wordsPerMinute / 60)
    val totalReadingTimeSeconds = totalWords / wordsPerSecond
    Log.d("READING TIME", "$totalReadingTimeSeconds totalReadingTimeSeconds")
    Log.d("READING TIME", "$wordsPerSecond wordsPerSecond")
    val readingTimeMinutes = Math.round(totalReadingTimeSeconds / 60.toDouble())

    // calculate remaining reading time
    val readingTimeSeconds = Math.round(totalReadingTimeSeconds - readingTimeMinutes * 60)

    if(readingTimeMinutes > 0) {
        return "$readingTimeMinutes ${getString(R.string.snack_rt)}"
    } else {
        return getString(R.string.snack_rt_less_than_one_min)
    }

}

fun ImageView.loadImage(imageUrl: String) {
    if(TextUtils.isEmpty(imageUrl) || imageUrl == null) {
        Picasso.with(context).load(R.mipmap.ic_launcher).into(this)
    } else {
        Picasso.with(context).load(imageUrl).into(this)
    }
}

fun ImageView.loadImage(context: Context, imageUrl: String, toolbar: CollapsingToolbarLayout, window: Window) {
    if(imageUrl == null || TextUtils.isEmpty(imageUrl)) {
        return;
    }

    Picasso.with(context).load(imageUrl).into(object: com.squareup.picasso.Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {

        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            setImageBitmap(bitmap)
            Palette.from(bitmap).generate(object: Palette.PaletteAsyncListener {
                override fun onGenerated(palette: Palette) {
                    val mutedColor = palette.getMutedColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    val mutedDarkColor = palette.getDarkMutedColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

                    toolbar.setContentScrimColor(mutedColor);
                    toolbar.setStatusBarScrimColor(mutedDarkColor);

                    @TargetApi(21)
                    window.statusBarColor = palette.getDarkVibrantColor(resources.getColor(R.color.colorPrimaryDark))
                }
            });
        }
    })
}