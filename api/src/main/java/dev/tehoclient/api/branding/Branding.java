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

package dev.tehoclient.api.branding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.tehoclient.api.Units;
import dev.tehoclient.api.asset.Asset;

public final class Branding {
	private final dev.tehoclient.api.protocol.Branding wire;
	private final List<Asset> assets;

	private Branding(dev.tehoclient.api.protocol.Branding wire, List<Asset> assets) {
		this.wire = wire;
		this.assets = Collections.unmodifiableList(assets);
	}

	public static Builder builder(int accentRgb) {
		return new Builder(accentRgb);
	}

	public static Branding reset() {
		return new Branding(new dev.tehoclient.api.protocol.Branding(0, null, null, null, "", true), Collections.<Asset>emptyList());
	}

	public List<Asset> assets() {
		return assets;
	}

	public dev.tehoclient.api.protocol.Branding wire() {
		return wire;
	}

	public static final class Builder {
		private final int accent;
		private Asset logo;
		private Asset banner;
		private Asset background;
		private String tagline = "";

		private Builder(int accentRgb) {
			this.accent = Units.argb(accentRgb);
		}

		private static Asset png(Asset asset) {
			if (asset != null && asset.kind() != Asset.Kind.PNG) {
				throw new IllegalArgumentException("branding art must be PNG assets");
			}
			return asset;
		}

		public Builder logo(Asset logo) {
			this.logo = png(logo);
			return this;
		}

		public Builder banner(Asset banner) {
			this.banner = png(banner);
			return this;
		}

		public Builder background(Asset background) {
			this.background = png(background);
			return this;
		}

		public Builder tagline(String tagline) {
			this.tagline = tagline == null ? "" : tagline;
			return this;
		}

		public Branding build() {
			List<Asset> assets = new ArrayList<>();
			for (Asset asset : new Asset[] {logo, banner, background}) {
				if (asset != null) {
					assets.add(asset);
				}
			}
			return new Branding(new dev.tehoclient.api.protocol.Branding(accent, hash(logo), hash(banner), hash(background), tagline, false),
					assets);
		}

		private static byte[] hash(Asset asset) {
			return asset == null ? new byte[0] : asset.hash();
		}
	}
}
