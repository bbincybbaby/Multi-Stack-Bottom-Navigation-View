[![Test & Build](https://github.com/bbincybbaby/MultiStackBottomNavigationView/actions/workflows/testandbuild.yml/badge.svg)](https://github.com/bbincybbaby/MultiStackBottomNavigationView/actions/workflows/testandbuild.yml)

## Bottom Navigation Menu: Similar back stack and back arrow action like Youtube, Instagram, Amazon and more

I created this repository to show how to manage individual fragments back stack like Instagram, YouTube, Amazon and more.

I was trying to create a similar back stack and back action like Youtube, Instagram, Amazon and more. I tried using a view pager, custom back stack and many custom lines of code still I was not happy with the end results. Handling the back stack was the major pain area. Starting with the navigation component version _2.4.0-alpha01_ `NavigationUI` support multiple back stacks for specific tab (child fragments inside selected tab) without any code change, but supporting back action to get back to the previous tab is still unavailable. Maybe in future, the official library may support that feature too.

For example, if we have **4 main** fragments (tabs) **A, B, C, and D**, the `startDestination` is **A**. **D** has **child** fragments **D1, D2, and D3.** If the user navigates like **A -> B -> C ->D -> D1 -> D2-> D3**, if the user clicks the back button with the official library the navigation will be **D3 -> D2-> D1-> D** followed by **A**. That means primary tabs **B and C** will not be in the back stack.

I have created this [Github repo](https://github.com/bbincybbaby/MultiStackBottomNavigationView) to show what I did. I reached this answer with the following medium articles.

*   [https://medium.com/r?url=https%3A%2F%2Fvedraj360.medium.com%2Fyoutube-like-backstack-in-jetpack-navigation-component-android-2537b446668d](https://medium.com/r?url=https%3A%2F%2Fvedraj360.medium.com%2Fyoutube-like-backstack-in-jetpack-navigation-component-android-2537b446668d)
*   [https://medium.com/androiddevelopers/navigation-multiple-back-stacks-6c67ba41952f](https://medium.com/androiddevelopers/navigation-multiple-back-stacks-6c67ba41952f)

Steps →

*   Add navigation dependencies to the Gradle file.

```plaintext
implementation 'androidx.navigation:navigation-runtime-ktx:2.5.3'
implementation 'androidx.navigation:navigation-fragment-ktx:2.5.3'
implementation 'androidx.navigation:navigation-ui-ktx:2.5.3'
```

*   Add FragmentContainerView and BottomNavigationView to the main activity layout file.

```plaintext
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_container"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        app:defaultNavHost="true"
        app:navGraph="@navigation/nav_graph"
        tools:layout="@layout/fragment_about" />

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_nav"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:menu="@menu/bottom_nav"/>
</LinearLayout>
```

*   Create menu.xml for BottomNavigationView and nav\_graph for FragmentContainerView. Instead of having a **single navigation graph** each item in BottomNavigationView will have a **separate navigation graph** and all three included in the **main navigation graph.**

```plaintext
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/home"
        android:icon="@drawable/ic_home"
        android:contentDescription="@string/cd_home"
        android:title="@string/title_home" />
    <item
        android:id="@+id/list"
        android:icon="@drawable/ic_list"
        android:contentDescription="@string/cd_list"
        android:title="@string/title_list" />
    <item
        android:id="@+id/form"
        android:icon="@drawable/ic_feedback"
        android:contentDescription="@string/cd_form"
        android:title="@string/title_register" />
</menu>
```

*   Link BottomNavigationView and FragmentContainerView with `setupWithNavController.`

```plaintext
val navHostFragment = supportFragmentManager.findFragmentById(
    R.id.nav_host_container
) as NavHostFragment
navController = navHostFragment.navController
bottomNavigationView = findViewById(R.id.bottom_nav)
bottomNavigationView.setupWithNavController(navController)
```

*   So far we have implemented whatever is supported by the official library, that is subfragments inside each tab will have back-stack support, switching the tabs will not recreate the fragment, instead it will stay where we last left. Now we have to customize it to support back functionality on main tabs. The fragmentBackStack will help us to save all the visited destinations in the stack & addToBackStack is a checker which will help to determine if we want to add the current destination into the stack or not.

```plaintext
private var needToAddToBackStack: Boolean = true
private lateinit var fragmentBackStack: Stack<Int> 
```

*   When `navHostFragment` changes the fragment we get a callback to `addOnDestinationChangedListener` and we check whether the fragment is already existing in the Stack or not. If not we will add to the top of the Stack, if yes we will swap the position to the Stack's top. As we are now using separate graphs for each tab the id in the `addOnDestinationChangedListener` and BottomNavigationView will be different, so we use `findBottomBarIdFromFragment` to find the BottomNavigationView item id from the destination fragment.

```plaintext
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
    
    
```

*   And when the user clicks back we override the activity’s `onBackPressed` method(NB:`onBackPressed` is deprecated I will update the answer once I find a replacement for `super.onBackPressed()` inside `override fun onBackPressed()`). When the user clicks back we will pop the last fragment from Stack and set the selected item id in the bottom navigation view.

```plaintext
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
```

Medium Link -> https://medium.com/@bbincybbaby/bottom-navigation-menu-similar-back-stack-and-back-arrow-action-like-youtube-instagram-amazon-46a22f6d2fed
