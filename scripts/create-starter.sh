#!/usr/bin/env bash
# create-starter.sh <N> — build the starter-<N> handout branch and push it to origin.
#
# Rule (progressive handout):
#   starter-<N> = [JEP demo showcase] + [step N README = the exercise/task]
#                 + [FULL solution of step N-1 = the code base the participant starts from]
#                 + [SVG viewer resource, needed when the base serves it]
#   It does NOT contain step N's solution code — the participant implements step N.
#
# Usage: scripts/create-starter.sh <N> [--push]    where N in 3..17; --push also pushes to origin
set -euo pipefail

N="${1:?usage: create-starter.sh <N> [--push]}"
PUSH=0
if [ "${2:-}" = "--push" ]; then PUSH=1; fi
if ! [[ "$N" =~ ^[0-9]+$ ]] || [ "$((10#$N))" -lt 3 ] || [ "$((10#$N))" -gt 17 ]; then
  echo "error: N must be an integer in 3..17"; exit 1
fi

N2=$(printf '%02d' "$((10#$N))")            # e.g. 04
NP1=$(printf '%02d' "$((10#$N - 1))")     # previous step (the base), e.g. 03

REPO="/Users/michal/workspace/java-workshops/jdk-features"
MAINP="src/main/java/dev/bottega/jdkfeatures/flightcontrol/step"
TESTP="src/test/java/dev/bottega/jdkfeatures/flightcontrol/step"
DIR="/tmp/starter-$N2"

cd "$REPO"
rm -rf "$DIR"
git worktree add --detach "$DIR" main >/dev/null 2>&1
cd "$DIR"
git checkout -q -B "starter-$N2" main

# 1) remove every step except step (N-1) [the base, kept fully] and step N [kept; reduced to README below]
for K in $(seq 1 17); do
  KK=$(printf '%02d' "$K")
  [ "$KK" = "$NP1" ] && continue
  [ "$KK" = "$N2" ] && continue
  git rm -rq --ignore-unmatch "$MAINP$KK" "$TESTP$KK"
done

# 2) for step N keep ONLY the README (the task); drop its solution code + tests
git rm -rq --ignore-unmatch "$MAINP$N2/domain" "$MAINP$N2/server" "$MAINP$N2/client" "$TESTP$N2"

# 3) drop demo server + the two plan docs (not part of the handout); keep demo JEP packages + viewer
git rm -rq --ignore-unmatch \
  "src/main/java/dev/bottega/jdkfeatures/flightcontrol/demo" \
  "src/test/java/dev/bottega/jdkfeatures/flightcontrol/demo" \
  "flight-control-exercise-plan.md" \
  "flight-control-exercise-jep-plan.md"

# 4) sanity: the branch must compile (the base is proven in main, this guards against bad removal)
./gradlew compileJava compileTestJava >/dev/null 2>&1

git add -A
git commit -q -m "starter-$N2: demos + step$NP1 full solution (base) + step$N2 README (task)"

if [ "$PUSH" = "1" ]; then
  git push --force origin "starter-$N2" >/dev/null 2>&1
  msg="starter-$N2 created + pushed to origin"
else
  msg="starter-$N2 created locally (use: scripts/create-starter.sh $N --push to push)"
fi

cd "$REPO"
git worktree remove "$DIR" --force 2>/dev/null || true
git worktree prune

echo "$msg"
