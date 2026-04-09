package dev.sagi.monotask.util

import dev.sagi.monotask.data.demo.DemoSeedData

object AuthUtils {

    suspend fun awaitUid(): String = DemoSeedData.DEMO_USER_ID

    fun currentUidOrNull(): String = DemoSeedData.DEMO_USER_ID
}
