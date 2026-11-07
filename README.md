# AIOEconomy — Paper 1.21.11 Prototype

All-in-one economy, shop, auction house, selling, payments, and player trading plugin.

## Build on GitHub

Push this repository to GitHub. GitHub Actions will build the plugin automatically.
The resulting JAR is uploaded as a workflow artifact.

## Commands

### Players
- `/sell gui` — opens a selling GUI
- `/sell hand` — sells the item in your main hand
- `/sell allhand` — sells all matching items from your inventory
- `/ah` — opens the auction house
- `/ah sell` — lists the item in your hand for its configured/default auction price
- `/ah sell <price>` — lists the item in your hand for a price
- `/ah search <keyword>` — searches auction listings
- Click your own AH listing to take it off the AH and return the item to your inventory. Other players can click to buy it.
- `/shop` — opens the shop category GUI; use the left/right arrow buttons to page through items
- In a shop section: left/right-click buys/sells 1; shift-left/shift-right buys/sells 16
- `/ah search hand` — searches listings matching your held item
- `/pay <player> <amount>`
- `/bal` or `/balance`
- `/baltop`
- `/trade <player>` — sends a request; the other player runs the same command to accept.

### Admin
- `/shopadd <section> <name>` — creates a shop section
- `/shopadd <item> <buyprice> <sellprice>` — adds the item in your hand to the selected/last shop section
- `/balset <player> <amount>`

## Prototype notes

This version intentionally uses YAML instead of an external database. It is suitable for
testing and a first server prototype. Auction listings, balances, shop configuration,
and pending trades are persisted under `plugins/AIOEconomy/`.

The shop item command uses the item in the admin's main hand. The current shop section is
the most recently created section.

The auction house uses a simple listing GUI and click-to-buy behavior. A future version
can add expiration, bidding, pagination, tax, confirmation screens, permissions,
transactions/history, and database-backed storage.


### Shop item sections

Admin shop items can be placed directly into a specific section:

`/shopadd item <buyprice> <sellprice> <section>`

The section can contain multiple words. If the section is omitted, the most recently created section is used.

### NBT / custom item data

Shop entries and auction listings store the complete Paper `ItemStack` as raw NBT (Base64 encoded in YAML), rather than storing only the material. This preserves item data such as enchantments, custom model data, names/lore, potion data, trim/data components, and other supported item NBT when the item is bought, sold, listed, or removed from the auction house.

Shop selling and `/sell hand` / `/sell allhand` match the full item template with `ItemStack.isSimilar()`, so two items with the same material but different item data are treated as different shop items.
