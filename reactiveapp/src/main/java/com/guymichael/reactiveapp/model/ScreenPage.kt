package com.guymichael.reactiveapp.model

import android.view.MenuItem

interface ScreenPage {
    fun onOptionsItemSelected(item: MenuItem): Boolean
}