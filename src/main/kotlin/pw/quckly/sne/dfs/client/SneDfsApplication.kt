package pw.quckly.sne.dfs.client

import jnr.ffi.Platform
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import ru.serce.jnrfuse.struct.FuseFileInfo
import java.nio.file.Paths

@SpringBootApplication
class SneDfsApplication {

    @Bean
    fun commandLineRunner(ctx: ApplicationContext, @Autowired dfs: DFS): CommandLineRunner {
        return CommandLineRunner { args ->
            println("Args: ${args.joinToString()}")

            try {
                val path: String
                when (Platform.getNativePlatform().os) {
                    jnr.ffi.Platform.OS.WINDOWS -> path = "J:\\"
                    else -> path = "/tmp/mntm"
                }
                dfs.mount(Paths.get(path), true, true)
            } finally {
                dfs.umount()
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<SneDfsApplication>(*args)
}
