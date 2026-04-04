package dev.sagi.monotask.util

import dev.sagi.monotask.designsystem.theme.IconPack

object DiceBearHelper {
    private const val BASE_AVATAR_URL = "https://api.dicebear.com/9.x"
    private const val RADIUS = 50
    private const val MOUTH = "laughing,nervous,pucker,smile,smirk,surprised" // prevent sad and frown
    private const val STYLE = "micah"  // chosen avatar style from DiceBear

    fun getAvatarUrl(seed: String, size: Int = 512): String {
        val safeSeed = seed.ifEmpty { "default" }
        return "$BASE_AVATAR_URL/$STYLE/png?seed=$safeSeed&size=$size&radius=$RADIUS&mouth=$MOUTH"
    }
}