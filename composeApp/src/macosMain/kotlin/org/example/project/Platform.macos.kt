package org.example.project

actual fun getPlatform(): Platform {
    return object : Platform {
        override val name: String = "MacOS"
    }
}