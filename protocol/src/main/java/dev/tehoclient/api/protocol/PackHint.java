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

package dev.tehoclient.api.protocol;

public final class PackHint implements Message {
	public final String url;
	public final String sha1;
	public final int size;
	public final boolean required;
	public final String prompt;
	public final String id;

	public PackHint(String url, String sha1, int size, boolean required, String prompt, String id) {
		this.url = url == null ? "" : url;
		this.sha1 = sha1 == null ? "" : sha1;
		this.size = size;
		this.required = required;
		this.prompt = prompt == null ? "" : prompt;
		this.id = id == null ? "" : id;
	}

	@Override
	public int type() {
		return TehoProtocol.PACK_HINT;
	}

	@Override
	public void write(WireWriter out) {
		out.string(1, url);
		out.string(2, sha1);
		out.uint(3, size);
		out.bool(4, required);
		out.string(5, prompt);
		out.string(6, id);
	}

	static PackHint read(WireReader in) {
		String url = "";
		String sha1 = "";
		int size = 0;
		boolean required = false;
		String prompt = "";
		String id = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					url = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 2:
					sha1 = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					size = in.readUint();
					break;
				case 4:
					required = in.readBool();
					break;
				case 5:
					prompt = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 6:
					id = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new PackHint(url, sha1, size, required, prompt, id);
	}

	@Override
	public String toString() {
		return "PackHint{" + "url=" + url + ", " + "sha1=" + sha1 + ", " + "size=" + size + ", " + "required=" + required + ", " + "prompt=" + prompt + ", " + "id=" + id + "}";
	}
}
