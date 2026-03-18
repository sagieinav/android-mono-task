package dev.sagi.monotask.domain.util

import androidx.annotation.DrawableRes
import dev.sagi.monotask.R

object DiceBearHelper {
    private const val BASE_AVATAR_URL = "https://api.dicebear.com/9.x"
    private const val RADIUS = 50
    private const val MOUTH = "laughing,nervous,pucker,smile,smirk,surprised" // prevent sad and frown
    private const val STYLE = "micah"  // chosen avatar style from DiceBear

    data class AvatarPreset(@DrawableRes val drawable: Int)

    val PRESETS = listOf(
        AvatarPreset(R.drawable.avatar_micah01),
        AvatarPreset(R.drawable.avatar_micah02),
        AvatarPreset(R.drawable.avatar_micah03),
        AvatarPreset(R.drawable.avatar_micah04),
        AvatarPreset(R.drawable.avatar_micah05),
        AvatarPreset(R.drawable.avatar_micah06),
        AvatarPreset(R.drawable.avatar_micah07),
        AvatarPreset(R.drawable.avatar_micah08),
        AvatarPreset(R.drawable.avatar_micah09),
        AvatarPreset(R.drawable.avatar_micah10),
        AvatarPreset(R.drawable.avatar_micah11),
        AvatarPreset(R.drawable.avatar_micah12),
        AvatarPreset(R.drawable.avatar_micah13),
        AvatarPreset(R.drawable.avatar_micah14),
        AvatarPreset(R.drawable.avatar_micah15),
        AvatarPreset(R.drawable.avatar_micah16),
        AvatarPreset(R.drawable.avatar_micah17),
        AvatarPreset(R.drawable.avatar_micah18),
        AvatarPreset(R.drawable.avatar_micah19),
        AvatarPreset(R.drawable.avatar_micah20),
        AvatarPreset(R.drawable.avatar_micah21),
        AvatarPreset(R.drawable.avatar_micah22),
        AvatarPreset(R.drawable.avatar_micah23),
    )

    fun getAvatarUrl(seed: String, size: Int = 512): String {
        val safeSeed = seed.ifEmpty { "default" }
        return "$BASE_AVATAR_URL/$STYLE/png?seed=$safeSeed&size=$size&radius=$RADIUS&mouth=$MOUTH"
    }
}
