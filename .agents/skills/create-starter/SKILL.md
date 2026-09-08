---
name: create-starter
description: Build and push the progressive "starter-<N>" handout branch of the Flight Control exercise in the jdk-features repo, following the rule starter-N = demo showcase + step N README (task) + FULL solution of step N-1 (code base).
---

# create-starter

Creates `starter-<N>` in `/Users/michal/workspace/java-workshops/jdk-features` locally (and, with `--push`, pushes it to `origin`).

## Rule

`starter-<N>` = **JEP demo showcase** + **`step<N>` README** (the new exercise/task) + **FULL solution of `step<N-1>`** (the code base the participant starts from).

- It does NOT contain `step<N>`'s solution code — the participant implements step N.
- The base (`step<N-1>`) is kept fully (domain + server + tests). All other steps (`01..N-2`, and the demos-native extras) and the demo server / plan docs are removed. The SVG viewer resource is kept (the base server serves it).
- `N` must be in `3..17` (for `N=2` the "base" `step01` has no code).

## Usage

```bash
cd /Users/michal/workspace/java-workshops/jdk-features
scripts/create-starter.sh <N>            # create the branch locally only
scripts/create-starter.sh <N> --push     # also push it to origin
```

The script:
1. Creates a temporary worktree from `main` (HEAD).
2. Branches `starter-<N>`; removes every step except `step<N-1>` (base) and `step<N>` (reduced to its README).
3. Removes the demo server + `flight-control-exercise-*.md` plans (keeps the JEP demo packages + viewer resource).
4. Compile-checks (`compileJava compileTestJava`), commits, and **force-pushes** `starter-<N>` to `origin`.
5. Cleans up the worktree.

## Rebuild the whole handout set (local)

```bash
for n in $(seq 3 17); do scripts/create-starter.sh "$n"; done
```
