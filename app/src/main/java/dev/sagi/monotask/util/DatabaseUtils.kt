package dev.sagi.monotask.util

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.data.model.Importance
import dev.sagi.monotask.data.model.Task
import dev.sagi.monotask.data.model.User
import dev.sagi.monotask.data.model.UserStats
import dev.sagi.monotask.data.model.Workspace
import dev.sagi.monotask.domain.service.XpEngine
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

object DatabaseUtils {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportCurrentDataForReview(db: FirebaseFirestore, userId: String) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            val activitySnap = db.collection("users").document(userId).collection("activity").get().await()
            val tasksSnap = db.collection("users").document(userId).collection("tasks").get().await()
            val wsSnap = db.collection("users").document(userId).collection("workspaces").get().await()
            val exportMap = mapOf(
                "user" to (userDoc.data ?: emptyMap<String, Any>()),
                "activity" to activitySnap.documents.map { mapOf("id" to it.id, "data" to it.data) },
                "tasks" to tasksSnap.documents.map { mapOf("id" to it.id, "data" to it.data) },
                "workspaces" to wsSnap.documents.map { mapOf("id" to it.id, "data" to it.data) }
            )
            Log.d("DB_EXPORT", gson.toJson(exportMap))
        } catch (e: Exception) {
            Log.e("DB_EXPORT", "Failed to export", e)
        }
    }

    suspend fun seedDatabase(db: FirebaseFirestore) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: run {
            Log.e("DB_SEED", "Abort: no authenticated user.")
            return
        }
        Log.d("DB_SEED", "Seeding for $userId")

        try {
            val today = LocalDate.now()

            // ─────────────────────────────────────────────
            // 1. WORKSPACE IDs  (reuse existing names so
            //    duplicates don't appear in the UI)
            // ─────────────────────────────────────────────
            val WS_PROJECTS  = "ws_projects"
            val WS_EDUCATION = "ws_education"
            val WS_WORK      = "ws_work"
            val WS_PERSONAL  = "ws_personal"

            // ─────────────────────────────────────────────
            // 2. TASKS  (title, importance, dueInDays or null, completed, daysAgoCompleted)
            //    dueInDays: null = no due date
            //    daysAgoCompleted: -1 = not completed (open task)
            // ─────────────────────────────────────────────
            data class TaskDef(
                val id: String,
                val title: String,
                val description: String = "",
                val importance: Importance,
                val workspaceId: String,
                val tags: List<String> = emptyList(),
                val dueInDays: Int? = null,
                val snoozeCount: Int = 0,
                val daysAgoCompleted: Int = -1   // -1 = open
            )

            val taskDefs = listOf(
                // ── ws_projects: open tasks (4) ──────────────────
                TaskDef("t_p1", "Add Task BottomSheet UX", "Keyboard handling, field layout",
                    Importance.HIGH, WS_PROJECTS, listOf("ux","sheet","keyboard"), dueInDays = 1),
                TaskDef("t_p2", "Sheets/Dialogs UI States", "Auto-focus on text box upon open",
                    Importance.HIGH, WS_PROJECTS, listOf("uistate","tweak"), dueInDays = 0),
                TaskDef("t_p3", "Build Brief Screen",
                    importance = Importance.HIGH, workspaceId = WS_PROJECTS,
                    tags = listOf("brief","ui","compose"), dueInDays = 5),
                TaskDef("t_p4", "Different animations for FocusCards appearing after a completion vs snooze",
                    importance = Importance.HIGH, workspaceId = WS_PROJECTS,
                    tags = listOf("focus","animation","card"), dueInDays = 3),

                // ── ws_projects: completed tasks (14) ────────────
                TaskDef("t_p5",  "Critical bug: undo completion", "Breaks XP on undo",
                    Importance.HIGH, WS_PROJECTS, listOf("bug","critical","focus.card"), daysAgoCompleted = 1),
                TaskDef("t_p6",  "Fix exclamation mark alt icons",
                    importance = Importance.MEDIUM, workspaceId = WS_PROJECTS, tags = listOf("ui"), daysAgoCompleted = 2),
                TaskDef("t_p7",  "FocusCard change",
                    importance = Importance.MEDIUM, workspaceId = WS_PROJECTS, tags = listOf("focus","ui"), daysAgoCompleted = 2),
                TaskDef("t_p8",  "Add an option to delete a friend", "Maybe swipe action",
                    Importance.HIGH, WS_PROJECTS, listOf("friends","ux"), daysAgoCompleted = 3),
                TaskDef("t_p9",  "Friends' data updates",
                    importance = Importance.HIGH, workspaceId = WS_PROJECTS, tags = listOf("bug","data","firebase"), daysAgoCompleted = 3),
                TaskDef("t_p10", "Make KanbanScreen default to open in active mode",
                    importance = Importance.MEDIUM, workspaceId = WS_PROJECTS, tags = listOf("kanban","archive","ux"), daysAgoCompleted = 4),
                TaskDef("t_p11", "FAQ BottomSheet", "Create FAQ BS questions & answers",
                    Importance.LOW, WS_PROJECTS, listOf("faq","settings"), daysAgoCompleted = 4),
                TaskDef("t_p12", "Add an option to edit the current active task",
                    importance = Importance.HIGH, workspaceId = WS_PROJECTS, tags = listOf("ux","edit","task"), daysAgoCompleted = 5),
                TaskDef("t_p13", "Screen rotation bug", "Closes BottomSheet on rotation",
                    Importance.MEDIUM, WS_PROJECTS, listOf("bug","rotation"), daysAgoCompleted = 5),
                TaskDef("t_p14", "Level up indication",
                    importance = Importance.MEDIUM, workspaceId = WS_PROJECTS, tags = listOf("ux","ui","xp","level"), daysAgoCompleted = 6),
                TaskDef("t_p15", "TextDialogs and BottomSheets UX",
                    importance = Importance.MEDIUM, workspaceId = WS_PROJECTS, tags = listOf("ux","textbox"), daysAgoCompleted = 6),
                TaskDef("t_p16", "Sheets/Dialogs UI States — round 1",
                    importance = Importance.HIGH, workspaceId = WS_PROJECTS, tags = listOf("uistate"), daysAgoCompleted = 7),
                TaskDef("t_p17", "Make the 'task created' snackbar only appear when not in empty state",
                    importance = Importance.MEDIUM, workspaceId = WS_PROJECTS, tags = listOf("ux","snackbar","focus"), daysAgoCompleted = 8),
                TaskDef("t_p18", "Consider ditching harabara",
                    importance = Importance.LOW, workspaceId = WS_PROJECTS, tags = listOf("harabara"), daysAgoCompleted = 9),

                // ── ws_education: 1 open, 3 completed ────────────
                TaskDef("t_e1", "Study for data structures exam", "Focus on trees, graphs, DP",
                    Importance.HIGH, WS_EDUCATION, listOf("uni","study"), dueInDays = 0),
                TaskDef("t_e2", "Complete algorithms homework", "Problems 3–7 from chapter 6",
                    Importance.HIGH, WS_EDUCATION, listOf("uni","homework"), daysAgoCompleted = 4),
                TaskDef("t_e3", "Submit OS lab report", "Lab 4 — memory management",
                    Importance.HIGH, WS_EDUCATION, listOf("uni","report"), daysAgoCompleted = 2),
                TaskDef("t_e4", "Read \"Clean Architecture\" ch. 8",
                    importance = Importance.LOW, workspaceId = WS_EDUCATION, tags = listOf("reading"), daysAgoCompleted = 6),

                // ── ws_work: 1 open, 2 completed ─────────────────
                TaskDef("t_w1", "Reply to beta tester feedback", "Check feedback thread",
                    Importance.MEDIUM, WS_WORK, listOf("feedback"), dueInDays = 0, snoozeCount = 1),
                TaskDef("t_w2", "Update project documentation", "Sync README & architecture diagrams",
                    Importance.MEDIUM, WS_WORK, listOf("docs"), daysAgoCompleted = 3),
                TaskDef("t_w3", "Review PR from colleague",
                    importance = Importance.MEDIUM, workspaceId = WS_WORK, tags = listOf("review"), daysAgoCompleted = 5),

                // ── ws_personal: 1 open, 2 completed ─────────────
                TaskDef("t_l1", "Book gym session for this week",
                    importance = Importance.LOW, workspaceId = WS_PERSONAL, tags = listOf("personal"), dueInDays = 7),
                TaskDef("t_l2", "Renew gym membership",
                    importance = Importance.MEDIUM, workspaceId = WS_PERSONAL, tags = listOf("personal"), daysAgoCompleted = 2),
                TaskDef("t_l3", "Buy groceries for the week",
                    importance = Importance.LOW, workspaceId = WS_PERSONAL, tags = listOf("personal"), daysAgoCompleted = 5)
            )

            // ── Build Task objects ──────────────────────────────
            val tasks = taskDefs.map { def ->
                val completed = def.daysAgoCompleted >= 0
                val completedAt = if (completed) ts(def.daysAgoCompleted.toLong()) else null
                val dueDate = def.dueInDays?.let { tsFuture(it.toLong()) }
                val base = Task(
                    id = def.id,
                    title = def.title,
                    description = def.description,
                    importance = def.importance,
                    workspaceId = def.workspaceId,
                    tags = def.tags,
                    dueDate = dueDate,
                    snoozeCount = def.snoozeCount,
                    completed = completed,
                    completedAt = completedAt,
                    createdAt = ts(def.daysAgoCompleted.coerceAtLeast(0).toLong() + 5),
                    ownerId = userId
                )
                base.copy(currentXp = XpEngine.calculateTaskXp(base))
            }

            // ─────────────────────────────────────────────
            // 3. BUILD DAILY ACTIVITY from tasks + filler
            // ─────────────────────────────────────────────
            val activityMap = mutableMapOf<Long, Pair<Int, Int>>() // epochDay -> (count, xp)

            // From completed tasks
            tasks.filter { it.completed && it.completedAt != null }.forEach { t ->
                val day = t.completedAt!!.seconds / 86400L
                val (c, x) = activityMap.getOrDefault(day, 0 to 0)
                activityMap[day] = (c + 1) to (x + t.currentXp)
            }

            // Filler for past 30 days so charts look full
            for (i in 1..30) {
                val day = today.minusDays(i.toLong())
                if (day.dayOfWeek.value < 6) { // weekdays only
                    val epochDay = day.toEpochDay()
                    val (c, x) = activityMap.getOrDefault(epochDay, 0 to 0)
                    if (c == 0) { // don't override real task activity
                        activityMap[epochDay] = (2 + (i % 3)) to (280 + (i * 17) % 300)
                    }
                }
            }

            // Streak: ensure consecutive days including today
            for (i in 0..11) {
                val epochDay = today.minusDays(i.toLong()).toEpochDay()
                val (c, x) = activityMap.getOrDefault(epochDay, 0 to 0)
                if (c == 0) activityMap[epochDay] = 2 to 250
            }

            val totalXp = activityMap.values.sumOf { it.second }
            val totalCompleted = activityMap.values.sumOf { it.first }
            val weeklyXp = activityMap
                .filter { it.key >= today.minusDays(6).toEpochDay() }
                .values.sumOf { it.second }

            // ─────────────────────────────────────────────
            // 4. BATCH — main user
            // ─────────────────────────────────────────────
            val batch1 = db.batch()

            // Workspaces
            val openTaskByWs = tasks.filter { !it.completed }.groupBy { it.workspaceId }
            listOf(
                Workspace(id = WS_PROJECTS,  name = "Projects",  ownerId = userId,
                    currentFocusTaskId = openTaskByWs[WS_PROJECTS]?.firstOrNull()?.id ?: "",
                    createdAt = ts(90).seconds * 1000),
                Workspace(id = WS_EDUCATION, name = "Education", ownerId = userId,
                    currentFocusTaskId = openTaskByWs[WS_EDUCATION]?.firstOrNull()?.id ?: "",
                    createdAt = ts(60).seconds * 1000),
                Workspace(id = WS_WORK,      name = "Work",      ownerId = userId,
                    currentFocusTaskId = openTaskByWs[WS_WORK]?.firstOrNull()?.id ?: "",
                    createdAt = ts(45).seconds * 1000),
                Workspace(id = WS_PERSONAL,  name = "Personal",  ownerId = userId,
                    currentFocusTaskId = openTaskByWs[WS_PERSONAL]?.firstOrNull()?.id ?: "",
                    createdAt = ts(30).seconds * 1000)
            ).forEach { ws ->
                batch1.set(db.collection("users").document(userId)
                    .collection("workspaces").document(ws.id), ws)
            }

            // Tasks
            tasks.forEach { task ->
                batch1.set(db.collection("users").document(userId)
                    .collection("tasks").document(task.id), task)
            }

            // Activity
            activityMap.forEach { (day, stats) ->
                batch1.set(
                    db.collection("users").document(userId)
                        .collection("activity").document(day.toString()),
                    DailyActivity(dateEpochDay = day, tasksCompleted = stats.first, xpEarned = stats.second)
                )
            }

            // User root document
            batch1.set(
                db.collection("users").document(userId),
                User(
                    id = userId,
                    displayName = auth.currentUser?.displayName ?: "Sagi Einav",
                    email = auth.currentUser?.email ?: "",
                    avatarPreset = 1,
                    xp = totalXp,
                    level = XpEngine.levelForXp(totalXp),
                    currentWorkspaceId = WS_PROJECTS,
                    friends = listOf("uid_roei", "uid_ofek", "uid_ofir"),
                    onboarded = true,
                    stats = UserStats(
                        totalTasksCompleted = totalCompleted,
                        aceCount = (totalCompleted * 0.78).toInt(),
                        currentStreak = 12,
                        longestStreak = 30,
                        lastActiveEpochDay = today.toEpochDay(),
                        weekStartEpochDay = today.minusDays(today.dayOfWeek.value.toLong() - 1).toEpochDay(),
                        weeklyXp = weeklyXp
                    )
                )
            )

            batch1.commit().await()
            Log.d("DB_SEED", "Main user batch committed.")

            // ─────────────────────────────────────────────
            // 5. BATCH — friends  (separate batch, each
            //    friend gets root doc + activity + 1 ws + tasks)
            // ─────────────────────────────────────────────
            data class FriendDef(
                val id: String, val name: String, val avatarPreset: Int,
                val streak: Int, val xpPerDay: IntRange
            )

            val friendDefs = listOf(
                FriendDef("uid_roei", "Roei Zalah",     21, streak = 4,  xpPerDay = 200..420),
                FriendDef("uid_ofek", "Ofek Fanian",    17, streak = 6,  xpPerDay = 280..540),
                FriendDef("uid_ofir", "Ofir Vizenblit", 14, streak = 2,  xpPerDay = 120..300)
            )

            friendDefs.forEach { f ->
                val batch2 = db.batch()
                val fRef = db.collection("users").document(f.id)

                // Friend activity — 30 days with guaranteed streak
                val fActivityMap = mutableMapOf<Long, Pair<Int, Int>>()
                for (i in 0..29) {
                    val day = today.minusDays(i.toLong())
                    val isStreakDay = i < f.streak
                    val isActiveDay = isStreakDay || (i in 7..29 && day.dayOfWeek.value < 6 && i % 3 != 0)
                    if (isActiveDay) {
                        val xp = f.xpPerDay.random()
                        fActivityMap[day.toEpochDay()] = (1 + i % 3) to xp
                    }
                }

                val fTotalXp = fActivityMap.values.sumOf { it.second } + f.xpPerDay.first * 20 // base
                val fTotalCompleted = fActivityMap.values.sumOf { it.first }
                val fWeeklyXp = fActivityMap
                    .filter { it.key >= today.minusDays(6).toEpochDay() }
                    .values.sumOf { it.second }
                val fWsId = "${f.id}_ws"

                // Friend workspace
                batch2.set(fRef.collection("workspaces").document(fWsId),
                    Workspace(id = fWsId, name = "Personal", ownerId = f.id, createdAt = ts(60).seconds * 1000))

                // Friend tasks (a few completed to make the profile look real)
                listOf(
                    "Complete daily review" to Importance.MEDIUM,
                    "Morning workout" to Importance.LOW,
                    "Read 20 pages" to Importance.LOW,
                    "Plan the week" to Importance.HIGH
                ).forEachIndexed { idx, (title, imp) ->
                    val base = Task(
                        id = "${f.id}_task_$idx", title = title, importance = imp,
                        workspaceId = fWsId, completed = true,
                        completedAt = ts(idx.toLong()),
                        createdAt = ts(idx.toLong() + 3),
                        ownerId = f.id
                    )
                    batch2.set(fRef.collection("tasks").document(base.id),
                        base.copy(currentXp = XpEngine.calculateTaskXp(base)))
                }

                // Friend activity docs
                fActivityMap.forEach { (day, stats) ->
                    batch2.set(
                        fRef.collection("activity").document(day.toString()),
                        DailyActivity(dateEpochDay = day, tasksCompleted = stats.first, xpEarned = stats.second)
                    )
                }

                // Friend root document
                batch2.set(fRef, User(
                    id = f.id,
                    displayName = f.name,
                    avatarPreset = f.avatarPreset,
                    xp = fTotalXp,
                    level = XpEngine.levelForXp(fTotalXp),
                    currentWorkspaceId = fWsId,
                    friends = listOf(userId),
                    onboarded = true,
                    stats = UserStats(
                        totalTasksCompleted = fTotalCompleted,
                        aceCount = (fTotalCompleted * 0.75).toInt(),
                        currentStreak = f.streak,
                        longestStreak = f.streak + 8,
                        lastActiveEpochDay = today.toEpochDay(),
                        weekStartEpochDay = today.minusDays(today.dayOfWeek.value.toLong() - 1).toEpochDay(),
                        weeklyXp = fWeeklyXp
                    )
                ))

                batch2.commit().await()
                Log.d("DB_SEED", "Friend ${f.name} committed.")
            }

            Log.d("DB_SEED", "All done! Database fully seeded.")
        } catch (e: Exception) {
            Log.e("DB_SEED", "Seed failed", e)
        }
    }

    private fun ts(daysAgo: Long): Timestamp {
        val epoch = LocalDate.now().minusDays(daysAgo).toEpochDay() * 86400L
        return Timestamp(epoch, 0)
    }

    private fun tsFuture(daysFromNow: Long): Timestamp {
        val epoch = LocalDate.now().plusDays(daysFromNow).toEpochDay() * 86400L
        return Timestamp(epoch, 0)
    }
}
