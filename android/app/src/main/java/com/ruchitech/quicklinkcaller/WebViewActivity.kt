package com.ruchitech.quicklinkcaller

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.WebViewContent
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold

class WebViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       val url = intent.getStringExtra("url")
        val type = intent.getStringExtra("type")
        setContent {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { finish() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = null
                        )
                    }

                    Text(
                        text = type ?: "",
                        fontSize = 16.sp.nonScaledSp,
                        color = Color.Black,
                        fontFamily = montserrat_semibold,
                        modifier = Modifier.padding(start = 15.dp)
                    )

                }
                Spacer(modifier = Modifier.height(5.dp))
                Divider(thickness = 1.dp, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(5.dp))
                WebViewPage(url = "$url")
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPage(url: String){

    val mutableStateTrigger = remember { mutableStateOf(false) }

    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()

                // to play video on a web view
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                webViewClient = object : WebViewClient() {

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        Log.e("kmjhuygftd", "onReceivedError: $error")
                      //  loadURL = "file:///android_asset/404.html"
                        //mutableStateTrigger.value = true
                    }
                }

                loadUrl(url)
            }
        }, update = {
            Log.e("kmjhuygftd", "WebViewPage: $url")
            it.loadUrl(url)
        })



}