package org.up.utils

import org.slf4j.MDC
import reactor.util.function.Tuple2
import java.util.*
import java.util.concurrent.CompletableFuture

fun <T : Any> Optional<T>.toNullable(): T? {
    return if (this.isPresent) {
        this.get()
    } else {
        null
    }
}

// ======Reactor Utils ======
operator fun <A, B> Tuple2<A, B>.component1() = this.t1

operator fun <A, B> Tuple2<A, B>.component2() = this.t2

// ======CompletableFuture Utils =======
fun <U> supplyAsync(supplier: () -> U): CompletableFuture<U> {
    return CompletableFuture.supplyAsync(SupplierMDC(supplier))
}

private class SupplierMDC<T>(val delegate: () -> T) : () -> T {
    private val mdc: MutableMap<String, String> = MDC.getCopyOfContextMap()

    override fun invoke(): T {
        MDC.setContextMap(mdc)
        return delegate()
    }
}
