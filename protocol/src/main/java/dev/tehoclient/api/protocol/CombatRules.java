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

public final class CombatRules implements Message {
	public final boolean legacySwing;
	public final boolean legacyBlockhit;
	public final boolean noHitDelayVisual;
	public final boolean missPenaltyVisual;
	public final boolean legacyHitColor;

	public CombatRules(boolean legacySwing, boolean legacyBlockhit, boolean noHitDelayVisual, boolean missPenaltyVisual, boolean legacyHitColor) {
		this.legacySwing = legacySwing;
		this.legacyBlockhit = legacyBlockhit;
		this.noHitDelayVisual = noHitDelayVisual;
		this.missPenaltyVisual = missPenaltyVisual;
		this.legacyHitColor = legacyHitColor;
	}

	@Override
	public int type() {
		return TehoProtocol.COMBAT_RULES;
	}

	@Override
	public void write(WireWriter out) {
		out.bool(1, legacySwing);
		out.bool(2, legacyBlockhit);
		out.bool(3, noHitDelayVisual);
		out.bool(4, missPenaltyVisual);
		out.bool(5, legacyHitColor);
	}

	static CombatRules read(WireReader in) {
		boolean legacySwing = false;
		boolean legacyBlockhit = false;
		boolean noHitDelayVisual = false;
		boolean missPenaltyVisual = false;
		boolean legacyHitColor = false;
		while (in.hasNext()) {
			switch (in.nextField()) {
				case 1:
					legacySwing = in.readBool();
					break;
				case 2:
					legacyBlockhit = in.readBool();
					break;
				case 3:
					noHitDelayVisual = in.readBool();
					break;
				case 4:
					missPenaltyVisual = in.readBool();
					break;
				case 5:
					legacyHitColor = in.readBool();
					break;
				default:
					in.skip();
			}
		}
		return new CombatRules(legacySwing, legacyBlockhit, noHitDelayVisual, missPenaltyVisual, legacyHitColor);
	}

	@Override
	public String toString() {
		return "CombatRules{" + "legacySwing=" + legacySwing + ", " + "legacyBlockhit=" + legacyBlockhit + ", " + "noHitDelayVisual=" + noHitDelayVisual + ", " + "missPenaltyVisual=" + missPenaltyVisual + ", " + "legacyHitColor=" + legacyHitColor + "}";
	}
}
