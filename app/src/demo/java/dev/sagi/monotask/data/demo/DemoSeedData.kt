package dev.sagi.monotask.data.demo

import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.UserStats
import dev.sagi.monotask.data.model.Workspace
import java.time.LocalDate

object DemoSeedData {
    const val DEMO_USER_ID = "demo_user"
    const val DEMO_WS_MAIN_ID = "demo_ws_main"
    const val DEMO_WS_SIDE_ID = "demo_ws_side"
    const val DEMO_FRIEND_ROEI_ID = "demo_friend_roei"
    const val DEMO_FRIEND_OFEK_ID = "demo_friend_ofek"

    private fun daysAgoTimestamp(days: Long): Timestamp {
        val epochSeconds = LocalDate.now().minusDays(days).toEpochDay() * 86400L
        return Timestamp(epochSeconds, 0)
    }

    private fun daysFromNowTimestamp(days: Long): Timestamp {
        val epochSeconds = LocalDate.now().plusDays(days).toEpochDay() * 86400L
        return Timestamp(epochSeconds, 0)
    }

    val DEMO_USER = User(
        id = DEMO_USER_ID,
        displayName = "Sagi Einav",
        email = "sagi@monotask.app",
        avatarPreset = 1,
        level = 15,
        xp = 12700,
        currentWorkspaceId = DEMO_WS_MAIN_ID,
        friends = listOf(DEMO_FRIEND_ROEI_ID, DEMO_FRIEND_OFEK_ID),
        onboarded = true,
        hyperfocusModeEnabled = false,
        dueDateWeight = 0.6f,
        stats = UserStats(
            totalTasksCompleted = 88,
            aceCount = 69,
            currentStreak = 9,
            longestStreak = 31,
            weeklyXp = 2750
        )
    )

    val DEMO_FRIEND_ROEI = User(
        id = DEMO_FRIEND_ROEI_ID,
        displayName = "Roei Zalah",
        email = "",
        avatarPreset = 7,
        level = 10,
        xp = 5500,
        currentWorkspaceId = "",
        friends = listOf(DEMO_USER_ID),
        onboarded = true,
        stats = UserStats(
            totalTasksCompleted = 98,
            aceCount = 70,
            currentStreak = 4,
            longestStreak = 15,
            weeklyXp = 540
        )
    )

    val DEMO_FRIEND_OFEK = User(
        id = DEMO_FRIEND_OFEK_ID,
        displayName = "Ofek Fanian",
        email = "",
        avatarPreset = 2,
        level = 11,
        xp = 6800,
        currentWorkspaceId = "",
        friends = listOf(DEMO_USER_ID),
        onboarded = true,
        stats = UserStats(
            totalTasksCompleted = 113,
            aceCount = 80,
            currentStreak = 6,
            longestStreak = 18,
            weeklyXp = 660
        )
    )

    val DEMO_WORKSPACES = listOf(
        Workspace(
            id = DEMO_WS_MAIN_ID,
            name = "MonoTask",
            ownerId = DEMO_USER_ID,
            currentFocusTaskId = "demo_task_1",
            createdAt = daysAgoTimestamp(90).seconds
        ),
        Workspace(
            id = DEMO_WS_SIDE_ID,
            name = "University",
            ownerId = DEMO_USER_ID,
            currentFocusTaskId = null,
            createdAt = daysAgoTimestamp(60).seconds
        )
    )

