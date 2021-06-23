package net.dblsaiko.quacklib.model

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import java.io.IOException

private val logger = LogManager.getLogger()

internal fun getRelativeResource(base: Identifier, rpath: String): Identifier {
    return when {
        ':' in rpath -> Identifier(rpath)
        rpath.startsWith('/') -> Identifier(base.namespace, rpath.trimStart('/'))
        else -> Identifier(base.namespace, base.path.dropLastWhile { it != '/' } + rpath)
    }
}

internal fun readCustom(s: String, ignore: Int, m: Int, o: Int): List<String> {
    val components = s.split(" ").drop(ignore)
    if ((o >= 0 && components.size !in m..m + o) || components.size < m)
        error("Invalid number of parameters! Expected $m mandatory, $o optional, got ${components.size}")
    return components
}

internal fun readFloats(s: String, ignore: Int, m: Int, o: Int): List<Float> = readCustom(s, ignore, m, o).map(String::toFloat)

internal fun readResource(rl: Identifier): String? {
    val resourceManager = MinecraftClient.getInstance().resourceManager
    val resource = try {
        resourceManager.getResource(rl)
    } catch (e: IOException) {
        logger.error("OBJ file not found: $rl")
        return null
    }

    return resource.inputStream.use { it.bufferedReader().readText() }
}