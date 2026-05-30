package com.ruchitech.quicklinkcaller.ui.screens.connectedui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NestedLazyColumnDemo() {
    // Wrap LazyColumn with Box and specify a fixed height
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(5) { outerIndex ->
                // Outer list item
                NestedList(outerIndex)
            }
        }
    }
}

@Composable
fun NestedList(outerIndex: Int) {
    // Inner list
    LazyColumn(modifier = Modifier.height(150.dp)) {
        items(5) { innerIndex ->
            // Inner list item
            ListItem(outerIndex, innerIndex)
        }
    }
}

@Composable
fun ListItem(outerIndex: Int, innerIndex: Int) {
    // Composable for individual list item
    // You can display your item content here
}

@Preview
@Composable
fun PreviewNestedLazyColumnDemo() {
    NestedLazyColumnDemo()
}
