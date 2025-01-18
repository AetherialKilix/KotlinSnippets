/*
 * This file was written by AetherialKilix (https://github.com/AetherialKilix) on 2025-01-19 (Jan 19 2025).
 *
 * This file is provided "as is" and without any guarantees of quality, functionality, or fitness for a particular purpose.
 * Use at your own risk; I make no claims that this code will work as intended or at all.
 *
 * You are free to use, modify, and distribute this code in any project, personal or professional,
 * without attribution or compensation. No credit to me is required.
 */
@file:Suppress("INAPPLICABLE_JVM_NAME")
import java.util.*
import java.util.function.Consumer

interface Emitter<T> {
    @JvmName("register")
    operator fun plusAssign(consumer: Consumer<T>)
    @JvmName("registerKt")
    operator fun plusAssign(consumer: T.() -> Unit)
    @JvmName("unregister")
    operator fun minusAssign(consumer: Consumer<T>)
    @JvmName("unregisterKt")
    operator fun minusAssign(consumer: T.() -> Unit)
    @JvmName("emit")
    operator fun invoke(value: T)
}
open class SimpleEmitter<T> : Emitter<T> {
    private val listeners: MutableList<Consumer<T>> = LinkedList()

    @JvmName("register")
    override operator fun plusAssign(consumer: Consumer<T>) { listeners.add(consumer) }
    @JvmName("registerKt")
    override operator fun plusAssign(consumer: T.() -> Unit) { listeners.add(consumer) }
    @JvmName("unregister")
    override operator fun minusAssign(consumer: Consumer<T>) { listeners.remove(consumer) }
    @JvmName("unregisterKt")
    override operator fun minusAssign(consumer: T.() -> Unit) { listeners.remove(consumer) }
    @JvmName("emit")
    override operator fun invoke(value: T) { listeners.forEach { it.accept(value) } }
}

class SafeEmitter<T> : SimpleEmitter<T>() {
    private val listeners: MutableList<Consumer<T>> = LinkedList()
    val onError: Emitter<Exception> = SimpleEmitter()
    @JvmName("emit")
    override operator fun invoke(value: T) { listeners.forEach {
        try { it.accept(value) } catch (e: Exception) { onError(e) }
    } }
}