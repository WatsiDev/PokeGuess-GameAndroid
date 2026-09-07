package com.watsidev.pokeguessredux.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.color.ColorProvider
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.watsidev.pokeguessredux.MainActivity
import com.watsidev.pokeguessredux.R
import com.watsidev.pokeguessredux.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class StreakGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val userPreferences = UserPreferencesRepository(context.applicationContext)
        val streak = userPreferences.currentStreak.first()

        val streakLabel = context.getString(R.string.widget_streak_label)
        val streakText = context.getString(R.string.widget_current_streak, streak)

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.streak_widget_background))
                    .cornerRadius(20.dp)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(start = 16.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = streakLabel,
                            style = TextStyle(
                                color = ColorProvider(
                                    day = Color(0xFFFFFFFF),
                                    night = Color(0xFFFFFFFF)
                                ),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = streakText,
                            style = TextStyle(
                                color = ColorProvider(
                                    day = Color(0xFFDAFFC9),
                                    night = Color(0xFFDAFFC9)
                                ),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Image(
                        provider = ImageProvider(R.drawable.ic_gengar),
                        contentDescription = null,
                        modifier = GlanceModifier.width(130.dp).height(130.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

class StreakGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakGlanceWidget()
}