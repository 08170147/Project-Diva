
package dev.pjdvm39.android

import android.content.Context
import java.security.MessageDigest

data class AssetVerification(
    val path: String,
    val expectedHash: String,
    val actualHash: String?,
    val matches: Boolean,
    val error: String? = null
)

class AssetResourceManager(context: Context) {

    private val assets = context.applicationContext.assets

    fun readText(path: String): Result<String> = runCatching {
        requireValidPath(path)

        assets.open(path).bufferedReader(Charsets.UTF_8).use {
            it.readText()
        }
    }

    fun sha256(path: String): Result<String> = runCatching {
        requireValidPath(path)

        val digest = MessageDigest.getInstance("SHA-256")

        assets.open(path).use { input ->
            val buffer = ByteArray(8192)

            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }

        digest.digest().joinToString("") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
    }

    fun verifySha256(
        path: String,
        expectedHash: String
    ): Result<Boolean> = runCatching {
        requireValidHash(expectedHash)
        sha256(path).getOrThrow()
            .equals(expectedHash, ignoreCase = true)
    }

    fun verifyManifest(
        manifestPath: String = "resource_manifest.txt"
    ): Result<List<AssetVerification>> = runCatching {
        val manifest = readText(manifestPath).getOrThrow()
        val results = mutableListOf<AssetVerification>()

        manifest.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .forEach { line ->
                val parts = line.split(Regex("\\s+"), limit = 2)

                require(parts.size == 2) {
                    "Invalid manifest line: $line"
                }

                val expectedHash = parts[0]
                val path = parts[1]

                requireValidHash(expectedHash)
                requireValidPath(path)

                val actualResult = sha256(path)

                if (actualResult.isSuccess) {
                    val actualHash = actualResult.getOrThrow()

                    results.add(
                        AssetVerification(
                            path = path,
                            expectedHash = expectedHash.lowercase(),
                            actualHash = actualHash,
                            matches = actualHash.equals(
                                expectedHash,
                                ignoreCase = true
                            )
                        )
                    )
                } else {
                    results.add(
                        AssetVerification(
                            path = path,
                            expectedHash = expectedHash.lowercase(),
                            actualHash = null,
                            matches = false,
                            error = actualResult.exceptionOrNull()
                                ?.javaClass?.simpleName ?: "Unknown error"
                        )
                    )
                }
            }

        results
    }

    private fun requireValidPath(path: String) {
        require(path.isNotBlank()) {
            "Asset path must not be blank"
        }

        require(!path.startsWith("/") && '\\' !in path) {
            "Asset path must be relative and use forward slashes"
        }

        require(path.split('/').none { it == ".." || it == "." }) {
            "Asset path must not contain dot segments"
        }
    }

    private fun requireValidHash(hash: String) {
        require(hash.matches(Regex("^[0-9a-fA-F]{64}$"))) {
            "Expected SHA-256 must contain 64 hexadecimal characters"
        }
    }
}
