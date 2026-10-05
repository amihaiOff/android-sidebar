package com.personal.sidebar.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Icons a rail folder can use, by key. Each has an outline form (inactive
 * folder) and a filled form (the selected folder), as in the design.
 */
object RailIcons {
    class Entry(val key: String, val outlined: ImageVector, val filled: ImageVector)

    val all: List<Entry> = listOf(
        Entry("history", Icons.Outlined.History, Icons.Rounded.History),
        Entry("play", Icons.Outlined.PlayCircle, Icons.Rounded.PlayCircle),
        Entry("work", Icons.Outlined.Work, Icons.Rounded.Work),
        Entry("ai", Icons.Outlined.AutoAwesome, Icons.Rounded.AutoAwesome),
        Entry("build", Icons.Outlined.Build, Icons.Rounded.Build),
        Entry("folder", Icons.Outlined.Folder, Icons.Rounded.Folder),
        Entry("home", Icons.Outlined.Home, Icons.Rounded.Home),
        Entry("star", Icons.Outlined.Star, Icons.Rounded.Star),
        Entry("favorite", Icons.Outlined.Favorite, Icons.Rounded.Favorite),
        Entry("apps", Icons.Outlined.Apps, Icons.Rounded.Apps),
        Entry("chat", Icons.Outlined.Forum, Icons.Rounded.Forum),
        Entry("mail", Icons.Outlined.Mail, Icons.Rounded.Mail),
        Entry("music", Icons.Outlined.MusicNote, Icons.Rounded.MusicNote),
        Entry("headphones", Icons.Outlined.Headphones, Icons.Rounded.Headphones),
        Entry("movie", Icons.Outlined.Movie, Icons.Rounded.Movie),
        Entry("camera", Icons.Outlined.PhotoCamera, Icons.Rounded.PhotoCamera),
        Entry("games", Icons.Outlined.SportsEsports, Icons.Rounded.SportsEsports),
        Entry("news", Icons.Outlined.Newspaper, Icons.Rounded.Newspaper),
        Entry("web", Icons.Outlined.Language, Icons.Rounded.Language),
        Entry("shopping", Icons.Outlined.ShoppingCart, Icons.Rounded.ShoppingCart),
        Entry("money", Icons.Outlined.Savings, Icons.Rounded.Savings),
        Entry("bank", Icons.Outlined.AccountBalance, Icons.Rounded.AccountBalance),
        Entry("school", Icons.Outlined.School, Icons.Rounded.School),
        Entry("code", Icons.Outlined.Code, Icons.Rounded.Code),
        Entry("travel", Icons.Outlined.Flight, Icons.Rounded.Flight),
        Entry("car", Icons.Outlined.DirectionsCar, Icons.Rounded.DirectionsCar),
        Entry("map", Icons.Outlined.Map, Icons.Rounded.Map),
        Entry("fitness", Icons.Outlined.FitnessCenter, Icons.Rounded.FitnessCenter),
        Entry("food", Icons.Outlined.Restaurant, Icons.Rounded.Restaurant),
        Entry("cafe", Icons.Outlined.LocalCafe, Icons.Rounded.LocalCafe),
        Entry("pets", Icons.Outlined.Pets, Icons.Rounded.Pets),
        Entry("cloud", Icons.Outlined.Cloud, Icons.Rounded.Cloud),
        Entry("idea", Icons.Outlined.Lightbulb, Icons.Rounded.Lightbulb),
        Entry("art", Icons.Outlined.Palette, Icons.Rounded.Palette),
        Entry("bolt", Icons.Outlined.Bolt, Icons.Rounded.Bolt),
    )

    private val byKey = all.associateBy { it.key }

    /** The entry for [key]; unknown keys fall back to a folder. */
    fun get(key: String?): Entry = byKey[key] ?: byKey.getValue("folder")
}
