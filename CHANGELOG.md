# Changelog

This file carries **both histories**: the 白い熊 Handy RSS fork's releases below, newest first, and —
were upstream ever to add a root changelog — theirs underneath, untouched. Upstream Handy News Reader
keeps its own per-release notes in `fastlane/metadata/android/en-US/changelogs/`.

Each fork release names the upstream release it is built on. The build counter in a version
(`1.1.6+003`) is zero-padded so releases sort in build order.

---

## 白い熊 Handy RSS 1.1.35+001 — 2026-10-31

Built on upstream **`1.1.35`** (versionCode 372) — the untagged `upstream/master` tip `3611801b`
(2026-10-12). Upstream moved from `1.1.6` to `1.1.35` on master without tagging a release; per this
fork's rule of always following the latest untagged version, the base is adopted and the build counter
starts again at `+001`. All fork features carry over unchanged.

### New from upstream

- **One shared HTTP connection for all feeds** (new Advanced setting, on by default) — a bounded
  connection pool replaces a new client per request, which leaked sockets and could crash long refresh
  cycles on EMUI.
- **Refreshes no longer stall** — each feed's fetch is capped at 30 seconds instead of a whole refresh
  pass waiting silently on one stuck feed; fewer screen updates while many feeds refresh; the long-
  operation wakelock is renewed during long refreshes and a hit traffic limit is reported in a
  notification.
- **Steadier read state and unread counts** — a swipe to unread survives list refreshes, and the
  per-feed unread counts are recounted from the database without racing an in-flight swipe.
- **Article view** — images load properly when an article opens; an open article reloads when its
  content changed underneath it; the first article opened is now marked read.
- **Draw around the camera cutout** (new Advanced setting).
- **Article-list search** moved into its own class, and the action bar turns a search colour while you
  search.
- **“Send report to developer”** in the ⋮ menu (emails upstream's developer a report file, including
  the feeds that failed to update); crash reports carry the stacktrace in the mail body.
- **Crash fixes** — 「Couldn't read row from CursorWindow」 during auto-refresh, the full-text download
  on a broken cursor, notifications on Android 8 (EMUI), a missing search box, and links no app can
  open; errors while downloading images no longer stop the download.
- The feed-filter edit dialog scrolls; the landscape control panel has a reload button; brightness
  gestures change brightness exponentially.

### Fork work carried across

- The Mafra paywall bypass (Googlebot user agent for lidovky.cz, idnes.cz, expres.cz and
  antiyoutuber.cz) now rides on upstream's shared HTTP connection.
- The black window and navigation-bar background sits beside upstream's cutout handling; crash
  messages keep the fork's styled toast.

---

## 白い熊 Handy RSS 1.1.6+014 — 2026-10-31

Built on upstream **`v1.1.6`** (versionCode 341) — same upstream base as `1.1.6+012`, so everything
here is fork work. (`1.1.6+013` was a test build of the grid dialog alone and is not released.)

### One grid-layout dialog, with a layout per fold state

The three grid icons in the article-list top bar — columns, title lines, image height — are now
**one grid icon** opening **one dialog with all three sliders**: articles per row (1–8), title lines
(1–12) and image height (40–400 dp). Every slider applies to the grid live while you drag it.

The layout is kept **separately for each fold state** of a foldable — **folded**, **semi-folded** and
**unfolded** — so each screen can have its own number of columns and card size. The dialog's title
names the state being edited (「Grid layout — semi-folded」). The state is read from the aspect ratio
of the physical panel (Huawei Mate XT: about 2.2 : 1 folded, 1.1 : 1 semi-folded, 1.4 : 1 unfolded),
which does not change with the orientation; an ordinary phone counts as folded, a tablet as unfolded.
Folding or unfolding the phone, or coming back to the list in a different state, switches the grid to
that state's layout.

A state never adjusted uses the previous single layout, so nothing changes until you tune it. The
three typed-number fields for these values in Settings → Article list are gone — the dialog is the one
place to set them. The title-font picker stays its own icon, one font for all states.

### Mark all as read always clears the list

With 「Show read articles」 off, the mark-all-read button (bottom right) sometimes left every card on
screen; only after going back did the drawer's unread count drop, and only reopening the group showed
it empty. The articles *were* marked read — but the app's content provider suppresses its change
notifications globally while any background job is running (the scheduled auto-refresh, the full-text
download, the silent mark-as-read on scroll), so a tap during one of them never told the list to
reload. The drawer count followed only because it uses a separate flag.

