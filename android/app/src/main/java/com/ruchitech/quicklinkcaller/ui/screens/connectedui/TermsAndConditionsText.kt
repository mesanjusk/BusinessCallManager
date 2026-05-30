package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.helper.Constant.Urls.termsAndCond

@Composable
fun TermsAndConditionsText(clickName:String = "Next",onTermsClicked: () -> Unit) {
    val text = buildAnnotatedString {
        append("By clicking '$clickName', you agree to accept the ")
        pushStringAnnotation(tag = "Terms", annotation = termsAndCond) // Change URL as per your requirement
        withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
            append("Terms & Conditions")
        }
        pop()
        append(".")
    }

    ClickableText(
        text = text,
        onClick = { offset ->
            text.getStringAnnotations("Terms", offset, offset)
                .firstOrNull()?.let {
                    onTermsClicked()
                }
        },
        style = TextStyle(fontSize = 10.sp.nonScaledSp)
    )
}
