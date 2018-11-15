package pw.quckly.sne.dfs

import jnr.ffi.Platform
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import java.nio.file.Paths
import java.util.*


@SpringBootApplication
class SneDfsApplication {

    @Bean
    fun commandLineRunner(ctx: ApplicationContext): CommandLineRunner {
        return CommandLineRunner { args ->
            println("Args: ${args.joinToString()}")

            val memfs = DFS()
            try {
                val path: String
                when (Platform.getNativePlatform().os) {
                    jnr.ffi.Platform.OS.WINDOWS -> path = "J:\\"
                    else -> path = "/tmp/mntm"
                }
                memfs.mount(Paths.get(path), true, true)
            } finally {
                memfs.umount()
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<SneDfsApplication>(*args)
}
