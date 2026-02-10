package com.example.data




object Hash {
    fun sha256(input: String): String {
        return java.security.MessageDigest
            .getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
