<p align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/b26afd48dd742676bd98a2f68603aef2691a0454_0.webp" alt="VSit Logo" width="1000">
</p>

<p align="center">
  <a href="https://discord.gg/ZPyb9g6Gs4">
    <img src="https://img.shields.io/discord/1322873747535040512" alt="Discord">
  </a>
  <a href="https://github.com/Varilx-Development/VSit/actions/workflows/build.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/Varilx-Development/VSit/build.yml?branch=main" alt="Build Status">
  </a>
  <a href="https://github.com/Varilx-Development/VSit/releases">
    <img src="https://img.shields.io/github/v/release/Varilx-Development/VSit" alt="Latest Release">
  </a>
</p>

<p align="center">
  <a href="https://modrinth.com/plugin/vsit">
    <img src="https://raw.githubusercontent.com/vLuckyyy/badges/main/avaiable-on-modrinth.svg" alt="Available on Modrinth" />
  </a>
</p>

---

# VSit

**VSit** is a fully customizable sitting plugin for Minecraft servers.  
Players can sit using commands or by interacting with various blocks.

---

## 🔧 Features

- Sit by using `/sit` or clicking on stairs, carpets, and slabs
- Sit on other players
- **Crawl mode**: Use `/crawl` command to enter crawling mode with swimming pose
- Empty hand requirement option for sitting and crawling
- Prevent shift-click sitting on players
- Eject players by crouching while sitting on them
- **Auto-stop crawling**: Automatically stop crawling on jump, sneak, damage, or vertical movement (configurable)
- Full language customization via [MiniMessage](https://docs.advntr.dev/minimessage/format.html)
- Permissions support
- Easily reload config with `/vsit reload`

---

## ✅ Permissions

| Permission       | Description                          |
|------------------|--------------------------------------|
| `vsit.sit`       | Allows the user to sit               |
| `vsit.crawl`     | Allows the user to crawl             |
| `vsit.crawl.toggle` | Allows players to use the `/crawl toggle` command |
| `vsit.reload`    | Allows reloading the plugin config   |

---

## 🎮 How to Use

### Sitting
You can sit by:

- Using the `/sit` command  
- Right-clicking supported blocks (stairs, carpets, slabs)
- Right-clicking on other players (if not shift-clicking)

### Crawling
- Use `/crawl` command to enter/exit crawling mode
- **Swimming pose**: Players will appear in a swimming position while crawling
- **Auto-stop**: Crawling automatically stops on jump, sneak, damage, or vertical movement (configurable)
- **Empty hand requirement**: If enabled in config, you must have an empty main hand to crawl

### Commands
- `/sit` - Sit on the block below you
- `/crawl` - Enter/exit crawling mode
- `/crawl toggle` - Enable/disable crawling for yourself
- `/crawl stop` - Stop crawling (alternative to `/crawl`)
- `/vsit reload` - Reload the plugin configuration (admin only)


### 🖼️ Preview

| `/sit` Command | Sitting on Stairs | Sitting on Players |
|----------------|-------------------|---------------------|
| ![slashsit](https://github.com/Varilx-Development/VSit/blob/main/assets/slashsit.gif?raw=true) | ![carpets](https://github.com/Varilx-Development/VSit/blob/main/assets/carpets.gif?raw=true) | ![players](https://github.com/Varilx-Development/VSit/blob/main/assets/OtherPlayers.gif?raw=true) |

---

## ⚙️ Configuration Example

### `config.yml`

```yaml
language: "en"

enabled: true
players:
  enabled: true
  require-empty-hand: false
  blocked-worlds:
    - "SOME_CUSTOM_DISABLED_WORLD"

blocks:
  enabled: true
  require-empty-hand: false
  right-click: true
  left-click: false
  blocked-worlds:
    - "DISABLED_WORLD"
  blocks:
    - STAIR
    - CARPET
    - SLAB

crawl:
  enabled: true
  require-empty-hand: false
  blocked-worlds:
    - "SOME_CUSTOM_DISABLED_WORLD"
  stop-on-jump: true
  stop-on-sneak: true
  stop-on-damage: true
  stop-on-vertical-movement: true
```

### language.yml
```yaml
# Using MiniMessage format

prefix: "<b><gradient:#08FB22:#BBFDAD>[VSit]</gradient></b><reset><!i><gray> "

startup: "<prefix>VSit has started up!"

commands:
  sit: "<prefix>You are now sitting down"
  reload: "<prefix>The configuration has been reloaded"
```

## 🛟 Need Help?

Join our Discord for support, updates, or to report bugs:
<p align="center"> <a href="https://discord.gg/ZPyb9g6Gs4"> <img src="https://cdn.varilx.de/raw/Zm9inS.png" alt="Join our Discord" width="400"> </a> </p>
