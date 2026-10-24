#!/usr/bin/env bash
# This file is part of Teho Server API, licensed under the MIT License.
#
# Copyright (c) 2026 SGM Studios LTD
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

set -euo pipefail

here="$(cd "$(dirname "$0")/../../lightweight" && pwd)"
base="${TEHO_REPO_URL:-https://repo.tehoclient.dev}/packages"
version="${1:?usage: publish.sh <version> [--check]}"
mode="${2:-}"
if [ "$#" -gt 2 ] || { [ -n "$mode" ] && [ "$mode" != "--check" ]; }; then
	echo 'usage: publish.sh <version> [--check]' >&2
	exit 1
fi
if [ "$mode" != "--check" ]; then
	user="${TEHO_REPO_USER:?set TEHO_REPO_USER}"
	token="${TEHO_REPO_TOKEN:?set TEHO_REPO_TOKEN}"
fi
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

exists() {
	local status
	status="$(curl -sS -o /dev/null -w '%{http_code}' "$base/$1")" || exit 1
	case "$status" in
		200) return 0 ;;
		404) return 1 ;;
		*) echo "cannot check $1: HTTP $status" >&2; exit 1 ;;
	esac
}

upload() {
	if exists "$2" && [ "${3:-}" != "replace" ]; then
		echo "refusing to overwrite $2, released versions never change" >&2
		exit 1
	fi
	curl -fsS -u "$user:$token" --upload-file "$1" "$base/$2" >/dev/null
	echo "uploaded $2"
}

fetch() {
	local status
	status="$(curl -sS -o "$2" -w '%{http_code}' "$base/$1")"
	case "$status" in
		200) ;;
		404) : > "$2" ;;
		*) echo "cannot fetch $1: HTTP $status" >&2; exit 1 ;;
	esac
}

check_version() {
	if [ "$1" != "$version" ]; then
		echo "$2 is at $1 but the release is $version" >&2
		exit 1
	fi
}

check_version "$(cd "$here/node" && node -p "require('./package.json').version")" "node/package.json"
check_version "$(sed -n 's/^version = "\(.*\)"/\1/p' "$here/python/pyproject.toml")" "python/pyproject.toml"
check_version "$(sed -n 's/^version = "\(.*\)"/\1/p' "$here/rust/Cargo.toml" | head -1)" "rust/Cargo.toml"

(cd "$here/node" && npm test && npm pack --pack-destination "$work" >/dev/null)
(cd "$here/python" && python -m unittest discover -s tests && python -m build --outdir "$work/python" >/dev/null)
(cd "$here/rust" && cargo test --quiet && cargo package --quiet --allow-dirty --target-dir "$work/rust")
crate="$work/rust/package/teho-api-$version.crate"

if [ "$mode" = "--check" ]; then
	echo "validated packages for $version (no uploads)"
	exit 0
fi

# Read both existing indexes before uploading anything. Only a 404 means an empty index.
fetch "pypi/index.html" "$work/index.html"
fetch "cargo/index/te/ho/teho-api" "$work/teho-api.index"
upload "$work/tehoclient-server-api-$version.tgz" "npm/tehoclient-server-api-$version.tgz"
for file in "$work"/python/*; do
	name="$(basename "$file")"
	upload "$file" "pypi/$name"
	sum="$(sha256sum "$file" | cut -d' ' -f1)"
	grep -Fq ">$name<" "$work/index.html" || echo "<a href=\"$name#sha256=$sum\">$name</a><br>" >> "$work/index.html"
done
upload "$work/index.html" "pypi/index.html" replace

upload "$crate" "cargo/crates/teho-api/teho-api-$version.crate"
sum="$(sha256sum "$crate" | cut -d' ' -f1)"
grep -Fq "\"vers\":\"$version\"" "$work/teho-api.index" || printf '{"name":"teho-api","vers":"%s","deps":[],"cksum":"%s","features":{},"yanked":false}\n' "$version" "$sum" >> "$work/teho-api.index"
upload "$work/teho-api.index" "cargo/index/te/ho/teho-api" replace
printf '{"dl":"%s/cargo/crates/{crate}/{crate}-{version}.crate"}\n' "$base" > "$work/config.json"
upload "$work/config.json" "cargo/index/config.json" replace
