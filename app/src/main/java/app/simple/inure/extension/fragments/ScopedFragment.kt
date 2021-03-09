package app.simple.inure.extension.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * [ScopedFragment] is lifecycle aware [CoroutineScope] fragment
 * used to bind independent coroutines with the lifecycle of
 * the given fragment. All [Fragment] classes must extend
 * this class instead
 */
open class ScopedFragment : Fragment(), CoroutineScope {

    /**
     * Get the job instance here, must be a final value
     */
    private val job = Job()

    /**
     * Use the job instance and attach it the [Dispatchers.Main]
     * which will be the main thread of the given app
     * instance
     */
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    /**
     * Cancel the job instance here, since
     * we have attached the [Job] with [Dispatchers.Main]
     * it will cancel all the coroutines of the given instance
     * and this way coroutines won't last more than the
     * lifecycle itself
     */
    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}
