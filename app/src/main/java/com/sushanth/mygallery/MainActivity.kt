package com.sushanth.mygallery

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                setupNavGraph()
            } else {
                finishAffinity()
                exitProcess(0)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        requestMediaPermissions()
    }

    private fun requestMediaPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isPermissionGranted(android.Manifest.permission.READ_MEDIA_IMAGES) || isPermissionGranted(
                    android.Manifest.permission.READ_MEDIA_VIDEO
                )
            ) {
                setupNavGraph()
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_IMAGES) || shouldShowRequestPermissionRationale(
                    android.Manifest.permission.READ_MEDIA_VIDEO
                )
            ) {
                showGoToSettingsDialog()
            } else {
                permissionsToRequest += android.Manifest.permission.READ_MEDIA_IMAGES
                permissionsToRequest += android.Manifest.permission.READ_MEDIA_VIDEO
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        } else if (Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.S_V2) {
            if (isPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                setupNavGraph()
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showGoToSettingsDialog()
            } else {
                permissionsToRequest += android.Manifest.permission.READ_EXTERNAL_STORAGE
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupNavGraph() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        navController.graph = navGraph
    }

    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs media access permission to function. Please enable it in App Settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Exit") { _, _ ->
                finishAffinity()
                exitProcess(0)
            }
            .setCancelable(false)
            .show()
    }
}