The mark — and its Undo — now notify the article list directly, so the list empties (or refills) at
once whatever is running in the background.

---

## 白い熊 Handy RSS 1.1.6+012 — 2026-10-17

Built on upstream **`v1.1.6`** (versionCode 341) — same upstream base as `1.1.6+011`, so everything
here is fork work.

### A horizontal swipe in the reading view changes the article at once

Swiping left or right on an article whose content was **wider than the screen** — a wide table, an
image or embed with a fixed width — first panned the article sideways, exposing up to about one and a
half screens of empty space on the right, and only then jumped to the next article.

The reading view is a `ViewPager` with one `WebView` per article, and the pager hands a horizontal
drag to any child that can still scroll horizontally before it pages itself. Our article `WebView`
(`WebViewExtended`) now reports that it **cannot scroll horizontally**, so the swipe goes straight to
the pager, and any horizontal offset the page picks up anyway (a fling, a layout change) is snapped
back to the left edge.

Trade-off: content wider than the screen can no longer be panned sideways; its overflow is clipped.

---

## 白い熊 Handy RSS 1.1.6+011 — 2026-10-03

Built on upstream **`v1.1.6`** (versionCode 341) — same upstream base as `1.1.6+010`, so everything
here is fork work.

### The grid's top row no longer slides off the screen on Back

`1.1.6+010` fixed the list jumping elsewhere on return, but left one case wrong. Scroll so that the
article you are about to read sits in the **top row** of the grid, open one that is **not the first
card of that row**, press Back — and that row came back lifted just above the top edge of the screen,
so you had to scroll down a little to see where you were.

The page was being put back **twice**:

- **The list widget already restores it.** Swapping in the shortened cursor fires the adapter's
  change observer, and `AbsListView` remembers its first position together with that row's pixel
  top. On the next layout, `handleDataChanged()` replaces the selection we had just asked for with
  that remembered position and offset. When the article you read sits after the first card of its
  row, the first card keeps its position — so this alone puts the page back exactly.
- **Then the fork applied the offset again.** `1.1.6+010` followed `setSelection()` with a posted
  `scrollListBy(-offset)`, assuming the row had been snapped flush to the top. It had not, so the
  page moved a second time by however much of the top row was hidden above the screen — usually
  most of a card, which is why the row ended up just out of sight.

Now the offset is a **correction, not a blind scroll**. Once the layout has run, the restore measures
where the anchor card actually is and scrolls only by the difference — nothing at all when the list
already got it right. If the anchor ended up off screen, it is selected and measured once more.

The scroll listener also records the **pixel top of every visible card**, not just the top row's. If
the article you read was the first card of its row, the next card takes its place and goes back to
that card's own old height instead of being pushed flush to the top of the screen.

---

## 白い熊 Handy RSS 1.1.6+010 — 2026-09-29

Built on upstream **`v1.1.6`** (versionCode 341) — same upstream base as `1.1.6+008`, so everything
here is fork work.

### The article list keeps its place when a read article drops out

Opening an article from the middle of the grid and pressing Back moved the page. With 「Show read
articles」 off the article leaves the cursor on the way back, and the list returned somewhere other
than where it had been — which makes it hard to find where you had got to.

The anchoring machinery was already in `EntriesListFragment`, and it had three faults:

- **The saved offset was never read.** `onScroll` records both the id of the row at the top of the
  screen and that row's pixel offset, but the restore called `setSelection()`, which snaps the row
  flush to the top of the viewport. With the grid's image-top cards that throws the page by most of
  a card height on every single return.
- **Reading the top row gave up entirely.** When the article you opened was the one at the top of
  the screen, it is gone from the shortened cursor, the id lookup returned `-1`, and the restore did
  nothing at all — leaving the list widget's own bookkeeping to put the page wherever it liked.
- **A stale anchor at the top of the list.** The save was guarded by `firstVisibleItem > 0`, so
  sitting at the very top left the anchor holding an id from earlier in the session, and returning
  from the first article restored to that old row.

