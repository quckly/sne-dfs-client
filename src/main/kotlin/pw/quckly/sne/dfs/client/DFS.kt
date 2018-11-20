package pw.quckly.sne.dfs.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.result.Result
import jnr.ffi.Platform
import jnr.ffi.Platform.OS.WINDOWS
import jnr.ffi.Pointer
import jnr.ffi.types.mode_t
import jnr.ffi.types.off_t
import jnr.ffi.types.size_t
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.serce.jnrfuse.ErrorCodes
import ru.serce.jnrfuse.FuseFillDir
import ru.serce.jnrfuse.FuseStubFS
import ru.serce.jnrfuse.struct.FileStat
import ru.serce.jnrfuse.struct.FuseFileInfo
import ru.serce.jnrfuse.struct.Statvfs
import java.lang.Exception

@Component
class DFS : FuseStubFS() {

    @Value("\${app.master.address}")
    var masterAddress: String = ""

    @Value("\${app.requests.timeout}")
    var requestTimeout = 5000

    private inline fun <S : Any, reified R : Any> handleCall(callName: String,
                                                             path: String,
                                                             apiUrlPath: String,
                                                             request: S,
                                                             handler: (R) -> Int): Int {
        try {
            logger.info("Tracer: $callName \"${path}\"")

            val requestJson = jsonMapper.writeValueAsString(request)

            val (fuelRequest, fuelResponse, fuelResult) = Fuel.post("$masterAddress$apiUrlPath")
                    .jsonBody(requestJson)
                    .responseObject<R>()

            return fuelResult.fold(handler, inerr@ { error ->
                logger.error("Error on invoke master", error)

                try {
                    val result = jsonMapper.readValue(error.errorData, StatusResponse::class.java)

                    return@inerr result.status
                } catch (e: Exception) {
                    // Do nothing
                }

                return@inerr ErrorCodes.EIO()
            })
        } catch (e: Exception) {
            e.printStackTrace()
            return ErrorCodes.EIO()
        }
    }

    override fun read(path: String?, buf: Pointer?, @size_t size: Long, @off_t offset: Long, fi: FuseFileInfo?): Int {
        if (path == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        val readRequest = ReadRequest(path)

        return (-1) * handleCall("Read",
                path,
                "/fs/read",
                readRequest) { response: ReadResponse ->

            // TODO: response.data

            response.status
        }
    }

    override fun write(path: String?, buf: Pointer?, @size_t size: Long, @off_t offset: Long, fi: FuseFileInfo?): Int {
        if (path == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        val writeRequest = WriteRequest

        return (-1) * handleCall("Write",
                path,
                "/fs/write",
                writeRequest) { response: StatusResponse ->

            response.status
        }
    }

    override fun create(path: String?, @mode_t mode: Long, fi: FuseFileInfo?): Int {
        if (path == null || fi == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        return (-1) * handleCall("Create file",
                path,
                "/fs/create",
                FilePathRequest(path)) { response: StatusResponse ->

            response.status
        }
    }

    override fun getattr(path: String?, stat: FileStat?): Int {
        if (path == null || stat == null) {
            return ErrorCodes.EINVAL()
        }

        return (-1) * handleCall("Getattr",
                path,
                "/fs/getattr",
                FilePathRequest(path)) { response: AttrResponse ->

            stat.st_mode.set(response.mode)
            stat.st_size.set(response.size)
            stat.st_uid.set(context.uid.get())
            stat.st_gid.set(context.gid.get())

            response.status
        }
    }

    override fun mkdir(path: String?, @mode_t mode: Long): Int {
        if (path == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        return (-1) * handleCall("Make directory",
                path,
                "/fs/mkdir",
                FilePathRequest(path)) { response: StatusResponse ->

            response.status
        }
    }

    override fun readdir(path: String?, buf: Pointer?, filter: FuseFillDir?, @off_t offset: Long, fi: FuseFileInfo?): Int {
        if (path == null || buf == null || filter == null || fi == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        return (-1) * handleCall("Read directory",
                path,
                "/fs/readdir",
                ReadDirRequest(path)) { response: ReadDirResponse ->

            response.contents.forEach {
                filter.apply(buf, it, null, 0)
            }

            response.status
        }
    }

    override fun statfs(path: String?, stbuf: Statvfs?): Int {
        if (path == null || stbuf == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        if (Platform.getNativePlatform().os == WINDOWS) {
            // statfs needs to be implemented on Windows in order to allow for copying
            // data from other devices because winfsp calculates the volume size based
            // on the statvfs call.
            // see https://github.com/billziss-gh/winfsp/blob/14e6b402fe3360fdebcc78868de8df27622b565f/src/dll/fuse/fuse_intf.c#L654
            if ("/" == path) {
                stbuf.f_blocks.set(1024 * 1024) // total data blocks in file system
                stbuf.f_frsize.set(1024)        // fs block size
                stbuf.f_bfree.set(1024 * 1024)  // free blocks in fs
            }
        }
        return super.statfs(path, stbuf)
    }

    override fun rename(path: String?, newName: String?): Int {
        if (path == null || newName == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        return (-1) * handleCall("Rename file",
                path,
                "/fs/rename",
                RenameRequest(path, newName)) { response: StatusResponse ->

            response.status
        }
    }

    override fun rmdir(path: String?): Int {
        if (path == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        return (-1) * handleCall("Remove directory",
                path,
                "/fs/rmdir",
                FilePathRequest(path)) { response: StatusResponse ->

            response.status
        }
    }

    override fun truncate(path: String?, offset: Long): Int {
        if (path == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        return (-1) * handleCall("Truncate file",
                path,
                "/fs/truncate",
                TruncateRequest(path, offset)) { response: StatusResponse ->

            response.status
        }
    }

    override fun unlink(path: String?): Int {
        if (path == null) {
            return (-1) * ErrorCodes.EINVAL()
        }

        return (-1) * handleCall("Unlink",
                path,
                "/fs/unlink",
                FilePathRequest(path)) { response: StatusResponse ->

            response.status
        }
    }

    override fun open(path: String?, fi: FuseFileInfo?): Int {
        return 0
    }

    companion object {
        val logger = LoggerFactory.getLogger(DFS::class.java)

        val jsonMapper = ObjectMapper().registerKotlinModule()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}
