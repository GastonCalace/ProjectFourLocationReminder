package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private var error = false
    private var remindersList = mutableListOf<ReminderDTO>()

    fun setReturnError(value: Boolean) {
        error = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (error) {
            return Result.Error("Exception getReminder")
        }
        return Result.Success(remindersList)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (error) {
            return Result.Error("Exception getReminder")
        }
        val found = remindersList.find { it.id == id }
        return if (found != null) {
            Result.Success(found)
        } else {
            Result.Error("Reminder Id $id not found!")
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }

}