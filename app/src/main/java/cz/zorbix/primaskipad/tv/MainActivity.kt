package cz.zorbix.primaskipad.tv

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private lateinit var webView: WebView
    private var skipSeconds: Int = 60

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        setupWebView()
        webView.loadUrl("https://www.oneplay.cz")
    }

    private fun setupWebView() {
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectSkipScript()
            }
        }
    }

    private fun injectSkipScript() {
        val javascriptCode = """
            (function() {
                if (window.primaSkipAdInjected) return;
                window.primaSkipAdInjected = true;

                window.skipVideoAd = function(seconds) {
                    const videos = document.querySelectorAll('video');
                    if (videos.length === 0) return false;

                    videos.forEach(video => {
                        if (!isNaN(video.duration) && video.currentTime < video.duration) {
                            video.currentTime = Math.min(video.currentTime + seconds, video.duration);
                        }
                    });
                    return true;
                };

                setInterval(function() {
                    const skipButtons = document.querySelectorAll('[class*="skip"], [id*="skip"], button:contains("Přeskočit")');
                    skipButtons.forEach(btn => {
                        if (btn && typeof btn.click === 'function') {
                            btn.click();
                        }
                    });
                }, 1000);
            })();
        """.trimIndent()

        webView.evaluateJavascript(javascriptCode, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_BUTTON_R1 -> {
                performSkip()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (webView.canGoBack()) {
                    webView.goBack()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun performSkip() {
        Toast.makeText(this, "Přeskakuji $skipSeconds s...", Toast.LENGTH_SHORT).show()
        webView.evaluateJavascript("window.skipVideoAd($skipSeconds);", null)
    }
}
