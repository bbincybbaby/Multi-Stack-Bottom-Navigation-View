package me.bincy.multistackbottomnavigationview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Collections
import java.util.Stack

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView
    private var needToAddToBackStack: Boolean = true
    private lateinit var fragmentBackStack: Stack<Int>

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val list = ArrayList<Int>()
        try {
            for (s in fragmentBackStack) {
                list.add(s)
            }
            outState.putIntegerArrayList("Stack", list)
        } catch (_: Exception) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragmentBackStack = Stack()
        if (savedInstanceState != null) {
            val stack = savedInstanceState.getIntegerArrayList("Stack")
            if (stack != null) {
                for (s in stack) {
                    fragmentBackStack.add(s)
                }
                needToAddToBackStack = false
            }
        }

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_container
        ) as NavHostFragment
        navController = navHostFragment.navController

        // Setup the bottom navigation view with navController
        bottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setupWithNavController(navController)
        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            val bottomBarId = findBottomBarIdFromFragment(destination.id)
            if (!::fragmentBackStack.isInitialized){
                fragmentBackStack = Stack()
            }
            if (needToAddToBackStack && bottomBarId!=null) {
                if (!fragmentBackStack.contains(bottomBarId)) {
                    fragmentBackStack.add(bottomBarId)
                } else if (fragmentBackStack.contains(bottomBarId)) {
                    if (bottomBarId == R.id.home) {
                        val homeCount =
                            Collections.frequency(fragmentBackStack, R.id.home)
                        if (homeCount < 2) {
                            fragmentBackStack.push(bottomBarId)
                        } else {
                            fragmentBackStack.asReversed().remove(bottomBarId)
                            fragmentBackStack.push(bottomBarId)
                        }
                    } else {
                        fragmentBackStack.remove(bottomBarId)
                        fragmentBackStack.push(bottomBarId)
                    }
                }

            }
            needToAddToBackStack = true
        }
    }

    override fun onBackPressed() {
        val bottomBarId = if (::navController.isInitialized){
            findBottomBarIdFromFragment(navController.currentDestination?.id)
        } else {
            null
        }
        if (bottomBarId!=null) {
            if (::fragmentBackStack.isInitialized && fragmentBackStack.size > 1) {
                if (fragmentBackStack.size == 2 && fragmentBackStack.lastElement() == fragmentBackStack.firstElement()){
                    finish()
                } else {
                    fragmentBackStack.pop()
                    val fragmentId = fragmentBackStack.lastElement()
                    needToAddToBackStack = false
                    bottomNavigationView.selectedItemId = fragmentId
                }
            } else {
                if (::fragmentBackStack.isInitialized && fragmentBackStack.size == 1) {
                    finish()
                } else {
                    super.onBackPressed()
                }
            }
        } else super.onBackPressed()
    }

    private fun findBottomBarIdFromFragment(fragmentId:Int?):Int?{
        if (fragmentId!=null){
            val bottomBarId = when(fragmentId){
                R.id.register ->{
                    R.id.form
                }
                R.id.leaderboard -> {
                    R.id.list
                }
                R.id.titleScreen ->{
                    R.id.home
                }
                else -> {
                    null
                }
            }
            return bottomBarId
        } else {
            return null
        }
    }
}