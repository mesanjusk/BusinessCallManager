package com.ruchitech.quicklinkcaller.ui.screens.home.screen

import SaveContactUi
import android.os.Handler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ruchitech.quicklinkcaller.R
import com.ruchitech.quicklinkcaller.contactutills.ContactHelper
import com.ruchitech.quicklinkcaller.helper.Event
import com.ruchitech.quicklinkcaller.helper.EventEmitter
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowAlignment
import com.ruchitech.quicklinkcaller.helper.bubble.ArrowShape
import com.ruchitech.quicklinkcaller.helper.copyToClipboard
import com.ruchitech.quicklinkcaller.helper.formatDate
import com.ruchitech.quicklinkcaller.helper.openWhatsapp
import com.ruchitech.quicklinkcaller.helper.saveNumberToContacts
import com.ruchitech.quicklinkcaller.helper.sendTextMessage
import com.ruchitech.quicklinkcaller.helper.shareContact
import com.ruchitech.quicklinkcaller.room.data.Contact
import com.ruchitech.quicklinkcaller.ui.screens.callerid.ui.Contact
import com.ruchitech.quicklinkcaller.ui.screens.connectedui.nonScaledSp
import com.ruchitech.quicklinkcaller.ui.screens.home.viewmodel.HomeVm
import com.ruchitech.quicklinkcaller.ui.theme.Orange
import com.ruchitech.quicklinkcaller.ui.theme.PurpleSolid
import com.ruchitech.quicklinkcaller.ui.theme.TextColor
import com.ruchitech.quicklinkcaller.ui.theme.ThemePurple
import com.ruchitech.quicklinkcaller.ui.theme.montserrat
import com.ruchitech.quicklinkcaller.ui.theme.montserrat_semibold
import com.ruchitech.quicklinkcaller.helper.bubble.bubble
import com.ruchitech.quicklinkcaller.helper.bubble.rememberBubbleState
import com.ruchitech.quicklinkcaller.helper.hasAllRequiredPermissions
import com.ruchitech.quicklinkcaller.ui.screens.home.screen.ShareContact
import com.ruchitech.quicklinkcaller.ui.theme.dimBlack
import com.ruchitech.quicklinkcaller.ui.theme.normalGoogleSansStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ShowContactsUi(viewModel: HomeVm) {
    // Collect the Flow of paginated and sorted contacts
    val contacts by viewModel.contacts.collectAsState()
    val searchContacts by viewModel.searchContacts.collectAsState()
    //  val query by remember { mutableStateOf("") }
    var showSaveInappDialog by remember {
        mutableStateOf(false)
    }
    var contactForEdit by remember {
        mutableStateOf<Contact?>(null)
    }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    if (showSaveInappDialog) {
        Dialog(onDismissRequest = { showSaveInappDialog = false }) {
            SaveContactUi(contactForEdit?.contact_mobile, contactForEdit?.contact_title, onClose = {
                showSaveInappDialog = false
            }, { name, number ->
                showSaveInappDialog = false
                viewModel.readyForEdit(
                    contactForEdit?.copy(
                        contact_title = name,
                        contact_mobile = number
                    )
                )
            }, onFocusChangesForName = {}, contactHelper = viewModel.contactHelper)
        }
    }
    val context = LocalContext.current
    /* SwipeRefresh(
         state = swipeRefreshState,
         onRefresh = {
             scope.launch {
                 swipeRefreshState.isRefreshing = true
                 viewModel.updateContacts()
                 delay(1500)
                 viewModel.reinitCallLogs()
                 swipeRefreshState.isRefreshing = false
             }
         }
     ) {*/
    LazyColumn(modifier = Modifier.fillMaxSize(), state) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            /*
                        CustomSearchBar(
                            emptyMsg = "Search name, number...",
                            query = query,
                            onQueryChange = { newQuery ->
                                query = newQuery
                                if (newQuery.length > 2) {
                                    viewModel.searchContacts(newQuery)
                                } else if (newQuery.isEmpty()) {
                                    viewModel.searchContacts(newQuery)
                                }

                            },
                            onSearch = {
                                // Handle search action
                                // You can perform the search operation here
                                // using the 'query' value.
                            },
                            onClear = {
                                query = ""
                                viewModel.searchContacts("")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        )
            */

        }

        if (searchContacts.isEmpty() && viewModel.searchContactsQuery.value.isEmpty()) {
            if (contacts.isEmpty()) {
                item {
                    Text(
                        text = "No contacts added yet...",
                        style = normalGoogleSansStyle.copy(fontSize = 14.sp.nonScaledSp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 25.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            itemsIndexed(contacts) { index, contact ->
                ContactItem(contact, onCallIcon = {
                    viewModel.makeCallToNum(contact.contact_mobile)
                },
                    onEdit = {
                        viewModel.indexNumForEditContact.intValue = index
                        contactForEdit = contact
                        showSaveInappDialog = true
                    }, onWhatsappIcon = {
                        viewModel.openWhatsAppByNum(contact.contact_mobile)
                    }, onDelete = {
                        viewModel.deleteContact(contact)
                    }, onTextMsg = {
                        context.sendTextMessage(contact.contact_mobile)
                    }, onSavePrimary = {
                        context.saveNumberToContacts(contact.contact_mobile, contact.contact_title)
                    }, onFocusChangesForNote = {
                        scope.launch {
                            val currentScroll = state.firstVisibleItemIndex
                            val scrollPosition = currentScroll + index
                            state.animateScrollToItem(
                                index,
                                scrollOffset = 1
                            )
                        }
                        if (it) {
                            Handler().postDelayed({
                                viewModel.openKeyboardWithoutFocus()
                            }, 1000)
                        }
                    }, onShareContact = { name, number ->
                        context.shareContact(name ?: "", number)
                    }, homeVm = viewModel)
                if (contacts.size > 25) {
                    if (index == contacts.size - 1) {
                        viewModel.loadMoreContacts()
                    }
                    if (viewModel.isLoading.collectAsState().value && index == contacts.size - 1) {
                        LoaderItem()
                    }
                }
            }

        } else {
            if (searchContacts.isEmpty()) {
                item {
                    Text(
                        text = "No record found!",
                        fontSize = 14.sp.nonScaledSp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 25.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            itemsIndexed(searchContacts) { index, contact ->
                ContactItem(contact, onCallIcon = {
                    viewModel.makeCallToNum(contact.contact_mobile)
                },
                    onEdit = {
                        viewModel.indexNumForEditContact.intValue = index
                        contactForEdit = contact
                        showSaveInappDialog = true
                    }, onWhatsappIcon = {
                        viewModel.openWhatsAppByNum(contact.contact_mobile)
                    }, onDelete = {
                        viewModel.deleteContact(contact, usingSearch = true)
                    }, onTextMsg = {
                        context.sendTextMessage(contact.contact_mobile)
                    }, onSavePrimary = {
                        context.saveNumberToContacts(contact.contact_mobile, contact.contact_title)
                    }, onFocusChangesForNote = {
                        if (it) {
                            Handler().postDelayed({
                                viewModel.openKeyboardWithoutFocus()
                            }, 1000)
                        }
                    }, onShareContact = { name, number ->
                        context.shareContact(name ?: "", number)
                    }, homeVm = viewModel)
            }

        }

    }

    //}

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ContactItem(
    contact: Contact,
    onEdit: () -> Unit,
    onCallIcon: () -> Unit,
    onWhatsappIcon: () -> Unit,
    onDelete: () -> Unit,
    onTextMsg: () -> Unit,
    onSavePrimary: () -> Unit,
    onFocusChangesForNote: (isFocused: Boolean) -> Unit,
    onShareContact: (name: String?, number: String) -> Unit,
    homeVm: HomeVm
) {
    val context = LocalContext.current
    val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    var deleteConfirm by remember {
        mutableStateOf(false)
    }
    var isShareContactOpen by remember {
        mutableStateOf(false)
    }

    if (deleteConfirm) {
        AlertDialog(onDismissRequest = {
            deleteConfirm = false
        },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
            title = {
                Text(text = "Confirm delete")
            },
            text = {
                Text(text = "Are you sure you want to delete this contact?")
            }, confirmButton = {
                TextButton(onClick = {
                    deleteConfirm = false
                    onDelete()
                }) {
                    Text(text = "Confirm", color = PurpleSolid)
                }
            }, dismissButton = {
                TextButton(onClick = {
                    deleteConfirm = false
                }) {
                    Text(text = "Cancel", color = TextColor)
                }
            })
    }

    Card(
        onClick = { onEdit() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 10.dp, vertical = 5.dp),
        border = BorderStroke(0.5.dp, color = Color(0xFFD8D9DB)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 0.dp, vertical = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .padding(start = 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        IconButton(onClick = { onSavePrimary() }) {
                            Image(
                                painter = painterResource(id = R.drawable.contact_book_filled),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(0.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(end = 0.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Top
                        ) {
                            IconButton(onClick = {
                                onCallIcon()
                            }) {
                                Image(
                                    painter = painterResource(id = R.drawable.call_outline),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .rotate(-90F)
                                        .size(25.dp)
                                        .padding(top = 5.dp)
                                )
                            }

                            IconButton(onClick = { onWhatsappIcon() }) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(0.dp)
                                )
                            }
                            IconButton(onClick = { onTextMsg() }) {
                                Image(
                                    painter = painterResource(id = R.drawable.text_msg_outlined),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(25.dp)
                                        .padding(2.dp)
                                )
                            }
                            IconButton(onClick = {
                                deleteConfirm = true
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete),
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .size(25.dp)
                                        .padding(0.dp)
                                )
                            }
                        }

                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Divider(color = Color(0x99E0E2E4))
                    Spacer(modifier = Modifier.height(2.5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                        ) {
                            Text(
                                text = contact.contact_title.capitalize(Locale.ROOT),
                                style = normalGoogleSansStyle.copy(fontSize = 16.sp.nonScaledSp),
                                overflow = TextOverflow.Ellipsis,
                                color = Orange,
                                lineHeight = 1.sp,
                                maxLines = 1,
                                modifier = Modifier
                                    .padding(start = 10.dp, end = 8.dp, top = 5.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier
                                    .padding(start = 10.dp, end = 0.dp, top = 0.dp, bottom = 0.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1.8f)
                                ) {
                                    Text(
                                        text = contact.contact_mobile,
                                        overflow = TextOverflow.Ellipsis,
                                        color = dimBlack,
                                        style = normalGoogleSansStyle.copy(fontSize = 14.sp.nonScaledSp),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_copy),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(15.dp)
                                            .scale(1F)
                                            .clickable {
                                                context.copyToClipboard(contact.contact_mobile)
                                            },
                                        tint = Color.LightGray,
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_share),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(25.dp)
                                            .padding(2.dp)
                                            .scale(1F)
                                            .clickable {
                                                onShareContact(
                                                    contact.contact_title,
                                                    contact.contact_mobile
                                                )
                                            },
                                        tint = Color.LightGray,
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Box {
                                        Icon(
                                            painter = painterResource(id = R.drawable.share),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(25.dp)
                                                .padding(1.dp)
                                                .scale(1F)
                                                .clickable {
                                                    isShareContactOpen = true
                                                },
                                            tint = Color.LightGray,
                                        )

                                        if (isShareContactOpen) {
                                            ShareContact(
                                                onDismiss = {
                                                    onFocusChangesForNote(false)
                                                    isShareContactOpen = false
                                                },
                                                onSave = {
                                                    isShareContactOpen = false
                                                    context.openWhatsapp(contact.contact_mobile, it)
                                                    //callLogNote(number, it)
                                                },
                                                onFocus = { onFocusChangesForNote(it) },
                                                yourBringIntoViewRequester,
                                                onValueChange = {

                                                    //onNoteValueChange(it)
                                                }, "",
                                                homeVm = homeVm
                                            )
                                        }
                                    }

                                }
                                Text(
                                    text = "Created : ${formatDate(contact.created_at)}",
                                    fontWeight = FontWeight.Bold,
                                    overflow = TextOverflow.Ellipsis,
                                    fontFamily = montserrat,
                                    fontSize = 8.sp.nonScaledSp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .padding(end = 0.dp)
                                        .weight(1F)
                                )
                            }
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
            }
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
private fun ShareContact(
    onDismiss: () -> Unit,
    onSave: (noteStr: String) -> Unit,
    onFocus: (hasFocus: Boolean) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester?,
    onValueChange: (value: String) -> Unit,
    preNote: String = "",
    homeVm: HomeVm
) {
    val context = LocalContext.current
    val contactHelper = homeVm.contactHelper
    val bubbleState = rememberBubbleState(
        alignment = ArrowAlignment.TopLeft,
        arrowOffsetY = 10.dp,
        arrowOffsetX = 70.dp,
        arrowShape = ArrowShape.FullTriangle,
        cornerRadius = 8.dp
    )
    val focusRequesterForNote = remember {
        FocusRequester()
    }
    //val yourBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val noteText by remember {
        mutableStateOf(preNote)
    }
    var contacts by remember {
        mutableStateOf(SnapshotStateMap<String?, String?>())
    }
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = noteText,
                selection = TextRange(noteText.length)
            )
        )
    }
    LaunchedEffect(true) {
        delay(1200)
        focusRequesterForNote.requestFocus()

    }
    Popup(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        alignment = Alignment.TopStart,
        offset = IntOffset(0, (20).dp.value.roundToInt()),
        properties = PopupProperties(
            clippingEnabled = true,
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = true
        )
    ) {
        Column(
            modifier = Modifier
                .padding(top = 5.dp, start = 0.dp, end = 10.dp)
                .background(Color.Transparent)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .bubble(
                        bubbleState = bubbleState,
                        color = Color(0xFFFFF2E0),
                    )
                    .fillMaxWidth()
                    .background(Color(0xFFFFF2E0))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier,
                        border = BorderStroke(0.5.dp, color = Color(0xFFD8D9DB)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(3.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Text(
                            text = "Share Contact",
                            color = Orange,
                            fontFamily = montserrat_semibold,
                            modifier = Modifier.padding(6.dp),
                            fontSize = 10.sp.nonScaledSp
                        )
                    }

                    IconButton(onClick = {
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Black)
                    }

                }
                TextField(
                    value = textFieldValueState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                        .focusRequester(focusRequesterForNote)
                        .onFocusChanged {
                            onFocus(it.isFocused)
                        }
                        .onFocusEvent {
                            if (it.isFocused) {
                                coroutineScope.launch {
                                    bringIntoViewRequester?.bringIntoView()
                                }
                            }
                        },
                    onValueChange = {
                        textFieldValueState = it
                        //onValueChange(textFieldValueState.text)
                        CoroutineScope(Dispatchers.IO).launch {
                            if (it.text.length >= 3) {
                                if (hasAllRequiredPermissions(context)) {
                                    contacts = contactHelper.searchContacts(it.text)
                                } else {
                                    EventEmitter.postEvent(Event.HomeVm(type = 5))
                                }
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedIndicatorColor = ThemePurple,
                        unfocusedIndicatorColor = Color.LightGray,
                    ),

                    textStyle = TextStyle(fontFamily = montserrat, fontSize = 12.sp.nonScaledSp),
                    placeholder = {
                        Text(
                            text = "Search contact using name,number",
                            color = Color.LightGray,
                            modifier = Modifier,
                            fontFamily = montserrat,
                            fontSize = 12.sp.nonScaledSp
                        )
                    })
                Spacer(modifier = Modifier.height(15.dp))

                LazyColumn(modifier = Modifier) {
                    items(contacts.keys.toList()) { key ->
                        Contact(name = contacts[key] ?: "", mobile = key ?: "") {
                            onSave("Number: $key\nName: ${contacts[key]}")
                        }
                    }
                }
                /*
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            focusManager.clearFocus()
                                            onDismiss()
                                        },
                                        modifier = Modifier.height(30.dp),
                                        border = BorderStroke(0.5.dp, color = Color.Red),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Text(text = "Cancel", color = Color.Red, fontSize = 10.sp.nonScaledSp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        onClick = {
                                            focusManager.clearFocus()
                                            onSave(textFieldValueState.text)
                                        },
                                        modifier = Modifier.height(30.dp),
                                        border = BorderStroke(0.5.dp, color = ThemePurple),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Text(text = "Save", color = ThemePurple, fontSize = 10.sp.nonScaledSp)
                                    }
                                }
                */
            }

        }
    }


}