Now the visible ids are recorded top row first, and the anchor is saved at position 0 as well. The
restore walks that row of ids until one survives the cursor change — the first is the old anchor and
carries its pixel offset, a fallback sat lower down the screen and goes flush to the top.
**The articles above the one that vanished do not move.** The ones below it close up by one card,
which is simply what removing an item from a grid means.

### Why this does not use `setSelectionFromTop`

Worth recording, because it is the obvious call and it is a trap here. `fragment_entry_list.xml`
declares a **`GridView`**, and row layout is that same `GridView` with `setNumColumns(1)` — so both
layout modes are a GridView, never a ListView. `GridView` does not override `setSelectionFromTop`,
so it falls through to `AbsListView`'s, which in touch mode records only `mResurrectToPosition` and
leaves `mSelectedPosition` at `INVALID_POSITION`. `GridView.layoutChildren` then hands that straight
to `fillSpecific()`:

- with 4 columns, `-1 - (-1 % 4)` resolves to row **0** — the list jumps to the top of the feed;
- with 1 column it resolves to **-1** — `getView(-1)` throws `IllegalStateException: couldn't move
  cursor to position -1`, and the app crashes on Back.

So the position is set with `setSelection()`, the primitive `GridView` actually implements, and the
pixel offset follows with `scrollListBy`, posted — `setSelection()` only *requests* the layout that
the offset depends on.

---

## 白い熊 Handy RSS 1.1.6+008 — 2026-09-11

Built on upstream **`v1.1.6`** (versionCode 341) — same upstream base as `1.1.6+007`, so everything
here is fork work.

A restore onto a new phone came up with feeds that refreshed but showed **no thumbnails**, and every
feed's own settings — 「Show full article」, 「Auto images load」 — sitting at their zero default.
This release fixes the two causes, both in the OPML export/import.

### Why a restored feed lost its settings — and its articles
- The backup ZIP holds two OPML documents: `feeds.opml` (Thunderbird-native — URL, title, group,
  nothing more) and `articles.backup` (the same feeds **with** their settings, filters and entries),
  written in that order. Import replays the archive in file order, so `feeds.opml` created every
  feed with every flag explicitly **off**, and `articles.backup` then found each feed already present
  and **skipped it wholesale** — settings, filters and entries alike. After `feeds.opml`, the whole
  `articles.backup` was a no-op. That is stock Handy News Reader's importer behaviour; the fork's
  two-document archive was what made it bite on every restore.
- A feed whose edit form was never saved keeps 「Auto images load」 as NULL, which every runtime read
  treats as **on** — but the backup wrote that NULL out as `false`.

### Export: the settings ride on both documents
- The per-feed settings (retrieve full text, show text in list, auto-refresh, image auto-load,
  options, fetch mode) are now written into **`feeds.opml` as well**, so even a Feeds-only restore
  carries them. Thunderbird ignores attributes it does not know; the file imports there exactly as
  before.
- A NULL 「Auto images load」 is written as `true` — the file now says what the app does.
- Last-update stamps and priority stay in `articles.backup` only.

