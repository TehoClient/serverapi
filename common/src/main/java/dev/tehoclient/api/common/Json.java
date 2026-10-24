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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {
	private final String text;
	private int at;

	private Json(String text) {
		this.text = text;
	}

	public static Object parse(String text) {
		Json json = new Json(text);
		json.space();
		Object value = json.value(0);
		json.space();
		if (json.at != text.length()) {
			throw new IllegalArgumentException("trailing JSON at " + json.at);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> object(String text) {
		Object value = parse(text);
		if (!(value instanceof Map)) {
			throw new IllegalArgumentException("expected a JSON object");
		}
		return (Map<String, Object>) value;
	}

	public static String string(Map<String, Object> map, String key) {
		Object value = map.get(key);
		return value instanceof String ? (String) value : "";
	}

	public static long number(Map<String, Object> map, String key) {
		Object value = map.get(key);
		return value instanceof Number ? ((Number) value).longValue() : 0L;
	}

	public static boolean bool(Map<String, Object> map, String key) {
		return Boolean.TRUE.equals(map.get(key));
	}

	@SuppressWarnings("unchecked")
	public static List<String> strings(Map<String, Object> map, String key) {
		List<String> out = new ArrayList<>();
		Object value = map.get(key);
		if (value instanceof List) {
			for (Object item : (List<Object>) value) {
				if (item instanceof String) {
					out.add((String) item);
				}
			}
		}
		return out;
	}

	public static String write(Object value) {
		StringBuilder out = new StringBuilder();
		write(value, out);
		return out.toString();
	}

	@SuppressWarnings("unchecked")
	private static void write(Object value, StringBuilder out) {
		if (value == null) {
			out.append("null");
		} else if (value instanceof String) {
			quote((String) value, out);
		} else if (value instanceof Number || value instanceof Boolean) {
			out.append(value);
		} else if (value instanceof Map) {
			out.append('{');
			boolean first = true;
			for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
				if (!first) {
					out.append(',');
				}
				first = false;
				quote(entry.getKey(), out);
				out.append(':');
				write(entry.getValue(), out);
			}
			out.append('}');
		} else if (value instanceof Collection) {
			out.append('[');
			boolean first = true;
			for (Object item : (Collection<Object>) value) {
				if (!first) {
					out.append(',');
				}
				first = false;
				write(item, out);
			}
			out.append(']');
		} else {
			quote(String.valueOf(value), out);
		}
	}

	private static void quote(String value, StringBuilder out) {
		out.append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '"':
					out.append("\\\"");
					break;
				case '\\':
					out.append("\\\\");
					break;
				case '\n':
					out.append("\\n");
					break;
				case '\r':
					out.append("\\r");
					break;
				case '\t':
					out.append("\\t");
					break;
				default:
					if (c < 0x20) {
						out.append(String.format("\\u%04x", (int) c));
					} else {
						out.append(c);
					}
			}
		}
		out.append('"');
	}

	private Object value(int depth) {
		if (depth > 64) {
			throw new IllegalArgumentException("JSON nested too deeply");
		}
		if (at >= text.length()) {
			throw new IllegalArgumentException("unexpected end of JSON");
		}
		char c = text.charAt(at);
		if (c == '{') {
			return object(depth);
		}
		if (c == '[') {
			return array(depth);
		}
		if (c == '"') {
			return string();
		}
		if (text.startsWith("true", at)) {
			at += 4;
			return Boolean.TRUE;
		}
		if (text.startsWith("false", at)) {
			at += 5;
			return Boolean.FALSE;
		}
		if (text.startsWith("null", at)) {
			at += 4;
			return null;
		}
		return number();
	}

	private Map<String, Object> object(int depth) {
		Map<String, Object> out = new LinkedHashMap<>();
		at++;
		space();
		if (peek('}')) {
			at++;
			return out;
		}
		while (true) {
			space();
			String key = string();
			space();
			expect(':');
			space();
			out.put(key, value(depth + 1));
			space();
			if (peek(',')) {
				at++;
				continue;
			}
			expect('}');
			return out;
		}
	}

	private List<Object> array(int depth) {
		List<Object> out = new ArrayList<>();
		at++;
		space();
		if (peek(']')) {
			at++;
			return out;
		}
		while (true) {
			space();
			out.add(value(depth + 1));
			space();
			if (peek(',')) {
				at++;
				continue;
			}
			expect(']');
			return out;
		}
	}

	private String string() {
		expect('"');
		StringBuilder out = new StringBuilder();
		while (at < text.length()) {
			char c = text.charAt(at++);
			if (c == '"') {
				return out.toString();
			}
			if (c != '\\') {
				out.append(c);
				continue;
			}
			if (at >= text.length()) {
				break;
			}
			char escaped = text.charAt(at++);
			switch (escaped) {
				case 'n':
					out.append('\n');
					break;
				case 'r':
					out.append('\r');
					break;
				case 't':
					out.append('\t');
					break;
				case 'b':
					out.append('\b');
					break;
				case 'f':
					out.append('\f');
					break;
				case 'u':
					if (at + 4 > text.length()) {
						throw new IllegalArgumentException("bad unicode escape");
					}
					out.append((char) Integer.parseInt(text.substring(at, at + 4), 16));
					at += 4;
					break;
				default:
					out.append(escaped);
			}
		}
		throw new IllegalArgumentException("unterminated JSON string");
	}

	private Number number() {
		int start = at;
		while (at < text.length() && "+-0123456789.eE".indexOf(text.charAt(at)) >= 0) {
			at++;
		}
		String raw = text.substring(start, at);
		if (raw.isEmpty()) {
			throw new IllegalArgumentException("unexpected JSON at " + start);
		}
		if (raw.indexOf('.') >= 0 || raw.indexOf('e') >= 0 || raw.indexOf('E') >= 0) {
			return Double.parseDouble(raw);
		}
		return Long.parseLong(raw);
	}

	private boolean peek(char c) {
		return at < text.length() && text.charAt(at) == c;
	}

	private void expect(char c) {
		if (!peek(c)) {
			throw new IllegalArgumentException("expected '" + c + "' at " + at);
		}
		at++;
	}

	private void space() {
		while (at < text.length() && Character.isWhitespace(text.charAt(at))) {
			at++;
		}
	}
}
