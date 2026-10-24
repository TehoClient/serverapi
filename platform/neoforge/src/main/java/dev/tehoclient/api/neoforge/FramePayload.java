/*
 * This file is part of Teho Server API, licensed under the MIT License.
 *
 * Copyright (c) 2026 SGM Studios LTD
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.tehoclient.api.neoforge;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.tehoclient.api.common.ProxyMessage;
import dev.tehoclient.api.protocol.TehoProtocol;

record FramePayload(Type<FramePayload> type, byte[] data) implements CustomPacketPayload {
	static final Type<FramePayload> API = new Type<>(Identifier.parse(TehoProtocol.CHANNEL));
	static final Type<FramePayload> PROXY = new Type<>(Identifier.parse(ProxyMessage.CHANNEL));

	static StreamCodec<RegistryFriendlyByteBuf, FramePayload> codec(Type<FramePayload> type) {
		return CustomPacketPayload.codec((payload, buf) -> buf.writeBytes(payload.data), buf -> {
			byte[] data = new byte[buf.readableBytes()];
			buf.readBytes(data);
			return new FramePayload(type, data);
		});
	}
}
