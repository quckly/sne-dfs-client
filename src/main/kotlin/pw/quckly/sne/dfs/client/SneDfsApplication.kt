package pw.quckly.sne.dfs.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import java.nio.file.Paths

@SpringBootApplication
class SneDfsApplication {

    @Value("\${app.client.mountpath}")
    var mountPath: String = ""

    @Bean
    fun commandLineRunner(ctx: ApplicationContext, @Autowired dfs: DFS): CommandLineRunner {
        return CommandLineRunner { args ->
            println("Args: ${args.joinToString()}")

            if (mountPath.isEmpty()) {
                println("Specify mount path by setting 'app.client.mountpath'")
                return@CommandLineRunner
            }

            try {
                dfs.mount(Paths.get(mountPath), true, true)
            } finally {
                dfs.umount()
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<SneDfsApplication>(*args)
}