    val DEMO_TASKS = listOf(
        Task(
            id = "demo_task_1",
            title = "Write unit tests for TaskViewModel",
            description = "Cover the main use cases: add, complete, snooze, and delete.",
            importance = Importance.HIGH,
            dueDate = daysFromNowTimestamp(1),
            workspaceId = DEMO_WS_MAIN_ID,
            tags = listOf("dev", "testing"),
            snoozeCount = 0,
            currentXp = 190,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(2)
        ),
        Task(
            id = "demo_task_2",
            title = "Fix focus card animation stutter",
            description = "Stutter occurs on older devices during swipe gesture. Profile and reduce recompositions.",
            importance = Importance.HIGH,
            dueDate = null,
            workspaceId = DEMO_WS_MAIN_ID,
            tags = listOf("dev", "bug"),
            snoozeCount = 1,
            currentXp = 160,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(3)
        ),
        Task(
            id = "demo_task_3",
            title = "Submit OS lab report",
            description = "Lab 4 — memory management and page replacement algorithms.",
            importance = Importance.HIGH,
            dueDate = daysAgoTimestamp(1),
            workspaceId = DEMO_WS_SIDE_ID,
            tags = listOf("uni", "report"),
            snoozeCount = 2,
            currentXp = 120,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(7)
        ),
        Task(
            id = "demo_task_4",
            title = "Prepare app store screenshots",
            description = "Update screenshots for all three screen sizes with the new UI.",
            importance = Importance.MEDIUM,
            dueDate = daysFromNowTimestamp(3),
            workspaceId = DEMO_WS_MAIN_ID,
            tags = listOf("design"),
            snoozeCount = 0,
            currentXp = 140,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(2)
        ),
        Task(
            id = "demo_task_5",
            title = "Study for data structures exam",
            description = "Focus on trees, graphs, and dynamic programming.",
            importance = Importance.HIGH,
            dueDate = daysFromNowTimestamp(5),
            workspaceId = DEMO_WS_SIDE_ID,
            tags = listOf("uni", "study"),
            snoozeCount = 0,
            currentXp = 170,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(4)
        ),
        Task(
            id = "demo_task_6",
            title = "Read \"Clean Architecture\" ch. 8",
            description = "",
            importance = Importance.LOW,
            dueDate = null,
            workspaceId = DEMO_WS_SIDE_ID,
            tags = listOf("reading"),
            snoozeCount = 0,
            currentXp = 130,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(5)
        ),
        Task(
            id = "demo_task_7",
            title = "Reply to beta tester feedback",
            description = "Check the latest feedback thread and respond to open questions.",
            importance = Importance.MEDIUM,
            dueDate = daysFromNowTimestamp(2),
            workspaceId = DEMO_WS_MAIN_ID,
            tags = listOf("feedback"),
            snoozeCount = 1,
            currentXp = 110,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(1)
        ),
        Task(
            id = "demo_task_8",
            title = "Book gym session for this week",
            description = "",
            importance = Importance.LOW,
            dueDate = daysFromNowTimestamp(7),
            workspaceId = DEMO_WS_MAIN_ID,
            tags = listOf("personal"),
            snoozeCount = 0,
            currentXp = 100,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(1)
        )
    )

