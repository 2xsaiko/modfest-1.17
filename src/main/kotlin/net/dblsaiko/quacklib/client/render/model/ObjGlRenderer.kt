package net.dblsaiko.quacklib.client.render.model

import net.dblsaiko.qcommon.croco.Mat4
import net.dblsaiko.qcommon.croco.Vec3
import net.dblsaiko.quacklib.model.Face
import net.dblsaiko.quacklib.model.OBJRoot
import net.dblsaiko.quacklib.model.Object
import net.dblsaiko.quacklib.model.fallbackMaterial
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import trucc.client.render.ExtraRenderLayers

class ObjGlRenderer(val obj: OBJRoot, val textureOverrides: Map<String, String> = emptyMap()) {
    private var translations: Map<Object, Mat4> = emptyMap()

    private var isDirty = true

    fun reset() {
        translations = emptyMap()
        isDirty = true
    }

    fun transformPart(name: String, mat: Mat4) {
        val part = obj.objects[name] ?: return
        val ct = translations[part] ?: Mat4.IDENTITY
        translations += part to ct.mul(mat)
        isDirty = true
    }

    fun transformGroup(name: String, mat: Mat4) {
        obj.objects.filter { (_, part) -> name in part.groups }.keys.forEach { transformPart(it, mat) }
        isDirty = true
    }

    fun draw(stack: MatrixStack, vcp: VertexConsumerProvider, light: Int = 0x00F000F0) {
        obj.faces.forEach { this.drawFace(stack, vcp, light, it) }

        for (o in obj.objects.values) {
            stack.push()
            translations[o]?.also { stack.method_34425(it.toMatrix4f()) }
            o.faces.forEach { this.drawFace(stack, vcp, light, it) }
            stack.pop()
        }

        isDirty = false
    }

    private fun drawFace(stack: MatrixStack, vcp: VertexConsumerProvider, light: Int, face: Face) {
        val mat = obj.materials[face.material] ?: fallbackMaterial
        val rl = (textureOverrides[face.material] ?: mat.diffuseTexture)?.let(::Identifier)

        val buf = vcp.getBuffer(ExtraRenderLayers.getEntityCutoutTris(rl))

        for (vertex in face.vertices) {
            val (x, y, z) = obj.vertPos[getRealIndex(obj.vertPos.size, vertex.xyz)]
            val (nx, ny, nz) = vertex.normal?.let { obj.vertNormal[getRealIndex(obj.vertNormal.size, it)] } ?: Vec3.ORIGIN
            val (u, v) = if (vertex.tex != null) obj.vertTex[getRealIndex(obj.vertTex.size, vertex.tex)] else Vec3.ORIGIN
            val (r, g, b) = mat.diffuse.getRGBColorComponents(FloatArray(3))
            buf
                .vertex(stack.peek().model, x, y, z)
                .color(r, g, b, mat.transparency)
                .texture(u, 1 - v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(stack.peek().normal, nx, ny, nz)
                .next()
        }
    }

    private fun getRealIndex(total: Int, a: Int) = when {
        a > 0 -> a - 1
        a < 0 -> total + a
        else -> error("Invalid index!")
    }
}

private operator fun Vec3.component1() = this.x
private operator fun Vec3.component2() = this.y
private operator fun Vec3.component3() = this.z