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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InfoRequest implements Message {
	public final int requestId;
	public final List<String> fields;
	public final String reason;

	public InfoRequest(int requestId, List<String> fields, String reason) {
		this.requestId = requestId;
		this.fields = fields == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(fields));
		this.reason = reason == null ? "" : reason;
	}

	@Override
	public int type() {
		return TehoProtocol.INFO_REQUEST;
	}

	@Override
	public void write(WireWriter out) {
		out.uint(1, requestId);
		for (String item : fields) {
			out.string(2, item);
		}
		out.string(3, reason);
	}

	static InfoRequest read(WireReader in) {
		int requestId = 0;
		List<String> fields = new ArrayList<>();
		String reason = "";
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					requestId = in.readUint();
					break;
				case 2:
					String fieldsItem = in.readString(TehoProtocol.MAX_STRING);
					if (fields.size() < TehoProtocol.MAX_LIST) {
						fields.add(fieldsItem);
					}
					break;
				case 3:
					reason = in.readString(TehoProtocol.MAX_STRING);
					break;
				default:
					in.skip();
			}
		}
		return new InfoRequest(requestId, fields, reason);
	}

	@Override
	public String toString() {
		return "InfoRequest{" + "requestId=" + requestId + ", " + "fields=" + fields + ", " + "reason=" + reason + "}";
	}
}
