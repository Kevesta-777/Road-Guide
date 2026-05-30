package com.example.roadguideapp.panorama.gl

import kotlin.math.cos
import kotlin.math.sin

/** Generates an inside-facing sphere mesh with equirectangular UV coordinates. */
object SphereMesh {

    data class Data(
        val vertices: FloatArray,
        val texCoords: FloatArray,
        val indices: ShortArray,
        val indexCount: Int,
    )

    fun create(radius: Float = 1f, stacks: Int = 48, slices: Int = 96): Data {
        val vertexCount = (stacks + 1) * (slices + 1)
        val vertices = FloatArray(vertexCount * 3)
        val texCoords = FloatArray(vertexCount * 2)

        var index = 0
        for (stack in 0..stacks) {
            val phi = Math.PI * stack / stacks
            val sinPhi = sin(phi)
            val cosPhi = cos(phi)

            for (slice in 0..slices) {
                val theta = 2.0 * Math.PI * slice / slices
                val sinTheta = sin(theta)
                val cosTheta = cos(theta)

                val x = (radius * sinPhi * cosTheta).toFloat()
                val y = (radius * cosPhi).toFloat()
                val z = (radius * sinPhi * sinTheta).toFloat()

                vertices[index * 3] = x
                vertices[index * 3 + 1] = y
                vertices[index * 3 + 2] = z

                texCoords[index * 2] = slice.toFloat() / slices
                texCoords[index * 2 + 1] = stack.toFloat() / stacks
                index++
            }
        }

        val indices = mutableListOf<Short>()
        for (stack in 0 until stacks) {
            for (slice in 0 until slices) {
                val first = (stack * (slices + 1) + slice).toShort()
                val second = (first + slices + 1).toShort()

                indices.add(first)
                indices.add((first + 1).toShort())
                indices.add(second)

                indices.add(second)
                indices.add((first + 1).toShort())
                indices.add((second + 1).toShort())
            }
        }

        val indexArray = indices.toShortArray()
        return Data(vertices, texCoords, indexArray, indexArray.size)
    }
}
