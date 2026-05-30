package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {

    AlertDialog(icon = {
        Icon(Icons.Default.Delete, contentDescription = "Example Icon")
    }, title = {
        Text(text = "Confirm Deletion")
    }, text = {
        Text(text = "Are you sure you want to delete your account?  after it your all the data will be deleted and no longer available.")
    }, onDismissRequest = onCancel, confirmButton = {
        TextButton(
            onClick = onConfirm
        ) {
            Text("Delete Account")
        }
    }, dismissButton = {
        TextButton(
            onClick = onCancel
        ) {
            Text("Dismiss")
        }
    })

}