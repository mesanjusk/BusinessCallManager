import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.montserrat
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import com.ruchitech.quicklinkcaller.ui.theme.sfSemibold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SaveContactUi(
    number: String?,
    nameStr: String? = "",
    onClose: () -> Unit,
    onSave: (name: String, number: String) -> Unit,
    onFocusChangesForName: (isFocused: Boolean) -> Unit,
    callingByService: Boolean = false,
    contactHelper: ContactHelper
) {
    var name by remember { mutableStateOf(nameStr ?: "") }
    var phoneNumber by remember { mutableStateOf(number ?: "") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val isNameValid = name.isNotBlank()
    val isPhoneValid = phoneNumber.isNotBlank()

    var nameFieldValue by remember {
        mutableStateOf(TextFieldValue(text = name, selection = TextRange(name.length)))
    }
    val nameFocusRequester = remember { FocusRequester() }
    val phoneFocusRequester = remember { FocusRequester() }

    if (!callingByService) {
        BackHandler { onClose() }
    }

    LaunchedEffect(number) {
        withContext(Dispatchers.IO) {
            val contactDetails = contactHelper.getContactDetailsByPhoneNumber(number ?: "")
            if (nameStr.isNullOrEmpty() &&
                contactDetails.displayName.isNotEmpty() &&
                contactDetails.displayName != "Unknown"
            ) {
                name = contactDetails.displayName
                nameFieldValue = nameFieldValue.copy(
                    text = name,
                    selection = TextRange(name.length)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        nameFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .widthIn(max = 520.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x33000000))
            )
            IconButton(
                onClick = { onClose() },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF6B7280)
                )
            }
        }

        Text(
            text = "Save to contacts",
            style = TextStyle(
                fontSize = 18.sp.nonScaledSp,
                fontFamily = sfSemibold,
                color = Color(0xFF0F172A)
            ),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "Add a quick label and number for this caller.",
            style = TextStyle(
                fontSize = 13.sp.nonScaledSp,
                fontFamily = montserrat,
                color = Color(0xFF6B7280)
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        TextField(
            value = nameFieldValue,
            onValueChange = {
                if (it.text.length <= 50) {
                    nameFieldValue = it
                    name = it.text
                }
            },
            label = { Text("Name") },
            placeholder = { Text("Contact name") },
            textStyle = TextStyle(
                fontFamily = montserrat,
                fontSize = 15.sp.nonScaledSp,
                color = Color(0xFF0F172A)
            ),
            isError = !isNameValid,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(
                onNext = { phoneFocusRequester.requestFocus() }
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF)
                )
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nameFocusRequester)
                .onFocusChanged { onFocusChangesForName(it.isFocused) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF4F6FA),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = PurpleSolid
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone") },
            placeholder = { Text("e.g. +1 202 555 0142") },
            textStyle = TextStyle(
                fontFamily = montserrat,
                fontSize = 15.sp.nonScaledSp,
                color = Color(0xFF0F172A)
            ),
            isError = !isPhoneValid,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Phone
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isNameValid && isPhoneValid) {
                        onSave(name, phoneNumber)
                    }
                }
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Phone,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF)
                )
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(phoneFocusRequester),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF4F6FA),
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                cursorColor = PurpleSolid
            )
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(thickness = 1.dp, color = Color(0x11000000))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(
                onClick = { onClose() },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = montserrat_semibold,
                    fontSize = 14.sp.nonScaledSp,
                    color = Color(0xFF6B7280)
                )
            }
            Button(
                onClick = {
                    if (isNameValid && isPhoneValid) {
                        onSave(name, phoneNumber)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleSolid,
                    contentColor = Color.White
                ),
                enabled = isNameValid && isPhoneValid
            ) {
                Text(
                    text = "Save",
                    fontFamily = montserrat_semibold,
                    fontSize = 14.sp.nonScaledSp
                )
            }
        }
    }
}
