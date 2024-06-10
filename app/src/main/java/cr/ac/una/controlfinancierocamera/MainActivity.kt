package cr.ac.una.controlfinancierocamera

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
//import androidx.fragment.app.commit
import com.google.android.material.navigation.NavigationView
import android.Manifest
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import cr.ac.una.controlfinancierocamera.service.LocationService

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reemplazarFragmento(Home(), "WikiLocation")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)



        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.FOREGROUND_SERVICE), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startLocationService()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val title: Int
        lateinit var fragment: Fragment
        when (menuItem.itemId) {
            R.id.nav_camera -> {
                title = R.string.menu_camera
                fragment = ListControlFinancieroFragment()
            }
            R.id.nav_manage -> {
                title = R.string.menu_tools
            }

            else -> {
                throw IllegalArgumentException("menu option not implemented!!")
            }
        }

        reemplazarFragmento(fragment, getString(title))
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun reemplazarFragmento(fragment: Fragment, title: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.home_content, fragment)
            .commit()
        setTitle(title)
    }
    fun volverAlMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
