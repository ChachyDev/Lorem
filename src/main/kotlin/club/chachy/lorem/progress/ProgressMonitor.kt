package club.chachy.lorem.progress

import club.chachy.lorem.progress.event.ProgressEvent

interface ProgressMonitor {
    /**
     * Called when an "event" is passed
     *
     * @author ChachyDev
     * @return Whether the event should be cancelled or not
     * @since 0.1.x
     */
    fun onProgress(event: ProgressEvent<*, *>): Boolean

    companion object Default : ProgressMonitor {
        override fun onProgress(event: ProgressEvent<*, *>) = false
    }
}