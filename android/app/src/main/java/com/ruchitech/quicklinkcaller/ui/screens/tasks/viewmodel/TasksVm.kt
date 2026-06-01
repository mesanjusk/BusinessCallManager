package com.ruchitech.quicklinkcaller.ui.screens.tasks.viewmodel

import androidx.lifecycle.viewModelScope
import com.ruchitech.quicklinkcaller.helper.AppPreference
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import com.ruchitech.quicklinkcaller.room.DbRepository
import com.ruchitech.quicklinkcaller.room.data.Lead
import com.ruchitech.quicklinkcaller.room.data.Task
import com.ruchitech.quicklinkcaller.ui.screens.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksVm @Inject constructor(
    private val routeNavigator: RouteNavigator,
    private val dbRepository: DbRepository,
    private val appPreference: AppPreference
) : SharedViewModel(), RouteNavigator by routeNavigator {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _leads = MutableStateFlow<List<Lead>>(emptyList())
    val leads: StateFlow<List<Lead>> = _leads.asStateFlow()

    val tabs = listOf("All", "Pending", "In Progress", "Done", "Overdue")

    init {
        loadTasks(null)
        loadLeads()
    }

    private fun loadLeads() {
        viewModelScope.launch {
            val userUuid = appPreference.userId ?: return@launch
            dbRepository.leadDao.getAllLeads(userUuid).collect { _leads.value = it }
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        loadTasks(if (index == 0) null else tabs[index])
    }

    private fun loadTasks(status: String?) {
        viewModelScope.launch {
            val userUuid = appPreference.userId ?: return@launch
            if (status == null) dbRepository.taskDao.getMyTasks(userUuid).collect { _tasks.value = it }
            else dbRepository.taskDao.getMyTasksByStatus(userUuid, status).collect { _tasks.value = it }
        }
    }

    fun updateTaskStatus(task: Task, newStatus: String) {
        viewModelScope.launch { dbRepository.taskDao.updateTask(task.copy(status = newStatus, updated_at = System.currentTimeMillis())) }
    }

    fun createTask(title: String, description: String?, priority: String, dueDateMillis: Long?, leadUuid: String? = null) {
        viewModelScope.launch {
            val userUuid = appPreference.userId ?: return@launch
            dbRepository.taskDao.insertTask(Task(
                task_uuid = java.util.UUID.randomUUID().toString(),
                created_by_uuid = userUuid, assigned_to_uuid = userUuid,
                title = title, description = description, priority = priority,
                due_date = dueDateMillis, lead_uuid = leadUuid
            ))
        }
    }
}
