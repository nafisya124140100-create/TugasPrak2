package com.example.tugasprak2

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform