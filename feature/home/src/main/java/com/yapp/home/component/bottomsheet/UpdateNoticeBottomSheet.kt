package com.yapp.home.component.bottomsheet

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yapp.designsystem.theme.OrbitTheme
import feature.home.R

private fun resolveVersionName(ctx: android.content.Context): String {
    return runCatching {
        val pm = ctx.packageManager
        val packageName = ctx.packageName
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, 0)
        }
        info.versionName ?: ""
    }.getOrDefault("")
}

private fun bannerUrl(versionName: String): String =
    "https://www.orbitalarm.net/images/aos/$versionName/update-banner.png"

@Composable
internal fun UpdateNoticeBottomSheet(
    onDontShowAgain: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    val versionName = remember(isPreview) {
        if (isPreview) "preview" else resolveVersionName(context)
    }
    val imageUrl = remember(versionName) { bannerUrl(versionName.ifEmpty { "unknown" }) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17191F).copy(alpha = 0.85f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = OrbitTheme.colors.gray_900,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                )
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { },
        ) {
            if (isPreview) {
                // 프리뷰용 박스
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(color = OrbitTheme.colors.white),
                )
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onDontShowAgain)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.update_notice_bottom_sheet_dont_show_again),
                        style = OrbitTheme.typography.body1SemiBold,
                        color = OrbitTheme.colors.white,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClose)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.update_notice_bottom_sheet_close),
                        style = OrbitTheme.typography.body1SemiBold,
                        color = OrbitTheme.colors.white,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun UpdateNoticeBottomSheetPreview() {
    OrbitTheme {
        UpdateNoticeBottomSheet(
            onDontShowAgain = {},
            onClose = {},
        )
    }
}
