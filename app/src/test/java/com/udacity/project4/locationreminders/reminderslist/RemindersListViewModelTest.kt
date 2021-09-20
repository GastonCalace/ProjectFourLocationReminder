package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutine
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutine()

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var remindersLocalRepository: FakeDataSource

    @Before
    fun setup() {
        stopKoin()
        remindersLocalRepository = FakeDataSource()
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            remindersLocalRepository
        )
    }

    @Test
    fun invalidateShowNoData_shouldShowNoData() = mainCoroutineRule.runBlockingTest {

        // Given no reminders
        remindersLocalRepository.deleteAllReminders()

        // When loading reminders
        viewModel.loadReminders()

        // Then reminders list is empty and no data is shown
        val reminderList = viewModel.remindersList.getOrAwaitValue()
        val noData = viewModel.showNoData.getOrAwaitValue()
        assertThat(reminderList.size, (`is`(0)))
        assertThat(noData, (`is`(true)))

    }

    @Test
    fun unavailableReminder_ShouldShowSnackBarError() = mainCoroutineRule.runBlockingTest {
        //Given any Error
        remindersLocalRepository.setReturnError(true)

        // When loading reminders
        viewModel.loadReminders()

        // Then reminders list is empty and no data is shown
        val showSnackBar = viewModel.showSnackBar.getOrAwaitValue()
        assertThat(showSnackBar, (`is`("Exception getReminder")))

    }

    @Test
    fun loadReminders_CheckLoading() = mainCoroutineRule.runBlockingTest {

        // Pause dispatcher
        mainCoroutineRule.pauseDispatcher()

        // Given no reminders
        remindersLocalRepository.deleteAllReminders()

        // When loading reminders
        viewModel.loadReminders()

        // Then - show loading
        assertThat(viewModel.showLoading.getOrAwaitValue(), (`is`(true)))

        // Execute pending coroutines
        mainCoroutineRule.resumeDispatcher()

        // Then - hide loading
        assertThat(viewModel.showLoading.getOrAwaitValue(), (`is`(false)))

    }

    @Test
    fun loadReminders_shouldShowDataCorrectly() = mainCoroutineRule.runBlockingTest {
        // Given some reminders
        remindersLocalRepository.saveReminder(
            ReminderDTO(
                "Central Park",
                "",
                "Central Park",
                40.785091,
                73.968285
            )
        )

        // When loading reminders
        viewModel.loadReminders()

        // Then reminders list is empty and no data is shown
        val reminderList = viewModel.remindersList.getOrAwaitValue()
        assertThat(reminderList.size, (`is`(1)))
        assertThat(reminderList.first().title, (`is`("Central Park")))
        assertThat(reminderList.first().description, (`is`("")))
        assertThat(reminderList.first().location, (`is`("Central Park")))
        assertThat(reminderList.first().latitude, (`is`(40.785091)))
        assertThat(reminderList.first().longitude, (`is`(73.968285)))
    }

}