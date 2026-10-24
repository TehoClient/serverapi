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

public final class ThirdPartyPolicy implements Message {
	public final List<String> deny;
	public final String reason;
	public final boolean report;

	public ThirdPartyPolicy(List<String> deny, String reason, boolean report) {
		this.deny = deny == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<>(deny));
		this.reason = reason == null ? "" : reason;
		this.report = report;
	}

	@Override
	public int type() {
		return TehoProtocol.THIRD_PARTY_POLICY;
	}

	@Override
	public void write(WireWriter out) {
		for (String item : deny) {
			out.string(1, item);
		}
		out.string(2, reason);
		out.bool(3, report);
	}

	static ThirdPartyPolicy read(WireReader in) {
		List<String> deny = new ArrayList<>();
		String reason = "";
		boolean report = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					String denyItem = in.readString(TehoProtocol.MAX_STRING);
					if (deny.size() < TehoProtocol.MAX_LIST) {
						deny.add(denyItem);
					}
					break;
				case 2:
					reason = in.readString(TehoProtocol.MAX_STRING);
					break;
				case 3:
					report = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new ThirdPartyPolicy(deny, reason, report);
	}

	@Override
	public String toString() {
		return "ThirdPartyPolicy{" + "deny=" + deny + ", " + "reason=" + reason + ", " + "report=" + report + "}";
	}
}
