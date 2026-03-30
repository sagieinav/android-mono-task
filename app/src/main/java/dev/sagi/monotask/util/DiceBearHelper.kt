package dev.sagi.monotask.util

import dev.sagi.monotask.R

object DiceBearHelper {
    private const val BASE_AVATAR_URL = "https://api.dicebear.com/9.x"
    private const val RADIUS = 50
    private const val MOUTH = "laughing,nervous,pucker,smile,smirk,surprised" // prevent sad and frown
    private const val STYLE = "micah"  // chosen avatar style from DiceBear

    val PRESETS: List<Int> = listOf(
        R.drawable.avatar_micah01,
        R.drawable.avatar_micah02,
        R.drawable.avatar_micah03,
        R.drawable.avatar_micah04,
        R.drawable.avatar_micah05,
        R.drawable.avatar_micah06,
        R.drawable.avatar_micah07,
        R.drawable.avatar_micah08,
        R.drawable.avatar_micah09,
        R.drawable.avatar_micah10,
        R.drawable.avatar_micah11,
        R.drawable.avatar_micah12,
        R.drawable.avatar_micah13,
        R.drawable.avatar_micah14,
        R.drawable.avatar_micah15,
        R.drawable.avatar_micah16,
        R.drawable.avatar_micah17,
        R.drawable.avatar_micah18,
        R.drawable.avatar_micah19,
        R.drawable.avatar_micah20,
        R.drawable.avatar_micah21,
        R.drawable.avatar_micah22,
        R.drawable.avatar_micah23,
    )

    fun getAvatarUrl(seed: String, size: Int = 512): String {
        val safeSeed = seed.ifEmpty { "default" }
        return "$BASE_AVATAR_URL/$STYLE/png?seed=$safeSeed&size=$size&radius=$RADIUS&mouth=$MOUTH"
    }
}