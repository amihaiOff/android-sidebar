package com.personal.sidebar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Anchor
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsBoat
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Handyman
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.Icecream
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.LocalMovies
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Rocket
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Weekend
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WineBar
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A curated set of outline (line-style) icons a folder can use instead of an
 * emoji. Each entry has a stable [key] persisted in the config, the Compose
 * [icon], and free-text [keywords] for the picker's search.
 */
object FolderIcons {
    data class Entry(val key: String, val icon: ImageVector, val keywords: String)

    val all: List<Entry> = listOf(
        // Money / shopping
        Entry("work", Icons.Outlined.Work, "work briefcase job office"),
        Entry("business", Icons.Outlined.BusinessCenter, "business work office briefcase"),
        Entry("cart", Icons.Outlined.ShoppingCart, "shopping cart buy store"),
        Entry("bag", Icons.Outlined.ShoppingBag, "shopping bag store buy"),
        Entry("store", Icons.Outlined.Store, "store shop market"),
        Entry("mall", Icons.Outlined.LocalMall, "mall shopping store"),
        Entry("grocery", Icons.Outlined.LocalGroceryStore, "grocery store shopping food"),
        Entry("money", Icons.Outlined.AttachMoney, "money dollar cash finance"),
        Entry("wallet", Icons.Outlined.AccountBalanceWallet, "wallet money finance"),
        Entry("card", Icons.Outlined.CreditCard, "credit card payment bank"),
        Entry("savings", Icons.Outlined.Savings, "savings piggy bank money"),
        Entry("bank", Icons.Outlined.AccountBalance, "bank finance building money"),
        Entry("receipt", Icons.Outlined.Receipt, "receipt bill invoice"),
        Entry("trending", Icons.Outlined.TrendingUp, "trending stocks finance chart up"),
        Entry("barchart", Icons.Outlined.BarChart, "chart stats analytics bar"),
        Entry("piechart", Icons.Outlined.PieChart, "chart stats analytics pie"),
        Entry("diamond", Icons.Outlined.Diamond, "diamond gem jewel luxury"),
        Entry("gift", Icons.Outlined.CardGiftcard, "gift present card"),
        // Transport / travel
        Entry("car", Icons.Outlined.DirectionsCar, "car vehicle drive transport"),
        Entry("bus", Icons.Outlined.DirectionsBus, "bus transport transit"),
        Entry("bike", Icons.Outlined.DirectionsBike, "bike bicycle cycle"),
        Entry("moto", Icons.Outlined.TwoWheeler, "motorcycle scooter bike"),
        Entry("walk", Icons.Outlined.DirectionsWalk, "walk pedestrian"),
        Entry("run", Icons.Outlined.DirectionsRun, "run running exercise"),
        Entry("boat", Icons.Outlined.DirectionsBoat, "boat ship ferry"),
        Entry("anchor", Icons.Outlined.Anchor, "anchor boat sailing sea"),
        Entry("flight", Icons.Outlined.Flight, "flight plane travel airplane"),
        Entry("rocket", Icons.Outlined.Rocket, "rocket space launch"),
        Entry("gas", Icons.Outlined.LocalGasStation, "gas fuel station petrol"),
        Entry("map", Icons.Outlined.Map, "map navigation location"),
        Entry("place", Icons.Outlined.Place, "place pin location marker"),
        Entry("explore", Icons.Outlined.Explore, "explore compass discover"),
        Entry("travel", Icons.Outlined.TravelExplore, "travel explore world search"),
        Entry("luggage", Icons.Outlined.Luggage, "luggage suitcase travel trip"),
        Entry("beach", Icons.Outlined.BeachAccess, "beach vacation umbrella holiday"),
        Entry("hiking", Icons.Outlined.Hiking, "hiking trek outdoor mountain"),
        Entry("terrain", Icons.Outlined.Terrain, "terrain mountain hill nature"),
        Entry("hotel", Icons.Outlined.Hotel, "hotel travel stay room"),
        // Food / drink
        Entry("food", Icons.Outlined.Restaurant, "food restaurant eat dining"),
        Entry("fastfood", Icons.Outlined.Fastfood, "fast food burger eat"),
        Entry("pizza", Icons.Outlined.LocalPizza, "pizza food eat"),
        Entry("coffee", Icons.Outlined.Coffee, "coffee cafe drink tea"),
        Entry("bar", Icons.Outlined.LocalBar, "bar cocktail drink alcohol"),
        Entry("wine", Icons.Outlined.WineBar, "wine drink bar alcohol"),
        Entry("cake", Icons.Outlined.Cake, "cake birthday dessert"),
        Entry("icecream", Icons.Outlined.Icecream, "icecream dessert sweet"),
        // Health / wellness
        Entry("fitness", Icons.Outlined.FitnessCenter, "fitness gym workout dumbbell exercise"),
        Entry("soccer", Icons.Outlined.SportsSoccer, "soccer football sport ball"),
        Entry("basketball", Icons.Outlined.SportsBasketball, "basketball sport ball"),
        Entry("pool", Icons.Outlined.Pool, "pool swim water sport"),
        Entry("yoga", Icons.Outlined.SelfImprovement, "yoga meditation wellness zen"),
        Entry("spa", Icons.Outlined.Spa, "spa relax wellness"),
        Entry("medical", Icons.Outlined.MedicalServices, "medical health doctor"),
        Entry("pill", Icons.Outlined.Medication, "pill medication medicine health"),
        Entry("healing", Icons.Outlined.Healing, "healing health bandage"),
        // People / comms
        Entry("home", Icons.Outlined.Home, "home house"),
        Entry("person", Icons.Outlined.Group, "people group friends person"),
        Entry("chat", Icons.Outlined.Chat, "chat message talk"),
        Entry("phone", Icons.Outlined.Phone, "phone call dial"),
        Entry("mail", Icons.Outlined.Email, "mail email message"),
        Entry("notifications", Icons.Outlined.Notifications, "notifications bell alert"),
        Entry("baby", Icons.Outlined.ChildCare, "baby child kids"),
        Entry("pets", Icons.Outlined.Pets, "pets paw dog cat animal"),
        // Media / creative
        Entry("camera", Icons.Outlined.PhotoCamera, "camera photo picture"),
        Entry("photos", Icons.Outlined.CameraAlt, "photos camera picture"),
        Entry("video", Icons.Outlined.Videocam, "video camera film record"),
        Entry("movie", Icons.Outlined.Movie, "movie film cinema"),
        Entry("cinema", Icons.Outlined.LocalMovies, "cinema movie film tickets"),
        Entry("music", Icons.Outlined.MusicNote, "music song audio note"),
        Entry("headphones", Icons.Outlined.Headphones, "headphones audio music"),
        Entry("mic", Icons.Outlined.Mic, "mic microphone record voice"),
        Entry("games", Icons.Outlined.SportsEsports, "games gaming controller play"),
        Entry("brush", Icons.Outlined.Brush, "brush paint art draw"),
        Entry("star", Icons.Outlined.Star, "star favorite"),
        Entry("heart", Icons.Outlined.FavoriteBorder, "heart favorite love like"),
        // Learning / work / tech
        Entry("school", Icons.Outlined.School, "school education graduate study"),
        Entry("book", Icons.Outlined.MenuBook, "book read study library"),
        Entry("book2", Icons.Outlined.Book, "book read notebook"),
        Entry("library", Icons.Outlined.LocalLibrary, "library book read study"),
        Entry("news", Icons.Outlined.Newspaper, "news newspaper article read"),
        Entry("article", Icons.Outlined.Article, "article document read text"),
        Entry("science", Icons.Outlined.Science, "science lab chemistry"),
        Entry("psychology", Icons.Outlined.Psychology, "psychology brain mind think"),
        Entry("code", Icons.Outlined.Code, "code dev programming"),
        Entry("terminal", Icons.Outlined.Terminal, "terminal console code dev"),
        Entry("bug", Icons.Outlined.BugReport, "bug debug issue"),
        Entry("laptop", Icons.Outlined.Laptop, "laptop computer work"),
        Entry("computer", Icons.Outlined.Computer, "computer desktop pc"),
        Entry("phone_device", Icons.Outlined.Smartphone, "phone smartphone mobile device"),
        Entry("tv", Icons.Outlined.Tv, "tv television screen"),
        Entry("keyboard", Icons.Outlined.Keyboard, "keyboard type input"),
        Entry("wifi", Icons.Outlined.Wifi, "wifi internet network"),
        Entry("cloud", Icons.Outlined.Cloud, "cloud storage weather"),
        Entry("web", Icons.Outlined.Language, "web internet browser globe"),
        // Home / lifestyle
        Entry("bed", Icons.Outlined.Bed, "bed sleep bedroom"),
        Entry("sofa", Icons.Outlined.Weekend, "sofa couch living room relax"),
        Entry("chair", Icons.Outlined.Chair, "chair furniture seat"),
        Entry("kitchen", Icons.Outlined.Kitchen, "kitchen fridge appliance"),
        Entry("clothes", Icons.Outlined.Checkroom, "clothes wardrobe closet fashion"),
        Entry("watch", Icons.Outlined.Watch, "watch time clock wear"),
        Entry("flower", Icons.Outlined.LocalFlorist, "flower plant florist nature"),
        Entry("park", Icons.Outlined.Park, "park tree nature outdoor"),
        Entry("sun", Icons.Outlined.WbSunny, "sun weather sunny day"),
        Entry("umbrella", Icons.Outlined.Umbrella, "umbrella rain weather"),
        Entry("bolt", Icons.Outlined.Bolt, "bolt energy power electric"),
        // Utility
        Entry("build", Icons.Outlined.Build, "build tools wrench fix"),
        Entry("handyman", Icons.Outlined.Handyman, "handyman tools repair diy"),
        Entry("construction", Icons.Outlined.Construction, "construction build work tools"),
        Entry("lightbulb", Icons.Outlined.Lightbulb, "lightbulb idea light"),
        Entry("key", Icons.Outlined.Key, "key unlock access"),
        Entry("lock", Icons.Outlined.Lock, "lock secure private password"),
        Entry("security", Icons.Outlined.Security, "security shield protect safe"),
        Entry("settings", Icons.Outlined.Settings, "settings gear config"),
        Entry("folder", Icons.Outlined.Folder, "folder files documents"),
        Entry("bookmark", Icons.Outlined.Bookmark, "bookmark save mark"),
        Entry("calendar", Icons.Outlined.CalendarMonth, "calendar date schedule month"),
        Entry("event", Icons.Outlined.Event, "event calendar date"),
        Entry("alarm", Icons.Outlined.Alarm, "alarm clock time wake"),
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
