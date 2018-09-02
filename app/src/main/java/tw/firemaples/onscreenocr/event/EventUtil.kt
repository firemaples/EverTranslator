package tw.firemaples.onscreenocr.event

import org.greenrobot.eventbus.EventBus

class EventUtil {
    companion object {
        private val eventBus: EventBus = EventBus.getDefault()

        fun register(clazz: Any) {
            eventBus.register(clazz)
        }

        fun unregister(clazz: Any) {
            eventBus.unregister(clazz)
        }

        fun post(event: BaseEvent) {
            eventBus.post(event)
        }

        fun postSticky(event: BaseEvent) {
            eventBus.postSticky(event)
        }
    }
}