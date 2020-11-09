package org.up.coroutines.repository


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.repository.reactive.ReactiveCrudRepository


//does not work yet as to 2.3.0.RC1
//@Repository
//interface UserRepository_ : CoroutineCrudRepository<User, Long> {
//
//    @Query("SELECT * FROM users WHERE id = ?0")
//    suspend fun findOne(id: Long): User?
//
//    //fun findByFirstname(firstname: String): Flow<User>
//}

//abstract class CoroutineCrudRepository<R:ReactiveCrudRepository<T, ID>, T:Any, ID>(val underlying:R) {
//
//    suspend fun <S : T?> save(entity: S): S = underlying.save(entity).awaitFirst()
//
//    fun <S : T> saveAll(entities: Iterable<S>): Flow<S> = underlying.saveAll(entities).asFlow()
//
//    suspend fun findById(id: ID): T? = underlying.findById(id).awaitFirstOrNull()
//
//    suspend fun existsById(id: ID): Boolean = underlying.existsById(id).awaitFirst()
//
//    fun findAll():Flow<T> = underlying.findAll().asFlow()
//
//    suspend fun count(): Long = underlying.count().awaitFirst()
//
//    suspend fun deleteById(id: ID) = underlying.deleteById(id).awaitFirst()
//
//    suspend fun delete(entity: T) = underlying.delete(entity).awaitFirst()
//
//    suspend fun deleteAll(entities: Iterable<T>) = underlying.deleteAll(entities).awaitFirst()
//
//    suspend fun deleteAll() = underlying.deleteAll().awaitFirst()
//
//}
