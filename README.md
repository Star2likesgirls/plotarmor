# Plot Armor

Have you ever watched SMP videos such as unstable and wondered how you too could be immortal?
This plugin is for you! By giving a player **Plot Armor**, any lethal damage will leave them at a minimum of **0.5 hearts**.

---

## Compatibility

* **Server Software:** Paper
* **Minecraft Version:** **1.21.***

---

## Commands

| Command                      | Alias                 | Description                                                    |
| ---------------------------- | --------------------- | -------------------------------------------------------------- |
| `/plotarmor add <player>`    | `/pa add <player>`    | Grants plot armor to a player                                  |
| `/plotarmor remove <player>` | `/pa remove <player>` | Removes plot armor from a player                               |
| `/plotarmor list`            | `/pa list`            | Lists all protected players *(green = online, gray = offline)* |

---

## Permissions

If you're using a permissions plugin, give administrators the permission below.

| Permission        | Description                         | Default |
| ----------------- | ----------------------------------- | ------- |
| `plotarmor.admin` | Allows managing the plot armor list | OP      |

---

## Permission Node (plugin.yml)

```yaml
permissions:
  plotarmor.admin:
    description: Allows managing the plot armor list
    default: op
```
