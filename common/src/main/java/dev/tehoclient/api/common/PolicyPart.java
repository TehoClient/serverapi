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

package dev.tehoclient.api.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.tehoclient.api.modules.CombatRules;
import dev.tehoclient.api.modules.PolicyReport;
import dev.tehoclient.api.protocol.Message;
import dev.tehoclient.api.protocol.ModEntry;
import dev.tehoclient.api.protocol.TehoProtocol;
import dev.tehoclient.api.protocol.ThirdPartyPolicy;
import dev.tehoclient.api.suspect.ModListResult;

final class PolicyPart extends Part {
	private CombatRules combat = CombatRules.VANILLA;
	private ThirdPartyPolicy policy;
	private CompletableFuture<PolicyReport> pending;

	PolicyPart(TehoSession session) {
		super(session);
	}

	synchronized boolean combat(CombatRules rules) {
		combat = rules == null ? CombatRules.VANILLA : rules;
		return send(TehoProtocol.MODULES, combat.wire());
	}

	synchronized CompletableFuture<PolicyReport> deny(String reason, String... modIds) {
		List<String> ids = new ArrayList<>();
		for (String id : Arrays.asList(modIds)) {
			if (id != null && !id.trim().isEmpty()) {
				ids.add(id.trim().toLowerCase(java.util.Locale.ROOT));
			}
		}
		policy = new ThirdPartyPolicy(ids, reason, true);
		if (pending != null) {
			pending.complete(new PolicyReport(false, new ArrayList<ModListResult.Mod>()));
		}
		pending = new CompletableFuture<>();
		CompletableFuture<PolicyReport> answer = pending;
		if (!send(TehoProtocol.MODULES, policy)) {
			pending = null;
			answer.complete(new PolicyReport(false, new ArrayList<ModListResult.Mod>()));
		}
		return answer;
	}

	@Override
	synchronized void reapply() {
		if (combat != CombatRules.VANILLA) {
			send(TehoProtocol.MODULES, combat.wire());
		}
		if (policy != null) {
			send(TehoProtocol.MODULES, policy);
		}
	}

	@Override
	boolean receive(Message message) {
		if (!(message instanceof dev.tehoclient.api.protocol.PolicyReport)) {
			return false;
		}
		dev.tehoclient.api.protocol.PolicyReport report = (dev.tehoclient.api.protocol.PolicyReport) message;
		List<ModListResult.Mod> violations = new ArrayList<>();
		for (ModEntry entry : report.violations) {
			violations.add(new ModListResult.Mod(entry.id, entry.name, entry.version));
		}
		CompletableFuture<PolicyReport> answer;
		synchronized (this) {
			answer = pending;
			pending = null;
		}
		if (answer != null) {
			answer.complete(new PolicyReport(report.granted, violations));
		}
		return true;
	}

	@Override
	synchronized void reset() {
		combat = CombatRules.VANILLA;
		policy = null;
	}

	@Override
	synchronized void close() {
		if (pending != null) {
			pending.complete(new PolicyReport(false, new ArrayList<ModListResult.Mod>()));
			pending = null;
		}
	}
}
