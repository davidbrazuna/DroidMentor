package com.davidbrazuna.droidmentor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.davidbrazuna.droidmentor.ui.chat.ChatRoute
import com.davidbrazuna.droidmentor.ui.theme.DroidMentorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                ChatRoute()
            }
        }
    }
}
