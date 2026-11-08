package com.example.data.preset

import com.example.data.model.VipMembership

data class VipPreset(
    val name: String,
    val monthlyCost: Double,
    val windowStartDay: Int = 1,
    val windowEndDay: Int = 5,
    val category: String,
    val portalUrl: String,
    val skipInstructions: String,
    val colorHex: Long,
    val cancellationTip: String
) {
    fun toVipMembership(): VipMembership = VipMembership(
        name = name,
        monthlyCost = monthlyCost,
        currency = "$",
        windowStartDay = windowStartDay,
        windowEndDay = windowEndDay,
        category = category,
        portalUrl = portalUrl,
        skipInstructions = skipInstructions,
        notes = "VIP Membership",
        colorHex = colorHex
    )
}

object VipPresetsCatalog {
    val presets = listOf(
        VipPreset(
            name = "Fabletics",
            monthlyCost = 69.95,
            windowStartDay = 1,
            windowEndDay = 5,
            category = "Activewear",
            portalUrl = "https://www.fabletics.com/account",
            skipInstructions = "1. Log into your Fabletics account between the 1st and 5th.\n2. On your dashboard, look for the 'Skip the Month' button.\n3. Click through the confirmation prompt until you see 'You have skipped this month' to avoid the $69.95 member charge.\n4. Take a screenshot of the confirmation page for your records.",
            colorHex = 0xFFE11D48,
            cancellationTip = "You can cancel online in Account Settings or call Customer Service at 1-844-322-5384 (available 24/7). You will retain any unused member credits."
        ),
        VipPreset(
            name = "Savage X Fenty",
            monthlyCost = 49.95,
            windowStartDay = 1,
            windowEndDay = 5,
            category = "Lingerie & Lounge",
            portalUrl = "https://www.savagex.com/account",
            skipInstructions = "1. Log into your Savage X account between the 1st and 5th.\n2. On the home page banner or Account dashboard, tap 'Skip the Month'.\n3. Confirm when prompted to avoid being charged a $49.95 VIP credit.",
            colorHex = 0xFF8B5CF6,
            cancellationTip = "Cancel online via live chat or call 1-855-728-2439. Unused member credits remain valid even after canceling."
        ),
        VipPreset(
            name = "JustFab",
            monthlyCost = 49.95,
            windowStartDay = 1,
            windowEndDay = 5,
            category = "Shoes & Fashion",
            portalUrl = "https://www.justfab.com/account",
            skipInstructions = "1. Log into JustFab between 1st and 5th.\n2. Navigate to your Boutique or Account page.\n3. Tap 'Skip This Month' before 11:59 PM PT on the 5th.\n4. Check your email for a skip confirmation.",
            colorHex = 0xFFEC4899,
            cancellationTip = "Cancel by calling 1-866-337-0906 or via online chat support."
        ),
        VipPreset(
            name = "ShoeDazzle",
            monthlyCost = 49.95,
            windowStartDay = 1,
            windowEndDay = 5,
            category = "Shoes & Accessories",
            portalUrl = "https://www.shoedazzle.com/account",
            skipInstructions = "1. Sign in between the 1st and 5th.\n2. Tap 'Skip the Month' in your personalized showroom.\n3. Complete the confirmation step.",
            colorHex = 0xFFF97316,
            cancellationTip = "Call 1-888-508-1888 or live chat on ShoeDazzle.com to cancel membership anytime."
        ),
        VipPreset(
            name = "Adore Me",
            monthlyCost = 39.95,
            windowStartDay = 1,
            windowEndDay = 5,
            category = "Lingerie & Sleepwear",
            portalUrl = "https://www.adoreme.com/customer/account/login",
            skipInstructions = "1. Sign into Adore Me between the 1st and 5th.\n2. Go to your Showroom and select 'Skip This Month'.\n3. Confirm to avoid the $39.95 VIP store credit charge.",
            colorHex = 0xFF06B6D4,
            cancellationTip = "Cancel via their automated live chat assistant or call 1-800-433-2367."
        ),
        VipPreset(
            name = "Bespoke Post",
            monthlyCost = 49.00,
            windowStartDay = 1,
            windowEndDay = 5,
            category = "Lifestyle & Gear",
            portalUrl = "https://www.bespokepost.com/box",
            skipInstructions = "1. On the 1st, Bespoke Post previews your club box.\n2. Visit your account by the 5th at 11:59 PM.\n3. Click 'Skip This Box' or swap items. Skipping is 100% free with unlimited skips.",
            colorHex = 0xFF78716C,
            cancellationTip = "Pause or cancel anytime right inside your account settings with no phone call required."
        ),
        VipPreset(
            name = "Book of the Month",
            monthlyCost = 17.99,
            windowStartDay = 1,
            windowEndDay = 5,
            category = "Books",
            portalUrl = "https://www.bookofthemonth.com",
            skipInstructions = "1. When new monthly book picks drop (usually by the 1st), log into BOTM.\n2. If no titles catch your eye, scroll down and tap 'Skip this month'.\n3. Your credit rolls over or you won't be billed.",
            colorHex = 0xFF0284C7,
            cancellationTip = "Cancel directly on the website under Membership settings."
        ),
        VipPreset(
            name = "BarkBox VIP",
            monthlyCost = 35.00,
            windowStartDay = 1,
            windowEndDay = 5,
            category = "Pets",
            portalUrl = "https://www.barkbox.com",
            skipInstructions = "1. Log into your BarkBox account before the 5th.\n2. Navigate to Subscriptions.\n3. Choose 'Skip Next Delivery' to pause for the current month.",
            colorHex = 0xFF14B8A6,
            cancellationTip = "Chat with customer support or update subscription settings online."
        )
    )
}
