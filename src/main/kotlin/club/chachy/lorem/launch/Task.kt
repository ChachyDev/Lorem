package club.chachy.lorem.launch

interface Task<T, R> {
    suspend fun execute(data: T): R
}