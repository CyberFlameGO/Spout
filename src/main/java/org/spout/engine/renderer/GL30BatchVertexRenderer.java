/*
 * This file is part of Spout (http://www.spout.org/).
 *
 * Spout is licensed under the SpoutDev License Version 1.
 *
 * Spout is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * Spout is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.engine.renderer;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class GL30BatchVertexRenderer extends BatchVertexRenderer {
	final int SIZE_FLOAT = 4;
	int vao;
	int vbos = -1;

	/**
	 * Batch Renderer using OpenGL 3.0 mode.
	 * @param renderMode Mode to render in
	 */
	public GL30BatchVertexRenderer(int renderMode) {
		super(renderMode);
		vao = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao);
	}

	@Override
	protected void doFlush() {
		if (activeShader == null) {
			throw new IllegalStateException("Batch must have a shader attached");
		}
		if (vbos != -1) {
			GL15.glDeleteBuffers(vbos);
		}

		GL30.glBindVertexArray(vao);
		int size = numVerticies * 4 * SIZE_FLOAT;
		if (useColors) {
			size += numVerticies * 4 * SIZE_FLOAT;
		}
		if (useNormals) {
			size += numVerticies * 4 * SIZE_FLOAT;
		}
		if (useTextures) {
			size += numVerticies * 2 * SIZE_FLOAT;
		}

		vbos = GL15.glGenBuffers();

		int offset = 0;
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbos);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, size, GL15.GL_STATIC_DRAW);

		FloatBuffer vBuffer = BufferUtils.createFloatBuffer(vertexBuffer.size());
		vBuffer.clear();
		vBuffer.put(vertexBuffer.toArray());
		vBuffer.flip();
		//GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, vBuffer);
		activeShader.enableAttribute("vPosition", 4, GL11.GL_FLOAT, 0, offset);
		offset += numVerticies * 4 * SIZE_FLOAT;
		if (useColors) {

			vBuffer = BufferUtils.createFloatBuffer(colorBuffer.size());
			vBuffer.clear();
			vBuffer.put(colorBuffer.toArray());
			vBuffer.flip();
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, vBuffer);

			activeShader.enableAttribute("vColor", 4, GL11.GL_FLOAT, 0, offset);
			offset += numVerticies * 4 * SIZE_FLOAT;
		}
		if (useNormals) {

			vBuffer = BufferUtils.createFloatBuffer(normalBuffer.size());
			vBuffer.clear();
			vBuffer.put(normalBuffer.toArray());
			vBuffer.flip();
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, vBuffer);

			activeShader.enableAttribute("vNormal", 4, GL11.GL_FLOAT, 0, offset);
			offset += numVerticies * 4 * SIZE_FLOAT;
		}
		if (useTextures) {

			vBuffer = BufferUtils.createFloatBuffer(uvBuffer.size());
			vBuffer.clear();
			vBuffer.put(uvBuffer.toArray());
			vBuffer.flip();
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, vBuffer);

			activeShader.enableAttribute("vTexCoord", 2, GL11.GL_FLOAT, 0, offset);
			offset += numVerticies * 2 * SIZE_FLOAT;
		}

		activeShader.assign();
	}

	/**
	 * Draws this batch
	 */
	@Override
	public void doRender() {
		GL30.glBindVertexArray(vao);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbos);

		activeShader.assign();
		GL11.glDrawArrays(renderMode, 0, numVerticies);
	}
}
