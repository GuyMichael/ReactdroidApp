package com.guymichael.reactiveapp.model

import android.view.Menu
import android.view.MenuItem

interface ScreenPage {
    fun onSaveOptionsMenu(menu: Menu)
    fun onOptionsItemSelected(item: MenuItem): Boolean
}