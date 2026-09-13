package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.OffWhiteMuted
import com.example.ui.theme.OffWhitePrimary

/**
 * Minimalist stats card widget using the strict charcoal & off-white aesthetic.
 */
@Composable
fun QuickStatCard(
  value: String,
  label: String,
  microcopy: String,
  modifier: Modifier = Modifier,
  tag: String = "stat_card"
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(CharcoalSurface)
      .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
      .padding(16.dp)
      .testTag(tag)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        color = OffWhitePrimary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = OffWhitePrimary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = microcopy,
        style = MaterialTheme.typography.bodySmall,
        color = OffWhiteMuted
      )
    }
  }
}
