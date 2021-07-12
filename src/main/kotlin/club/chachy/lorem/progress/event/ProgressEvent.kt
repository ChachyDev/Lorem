package club.chachy.lorem.progress.event

import club.chachy.lorem.services.Service

open class ProgressEvent<T, R>(open val task: Service<T, R>)