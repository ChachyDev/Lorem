package club.chachy.lorem.services

interface Service<T, R> {
    suspend fun init(data: T) {

    }

    suspend fun executeTask(data: T): R
}