package pw.quckly.sne.dfs.client

data class SlaveRegisterRequest(val guid: String, val port: Int, val chunksCount: Int)

// Common data structures
data class FilePathRequest(val path: String)
data class StatusResponse(val status: Int, val message: String? = null)
data class IntResponse(val status: Int, val value: Int)

// Per operation
data class ReadRequest(val path: String, val size: Long, val offset: Long)
data class ReadResponse(val status: Int, val data: String)

data class WriteRequest(val path: String, val offset: Long, val data: String)

data class ReadDirRequest(val path: String)
data class ReadDirResponse(val status: Int, val contents: Array<String>)

data class RenameRequest(val from: String, val to: String)

data class TruncateRequest(val path: String, val size: Long)

data class AttrResponse(val status: Int, val mode: Int, val size: Long, val uid: Int, val gid: Int)