    // Deterministic activity — no random() calls so data is stable across launches.
    val DEMO_ACTIVITY: List<DailyActivity> = buildList {
        val today = LocalDate.now()
        // days back → (tasks, xp); absent = rest day
        val pattern = listOf(
            1 to (3 to 480),
            2 to (4 to 620),
            3 to (2 to 310),
            4 to (5 to 740),
            5 to (3 to 450),
            6 to (1 to 150),
            // day 7 = rest
            8 to (4 to 580),
            9 to (2 to 290),
            10 to (3 to 420),
            11 to (5 to 760),
            12 to (1 to 130),
            // day 13 = rest
            // day 14 = rest
            15 to (4 to 560),
            16 to (3 to 400),
            17 to (2 to 280),
            18 to (4 to 610),
            19 to (1 to 140),
            // day 20 = rest
            21 to (3 to 460),
            22 to (5 to 720),
            23 to (2 to 310),
            24 to (4 to 550),
            // day 25 = rest
            26 to (2 to 270),
            27 to (3 to 390),
            28 to (4 to 530),
            // day 29 = rest
            30 to (3 to 470)
        )
        for ((daysBack, data) in pattern) {
            add(DailyActivity(
                dateEpochDay = today.minusDays(daysBack.toLong()).toEpochDay(),
                tasksCompleted = data.first,
                xpEarned = data.second
            ))
        }
        // Days 31–90: deterministic sparse pattern
        val sparsePattern = listOf(
            31 to (2 to 300), 32 to (3 to 410), 34 to (1 to 160), 35 to (4 to 540),
            36 to (2 to 280), 38 to (3 to 380), 39 to (2 to 250), 40 to (4 to 520),
            41 to (1 to 130), 43 to (3 to 440), 44 to (2 to 310), 45 to (5 to 650),
            46 to (2 to 270), 48 to (3 to 400), 49 to (1 to 170), 50 to (4 to 500),
            51 to (2 to 290), 53 to (3 to 360), 54 to (2 to 240), 55 to (3 to 430),
            56 to (4 to 560), 58 to (2 to 300), 59 to (1 to 150), 60 to (3 to 420),
            61 to (2 to 260), 63 to (4 to 510), 64 to (3 to 380), 65 to (2 to 310),
            66 to (1 to 140), 68 to (3 to 450), 69 to (2 to 290), 70 to (4 to 530),
            71 to (3 to 400), 73 to (2 to 270), 74 to (1 to 130), 75 to (3 to 390),
            76 to (4 to 520), 78 to (2 to 280), 79 to (3 to 360), 80 to (2 to 240),
            81 to (4 to 500), 83 to (1 to 150), 84 to (3 to 410), 85 to (2 to 300),
            86 to (4 to 540), 88 to (3 to 420), 89 to (2 to 260), 90 to (3 to 390)
        )
        for ((daysBack, data) in sparsePattern) {
            add(DailyActivity(
                dateEpochDay = today.minusDays(daysBack.toLong()).toEpochDay(),
                tasksCompleted = data.first,
                xpEarned = data.second
            ))
        }
    }

    val DEMO_FRIEND_ROEI_ACTIVITY: List<DailyActivity> = buildList {
        val today = LocalDate.now()
        val pattern = listOf(
            1 to (2 to 340), 2 to (3 to 470),
            4 to (1 to 160), 5 to (4 to 590),
            6 to (2 to 290),
            8 to (3 to 420), 9 to (1 to 140),
            10 to (4 to 510), 11 to (2 to 310),
            13 to (3 to 440),
            15 to (2 to 280), 16 to (4 to 560),
            18 to (1 to 120), 19 to (3 to 400),
            20 to (2 to 270),
            22 to (4 to 530), 23 to (3 to 390),
            25 to (1 to 150), 26 to (2 to 300),
            28 to (3 to 450), 29 to (2 to 260)
        )
        for ((daysBack, data) in pattern) {
            add(DailyActivity(
                dateEpochDay = today.minusDays(daysBack.toLong()).toEpochDay(),
                tasksCompleted = data.first,
                xpEarned = data.second
            ))
        }
    }

    val DEMO_FRIEND_OFEK_ACTIVITY: List<DailyActivity> = buildList {
        val today = LocalDate.now()
        val pattern = listOf(
            1 to (3 to 510), 2 to (4 to 640),
            3 to (2 to 320),
            5 to (3 to 460), 6 to (1 to 150),
            7 to (4 to 580),
            9 to (2 to 300), 10 to (3 to 430),
            11 to (5 to 700), 12 to (1 to 130),
            14 to (3 to 470), 15 to (2 to 290),
            16 to (4 to 560),
            18 to (3 to 400), 19 to (2 to 280),
            21 to (4 to 590), 22 to (1 to 160),
            23 to (3 to 440),
            25 to (2 to 310), 26 to (4 to 530),
            28 to (3 to 410), 29 to (2 to 270)
        )
        for ((daysBack, data) in pattern) {
            add(DailyActivity(
                dateEpochDay = today.minusDays(daysBack.toLong()).toEpochDay(),
                tasksCompleted = data.first,
                xpEarned = data.second
            ))
        }
    }
}
