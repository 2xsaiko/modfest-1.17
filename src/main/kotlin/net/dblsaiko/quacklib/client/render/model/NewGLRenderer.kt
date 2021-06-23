package net.dblsaiko.quacklib.client.render.model

import com.mojang.blaze3d.systems.RenderSystem.activeTexture
import com.mojang.blaze3d.systems.RenderSystem.bindTexture
import com.mojang.blaze3d.systems.RenderSystem.disableTexture
import com.mojang.blaze3d.systems.RenderSystem.enableTexture
import com.mojang.blaze3d.systems.RenderSystem.glUniformMatrix4
import net.dblsaiko.qcommon.croco.Mat4
import net.dblsaiko.qcommon.croco.Vec2
import net.dblsaiko.quacklib.model.Face
import net.dblsaiko.quacklib.model.OBJRoot
import net.dblsaiko.quacklib.model.Object
import net.dblsaiko.quacklib.model.fallbackMaterial
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL30.*

val logger = LogManager.getLogger()

private val dataBuffer = BufferUtils.createByteBuffer(0x100000)

class NewGLRenderer(val obj: OBJRoot, val textureOverrides: Map<String, String> = emptyMap()) {
    // buffer layout:
    //  - texture id (float)  1
    //  - object id (float)   1
    //  - xyz (vec3f)        12
    //  - normal (vec3f)     12
    //  - uv (vec2f)          8
    //  - lm uv (vec2f)       8
    //  - color (vec4f)      16
    val vdataSize = 58

    val mc = MinecraftClient.getInstance()

    private var translations: Map<Object, Mat4> = emptyMap()

    private var init = false
    private var buffer = 0
    private var shader = 0
    private var vao = 0

    private val objPos = mutableMapOf<Object, Int>()
    private val glTextureIds = mutableMapOf<Identifier, Int>()
    private val textures = mutableListOf<Int>()
    private var vcount = 0

    private var attrTexture = 0
    private var attrObject = 0
    private var attrPosition = 0
    private var attrNormal = 0
    private var attrUv = 0

    // private var attrLightmapUv = 0
    private var attrColor = 0

    private var uniformLightmap = 0
    private var uniformLightmapC = 0
    private var uniformTransforms = 0
    private var uniformTextures = 0

    // private var fallback: OBJGLRenderer? = null

    fun reset() {
        translations = emptyMap()
        //    fallback?.reset()
    }

    fun transformPart(name: String, mat: Mat4) {
        val part = obj.objects[name] ?: return
        val ct = translations[part] ?: Mat4.IDENTITY
        translations += part to (ct.mul(mat))
        //    fallback?.transformPart(name, mat)
    }

    fun transformGroup(name: String, mat: Mat4) {
        obj.objects.filter { (_, part) -> name in part.groups }.keys.forEach { transformPart(it, mat) }
        //    fallback?.transformGroup(name, mat)
    }

    fun draw(light: Int = 0xF000F0) {
        if (!init) {
            initBuf()
            initShader(objPos.size + 1, textures.size)
            init = true
        }

        if (shader != 0) {
            glUseProgram(shader)
            glBindVertexArray(vao)

            glBindBuffer(GL_ARRAY_BUFFER, buffer)

            // write transform matrices
            dataBuffer.clear()
            val fb = dataBuffer.asFloatBuffer()
            Mat4.IDENTITY.intoBuffer(fb)
            for (obj in obj.objects.values) {
                (translations[obj] ?: Mat4.IDENTITY).intoBuffer(fb)
            }
            fb.flip()

            glUniformMatrix4(uniformTransforms, true, fb)

            // write textures
            for ((index, id) in textures.withIndex()) {
                activeTexture(GL_TEXTURE2 + index)
                enableTexture()
                bindTexture(id)
            }

            dataBuffer.clear()
            val ib = dataBuffer.asIntBuffer()
            for (i in textures.indices)
                ib.put(2 + i)
            ib.flip()
            glUniform1iv(uniformTextures, ib)
            glUniform1i(uniformLightmap, 1)

            // write lightmap
            val x = ((light % 65536) + 8) * 0.00390625f
            val y = ((light / 65536) + 8) * 0.00390625f
            glUniform2f(uniformLightmapC, x, y)

            glEnableVertexAttribArray(attrTexture)
            glEnableVertexAttribArray(attrObject)
            glEnableVertexAttribArray(attrPosition)
            glEnableVertexAttribArray(attrNormal)
            glEnableVertexAttribArray(attrUv)
            // glEnableVertexAttribArray(attrLightmapUv)
            glEnableVertexAttribArray(attrColor)

            glDrawArrays(GL_TRIANGLES, 0, vcount)

            glDisableVertexAttribArray(attrTexture)
            glDisableVertexAttribArray(attrObject)
            glDisableVertexAttribArray(attrPosition)
            glDisableVertexAttribArray(attrNormal)
            glDisableVertexAttribArray(attrUv)
            // glDisableVertexAttribArray(attrLightmapUv)
            glDisableVertexAttribArray(attrColor)

            glBindBuffer(GL_ARRAY_BUFFER, 0)

            glBindVertexArray(0)
            glUseProgram(0)

            for (index in textures.indices) {
                activeTexture(GL_TEXTURE2 + index)
                bindTexture(0)
                disableTexture()
            }

            activeTexture(GL_TEXTURE0)
        } else {
            // if (fallback == null) fallback = OBJGLRenderer(obj, textureOverrides)
            // val fallback = fallback!!
            //
            // fallback.draw(light)
        }
    }

