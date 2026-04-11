package dev.sagi.monotask.data.demo

import com.google.firebase.Timestamp
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.UserStats
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.service.XpEngine
import java.time.LocalDate

object DemoSeedData {
    const val DEMO_USER_ID = "demo_user"
    const val DEMO_WS_PERSONAL_ID = "demo_ws_personal"
    const val DEMO_WS_EDUCATION_ID = "demo_ws_education"
    const val DEMO_WS_WORK_ID = "demo_ws_work"
    const val DEMO_WS_ANDROID_DEV_ID = "demo_ws_android_dev"
    const val DEMO_FRIEND_ROEI_ID = "demo_friend_roei"
    const val DEMO_FRIEND_OFEK_ID = "demo_friend_ofek"
    const val DEMO_FRIEND_OFIR_ID = "demo_friend_ofir"
    const val DEMO_FRIEND_KEREN_ID = "demo_friend_keren"

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
        currentWorkspaceId = DEMO_WS_ANDROID_DEV_ID,
        friends = listOf(DEMO_FRIEND_ROEI_ID, DEMO_FRIEND_OFEK_ID, DEMO_FRIEND_OFIR_ID, DEMO_FRIEND_KEREN_ID),
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
        avatarPreset = 21,
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
        avatarPreset = 17,
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

    val DEMO_FRIEND_OFIR = User(
        id = DEMO_FRIEND_OFIR_ID,
        displayName = "Ofir Vizenblit",
        email = "",
        avatarPreset = 14,
        level = 8,
        xp = 3200,
        currentWorkspaceId = "",
        friends = listOf(DEMO_USER_ID),
        onboarded = true,
        stats = UserStats(
            totalTasksCompleted = 54,
            aceCount = 31,
            currentStreak = 2,
            longestStreak = 11,
            weeklyXp = 380
        )
    )

    val DEMO_FRIEND_KEREN = User(
        id = DEMO_FRIEND_KEREN_ID,
        displayName = "Keren Kayrich",
        email = "",
        avatarPreset = 20,
        level = 13,
        xp = 9800,
        currentWorkspaceId = "",
        friends = listOf(DEMO_USER_ID),
        onboarded = true,
        stats = UserStats(
            totalTasksCompleted = 142,
            aceCount = 105,
            currentStreak = 12,
            longestStreak = 27,
            weeklyXp = 1430
        )
    )

    val DEMO_WORKSPACES = listOf(
        Workspace(
            id = DEMO_WS_ANDROID_DEV_ID,
            name = "Android Dev",
            ownerId = DEMO_USER_ID,
            currentFocusTaskId = "demo_task_1",
            createdAt = daysAgoTimestamp(90).seconds
        ),
        Workspace(
            id = DEMO_WS_EDUCATION_ID,
            name = "Education",
            ownerId = DEMO_USER_ID,
            currentFocusTaskId = "demo_task_3",
            createdAt = daysAgoTimestamp(60).seconds
        ),
        Workspace(
            id = DEMO_WS_WORK_ID,
            name = "Work",
            ownerId = DEMO_USER_ID,
            currentFocusTaskId = "demo_task_7",
            createdAt = daysAgoTimestamp(45).seconds
        ),
        Workspace(
            id = DEMO_WS_PERSONAL_ID,
            name = "Personal",
            ownerId = DEMO_USER_ID,
            currentFocusTaskId = "demo_task_8",
            createdAt = daysAgoTimestamp(30).seconds
        )
    )

