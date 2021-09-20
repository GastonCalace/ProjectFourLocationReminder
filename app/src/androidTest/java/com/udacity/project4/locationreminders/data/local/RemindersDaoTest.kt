package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest

import org.hamcrest.CoreMatchers.`is`
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var dao: RemindersDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        dao = database.reminderDao()
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun getReminders() = runBlockingTest {

        // Give a reminder
        val reminderDTO = ReminderDTO(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )
        dao.saveReminder(reminderDTO)

        // When get reminders
        val reminders = dao.getReminders()

        // Then reminder list size is 1
        Assert.assertThat(reminders.size, (`is`(1)))

        val reminder = reminders.first()
        Assert.assertThat(reminder.id, (`is`(reminderDTO.id)))
        Assert.assertThat(reminder.title, (`is`(reminderDTO.title)))
        Assert.assertThat(reminder.description, (`is`(reminderDTO.description)))
        Assert.assertThat(reminder.location, (`is`(reminderDTO.location)))
        Assert.assertThat(reminder.latitude, (`is`(reminderDTO.latitude)))
        Assert.assertThat(reminder.longitude, (`is`(reminderDTO.longitude)))
    }

    @Test
    fun getReminderById() = runBlockingTest {

        // Give a reminder
        val reminderDTO = ReminderDTO(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )
        dao.saveReminder(reminderDTO)

        // When get reminders
        val reminder = dao.getReminderById(reminderDTO.id)

        // Then reminder is returned
        Assert.assertThat(reminder?.id, (`is`(reminderDTO.id)))
        Assert.assertThat(reminder?.title, (`is`(reminderDTO.title)))
        Assert.assertThat(reminder?.description, (`is`(reminderDTO.description)))
        Assert.assertThat(reminder?.location, (`is`(reminderDTO.location)))
        Assert.assertThat(reminder?.latitude, (`is`(reminderDTO.latitude)))
        Assert.assertThat(reminder?.longitude, (`is`(reminderDTO.longitude)))
    }

    @Test
    fun saveReminder() = runBlockingTest {

        // Give a reminder dto
        val reminderDTO = ReminderDTO(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )

        // When save reminder
        dao.saveReminder(reminderDTO)

        // Then should be saved
        val reminder = dao.getReminders().first()
        Assert.assertThat(reminder.id, (`is`(reminderDTO.id)))
        Assert.assertThat(reminder.title, (`is`(reminderDTO.title)))
        Assert.assertThat(reminder.description, (`is`(reminderDTO.description)))
        Assert.assertThat(reminder.location, (`is`(reminderDTO.location)))
        Assert.assertThat(reminder.latitude, (`is`(reminderDTO.latitude)))
        Assert.assertThat(reminder.longitude, (`is`(reminderDTO.longitude)))
    }

    @Test
    fun deleteAllReminders() = runBlockingTest {

        // Give a reminder
        val reminderDTO = ReminderDTO(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )
        dao.saveReminder(reminderDTO)

        // When get reminders
        dao.deleteAllReminders()

        // Then reminder list is empty
        val reminders = dao.getReminders()
        Assert.assertThat(reminders.size, (`is`(0)))
    }


}