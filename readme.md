# AutoSwitch ![Java CI](https://github.com/dexman545/Fabric-Autoswitch/workflows/Java%20CI/badge.svg)
![client](https://img.shields.io/badge/Environment-Client-1976d2?style=for-the-badge)
[![Fabric API](https://img.shields.io/badge/Requires-Fabric%20API-purple?style=for-the-badge)](https://modrinth.com/mod/fabric-api "Download Fabric API")

**AutoSwitch** is a Minecraft mod (for Fabric / NeoForge) that automatically swaps the held item based on what you’re
about to do - mining, attacking, interacting, or when specific stats change and what is available on your hotbar
(i.e. you hit a stone block with your fist, it will switch to a pickaxe).

It’s rule-driven and highly configurable via a HOCON config file `autoswitch.conf`,
located in the `config` folder which is next to the resource pack folder.
A quick way to navigate there is by going to `Options > Resource Packs > navigate up one folder`.

The mod can be toggled on/off dynamically in game by pressing `R`.

---

## Configuration

> This section only applies to AutoSwitch 12+. Earlier versions used a different config format.

There are four sections in the config file - three for defining selectors and targets when various actions occur,
and one for general feature configuration:

* `attack-action` - rules used when **attacking** (entities or blocks).
* `interact-action` - rules used when **interacting** (right-click-like actions).
* `stat-change-action` - rules triggered by stat events (e.g., using a totem of undying).
* `feature-config` - Controls the overall behaviour of the mod.

Each rule contains:

* `priority` - integer; higher values are given precedence what multiple targets match.
* `target` - what this rule matches (types: `BLOCK`, `ENTITY`, `STAT`, or an `EXPRESSION` allowing the combination of other targets).
  Targets can reference tags or specific explicit IDs (`minecraft:ender_chest`, `sugar_cane`).
* `tools` - ordered array of candidates to switch to. Each must be an `ITEM` or an `EXPRESSION` of `ITEM`s,
  or be an empty list (to disable switching for that target).

`EXPRESSION`s can have as many elements or as much depth as you wish, allowing for a great deal of control.
The default config covers a wide range of cases and should serve as a great basis for expanding.

Tools and targets can have an optional `data` field to control things like enchantments or blockstates that should be matched.

### Live edits

Simply resave the config file when ingame for it to be automatically reloaded.

### The Gritty Details

#### Expressions

Expressions can be nested to any depth you desire.

Expressions support the following operations:
* `OR` - Match any one of its children
* `AND` - Match all of its children
* `NOT` - Match none of its children
* `XOR` - Match only one of its children

#### Data

Currently, the following data types are supported:
* `BlockState` - The block state to match
* `Component` - Currently only supports `POTION_CONTENTS` on items
* `Enchantment` - The enchantments an item has
* `EntityEquipment` - The equipment an entity is wearing, such as a pig with a saddle

#### Selection Priority

**Decision order (first thing that differs wins):**
1. **Target Priority** - rules with higher priority are preferred.
2. **Target Rating (multi-level)** - compare rating level 0 - n up to the configured maximum.
   At each level compare, in order:
    * `isGroup` (prefer non-groups)
    * `hasData` (prefer those with data)
    * `typeRating` (higher preferred, e.g. weapon DPS or mining level)
    * `dataRating` (higher preferred, e.g. normalized enchantment level)
3. **Tool Priority** - prefer tools that are intrinsically prioritized higher.
4. **Tool Rating (multi-level)** - same multi-level comparison logic as Target Rating (isGroup → hasData → typeRating → dataRating).
5. **Is the slot currently selected?** - prefer the currently held slot if tied so far.
6. **Smallest slot index** - final tie-breaker: lowest (leftmost) inventory slot wins.

##### Text-based Flowchart
```
START: candidate inventory slots (all slots that matched a selector)
|
v
Compare Target Priority
|-- if different --> choose highest Target Priority --> END
|
v (same)
Compare Target Rating (levels 0..N)
For each level:
- compare: isGroup? (non-group preferred)
- compare: hasData? (with data preferred)
- compare: typeRating (higher wins)
- compare: dataRating (higher wins)
|-- if difference found --> choose winner --> END
|
v (no difference)
Compare Tool Priority
|-- if different --> choose highest Tool Priority --> END
|
v (same)
Compare Tool Rating (levels 0..N)
(same per-level rules as Target Rating)
|-- if difference found --> choose winner --> END
|
v (still tied)
Prefer currently selected slot?
|-- Yes --> choose currently selected --> END
|-- No  --> choose smallest (leftmost) slot --> END
```