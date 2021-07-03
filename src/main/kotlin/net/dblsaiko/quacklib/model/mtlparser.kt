package net.dblsaiko.quacklib.model

import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import java.awt.Color

private val logger = LogManager.getLogger()

val fallbackMaterial = Material(Color(-1), 1.0f, null)

internal val regex = Regex("#.*$")

fun loadMaterialLibrary(rl: Identifier): Map<String, Material>? {
    val source = readResource(rl) ?: return null

    var mats: Map<String, Material> = emptyMap()

    var name = ""
    var mat: Material? = null

    for (it in source.lines().map { it.replace(regex, "") }.map(String::trim)) {
        if (it.isBlank()) continue
        val cmd = it.split(" ").getOrNull(0)
        if (cmd == "newmtl") {
            mat?.also { mats += name to it }
            mat = fallbackMaterial
            name = readCustom(it, 1, 1, 0)[0]
            continue
        }
        if (mat == null) {
            logger.warn("Ignoring line '$it' because no material is currently being defined")
            continue
        }
        when (cmd) {
            "Kd" -> mat = mat.copy(diffuse = readFloats(it, 1, 3, 0).let { Color(it[0], it[1], it[2]) })
            "d" -> mat = mat.copy(transparency = readFloats(it, 1, 1, 0)[0])
            "Tr" -> mat = mat.copy(transparency = 1 - readFloats(it, 1, 1, 0)[0])
            "map_Kd" -> mat = mat.copy(diffuseTexture = readCustom(it, 1, 1, 0)[0])
            else -> logger.warn("Unrecognized/unimplemented MTL statement '$it'")
        }
    }

    mat?.also { mats += name to it }
    return mats
}