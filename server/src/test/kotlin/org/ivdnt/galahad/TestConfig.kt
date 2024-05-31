package org.ivdnt.galahad

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.io.File
import kotlin.io.path.createTempDirectory

@TestConfiguration
class TestConfig {
    val workDir: String = createTempDirectory().toString()

    @Bean
    @Primary
    fun getWorkingDirectory(): File {
        return File(workDir)
    }
}