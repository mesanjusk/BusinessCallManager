package com.ruchitech.quicklinkcaller.ui.screens.otp.ui

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.WebViewActivity
import com.ruchitech.quicklinkcaller.helper.Constant
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.CircularLoadingIndicator
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.PrivacyPolicyText
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.TermsAndConditionsText
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.otp.viewmodel.OtpRequestVM
import com.ruchitech.quicklinkcaller.ui.theme.TextFieldBg
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.montserrat
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_medium

@Composable
fun OtpRequestUi(viewModel: OtpRequestVM) {
    var mobileNumber by remember { mutableStateOf("") }
    var isMobileNumberError by remember { mutableStateOf(false) }
    val circularLoadingIndicator by viewModel.circularLoadingIndicator.collectAsState()
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), contentAlignment = Alignment.Center
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 10.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Image(
                        painter = painterResource(id = R.drawable.welcomeback_signin),
                        contentDescription = "mobile icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(250.dp)
                            .padding(0.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BasicTextField(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        value = mobileNumber,
                        onValueChange = {
                            isMobileNumberError = it.length != 10
                            mobileNumber = it
                            viewModel.mobileNumber.value = it
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {}
                        ),
                        textStyle = TextStyle(
                            fontFamily = montserrat,
                            fontSize = 12.sp.nonScaledSp
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                Modifier
                                    .background(TextFieldBg, RoundedCornerShape(percent = 18))
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 18.dp)
                            ) {
                                if (mobileNumber.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.enter_the_mobile_number),
                                        color = Color.Gray,
                                        fontSize = 12.sp.nonScaledSp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    // Login button
                    Button(
                        onClick = {
                            viewModel.checkValidation()
                        },
                        shape = RoundedCornerShape(percent = 18),
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(shape = RoundedCornerShape(percent = 0), elevation = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemePurple)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.next),
                                color = Color.White,
                                fontSize = 18.sp.nonScaledSp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = montserrat_medium
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(25.dp))
                    TermsAndConditionsText(onTermsClicked = {
                        val intent = Intent(context, WebViewActivity::class.java)
                        intent.putExtra("url", Constant.Urls.termsAndCond)
                        intent.putExtra("type", "Terms & Conditions")
                        context.startActivity(intent)
                    })
                    Spacer(modifier = Modifier.height(5.dp))
                    PrivacyPolicyText(onPrivacyPolicyClicked = {
                        val intent = Intent(context, WebViewActivity::class.java)
                        intent.putExtra("url", Constant.Urls.privacyPolicy)
                        intent.putExtra("type", "Privacy Policy")
                        context.startActivity(intent)
                    })

                }
            }
        }
        if (circularLoadingIndicator) CircularLoadingIndicator()
    }
}