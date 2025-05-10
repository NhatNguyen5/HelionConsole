# The Helion Console TODO List

## Phase 1: Core Boot System
- [x] Load and parse boot `.txt` file
- [x] Support `[DELAY]`, `[TAP_TO_CONTINUE]`, `/(`/`)` block groups
- [x] Animate single and grouped lines (parallel)
- [x] Prevent jitter/stretch on line display
- [x] Modularize `runBootSequence`, `parseScreenFile`, and rendering

## Phase 2: Display Engine
- [x] Create `TerminalLine` composable with locked height
- [ ] Create `TerminalBlock` for grouped sections (title, box art, etc)
- [ ] Add global style constants for fonts/colors/sizing
- [ ] Support newline-aware ASCII formatting (aligned blocks)

## Phase 3: UI Infrastructure
- [ ] Add screen manager system (load screen based on `state`)
- [ ] Wire `[JUMP:screen_id]` or `[JUMP:filename.txt]` support
- [ ] Add `[CLEAR]` command (wipe visibleLines)
- [ ] Route to 7 interface modules from main menu (based on number input)

## Phase 4: Input + Interaction
- [ ] Add soft keyboard or tap listener for terminal input
- [ ] Implement command parser: detect strings like `help`, `menu`, `bridge`
- [ ] Allow "tap to continue" to resume sequence
- [ ] Add blinking `>_` cursor when waiting

## Phase 5: Data & External Files
- [ ] Load inventory/status from JSON
- [ ] Use `.json` files for menu construction
- [ ] Optionally allow user file override for boot sequence

## Phase 6: QoL + Style
- [ ] Add `[SPEED:x]` to override typewriter delay
- [ ] Add `[BEEP]` and `[GLITCH]` effects
- [ ] Add `[ECHO:event]` for Ava’s personality moments
- [ ] Apply fade-in/out animation to finished lines
