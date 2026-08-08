# Sunstone — English description

> For the CurseForge project Description field. This file is not shipped with the mod.

---

## Sunstone

The sun isn't only something you look at. You can dig it up.

This mod adds sunstone ore, and from it: blocks that light a room like a torch,
apples that wind the clock backwards, and a brewing line that starts with a
flower on a farm plot and ends with you on fire.

### The ore

Sunstone ore generates between Y −24 and Y 56 — roughly the same band as iron —
in both stone and deepslate. You'll need an iron pickaxe or better. Fortune and
Silk Touch both work as you'd expect.

Smelt the raw sunstone you mine and you get **sunstone**. Everything starts there.

### Two branches

```
ore block --mine--> raw sunstone --smelt--> sunstone
                                               |
                    +--------------------------+--------------------------+
                    |                                                     |
            9 in a crafting grid                        8 sunstone + a wheat seed
                    v                                                     v
            block of sunstone                                     sunstone seed
            (lights like a torch)                                         |
                    |                                            plant on farmland
            8 blocks + an apple                                           v
                    v                                    6 growth stages -> sunstone flower
             sunstone apple                                               |
        (winds the day backwards)                                      brewing
```

### Sunstone apple

Eats like a normal apple — same 4 hunger — but rewinds the time of day by
2000 ticks. Like golden apples, you can eat it on a full hunger bar.

That build you're finishing at dusk? Buy yourself another stretch of daylight.
It won't push past dawn, though, so you can't farm it into an endless noon.
And after sunset it simply won't be eaten — no sense wasting one on the night.

### The sunstone rose

Grows from a sunstone seed on farmland through six stages, glowing brighter as
it goes. Bone meal works.

A harvested flower goes into the brewing stand — or you can just plant it,
or drop it in a flower pot. It gives off a faint light either way. And yes,
it works in suspicious stew.

### Brewing

Built on an awkward potion, like every vanilla brew. The ingredient is always
a sunstone flower, and each distillation stacks on top of the last one rather
than replacing it — though the stronger it gets, the shorter it lasts.

| Potion | What it does | Duration |
|---|---|---|
| **Solar Distillate** | Haste — ore comes away faster. No vanilla potion grants this at all; a beacon is your only other source | 1:30 |
| **Second Order** | the same, plus **you light up the space around you** like a torch. Leave the torches at home | 1:30 |
| **Third Order** | Haste II, the light — and you **burn**, trailing flames | 0:30 |

The third order is the only brew that asks something back. Fire resistance is
left out on purpose: if you want the power, come prepared — your own potion,
a rainstorm, or standing waist-deep in water.

Thrown, it's a weapon instead. Whatever it lands on catches fire.

Splash and lingering versions are made the usual way, with gunpowder and
dragon's breath.

### Requirements

- Minecraft **1.21.1**, Java Edition
- **Fabric Loader** 0.16.0 or newer
- **Fabric API** — required; the mod won't load without it

Works in singleplayer and on servers. For multiplayer the mod has to be
installed on the server *and* on every client.

### Source and licence

Free software under the **GNU GPL v3**. Sources are open:
https://github.com/leonlepaper/Sunstone

Study it, change it, redistribute it — including modified versions — as long as
those stay open under the GPL too.

By **lengineer**
