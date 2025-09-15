package com.yapp.home.component.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yapp.designsystem.theme.OrbitTheme
import feature.home.R

const val BANNER_IMAGE_URL = "https://www.orbitalarm.net/images/aos/1.1.3/update-banner.png"

@Composable
internal fun UpdateNoticeBottomSheet(
    onDontShowAgain: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .clickable { onClose },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = OrbitTheme.colors.gray_900,
                    shape = RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp,
                    ),
                )
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp,
                    ),
                ),

        ) {
            if (LocalInspectionMode.current) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.0f)
                        .background(
                            color = OrbitTheme.colors.white,
                        ),
                )
            } else {
                AsyncImage(
                    model = BANNER_IMAGE_URL,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp,
                        bottom = 20.dp,
                        start = 20.dp,
                        end = 20.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onDontShowAgain
                        }
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
                        .clickable {
                            onClose
                        }
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