    val DEMO_TASKS = listOf(
        // ── Android Dev ──────────────────────────────────────────────────────
        Task(
            id = "demo_task_1",
            title = "Write unit tests for TaskViewModel",
            description = "Cover the main use cases: add, complete, snooze, and delete.",
            importance = Importance.HIGH,
            dueDate = daysFromNowTimestamp(1),
            workspaceId = DEMO_WS_ANDROID_DEV_ID,
            tags = listOf("dev", "testing"),
            snoozeCount = 0,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(2)
        ),
        Task(
            id = "demo_task_2",
            title = "Fix focus card animation stutter",
            description = "Stutter occurs on older devices during swipe gesture. Profile and reduce recompositions.",
            importance = Importance.HIGH,
            dueDate = null,
            workspaceId = DEMO_WS_ANDROID_DEV_ID,
            tags = listOf("dev", "bug"),
            snoozeCount = 1,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(3)
        ),
        Task(
            id = "demo_task_4",
            title = "Prepare app store screenshots",
            description = "Update screenshots for all three screen sizes with the new UI.",
            importance = Importance.MEDIUM,
            dueDate = daysFromNowTimestamp(3),
            workspaceId = DEMO_WS_ANDROID_DEV_ID,
            tags = listOf("design"),
            snoozeCount = 0,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(2)
        ),
        // Archived — Android Dev
        Task(
            id = "demo_arch_a1",
            title = "Set up CI/CD pipeline",
            description = "Configure GitHub Actions for automated build and test on each PR.",
            importance = Importance.HIGH,
            dueDate = null,
            workspaceId = DEMO_WS_ANDROID_DEV_ID,
            tags = listOf("dev", "infra"),
            snoozeCount = 0,
            completed = true,
            completedAt = daysAgoTimestamp(5),
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(10)
        ),
        Task(
            id = "demo_arch_a2",
            title = "Refactor authentication module",
            description = "Split AuthViewModel into separate login and register flows.",
            importance = Importance.HIGH,
            dueDate = null,
            workspaceId = DEMO_WS_ANDROID_DEV_ID,
            tags = listOf("dev", "refactor"),
            snoozeCount = 1,
            completed = true,
            completedAt = daysAgoTimestamp(8),
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(14)
        ),
        // ── Education ────────────────────────────────────────────────────────
        Task(
            id = "demo_task_3",
            title = "Submit OS lab report",
            description = "Lab 4 — memory management and page replacement algorithms.",
            importance = Importance.HIGH,
            dueDate = daysAgoTimestamp(1),  // overdue
            workspaceId = DEMO_WS_EDUCATION_ID,
            tags = listOf("uni", "report"),
            snoozeCount = 2,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(7)
        ),
        Task(
            id = "demo_task_5",
            title = "Study for data structures exam",
            description = "Focus on trees, graphs, and dynamic programming.",
            importance = Importance.HIGH,
            dueDate = daysFromNowTimestamp(0),  // due today
            workspaceId = DEMO_WS_EDUCATION_ID,
            tags = listOf("uni", "study"),
            snoozeCount = 0,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(4)
        ),
        Task(
            id = "demo_task_6",
            title = "Read \"Clean Architecture\" ch. 8",
            description = "",
            importance = Importance.LOW,
            dueDate = null,
            workspaceId = DEMO_WS_EDUCATION_ID,
            tags = listOf("reading"),
            snoozeCount = 0,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(5)
        ),
        // Archived — Education
        Task(
            id = "demo_arch_e1",
            title = "Complete algorithms homework",
            description = "Problems 3–7 from chapter 6.",
            importance = Importance.HIGH,
            dueDate = null,
            workspaceId = DEMO_WS_EDUCATION_ID,
            tags = listOf("uni", "homework"),
            snoozeCount = 0,
            completed = true,
            completedAt = daysAgoTimestamp(4),
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(9)
        ),
        Task(
            id = "demo_arch_e2",
            title = "Submit project proposal",
            description = "Final-year project proposal document, max 2 pages.",
            importance = Importance.HIGH,
            dueDate = null,
            workspaceId = DEMO_WS_EDUCATION_ID,
            tags = listOf("uni", "project"),
            snoozeCount = 1,
            completed = true,
            completedAt = daysAgoTimestamp(12),
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(18)
        ),
        // ── Work ─────────────────────────────────────────────────────────────
        Task(
            id = "demo_task_7",
            title = "Reply to beta tester feedback",
            description = "Check the latest feedback thread and respond to open questions.",
            importance = Importance.MEDIUM,
            dueDate = daysFromNowTimestamp(0),  // due today
            workspaceId = DEMO_WS_WORK_ID,
            tags = listOf("feedback"),
            snoozeCount = 1,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(1)
        ),
        Task(
            id = "demo_task_9",
            title = "Update project documentation",
            description = "Sync the README and architecture diagrams with recent changes.",
            importance = Importance.MEDIUM,
            dueDate = daysFromNowTimestamp(4),
            workspaceId = DEMO_WS_WORK_ID,
            tags = listOf("docs"),
            snoozeCount = 0,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(2)
        ),
        // Archived — Work
        Task(
            id = "demo_arch_w1",
            title = "Update team Confluence page",
            description = "Add notes from last sprint retrospective.",
            importance = Importance.MEDIUM,
            dueDate = null,
            workspaceId = DEMO_WS_WORK_ID,
            tags = listOf("docs"),
            snoozeCount = 0,
            completed = true,
            completedAt = daysAgoTimestamp(3),
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(7)
        ),
        Task(
            id = "demo_arch_w2",
            title = "Review PR from colleague",
            description = "Review the feature branch for the new onboarding flow.",
            importance = Importance.MEDIUM,
            dueDate = null,
            workspaceId = DEMO_WS_WORK_ID,
            tags = listOf("review"),
            snoozeCount = 0,
            completed = true,
            completedAt = daysAgoTimestamp(6),
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(10)
        ),
        // ── Personal ─────────────────────────────────────────────────────────
        Task(
            id = "demo_task_8",
            title = "Book gym session for this week",
            description = "",
            importance = Importance.LOW,
            dueDate = daysFromNowTimestamp(7),
            workspaceId = DEMO_WS_PERSONAL_ID,
            tags = listOf("personal"),
            snoozeCount = 0,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(1)
        ),
        Task(
            id = "demo_task_10",
            title = "Call dentist to schedule checkup",
            description = "",
            importance = Importance.LOW,
            dueDate = null,
            workspaceId = DEMO_WS_PERSONAL_ID,
            tags = listOf("personal", "health"),
            snoozeCount = 0,
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(3)
        ),
        // Archived — Personal
        Task(
            id = "demo_arch_p1",
            title = "Renew gym membership",
            description = "",
            importance = Importance.MEDIUM,
            dueDate = null,
            workspaceId = DEMO_WS_PERSONAL_ID,
            tags = listOf("personal"),
            snoozeCount = 0,
            completed = true,
            completedAt = daysAgoTimestamp(2),
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(6)
        ),
        Task(
            id = "demo_arch_p2",
            title = "Buy groceries for the week",
            description = "",
            importance = Importance.LOW,
            dueDate = null,
            workspaceId = DEMO_WS_PERSONAL_ID,
            tags = listOf("personal"),
            snoozeCount = 0,
            completed = true,
            completedAt = daysAgoTimestamp(5),
            ownerId = DEMO_USER_ID,
            createdAt = daysAgoTimestamp(8)
        )
    ).map { it.copy(currentXp = XpEngine.calculateTaskXp(it)) }

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

