package com.codconfirm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.codconfirm.app.presentation.designsystem.CodConfirmTheme
import com.codconfirm.app.presentation.navigation.CodConfirmNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodConfirmTheme {
                CodConfirmNavHost()
            }
        }
    }
}
