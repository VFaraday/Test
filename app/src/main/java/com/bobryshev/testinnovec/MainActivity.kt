package com.bobryshev.testinnovec

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.bobryshev.testinnovec.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            viewModel.isNetworkAvailable = true
        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            viewModel.isNetworkAvailable = false
        }
    }

    private val viewModel: MainActivityViewModel by viewModels()
    private val getContactResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::receiveGetContactResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)

        viewModel.actionsLiveData.observe(this, ::handleAction)

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.convertString(FileReader.readAssetFile(this@MainActivity, "actions.json").orEmpty())
        }

        binding.btnClick.setOnClickListener {
            viewModel.clickButton()
        }
    }

    private fun handleAction(action: Action) {
        when(action) {
            Action.ANIMATION -> rotateButton()
            Action.TOAST -> showToast()
            Action.CALL -> pickContact()
            Action.NOTIFICATION -> sendNotification()
        }
    }

    private fun rotateButton() {
        ObjectAnimator.ofFloat(
            binding.btnClick.rootView, "rotation", 0f, 360f
        )
            .setDuration(500)
            .start()
    }

    private fun showToast() {
        Toast.makeText(this, getString(R.string.toast_text), Toast.LENGTH_LONG).show()
    }

    private fun pickContact() {
        getContactResult.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
    }

    private fun sendNotification() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Action is Notification!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Name"
            val descriptionText = "Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun receiveGetContactResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri? = activityResult.data?.data
            contactUri?.let {
                val cursor = contentResolver.query(it, null, null, null, null)
                cursor.use { c ->
                    c?.moveToFirst()
                    val column = c?.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    c?.getString(column ?: 0)
                }
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "101"
    }
}