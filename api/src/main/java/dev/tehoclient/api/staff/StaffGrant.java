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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StaffGrant {
	public static final List<XrayBlock> DEFAULT_ORES = Collections.unmodifiableList(Arrays.asList(
			XrayBlock.of("minecraft:diamond_ore", 0x5FE0DC),
			XrayBlock.of("minecraft:deepslate_diamond_ore", 0x5FE0DC),
			XrayBlock.of("minecraft:ancient_debris", 0xB07CFF),
			XrayBlock.of("minecraft:emerald_ore", 0x7CDB8A),
			XrayBlock.of("minecraft:deepslate_emerald_ore", 0x7CDB8A),
			XrayBlock.of("minecraft:gold_ore", 0xFFC24D),
			XrayBlock.of("minecraft:deepslate_gold_ore", 0xFFC24D),
			XrayBlock.of("minecraft:spawner", 0xFF5555)));

	public static final List<XrayBlock> DEFAULT_STORAGE = Collections.unmodifiableList(Arrays.asList(
			XrayBlock.of("minecraft:chest", 0xFFC24D),
			XrayBlock.of("minecraft:trapped_chest", 0xFF9950),
			XrayBlock.of("minecraft:barrel", 0xC08A4D),
			XrayBlock.of("minecraft:ender_chest", 0xB07CFF),
			XrayBlock.of("minecraft:shulker_box", 0xFF7AD9),
			XrayBlock.of("minecraft:hopper", 0x9AA4B1),
			XrayBlock.of("minecraft:dropper", 0x9AA4B1),
			XrayBlock.of("minecraft:dispenser", 0x9AA4B1),
			XrayBlock.of("minecraft:furnace", 0x7A7A7A)));

	public static final List<XrayBlock> DEFAULT_ENTITIES = Collections.unmodifiableList(Arrays.asList(
			XrayBlock.of("minecraft:item", 0xFFFFFF),
			XrayBlock.of("minecraft:villager", 0x7CDB8A),
			XrayBlock.of("minecraft:armor_stand", 0x9AA4B1),
			XrayBlock.of("minecraft:minecart", 0x9AA4B1),
			XrayBlock.of("minecraft:tnt", 0xFF5555),
			XrayBlock.of("minecraft:wither", 0xB07CFF),
			XrayBlock.of("minecraft:ender_dragon", 0xB07CFF)));

	private final Set<StaffTool> tools;
	private final String reason;
	private final List<XrayBlock> xrayBlocks;
	private final List<XrayBlock> storageBlocks;
	private final List<XrayBlock> entityTypes;
	private final int radius;

	private StaffGrant(Builder builder) {
		this.tools = Collections.unmodifiableSet(EnumSet.copyOf(builder.tools));
		this.reason = builder.reason;
		this.xrayBlocks = Collections.unmodifiableList(new ArrayList<>(builder.blocks.values()));
		this.storageBlocks = Collections.unmodifiableList(new ArrayList<>(builder.storage.values()));
		this.entityTypes = Collections.unmodifiableList(new ArrayList<>(builder.entities.values()));
		this.radius = builder.radius;
	}

	public static Builder of(StaffTool first, StaffTool... rest) {
		Builder builder = new Builder();
		builder.tools.add(first);
		builder.tools.addAll(Arrays.asList(rest));
		return builder;
	}

	public static Builder of(Collection<StaffTool> tools) {
		if (tools.isEmpty()) {
			throw new IllegalArgumentException("at least one staff tool is required");
		}
		Builder builder = new Builder();
		builder.tools.addAll(tools);
		return builder;
	}

	public static Builder everything() {
		return of(EnumSet.allOf(StaffTool.class));
	}

	public Set<StaffTool> tools() {
		return tools;
	}

	public String reason() {
		return reason;
	}

	public List<XrayBlock> xrayBlocks() {
		return xrayBlocks;
	}

	public List<XrayBlock> storageBlocks() {
		return storageBlocks;
	}

	public List<XrayBlock> entityTypes() {
		return entityTypes;
	}

	public int radius() {
		return radius;
	}

	public Builder toBuilder() {
		Builder builder = of(tools).reason(reason).radius(radius);
		builder.xray(xrayBlocks).storage(storageBlocks).entities(entityTypes);
		return builder;
	}

	public static final class Builder {
		private final Set<StaffTool> tools = EnumSet.noneOf(StaffTool.class);
		private final Map<String, XrayBlock> blocks = new LinkedHashMap<>();
		private final Map<String, XrayBlock> storage = new LinkedHashMap<>();
		private final Map<String, XrayBlock> entities = new LinkedHashMap<>();
		private String reason = "";
		private int radius = 48;

		public Builder reason(String reason) {
			this.reason = reason == null ? "" : reason;
			return this;
		}

		public Builder xray(XrayBlock... blocks) {
			for (XrayBlock block : blocks) {
				this.blocks.put(block.block(), block);
			}
			return this;
		}

		public Builder xray(Collection<XrayBlock> blocks) {
			return xray(blocks.toArray(new XrayBlock[0]));
		}

		public Builder storage(XrayBlock... blocks) {
			for (XrayBlock block : blocks) {
				this.storage.put(block.block(), block);
			}
			return this;
		}

		public Builder storage(Collection<XrayBlock> blocks) {
			return storage(blocks.toArray(new XrayBlock[0]));
		}

		public Builder entities(XrayBlock... types) {
			for (XrayBlock type : types) {
				this.entities.put(type.block(), type);
			}
			return this;
		}

		public Builder entities(Collection<XrayBlock> types) {
			return entities(types.toArray(new XrayBlock[0]));
		}

		public Builder radius(int blocks) {
			this.radius = Math.max(8, Math.min(128, blocks));
			return this;
		}

		public StaffGrant build() {
			if (tools.contains(StaffTool.XRAY) && blocks.isEmpty()) {
				xray(DEFAULT_ORES);
			}
			if (tools.contains(StaffTool.STORAGE_ESP) && storage.isEmpty()) {
				storage(DEFAULT_STORAGE);
			}
			if (tools.contains(StaffTool.ENTITY_ESP) && entities.isEmpty()) {
				entities(DEFAULT_ENTITIES);
			}
			return new StaffGrant(this);
		}
	}
}
