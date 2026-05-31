package com.ruchitech.quicklinkcaller.ui.screens.tasks.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruchitech.quicklinkcaller.room.data.Task
import com.ruchitech.quicklinkcaller.ui.components.*
import com.ruchitech.quicklinkcaller.ui.screens.tasks.viewmodel.TasksVm
import com.ruchitech.quicklinkcaller.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: TasksVm) {
    val tasks by viewModel.tasks.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NavyPrimary,
        topBar = {
            TopAppBar(
                title = { Text("My Tasks", fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = ElectricBlue, contentColor = Color.White, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = NavyPrimary,
                contentColor = ElectricBlue,
                edgePadding = 16.dp
            ) {
                viewModel.tabs.forEachIndexed { i, tab ->
                    Tab(selected = selectedTab == i, onClick = { viewModel.selectTab(i) }, text = { Text(tab, fontSize = 13.sp) })
                }
            }
            if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateView("No tasks", "Tasks assigned to you will appear here")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(tasks, key = { it.task_uuid }) { task ->
                        TaskCard(task = task, onStatusChange = { viewModel.updateTaskStatus(task, it) })
                    }
                }
            }
        }
    }

    if (showAddDialog) AddTaskDialog(onDismiss = { showAddDialog = false }, onAdd = { t, d, p -> viewModel.createTask(t, d, p, null); showAddDialog = false })
}

@Composable
private fun TaskCard(task: Task, onStatusChange: (String) -> Unit) {
    val statusColor = when (task.status) {
        "In Progress" -> TaskInProgress
        "Done" -> TaskDone
        "Overdue" -> TaskOverdue
        else -> TaskPending
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Row {
            PriorityIndicator(priority = task.priority, modifier = Modifier.height(IntrinsicSize.Min))
            Column(modifier = Modifier.weight(1f).padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        task.title, fontWeight = FontWeight.SemiBold, color = if (task.status == "Done") TextDisabled else TextPrimary,
                        textDecoration = if (task.status == "Done") TextDecoration.LineThrough else null, modifier = Modifier.weight(1f)
                    )
                    Surface(shape = RoundedCornerShape(50.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Text(task.status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
                task.description?.let { Text(it, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
                if (task.status != "Done") {
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (task.status == "Pending") SuggestionChip(onClick = { onStatusChange("In Progress") }, label = { Text("Start", fontSize = 11.sp) })
                        if (task.status == "In Progress" || task.status == "Pending") SuggestionChip(onClick = { onStatusChange("Done") }, label = { Text("Complete", fontSize = 11.sp) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String?, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        title = { Text("New Task", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title", color = TextSecondary) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description (opt.)", color = TextSecondary) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = DividerColor, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), modifier = Modifier.fillMaxWidth())
                Text("Priority", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High").forEach { p ->
                        FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(p, fontSize = 12.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ElectricBlue, selectedLabelColor = Color.White))
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { if (title.isNotBlank()) onAdd(title, desc.ifBlank { null }, priority) }, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)) { Text("Add", color = Color.White) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}
