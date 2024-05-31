package org.ivdnt.galahad.app

/**
 * A resource that might be expensive to load, can be encapsulated through this interface
 * so that it only needs to be loaded if it is actually consumed.
 */
interface ExpensiveGettable<T> {
    fun expensiveGet(): T
}

/**
 * Indicates the class has the proper annotations to be serialized to JSON
 */
interface JSONable

interface RUDSet<Key, ReadType, WriteType> {
    fun readAll(): Set<ReadType>
    fun readOrNull( key: Key ): ReadType?
//    fun read( key: Key ): ReadType
    fun readOrThrow( key: Key ) = (readOrNull( key ) ?: throw Exception("Failed to read $key")) // Somehow readOrThrow fails to assert non-nullability
    fun update( key: Key, value: WriteType ): ReadType?
    fun delete( key: Key ): ReadType? // return the new read
}

interface CRUDSet<Key, ReadType, WriteType> : RUDSet<Key, ReadType, WriteType> {
    fun create( value: WriteType ): Key // TODO: think about create or Null instead
}

interface NamedCRUDSet<Key, ReadType, WriteType> : RUDSet<Key, ReadType, WriteType> {
    fun createOrNull( key: Key ): ReadType?
    fun createOrThrow( key: Key ) = createOrNull( key ) ?: throw Exception("Failed to create $key")
    fun readOrCreateOrNull( key: Key ) = readOrNull( key ) ?: createOrNull( key )
}