    val DEMO_FRIEND_OFIR_ACTIVITY: List<DailyActivity> = buildList {
        val today = LocalDate.now()
        val pattern = listOf(
            1 to (1 to 160), 2 to (2 to 280),
            4 to (3 to 390),
            6 to (1 to 140), 7 to (2 to 260),
            9 to (3 to 410), 10 to (1 to 130),
            12 to (2 to 300),
            14 to (3 to 370), 15 to (1 to 150),
            17 to (2 to 270),
            19 to (3 to 400), 20 to (2 to 240),
            22 to (1 to 120), 23 to (3 to 360),
            25 to (2 to 290),
            27 to (1 to 140), 28 to (3 to 380)
        )
        for ((daysBack, data) in pattern) {
            add(DailyActivity(
                dateEpochDay = today.minusDays(daysBack.toLong()).toEpochDay(),
                tasksCompleted = data.first,
                xpEarned = data.second
            ))
        }
    }

    val DEMO_FRIEND_KEREN_ACTIVITY: List<DailyActivity> = buildList {
        val today = LocalDate.now()
        val pattern = listOf(
            1 to (4 to 620), 2 to (5 to 750),
            3 to (3 to 450),
            5 to (4 to 590), 6 to (2 to 310),
            7 to (5 to 730),
            9 to (3 to 470), 10 to (4 to 600),
            11 to (5 to 780), 12 to (2 to 290),
            14 to (4 to 560), 15 to (3 to 420),
            16 to (5 to 710),
            18 to (4 to 580), 19 to (2 to 300),
            21 to (5 to 740), 22 to (3 to 440),
            23 to (4 to 610),
            25 to (3 to 460), 26 to (5 to 700),
            28 to (4 to 570), 29 to (3 to 410)
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
