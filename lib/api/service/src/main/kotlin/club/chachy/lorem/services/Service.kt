package club.chachy.lorem.services

interface Service<T, R> {
    suspend fun executeTask(data: T): R
}