
## Helpers

Since there are also Helpers like the Builder's Apprentice and the Lab Assistant that work once every 24 hours and shorten the time of the building they are assigned to by a fixed amount (the time shortened depends upon the level of the helper). I should also make an area where the user can set the level of their helpers and a button next to each ongoing upgrade that they can press and by pressing the button they would cut down the time remaining by a fixed amount. The Builder's Apprentice cuts down time for the Builder upgrades while the Research Assistant cuts down time for the laboratory upgrades. So their respective buttons should only appear in front of their relevant ongoing upgrades (i.e. The button that cuts down building upgrade times via the Builder's Apprentice should only appear with Building Upgrades and the button that cuts down Lab Research time via the Research assistant should only appear with the Lab upgrades)

The following are the levels and the Work Rate of the Builder's Apprentice

| Level  <br>![Level](https://static.wikia.nocookie.net/clashofclans/images/3/3d/Level.png/revision/latest/scale-to-width-down/17?cb=20130717031826) | Work Rate  <br>![Repair](https://static.wikia.nocookie.net/clashofclans/images/e/e2/Repair.png/revision/latest/scale-to-width-down/20?cb=20210529045411) |
| -------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1                                                                                                                                                  | 1x                                                                                                                                                       |
| 2                                                                                                                                                  | 2x                                                                                                                                                       |
| 3                                                                                                                                                  | 3x                                                                                                                                                       |
| 4                                                                                                                                                  | 4x                                                                                                                                                       |
| 5                                                                                                                                                  | 5x                                                                                                                                                       |
| 6                                                                                                                                                  | 6x                                                                                                                                                       |
| 7                                                                                                                                                  | 7x                                                                                                                                                       |
| 8                                                                                                                                                  | 8x                                                                                                                                                       |

**Helper Math & Cooldowns:**
* **Work Rate Conversion:** A Work Rate of `1x` translates to **1 hour** of upgrade time skipped. For example, a Level 8 Apprentice (`8x`) instantly reduces an active upgrade timer by 8 hours. The app must subtract this exact duration from the calculated `Time Remaining`.
* **24-Hour Cooldown:** Both helpers have a strict 24-hour cooldown after use. But the app is not going to be tracking this cooldown.

The following are the levels and Work Rate of the Lab Assistant

| Level  <br>![Level](https://static.wikia.nocookie.net/clashofclans/images/3/3d/Level.png/revision/latest/scale-to-width-down/17?cb=20130717031826) | Work Rate  <br>![Repair](https://static.wikia.nocookie.net/clashofclans/images/e/e2/Repair.png/revision/latest/scale-to-width-down/20?cb=20210529045411) |
| -------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1                                                                                                                                                  | 1x                                                                                                                                                       |
| 2                                                                                                                                                  | 2x                                                                                                                                                       |
| 3                                                                                                                                                  | 3x                                                                                                                                                       |
| 4                                                                                                                                                  | 4x                                                                                                                                                       |
| 5                                                                                                                                                  | 5x                                                                                                                                                       |
| 6                                                                                                                                                  | 6x                                                                                                                                                       |
| 7                                                                                                                                                  | 7x                                                                                                                                                       |
| 8                                                                                                                                                  | 8x                                                                                                                                                       |
| 9                                                                                                                                                  | 9x                                                                                                                                                       |
| 10                                                                                                                                                 | 10x                                                                                                                                                      |
| 11                                                                                                                                                 | 11x                                                                                                                                                      |
| 12                                                                                                                                                 | 12x                                                                                                                                                      |

## Goblin Builder & Researcher

Sometimes for a month at a time clash of clans will have an event "Work for Hire" in which they have the goblin builder & goblin researcher available for the players to use. He is a temporary builder that can work on building upgrades in exchange for gems, in essence he is essentially a temporary +1 builder. This is also the case with the Goblin Researcher who is available at the same time as the Goblin Builder. The Goblin Researcher is in essence a +1 slot for lab upgrades, that is to say when normally a player can only do one lab upgrade at a time when the goblin researcher is available, they (the players) are able to start a second upgrade in exchange for gems. The cost of using the Goblin Workers is 40 Gems per day, or 1 Gem every 36 minutes, with fractions rounded down. The minimum cost (regardless of build/upgrade time) is 10 Gems, which is achieved by starting any work taking 6 hours or less.

**Goblin Event Management & Gem Calculator:**
* **Event Toggle:** Since this event is temporary, the main app will feature an "Event Settings" toggle. When enabled, the UI and Widgets will automatically expand to show `?/7` Builders and `?/2` Lab Upgrades. When the event ends, the user can toggle it off to hide the extra slots.
* **Gem Cost Preview:** This app or any widget will not show any preview of the gem cost of using the Goblin Builder or Goblin Researcher. 

## Widget Ideas

1. 2x3 or 2x4 widget that shows icons that tell if you have ?/6 or ?/7 (when the goblin builder is available) builders working, then ?/1 lab upgrade going or ?/2 (when the goblin researcher is available), and then ?/1 pet upgrade going. These would be side-by-side in one row. This is a minimal app that is only showing availability next to their respective icons of Builders, Laboratory, and Pet House. Then on the second row there will be the Builder Base availability (Master Builder 1/2, Star Lab 0/1). These sit side-by-side in one row. So ultimately the top row is for Home Village and the bottom row is for Builder Base. The icons would be centered in the rows meaning the two rows would have an offset of half a cell from each other.  

| <Home_Builder_Icon> 3/6 | <Research_Icon> 1/1 | <Pets_Icon> 1/1 |
| <BuilderBase_Icon> 1/2 | <StarLab_Research_Icon 1/1> |


2. A 4x4 or 5x4 widget that shows the availability and also the progress of the ongoing upgrades. It would show for example

The Table below is an example of the parts that would be in the builders column. Then of course there would be a similar columns for the Lab Upgrades and then also the Pet House. These columns would sit side-by-side in one row.

| Builder | Working on   | Time Remaining | Time of completion |
| 1.      | Archer Tower | Time Remaining in days, hours, minutes, seconds and also a progress bar underneath that grows to 100% as the upgrade nears its completion | The system time by which the upgrade would be complete. This would be calculated based on the time remaining being added to system time. To show the final time by which you can expect the upgrade to finish. This should be in the 12 hour clock with either am or pm |

The Idea for the larger widget is also the same as that of the Idea #1. There are essentially 3 columns (for the Builders, Lab Upgrades, and Pet House). The difference is that while the Idea #1 widget only shows availability. This widget will show much more detail.

## Alarms & Notifications

Instead of standard push notifications, the app will set an actual Android Alarm for exactly **1 minute before** an upgrade finishes. 
* **Custom Sound:** The alarm will play the official Clash of Clans "upgrade finished" sound effect to distinguish it from regular system alarms.

## UI Constraints
* **Portrait Mode Only:** The main app and all widgets will strictly enforce a portrait viewing experience. Landscape mode is not supported, ensuring a clean and consistent layout tailored for standard mobile holding positions.

## Technical Constraints
* **Language:** The app will be developed entirely in **Java**, not Kotlin. Because of this, modern Kotlin-only frameworks like Jetpack Compose and Jetpack Glance cannot be used. The app will rely on standard XML layouts for the main app and RemoteViews for the widgets.