    private fun initBuf() {
        dataBuffer.clear()
        objPos.clear()
        glTextureIds.clear()
        textures.clear()
        vcount = 0

        for (face in obj.faces) {
            processFace(face, 0)
        }

        for ((i, o) in obj.objects.values.withIndex()) {
            objPos[o] = vcount * vdataSize
            for (face in o.faces) {
                processFace(face, 1 + i)
            }
        }

        buffer = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, buffer)
        dataBuffer.flip()
        glBufferData(GL_ARRAY_BUFFER, dataBuffer, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    private fun processFace(face: Face, objindex: Int) {
        if (face.vertices.size != 3) error("Model must be triangulated!")

        val mat = obj.materials[face.material] ?: fallbackMaterial
        val textureRL = (textureOverrides[face.material]
            ?: mat.diffuseTexture)?.let(::Identifier)
            ?: Identifier("quacklib", "textures/white.png")

        if (textureRL !in glTextureIds) {
            mc.textureManager.bindTexture(textureRL)
            val texid = mc.textureManager.getTexture(textureRL).glId
            glTextureIds[textureRL] = texid
            textures += texid
        }

        val texIndex = textures.indexOf(glTextureIds.getValue(textureRL))

        val faceNormal = face.vertices.map { get(obj.vertPos, it.xyz) }.let { (v1, v2, v3) -> ((v2.sub(v1)).cross((v3.sub(v1))).normalized) }

        for (vert in face.vertices) {
            val xyz = get(obj.vertPos, vert.xyz)
            val normal = vert.normal?.let { get(obj.vertNormal, it) }
                ?: faceNormal
            val tex = vert.tex?.let { get(obj.vertTex, it) }?.let { Vec2(it.x, it.y) }
                ?: Vec2(0.5f, 0.5f)

            dataBuffer.put(texIndex.toByte())
            dataBuffer.put(objindex.toByte())
            dataBuffer.putFloat(xyz.x)
            dataBuffer.putFloat(xyz.y)
            dataBuffer.putFloat(xyz.z)
            dataBuffer.putFloat(normal.x)
            dataBuffer.putFloat(normal.y)
            dataBuffer.putFloat(normal.z)
            dataBuffer.putFloat(tex.x)
            dataBuffer.putFloat(1 - tex.y)
            dataBuffer.putFloat(0f)
            dataBuffer.putFloat(0f)
            dataBuffer.putFloat(mat.diffuse.red / 255f)
            dataBuffer.putFloat(mat.diffuse.green / 255f)
            dataBuffer.putFloat(mat.diffuse.blue / 255f)
            dataBuffer.putFloat(mat.transparency)
            vcount++
        }
    }

    private fun initShader(objectCount: Int, textureCount: Int) {
        val vshs = generateVShader(objectCount)
        val fshs = generateFShader(textureCount)
        shader = mkShader(vshs, fshs)
        if (shader == 0) return

        vao = glGenVertexArrays()

        glUseProgram(shader)
        glBindBuffer(GL_ARRAY_BUFFER, buffer)
        glBindVertexArray(vao)

        attrTexture = glGetAttribLocation(shader, "texture")
        attrObject = glGetAttribLocation(shader, "object")
        attrPosition = glGetAttribLocation(shader, "position")
        attrNormal = glGetAttribLocation(shader, "normal")
        attrUv = glGetAttribLocation(shader, "uv")
        // attrLightmapUv = glGetAttribLocation(prog, "lightmapUv")
        attrColor = glGetAttribLocation(shader, "color")

        uniformLightmap = glGetUniformLocation(shader, "lightmap")
        uniformLightmapC = glGetUniformLocation(shader, "lightmapCoord")
        uniformTextures = glGetUniformLocation(shader, "textures")
        uniformTransforms = glGetUniformLocation(shader, "transforms")

        glVertexAttribIPointer(attrTexture, 1, GL_BYTE, vdataSize, 0)
        glVertexAttribIPointer(attrObject, 1, GL_BYTE, vdataSize, 1)
        glVertexAttribPointer(attrPosition, 3, GL_FLOAT, false, vdataSize, 2)
        glVertexAttribPointer(attrNormal, 3, GL_FLOAT, false, vdataSize, 14)
        glVertexAttribPointer(attrUv, 2, GL_FLOAT, false, vdataSize, 26)
        // glVertexAttribPointer(attrLightmapUv, 2, GL_FLOAT, false, vdataSize, 34)
        glVertexAttribPointer(attrColor, 4, GL_FLOAT, false, vdataSize, 42)

        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glUseProgram(0)
    }

    private fun generateVShader(objectCount: Int): String =
        """#version 130
           |
           |uniform mat4[$objectCount] transforms;
           |
           |in int texture;
           |in int object;
           |in vec3 position;
           |in vec3 normal;
           |in vec2 uv;
           |in vec4 color;
           |
           |flat out int texture1;
           |out vec3 normal1;
           |out vec2 uv1;
           |out vec4 color1;
           |
           |void main() {
           |  texture1 = texture;
           |  mat4 tr = transforms[object];
           |  normal1 = normalize(vec3(tr * vec4(normal, 1.0)));
           |  uv1 = uv;
           |  color1 = color;
           |
           |  gl_Position = gl_ModelViewProjectionMatrix * tr * vec4(position, 1.0);
           |}
    """.trimMargin()

    private fun generateFShader(textureCount: Int): String =
        """#version 130
           |
           |uniform sampler2D lightmap;
           |uniform sampler2D[$textureCount] textures;
           |uniform vec2 lightmapCoord;
           |
           |flat in int texture1;
           |in vec3 normal1;
           |in vec2 uv1;
           |in vec4 color1;
           |
           |out vec4 color;
           |
           |void main() {
           |  vec4 texColor = texture(textures[texture1], uv1);
           |  vec4 lightmapColor = texture(lightmap, lightmapCoord);
           |  vec4 materialColor = color1;
           |
           |  vec3 lightAngle = vec3(0, -1, 0);
           |  float lightStr = 0.7 + 0.3 * (dot(normal1, -lightAngle) + 1) / 2;
           |
           |  color = texColor * lightmapColor * materialColor * vec4(lightStr, lightStr, lightStr, 1);
           |}
    """.trimMargin()

    private fun getRealIndex(total: Int, a: Int) = when {
        a > 0 -> a - 1
        a < 0 -> total + a
        else -> error("Invalid index!")
    }

    private fun <T> get(l: List<T>, index: Int): T =
        l[getRealIndex(l.size, index)]

}

private fun mkShader(vshs: String, fshs: String): Int {
    val vsh = glCreateShader(GL_VERTEX_SHADER)
    val fsh = glCreateShader(GL_FRAGMENT_SHADER)
    val prog = glCreateProgram()

    // No goto? I'll make my own.
    run {
        glShaderSource(vsh, vshs)
        glShaderSource(fsh, fshs)

        glCompileShader(vsh)
        if (glGetShaderi(vsh, GL_COMPILE_STATUS) == GL_FALSE) {
            // TODO use logger
            val log = glGetShaderInfoLog(vsh, 32768)
            logger.error("Failed to compile vertex shader")
            for (line in log.lineSequence()) println(line)
            return@run
        }

        glCompileShader(fsh)
        if (glGetShaderi(fsh, GL_COMPILE_STATUS) == GL_FALSE) {
            // TODO use logger
            val log = glGetShaderInfoLog(fsh, 32768)
            logger.error("Failed to compile fragment shader")
            for (line in log.lineSequence()) println(line)
            return@run
        }

        glAttachShader(prog, vsh)
        glAttachShader(prog, fsh)
        glLinkProgram(prog)

        if (glGetProgrami(prog, GL_LINK_STATUS) == GL_FALSE) {
            // TODO use logger
            val log = glGetProgramInfoLog(prog, 32768)
            logger.error("Failed to link program")
            for (line in log.lineSequence()) println(line)
            return@run
        }

        glDeleteShader(vsh)
        glDeleteShader(fsh)
        return prog
    }

    glDeleteShader(vsh)
    glDeleteShader(fsh)
    glDeleteProgram(prog)
    return 0
}