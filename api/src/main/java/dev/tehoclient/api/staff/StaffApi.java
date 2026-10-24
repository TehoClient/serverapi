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

package dev.tehoclient.api.staff;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface StaffApi {
	boolean grant(StaffGrant grant);

	boolean revoke(StaffTool... tools);

	boolean revokeAll();

	Set<StaffTool> granted();

	Set<StaffTool> accepted();

	Set<StaffTool> active();

	Set<StaffTool> supported();

	boolean espColors(Map<UUID, Integer> colors, Map<UUID, String> labels);

	boolean highlight(Highlight highlight);

	boolean removeHighlights(String... ids);

	boolean clearHighlights();

	boolean trail(UUID player, List<int[]> points, int rgb, String label, String dimension, int ttlMs);

	boolean actionWheel(String title, List<WheelAction> actions);

	boolean alert(StaffAlert alert);

	boolean targetStats(UUID player, List<StatRow> rows, int ttlMs);

	boolean spectate(UUID player, int distance);

	boolean stopSpectating();

	boolean lagMap(String dimension, Collection<LagCell> cells, int ttlMs);
}
