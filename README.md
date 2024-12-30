![Discord](https://img.shields.io/discord/1322873747535040512)
![Build Status](https://img.shields.io/github/actions/workflow/status/Varilx-Development/VSit/build.yml?branch=main)
![Release](https://img.shields.io/github/v/release/Varilx-Development/VSit)

# VSit

A fully customizable Sit plugin

You can use `/sit` or click on blocks like `stairs` or `carpets`

![/sit](assets/slashsit.gif)
![carpets or stairs](assets/carpets.gif)

---

You can sit on other players:
![Ride other players](assets/OtherPlayers.gif)

Custom configuration:

```yaml
language: "en"

enabled: true
players:
  enabled: true
blocks:
  enabled: true
  blocks:
    - STAIR
    - CARPET
    - SLAB
```
---

Custom language configuration:

```yaml
# Using Minimessage https://docs.advntr.dev/minimessage/format.html

prefix: "<b><gradient:#08FB22:#BBFDAD>[VSit]</gradient></b><reset><!i><gray> " # This prefix can be used anywhere as "<prefix>"
startup: "<prefix>VSit has started up!"

commands:
  sit: "<prefix>You are now sitting down"
  reload: "<prefix>The Configurations has been reloaded"
```