### Import: merge into what is already there
- A feed that already exists is **merged, not skipped**: the settings the outline states are
  applied by update, and its filters and entries go in. Absent attributes leave the column alone,
  so an attribute-less OPML (Thunderbird's own export, an older `feeds.opml`) never zeroes a
  setting. Priority is deliberately left out of the merge — the provider's feed update reshuffles
  the whole list on it.
- **Filters** are deduplicated by text + scope, **entries** by link, **labels** by name, so a
  re-import never doubles anything.
- An entry the phone has already fetched keeps its content and gains the backup's **read / starred /
  scroll / zoom / saved full text** as a **union** — nothing read or starred on the phone since is
  undone by an older file.
- On a fresh insert an absent flag is left NULL — the app's own default, which for 「Auto images
  load」 means on — instead of being forced off.
- Label-to-article rows whose article never made it in are no longer inserted with an empty
  article reference.

### Restoring an existing backup
- A ZIP taken **before** this release restores every feed whose settings had been saved explicitly
  (their flags were written as `true`), plus the articles and filters that were being dropped; only
  feeds whose 「Auto images load」 had never been saved still need that box ticked by hand. A fresh
  export from `1.1.6+008` carries everything.

---

## 白い熊 Handy RSS 1.1.6+007 — 2026-09-05

Built on upstream **`v1.1.6`** (versionCode 341) — same upstream base as `1.1.6+006`, so everything
here is fork work.

A small, sharp follow-up to the `1.1.6+006` automation release.

### A failed backup row now offers the repair — but only when the repair works
- When a backup is triggered while this app is cold, Android can refuse the foreground-service start
  outright. That failure was already caught and reported rather than crashing; what is new is that
  it now reports **which kind** of refusal it was, using a key the backup app matches to put a
  「電池最適化を除外」 button directly on the failed row — the same treatment a missing All-Files-Access
  grant already gets.
- **The key is emitted only when the button can actually fix the fault**: the refusal must be the
  platform's foreground-start refusal *and* this app must not already hold the battery-optimisation
  exemption. If the exemption is already granted and the start was still refused, the cause is
  something the button cannot touch — on this phone most likely アプリ起動管理 set to 自動管理, which
  no app can change for itself. Every other start failure keeps a descriptive line and earns no
  button.
- The reasoning is worth stating because the tempting implementation is the wrong one: a blanket
  key on any start failure produces a button that appears often and repairs nothing, pressed by the
  one person who cannot route around the fault.
- The battery-optimisation notification this app already raised is unchanged and still shown — it is
  the part that actually gets the exemption granted.

---

## 白い熊 Handy RSS 1.1.6+006 — 2026-09-04

Built on upstream **`v1.1.6`** (versionCode 341) — the same upstream base as `1.1.6+003`, so
everything here is fork work.

The 保存復元 backup contract moved to **v2**. The case it now has to serve is 応用管理 restoring apps
**and their data** onto a wiped phone, where nothing has been configured and nobody has pasted
anything — so the gate that used to depend on a pasted secret had to stop depending on one, and the
app gained a second door that hands its whole archive to a caller the system can identify.

### The gate opens by default
- **Automation export is now ON out of the box**, and the authorization token became an **opt-in**
  under a new 「Use authorization token?」 switch. There is nothing to turn on and nothing to paste
  for this app to answer the backup batch. The master switch stays, because it is the only way to
  close this app off — a feature that can be turned on but never off is one you cannot retreat from.
- **A token sent to an app that is not asking for one is ignored, never refused.** Tokens outlive
  the settings they were pasted for, and refusing them would turn "the switch was turned off" into
  "half the batch mysteriously fails".
- **The token row is hidden unless the token is actually being asked for** — a 48-character secret
  sitting under an off switch only invites pasting it somewhere it will do nothing.
- Both checks now live in **one function** rather than being written out at each entry point, so
  "automation disabled" and "bad token" cannot drift apart — they are different faults with
  different fixes and are reported separately.
- The new preference joins the other two in the export-exclusion list, so it can never ride inside
  a backup.

### Back up the app itself, data and all
- **A new data door** lets a companion app ask for the complete archive and hand it back on a wiped
  phone, so the app can be restored with its feeds, articles, downloaded text, images and settings
  rather than reinstalled empty.
- **The caller is identified by the system and checked three ways** — exact package name, a uid
  cross-check the kernel answers, and a pinned signing certificate. Not a name prefix: package names
  are not a namespace anyone owns, and since the caller supplies the destination, a prefix check
  would have been weaker than the token it replaces.
- **The archive travels down a file descriptor the caller opens** — not a path and not a URI. The
  caller stages its backup in a temporary directory it renames on commit, and encrypts and checksums
  each file it knows about, so a file this app dropped in itself would end up in plaintext and
  unverified inside an otherwise protected backup. A descriptor is also a capability that expires
  when it is closed.
- **Restoring exists only on that identified channel** and deliberately has no broadcast form: an
  import overwrites the app's data, and the broadcast half of the contract is open by design.
- Both directions run in a foreground service under a wakelock, report the same real-count progress
  as the existing export, and can be cancelled.

### Fixes found while building it
- **Replies to the app that actually drives the backup batch were being discarded.** The manifest's
  package-visibility list named only the restore app, so `setPackage()` on the reply silently failed
  on Android 11+ — the export ran, wrote correctly, and was never heard of. Both callers are now
  named, which also matters for the identity check: the calls that read a caller's certificate are
  visibility-filtered too, so an invisible caller failed as "signature unreadable" rather than
  merely losing its reply.
- **A restore could report success over settings that were never written to disk.** The restoring
  app force-stops this one the instant it reports success — correctly, since a live process would
  write its cached preferences back out and undo the import — but that stop is a kill, and the
  preference write was asynchronous. The import now flushes synchronously before answering. This was
  invisible in testing, because a hand-run import is followed by a normal shutdown that flushes it.
- **A refused service start no longer leaks the caller's open file.** Several things can fail in the
  window between accepting a job and the worker thread owning the descriptor — a background
  foreground-service start can be refused outright on Android 12+, the wakelock can throw, the
  thread can fail to start — and any of them would have held the caller's file open and left it
  waiting. One guard now covers the whole window and answers with a real reason.
- **A refused start also raises the one repair the user can actually perform**, as a notification
  offering the battery-optimisation exemption, without blocking the reply.
- **Progress and replies from the data door carry the correlation id under both names** the family's
  callers read, so neither side needs a special case.

### Notes for other forks on this contract
- **A large import spools to disk rather than into a byte array.** Reading the whole archive before
  touching anything is the right rule — a partial read would otherwise import half an archive — but
  this app's backup carries its article corpus and roughly 3,700 images, so the bound has to be disk
  rather than memory.
- **The data service declares `dataSync`, not `specialUse`.** The latter is an API 34 value that the
  resource compiler rejects outright below that, which is a build failure rather than a preference.

---

## 白い熊 Handy RSS 1.1.6+003 — 2026-08-17

Built on upstream **`v1.1.6`** (versionCode 341). The first release in which the fork sits on a real
upstream tag again since the master-tip refreshes — `v1.1.6` is also upstream's `master` tip.

### Pulled in from upstream v1.1.6
- **Screen keep-on duration** — a new setting that holds the display awake for a chosen interval while reading.
- **Full-width images now only in books** (FB2/EPUB) rather than in every article.
- **A wakelock over long operations** — refresh, import, delete-old and full-text loads no longer stall when the CPU dozes.
- **FB2 footnote extraction fixed.**
- 45 translation updates across ~20 locales.

### Fork changes in this release
- **Build numbers are zero-padded to three digits** (`1.1.6+003`), so APKs and release tags sort in
  build order instead of `+10` landing before `+3`. Previously published unpadded tags are left as they are.
- **`gradlew` executable bit restored** — upstream shipped it non-executable as fallout of a repo-wide
  line-ending round-trip, which broke the build outright.
- **Duplicate `WAKE_LOCK` permission removed** — upstream's new declaration for its wakelock landed
  beside the fork's existing one for the export service; one declaration now serves both.
- **Ghost-sweep call site retargeted** to upstream's extracted `LongOper` class, which moved the
  refresh-cancel API off `FetcherService`.
- **The Chinese app label stayed rebranded** — upstream added a localized `app_name` of "Handy News
  Reader", which would have relabelled the app on a Chinese-locale device; it now reads 白い熊 Handy RSS
  like every other locale.
- **Reading-view fonts survived upstream's CSS refactor** — the configurable body and heading
  family/size/weight rules were carried onto upstream's reworked stylesheet builder.

---

## Everything this fork adds on top of stock Handy News Reader

The full standing feature set, carried by every release above.

### Identity & packaging
- Installs **side-by-side** with the official app: own `applicationId` (`shiroikuma.handyrss`), own
  content-provider authorities and custom permission, own launcher icon and app label (白い熊 Handy RSS).
- Black/yellow line-art launcher icon at all six densities.
- Self-signed with a stable key, so builds update cleanly over one another.
- `fdroid` flavor (no GMS), correct for a microG device.
- De-branded About screen and issue form; product name purged across all locales.

### Article list & grid
- **Grid article layout** — image-top cards as an alternative to classic list rows.
- **Configurable list layout** — row/grid toggle, columns per row, title line count, and grid image height.
- **Uniform card height** in grid mode, so a taller card is never painted over by the next row.
- **Quick-control top bar** — live sliders for columns, title lines and image height, plus an
  article-title font picker, each row previewing its own typeface.

### Fonts
- **Per-surface fonts** — independent family, weight and size for the article title, the in-list
  article text, and the feed/category drawer.
- **Reading-view body font** — family (via CSS `@font-face`), size and weight, resolved independently
  of the app-wide font.
- **Reading-view heading font** — its own family/weight/size, with h2 tracking 2 pt below h1 to keep
  the hierarchy; falls back to inheriting the body font when unset.
- Fonts may come from the bundled assets or from your own font files on storage.
- Live size-slider previews and a named weight dropdown instead of cryptic numeric fields.

### Theme & chrome
- **Black/yellow chrome across every surface** — window, toolbar, drawer, overflow and popup menus,
  settings screens, dialogs, action icons, FAB, feed-list rows and the label list.
- **Customizable chrome colors** — foreground and background pickers driving toolbars, icons, drawer,
  FAB and dialog text.
- **Content-color pickers** — article background, quote background and rule, subtitle and its border.
- Every toast styled to match the theme.
- All 52 hard-tinted action-icon vectors retinted.

### The 白い熊 Handy RSS UI screen
- One consolidated screen for all fonts and colors, styled with text-wide underlined headings, thin
  section hairlines and a strict indentation ladder.
- **Three entry points** — long-press the drawer settings icon, the top-left home icon, or the article
  list's top-right ⋮.

### Backup, export & import
- **One ZIP holding nine categories** — feeds, articles with their read/starred/scroll state, labels
  and filters, downloaded article text, downloaded images, installed font files, and the settings groups.
  Stock's own backup carries none of the last three, and restores font settings pointing at fonts it
  never saved.
- **Export/Import by category** — a shared export folder with a live "last export" line, and category
  checkboxes behind round pill buttons.
- **Crash-safe writes** — every export goes to a `.part` file and is renamed only once whole, so an
  interrupted backup never leaves a half-written archive that looks real.
- **Thunderbird-native feed OPML** — categories and feeds import into Mozilla Thunderbird as folders
  with individually readable feeds inside.
- **Scheduled auto-backup** to a folder of your choice, on a configurable interval.
- Import still reads every older plain-OPML backup, and can no longer be used to plant a foreign
  automation token or export directory.

### Headless automation
- A **token-gated intent** lets a companion automation app trigger the same export with no UI, choose
  categories and destination, and read back the written path and size.
- **Live progress in real counts** — names the category in progress, files and articles done, and bytes
  written, with a heartbeat so a long silent phase never looks dead.
- **Cancellable from outside**, mid-export, cleanly.
- Off by default, behind a 24-byte random token that never travels inside a backup.
- The export runs in a **foreground service holding a wakelock**, so it survives the screen going off
  instead of being killed part-way with nothing written and nobody told.

### Storage hygiene
- Deleting a feed or a group **reaps the downloaded images and article files** that went with it —
  upstream leaves them behind indefinitely.
- The periodic cleanup **no longer requires the charger**, matching what its own settings checkbox showed.

### Reading
- **Reading-view toolbar** — text size, title size and body font adjustable in place.
- **★ Star always visible** in the reading toolbar rather than buried in the overflow.
- **Articles are marked read on leaving the reading view even when starred.**
- **Pay-or-consent wall bypass** for Mafra sites (lidovky.cz, idnes.cz, expres.cz, antiyoutuber.cz),
  fetched with a search-crawler user agent so the full text loads.

### Fixes
- **Co-install crash fixed** — settings entries carried an explicit intent still targeting the official
  package, which crashed with a permission denial whenever both apps were installed.
- Toolbar/menu icons, compound-button labels and outlier screens recolored where the theme could not reach them.

---

## Earlier fork releases

- **1.1.5+9** (2026-07-28) — export progress that names the running category, keeps a heartbeat through
  the long articles write and reports bytes; the export moved into a foreground service with a wakelock.
- **1.1.5+8** (2026-07-28) — orphaned images and article files reaped on feed and group deletion; the
  delete-old job no longer requires the charger.
- **1.1.5+5** (2026-07-25) — the 保存復元 automation contract: one-ZIP export and a token-gated receiver.
  Base moved to upstream's untagged `1.1.5`.
- **1.1.4+46** (2026-07-24) — the UI screen restyled, and the Export/Import panel with category selection.
- **1.1.4+41** (2026-07-16) — first master-tip rebase; upstream's feed-editor split reconciled with the
  fork's dialog theming.
- **1.1.4+38** (2026-06-27) — first published release.
