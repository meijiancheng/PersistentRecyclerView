# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew clean                  # Clean build outputs

./gradlew test                   # Run all unit tests
./gradlew testDebugUnitTest      # Run debug unit tests only
./gradlew connectedAndroidTest   # Run instrumentation tests (requires connected device/emulator)

./gradlew lint                   # Run lint checks
./gradlew lintFix                # Auto-fix safe lint issues
```

## Architecture

This is a two-module Android library that implements seamless nested scroll momentum transfer — mimicking JD.com's home page UX where fling velocity flows from an inner `RecyclerView` up through an `AppBarLayout` collapse and back down, with no perceptible break.

### Library module (`library/`)

Three classes, all in `com.stone.persistent.library`:

**`PersistentCoordinatorLayout`** — The outer container (extends `CoordinatorLayout`). Expects an `AppBarLayout` as first child and a `ViewPager`/`ViewPager2` as second child. On layout, it injects a `HookedScroller` into `AppBarLayout.Behavior` via reflection to intercept fling state. When the `AppBarLayout` fully collapses and a fling is still active, it retrieves the remaining velocity from `HookedScroller` and forwards it to the active `PersistentRecyclerView` via `.fling()`.

**`PersistentRecyclerView`** — The inner scrollable list (extends `RecyclerView`). On `onAttachedToWindow`, it walks up the view hierarchy to find a `PersistentCoordinatorLayout` and registers itself in parent view tags (`R.id.tag_saved_child_recycler_view`) for later retrieval.

**`HookedScroller`** — Extends `OverScroller`. Injected into `AppBarLayout.Behavior`'s private `mScroller` field. Overrides `fling()` to detect upward flings and track velocity. `consumeFlingVelocity()` returns remaining velocity for transfer; `clearPendingFling()` cancels on touch.

### Velocity transfer flow

```
User flings up on PersistentRecyclerView
  → CoordinatorLayout routes scroll to AppBarLayout.Behavior
  → HookedScroller.fling() captures velocityY
  → AppBar collapses; onOffsetChanged fires when fully collapsed
  → PersistentCoordinatorLayout.consumeFlingVelocity() extracts remaining velocity
  → Active PersistentRecyclerView.fling(velocity) continues seamlessly
```

### Demo app (`app/`)

`MainActivity` builds a home-page-style layout: `AppBarLayout` contains a carousel + search bar + tab strip; `ViewPager2` pages contain `FeedsListFragment` (with `PersistentRecyclerView`) and `MenuGridFragment`. The app exists only to demonstrate the library.

## SDK / Toolchain

- AGP 8.7.3, Kotlin 1.9.22, Java 17
- `minSdk` 21, `targetSdk` 34
- Reflection is used to inject `HookedScroller` into `AppBarLayout.Behavior`'s private `mScroller` field — changes to the Material library's internal implementation can break this.

## test
Before completing any task, describe how you will verify this work