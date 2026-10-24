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

package dev.tehoclient.api.modules;

import java.util.function.UnaryOperator;

public interface ModulesApi {
	ModuleRules rules();

	boolean apply(ModuleRules rules);

	default boolean update(UnaryOperator<ModuleRules.Builder> change) {
		return apply(change.apply(rules().toBuilder()).build());
	}

	default boolean disable(String... modules) {
		return update(builder -> builder.disable(modules));
	}

	default boolean competitive(String reason, String... allowedModules) {
		return update(builder -> builder.competitive(allowedModules).reason(reason));
	}

	default boolean cap(SettingCap cap) {
		return update(builder -> builder.cap(cap));
	}

	boolean combat(CombatRules rules);

	java.util.concurrent.CompletableFuture<PolicyReport> denyMods(String reason, String... modIds);

	default boolean clear() {
		return apply(ModuleRules.NONE);
	}
}
