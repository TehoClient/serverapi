# Teho Server API

Server-side integrations for [Teho](https://tehoclient.com). Supports Bukkit, Paper, Folia, Velocity, BungeeCord, Minestom, Fabric, NeoForge and Sponge, with lightweight libraries for Node.js, Python and Rust.

[Documentation](https://tehoclient.dev) · [Downloads](https://repo.tehoclient.dev/releases/dev/tehoclient)

## Usage

Install the TehoAPI plugin on your server, then add the repository and platform dependency to your plugin's build:

```kotlin
repositories {
    maven("https://repo.tehoclient.dev/releases")
}

dependencies {
    compileOnly("dev.tehoclient:teho-api-bukkit:1.0.0")
}
```

For Bukkit/Paper, add `depend: [TehoAPI]` to your `plugin.yml`. Other platform integrations and the lightweight libraries are covered in the [developer documentation](https://tehoclient.dev).

## Modules

| Directory | Purpose |
|---|---|
| `api` | Platform-neutral Java API |
| `protocol` | Wire schema, codec and compatibility tests |
| `common` | Shared server implementation |
| `platform` | Server and proxy adapters |
| `lightweight` | Node.js, Python and Rust libraries |
| `sample` | Example Bukkit plugin |

## Building

Requires JDK 25.

```sh
bash ./gradlew build
```

On Windows, run `.\gradlew.bat build`.

To regenerate the lightweight protocol bindings:

```sh
python lightweight/generate.py
```

## License

[MIT](LICENSE). The Gradle wrapper retains its Apache 2.0 license, included in `gradle/wrapper/LICENSE-2.0.txt`.
