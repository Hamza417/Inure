package app.simple.inure.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BatchExtractWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return Result.success()
    }

}
