package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutine
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {


    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutine()

    private lateinit var app: Application
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var remindersLocalRepository: FakeDataSource

    @Before
    fun setup() {
        stopKoin()
        app = ApplicationProvider.getApplicationContext()
        remindersLocalRepository = FakeDataSource()
        viewModel = SaveReminderViewModel(
            app,
            remindersLocalRepository
        )
    }

    @Test
    fun validateEnteredData_ShowErrorForEmptyTitle() {

        // Give a reminder with empty title
        val reminderDataItem = ReminderDataItem(
            "",
            "",
            "Central Park",
            40.785091,
            73.968285
        )

        // When validate and save reminder
        viewModel.validateAndSaveReminder(reminderDataItem)

        // The snackBar with enter title is shown
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            (`is`(R.string.err_enter_title))
        )
    }

    @Test
    fun validateEnteredData_ShowErrorForNullTitle() {

        // Give a reminder with null title
        val reminderDataItem = ReminderDataItem(
            null,
            "",
            "Central Park",
            40.785091,
            73.968285
        )

        // When validate and save reminder
        viewModel.validateAndSaveReminder(reminderDataItem)

        // The snackBar with enter title is shown
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            (`is`(R.string.err_enter_title))
        )
    }

    @Test
    fun validateEnteredData_ShowErrorForEmptyLocation() {

        // Give a reminder with empty location
        val reminderDataItem = ReminderDataItem(
            "Central Park",
            "",
            "",
            40.785091,
            73.968285
        )

        // When validate and save reminder
        viewModel.validateAndSaveReminder(reminderDataItem)

        // The snackBar with select location message is shown
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            (`is`(R.string.err_select_location))
        )
    }

    @Test
    fun validateEnteredData_ShowErrorForNullLocation() {

        // Give a reminder with null location
        val reminderDataItem = ReminderDataItem(
            "Central Park",
            "",
            null,
            40.785091,
            73.968285
        )

        // When validate and save reminder
        viewModel.validateAndSaveReminder(reminderDataItem)

        // The snackBar with select location message is shown
        assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            (`is`(R.string.err_select_location))
        )
    }

    @Test
    fun saveReminder_ShouldBeSavedCorrectly() = mainCoroutineRule.runBlockingTest {

        // Give a reminder
        val reminderDataItem = ReminderDataItem(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )

        // When save reminder
        viewModel.validateAndSaveReminder(reminderDataItem)


        // The reminder is saved and saved toast message is shown
        assertThat(
            viewModel.reminderId.getOrAwaitValue(),
            (`is`(reminderDataItem.id))
        )
        assertThat(
            viewModel.showToast.getOrAwaitValue(),
            (`is`("Reminder Saved !"))
        )
        assertThat<NavigationCommand>(
            viewModel.navigationCommand.getOrAwaitValue(),
            (`is`(NavigationCommand.Back))
        )
    }

    @Test
    fun saveReminder_CheckLoading() = mainCoroutineRule.runBlockingTest {

        // Pause dispatcher
        mainCoroutineRule.pauseDispatcher()

        // Given a reminder
        val reminderDataItem = ReminderDataItem(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )

        // When save reminder
        viewModel.validateAndSaveReminder(reminderDataItem)

        // Then - show loading
        assertThat(viewModel.showLoading.getOrAwaitValue(), (`is`(true)))

        // Execute pending coroutines
        mainCoroutineRule.resumeDispatcher()

        // Then - hide loading
        assertThat(viewModel.showLoading.getOrAwaitValue(), (`is`(false)))

    }

    @Test
    fun onClear_ShouldCleanFieldValues() {

        // Given a reminder
        val reminderDataItem = ReminderDataItem(
            "Central Park",
            "",
            "Central Park",
            40.785091,
            73.968285
        )
        viewModel.reminderId.value = reminderDataItem.id
        viewModel.reminderTitle.value = reminderDataItem.title
        viewModel.reminderDescription.value = reminderDataItem.description
        viewModel.selectedPOI.value = PointOfInterest(
            LatLng(reminderDataItem.latitude!!, reminderDataItem.longitude!!),
            reminderDataItem.title,
            reminderDataItem.title
        )
        viewModel.latitude.value = reminderDataItem.latitude
        viewModel.longitude.value = reminderDataItem.longitude

        // When call clear method
        viewModel.onClear()

        // Then the field values should be reset
        assertThat(viewModel.reminderId.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderTitle.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderDescription.getOrAwaitValue(), nullValue())
        assertThat(viewModel.selectedPOI.getOrAwaitValue(), nullValue())
        assertThat(viewModel.latitude.getOrAwaitValue(), nullValue())
        assertThat(viewModel.longitude.getOrAwaitValue(), nullValue())
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), (`is`("")))
    }

    @Test
    fun reminder_selected_location_str() {

        // Given selected POI
        viewModel.selectedPOI.value =
            PointOfInterest(LatLng(40.785091, 73.968285), "", "Central Park")

        // Then transform location name
        assertThat(
            viewModel.reminderSelectedLocationStr.getOrAwaitValue(),
            (`is`("Central Park"))
        )
    }


}