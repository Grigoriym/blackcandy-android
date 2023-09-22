package org.blackcandy.android

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import dev.hotwire.turbo.activities.TurboActivity
import dev.hotwire.turbo.delegates.TurboActivityDelegate
import dev.hotwire.turbo.session.TurboSessionNavHostFragment
import kotlinx.coroutines.launch
import org.blackcandy.android.databinding.ActivityMainBinding
import org.blackcandy.android.fragments.navs.HomeNavHostFragment
import org.blackcandy.android.fragments.navs.LibraryNavHostFragment
import org.blackcandy.android.viewmodels.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), TurboActivity, OnItemSelectedListener {
    private val viewModel: MainViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding
    override lateinit var delegate: TurboActivityDelegate
    lateinit var homeNav: TurboSessionNavHostFragment
    lateinit var libraryNav: TurboSessionNavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUserFlow.collect() {
                    if (it == null) {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                        startActivity(intent)
                    }
                }
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        homeNav = HomeNavHostFragment()
        libraryNav = LibraryNavHostFragment()

        supportFragmentManager.commitNow {
            add(R.id.main_container, homeNav)
        }

        delegate = TurboActivityDelegate(this, homeNav.id)

        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener(this)

        // Displaying edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_menu_home -> {
                supportFragmentManager.commit {
                    hide(libraryNav)
                    show(homeNav)
                }

                delegate.currentNavHostFragmentId = homeNav.id

                true
            }

            R.id.nav_menu_library -> {
                val libraryNavFragment = supportFragmentManager.findFragmentById(libraryNav.id)

                if (libraryNavFragment == null) {
                    supportFragmentManager.commitNow {
                        add(R.id.main_container, libraryNav)
                        hide(homeNav)
                    }

                    delegate.registerNavHostFragment(libraryNav.id)
                } else {
                    supportFragmentManager.commit {
                        hide(homeNav)
                        show(libraryNav)
                    }
                }

                delegate.currentNavHostFragmentId = libraryNav.id

                true
            }
            else -> false
        }
    }
}
