package com.personal.sidebar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WineBar
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A curated set of outline (line-style) icons a folder can use instead of an
 * emoji. Each entry has a stable [key] that is persisted in the config, the
 * Compose [icon], and free-text [keywords] for the picker's search.
 */
object FolderIcons {
    data class Entry(val key: String, val icon: ImageVector, val keywords: String)

    val all: List<Entry> = listOf(
        Entry("work", Icons.Outlined.Work, "work briefcase job office"),
        Entry("cart", Icons.Outlined.ShoppingCart, "shopping cart buy store"),
        Entry("bag", Icons.Outlined.ShoppingBag, "shopping bag store buy"),
        Entry("heart", Icons.Outlined.FavoriteBorder, "heart favorite love like"),
        Entry("money", Icons.Outlined.AttachMoney, "money dollar cash finance"),
        Entry("wallet", Icons.Outlined.AccountBalanceWallet, "wallet money finance"),
        Entry("card", Icons.Outlined.CreditCard, "credit card payment bank"),
        Entry("savings", Icons.Outlined.Savings, "savings piggy bank money"),
        Entry("bank", Icons.Outlined.AccountBalance, "bank finance building money"),
        Entry("receipt", Icons.Outlined.Receipt, "receipt bill invoice"),
        Entry("car", Icons.Outlined.DirectionsCar, "car vehicle drive transport"),
        Entry("bus", Icons.Outlined.DirectionsBus, "bus transport transit"),
        Entry("bike", Icons.Outlined.DirectionsBike, "bike bicycle cycle"),
        Entry("flight", Icons.Outlined.Flight, "flight plane travel airplane"),
        Entry("food", Icons.Outlined.Restaurant, "food restaurant eat dining"),
        Entry("coffee", Icons.Outlined.Coffee, "coffee cafe drink tea"),
        Entry("pizza", Icons.Outlined.LocalPizza, "pizza food eat"),
        Entry("wine", Icons.Outlined.WineBar, "wine drink bar alcohol"),
        Entry("cake", Icons.Outlined.Cake, "cake birthday dessert"),
        Entry("school", Icons.Outlined.School, "school education graduate study"),
        Entry("book", Icons.Outlined.MenuBook, "book read study library"),
        Entry("fitness", Icons.Outlined.FitnessCenter, "fitness gym workout dumbbell exercise"),
        Entry("medical", Icons.Outlined.MedicalServices, "medical health doctor"),
        Entry("pill", Icons.Outlined.Medication, "pill medication medicine health"),
        Entry("baby", Icons.Outlined.ChildCare, "baby child kids"),
        Entry("pets", Icons.Outlined.Pets, "pets paw dog cat animal"),
        Entry("gift", Icons.Outlined.CardGiftcard, "gift present card"),
        Entry("home", Icons.Outlined.Home, "home house"),
        Entry("star", Icons.Outlined.Star, "star favorite"),
        Entry("music", Icons.Outlined.MusicNote, "music song audio note"),
        Entry("headphones", Icons.Outlined.Headphones, "headphones audio music"),
        Entry("games", Icons.Outlined.SportsEsports, "games gaming controller play"),
        Entry("camera", Icons.Outlined.PhotoCamera, "camera photo picture"),
        Entry("photos", Icons.Outlined.CameraAlt, "photos camera picture"),
        Entry("mail", Icons.Outlined.Mail, "mail email message"),
        Entry("cloud", Icons.Outlined.Cloud, "cloud storage weather"),
        Entry("web", Icons.Outlined.Language, "web internet browser globe"),
        Entry("code", Icons.Outlined.Code, "code dev programming"),
        Entry("terminal", Icons.Outlined.Terminal, "terminal console code dev"),
    )

    private val byKey: Map<String, ImageVector> = all.associate { it.key to it.icon }

    fun icon(key: String?): ImageVector? = key?.let { byKey[it] }

    fun search(query: String): List<Entry> {
        val q = query.trim().lowercase()
        return if (q.isEmpty()) all else all.filter { it.keywords.contains(q) || it.key.contains(q) }
    }
}

/** The colour palette offered for folder icons; null = "auto" (system theme). */
val FOLDER_ICON_COLORS: List<Int?> = listOf(
    null, // auto / theme
    0xFFEF4444.toInt(), // red
    0xFFF97316.toInt(), // orange
    0xFFF59E0B.toInt(), // amber
    0xFFEAB308.toInt(), // yellow
    0xFF22C55E.toInt(), // green
    0xFF10B981.toInt(), // teal
    0xFF06B6D4.toInt(), // cyan
    0xFF3B82F6.toInt(), // blue
    0xFF60A5FA.toInt(), // light blue
    0xFF8B5CF6.toInt(), // purple
    0xFFEC4899.toInt(), // pink
    0xFF9CA3AF.toInt(), // grey
)
