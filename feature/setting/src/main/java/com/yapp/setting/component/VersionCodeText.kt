package com.yapp.setting.component

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.yapp.designsystem.theme.OrbitTheme

@Composable
fun VersionCodeText() {
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        "1.0.0"
    }

    Text(
        text = "v$versionName",
        modifier = Modifier.fillMaxWidth(),
        style = OrbitTheme.typography.body1Regular,
        color = OrbitTheme.colors.gray_300,
        textAlign = TextAlign.Center,
    )
}
