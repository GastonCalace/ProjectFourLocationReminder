package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun getReminders() = runBlocking {

        // Give a reminder
        val reminderDTO = ReminderDTO(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )
        repository.saveReminder(reminderDTO)

        // When get reminders
        val reminders = (repository.getReminders() as Result.Success)

        // Then reminder list size is 1
        Assert.assertThat(reminders.data.size, (`is`(1)))

        val reminder = reminders.data.first()
        Assert.assertThat(reminder.id, (`is`(reminderDTO.id)))
        Assert.assertThat(reminder.title, (`is`(reminderDTO.title)))
        Assert.assertThat(reminder.description, (`is`(reminderDTO.description)))
        Assert.assertThat(reminder.location, (`is`(reminderDTO.location)))
        Assert.assertThat(reminder.latitude, (`is`(reminderDTO.latitude)))
        Assert.assertThat(reminder.longitude, (`is`(reminderDTO.longitude)))
    }

    @Test
    fun getReminderById() = runBlocking {

        // Give a reminder
        val reminderDTO = ReminderDTO(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )
        repository.saveReminder(reminderDTO)

        // When get reminder by id
        val reminder = (repository.getReminder(reminderDTO.id) as Result.Success)

        // Then reminder is returned
        Assert.assertThat(reminder.data.id, (`is`(reminderDTO.id)))
        Assert.assertThat(reminder.data.title, (`is`(reminderDTO.title)))
        Assert.assertThat(reminder.data.description, (`is`(reminderDTO.description)))
        Assert.assertThat(reminder.data.location, (`is`(reminderDTO.location)))
        Assert.assertThat(reminder.data.latitude, (`is`(reminderDTO.latitude)))
        Assert.assertThat(reminder.data.longitude, (`is`(reminderDTO.longitude)))
    }

    @Test
    fun getReminderById_NotFound() = runBlocking {

        // Give a reminder
        val reminderDTO = ReminderDTO(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )
        repository.saveReminder(reminderDTO)

        // When get reminder by invalid id
        val reminder = (repository.getReminder("123") as Result.Error)

        // Then message reminder not found is shown
        Assert.assertThat(reminder.message, (`is`("Reminder not found!")))
    }

    @Test
    fun deleteAllReminders() = runBlocking {

        // Give a reminder
        val reminderDTO = ReminderDTO(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )
        repository.saveReminder(reminderDTO)

        // When get reminders
        repository.deleteAllReminders()

        // Then reminder list is empty
        val reminders = (repository.getReminders() as Result.Success)
        Assert.assertThat(reminders.data.size, (Matchers.`is`(0)))
    }

}