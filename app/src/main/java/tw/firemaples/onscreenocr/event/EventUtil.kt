package tw.firemaples.onscreenocr.event

import org.greenrobot.eventbus.EventBus

class EventUtil {
    companion object {
        private val eventBus: EventBus = EventBus.getDefault()

        @JvmStatic
        fun register(clazz: Any) {
            try {
                eventBus.register(clazz)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun unregister(clazz: Any) {
            eventBus.unregister(clazz)
        }

        @JvmStatic
        fun post(event: BaseEvent) {
            eventBus.post(event)
        }

        @JvmStatic
        fun postSticky(event: BaseEvent) {
            eventBus.postSticky(event)
        }
    }
}