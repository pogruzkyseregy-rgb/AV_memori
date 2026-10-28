package com.avmemori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.model.sampleBlocks
import com.avmemori.ui.feed.FeedScreen
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.AvTheme
import com.avmemori.ui.theme.NoteFont

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AvTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AvColors.Paper) {
                    Column {
                        Text(
                            text = "наш малыш",
                            modifier = Modifier.padding(start = 20.dp, top = 48.dp, bottom = 4.dp),
                            fontFamily = NoteFont,
                            fontStyle = FontStyle.Normal,
                            fontSize = 26.sp,
                            color = AvColors.Ink,
                        )
                        FeedScreen(blocks = sampleBlocks)
                    }
                }
            }
        }
    }
}
