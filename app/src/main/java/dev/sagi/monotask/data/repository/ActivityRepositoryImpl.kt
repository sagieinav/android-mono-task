package dev.sagi.monotask.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import dev.sagi.monotask.data.model.DailyActivity
import dev.sagi.monotask.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ActivityRepository {

    private fun activityCol(userId: String) =
        db.collection("users").document(userId).collection("activity")

    override suspend fun logDailyActivity(userId: String, xpEarned: Int, tasksCompleted: Int) {
        val today = LocalDate.now().toEpochDay()
        activityCol(userId).document(today.toString())
            .set(
                mapOf(
                    "dateEpochDay" to today,
                    "xpEarned" to FieldValue.increment(xpEarned.toLong()),
                    "tasksCompleted" to FieldValue.increment(1L)
                ),
                SetOptions.merge()
            ).await()
    }

    override suspend fun removeDailyActivity(
        userId: String,
        xpToSubtract: Int,
        tasksToSubtract: Int,
        dateEpochDay: Long
    ) {
        activityCol(userId).document(dateEpochDay.toString())
            .set(
                mapOf(
                    "xpEarned" to FieldValue.increment(-xpToSubtract.toLong()),
                    "tasksCompleted" to FieldValue.increment(-tasksToSubtract.toLong())
                ),
                SetOptions.merge()
            ).await()
    }

    override fun getActivity(userId: String, range: ClosedRange<Long>?): Flow<List<DailyActivity>> {
        val col = activityCol(userId)
        val query = if (range != null)
            col.whereGreaterThanOrEqualTo("dateEpochDay", range.start)
               .whereLessThanOrEqualTo("dateEpochDay", range.endInclusive)
        else col
        return query.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(DailyActivity::class.java) }
        }
    }

    override suspend fun getActivityOnce(userId: String, range: ClosedRange<Long>?): List<DailyActivity> {
        val col = activityCol(userId)
        val query = if (range != null)
                        col.whereGreaterThanOrEqualTo("dateEpochDay", range.start)
                            .whereLessThanOrEqualTo("dateEpochDay", range.endInclusive)
                    else col
        return query.get().await().mapNotNull {
            it.toObject(DailyActivity::class.java)
        }
    }

    override suspend fun getTopPerformanceDay(userId: String): DailyActivity? =
        activityCol(userId)
            .orderBy("xpEarned", Query.Direction.DESCENDING)
            .limit(1)
            .get().await()
            .mapNotNull {
                it.toObject(DailyActivity::class.java)
            }
            .firstOrNull()
}
