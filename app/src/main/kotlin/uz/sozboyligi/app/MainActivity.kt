package uz.sozboyligi.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var splashView: FrameLayout
    private var splashDone = false
    private val handler = Handler(Looper.getMainLooper())

    // Ranglar (asl ilovadagi)
    private val colorBg     = "#09090f"
    private val colorGreen  = "#34d399"
    private val colorBlue   = "#60a5fa"
    private val colorGray   = "#94a3b8"
    private val colorPurple = "#a78bfa"
    private val colorViolet = "#c4b5fd"
    private val colorRed    = "#f87171"

    private val siteUrl = "https://soz-boyligi.zya.me"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.parseColor(colorBg))

        // ---- WebView ----
        webView = WebView(this)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = false
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
        }
        webView.setBackgroundColor(Color.parseColor(colorBg))
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: ConsoleMessage) = true
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                hideSplash()
            }
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                hideSplash()
                if (request.isForMainFrame) {
                    // Foydalanuvchiga sayt ko'rinmasin – bo'sh sahifa yuklaymiz
                    view.loadUrl("about:blank")
                    showNoInternetDialog()
                }
            }
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: android.net.http.SslError
            ) {
                handler.proceed()
            }
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean = false
        }

        root.addView(
            webView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // ---- Splash ----
        splashView = buildSplash()
        root.addView(splashView)

        setContentView(root)

        webView.loadUrl(siteUrl)
    }

    // ------------------------------------------------------------------ //
    //  Internet yo'q bo'lganda dialog                                      //
    // ------------------------------------------------------------------ //
    private fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setMessage("Internetga ulaning")
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                if (isNetworkAvailable()) {
                    webView.visibility = View.VISIBLE
                    webView.loadUrl(siteUrl)
                } else {
                    // Internet hali yo'q – dialogni yana ko'rsatamiz
                    showNoInternetDialog()
                }
            }
            .show()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ------------------------------------------------------------------ //
    //  Splash screen                                                        //
    // ------------------------------------------------------------------ //
    private fun buildSplash(): FrameLayout {
        val splash = FrameLayout(this)
        splash.setBackgroundColor(Color.parseColor(colorBg))

        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(dm)

        // Logo rasmini assets dan yuklash
        val logoParams = FrameLayout.LayoutParams(
            (dm.widthPixels * 0.75).roundToInt(),
            (dm.widthPixels * 0.50).roundToInt()
        ).apply { gravity = Gravity.CENTER }

        val logoView = ImageView(this)
        logoView.scaleType = ImageView.ScaleType.FIT_CENTER
        try {
            val stream = assets.open("logo.png")
            val bitmap = BitmapFactory.decodeStream(stream)
            logoView.setImageDrawable(android.graphics.drawable.BitmapDrawable(resources, bitmap))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        splash.addView(logoView, logoParams)

        // Loading bar (pastki qismda)
        val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        bar.isIndeterminate = true
        val barParams = FrameLayout.LayoutParams(
            (dm.widthPixels * 0.5).roundToInt(),
            dpToPx(4)
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = dpToPx(48)
        }
        bar.progressDrawable?.setColorFilter(
            Color.parseColor(colorGreen),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        bar.indeterminateDrawable?.setColorFilter(
            Color.parseColor(colorGreen),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        splash.addView(bar, barParams)

        return splash
    }

    private fun hideSplash() {
        if (splashDone) return
        splashDone = true
        splashView.animate()
            .alpha(0f)
            .setDuration(400)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .withEndAction { splashView.visibility = View.GONE }
            .start()
    }

    // ------------------------------------------------------------------ //
    //  Utility                                                             //
    // ------------------------------------------------------------------ //
    private fun dpToPx(dp: Int): Int {
        val dm = resources.displayMetrics
        return (dp * dm.density).roundToInt()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}
