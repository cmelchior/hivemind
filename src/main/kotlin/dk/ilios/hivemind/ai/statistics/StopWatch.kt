package dk.ilios.hivemind.ai.statistics

/**
 * StopWatch class used for various statistics across classes
 */
class StopWatch private constructor() {
    private val watches: MutableMap<String?, Watch?> = HashMap<String?, Watch?>()

    fun start(): Watch {
        return start(SINGLE_WATCH_ID)
    }

    @Synchronized
    fun start(name: String?): Watch {
        val watch = Watch()
        watches.put(name, watch)
        watch.start()
        return watch
    }

    @Synchronized
    fun stop(name: String?): Watch {
        val watch = watches.get(name)
        if (watch != null) {
            watch.stop()
        } else {
            throw IllegalStateException("Watch not found for: " + name)
        }

        return watch
    }

    fun stop(): Watch {
        return stop(SINGLE_WATCH_ID)
    }

    fun get(name: String?): Watch? {
        return watches.get(name)
    }

    class Watch {
        var name: String? = null
        var start: Long = 0
        var end: Long = 0
        var isRunning: Boolean = false

        fun start(): Watch {
            if (this.isRunning) return this
            this.isRunning = true
            start = System.nanoTime()
            return this
        }

        fun stop(): Watch {
            if (!this.isRunning) return this
            this.isRunning = false
            end = System.nanoTime()
            return this
        }

        val elapsedTimeInMillis: Long
            get() {
                val elapsed: Long
                if (this.isRunning) {
                    elapsed = ((System.nanoTime() - start) / 1000000)
                } else {
                    elapsed = ((end - start) / 1000000)
                }
                return elapsed
            }

        val elapsedTimeInSeconds: Long
            get() {
                val elapsed: Long
                if (this.isRunning) {
                    elapsed = ((System.nanoTime() - start) / 1000000000)
                } else {
                    elapsed = ((end - start) / 1000000000)
                }
                return elapsed
            }

        fun log(prefix: String?) {
            println(prefix + " Elapsed time: " + this.elapsedTimeInMillis + " ms.")
        }
    }

    companion object {
        private const val SINGLE_WATCH_ID = "StopWatch.class#1"

        @get:Synchronized
        var instance: StopWatch? = null
            get() {
                if (field == null) {
                    field = StopWatch()
                }

                return field
            }
            private set
    }
}
