package com.ruchitech.quicklinkcaller.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
val sfMediumFont = FontFamily(Font(R.font.sf_ui_display_medium))
val sfSemibold = FontFamily(Font(R.font.poppins_semibold))
val montserrat = FontFamily(Font(R.font.montserrat_regular))
val montserrat_medium = FontFamily(Font(R.font.montserrat_medium))
val montserrat_semibold = FontFamily(Font(R.font.montserrat_semibold))
val google_sans_medium = FontFamily(Font(R.font.googlesans_medium))
val google_sans_regular = FontFamily(Font(R.font.googlesans_regular))


var normalGoogleSansStyle = TextStyle(
    fontFamily = google_sans_regular,
    letterSpacing = 0.sp,
    color =dimBlack
)



