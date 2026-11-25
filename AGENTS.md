# Agent instructions

Standing rules for anyone, human or agent, working in this repo. They change rarely.
Task state, findings and per-app detail live elsewhere (see "Where things are").

If `AGENTS.local.md` exists in the repo root, read it too. It is untracked and holds the
setup of the machine you are on: tool paths, the exact build command, device and network
notes.

## What this repo is

A Morphe patch bundle for Brazilian Android and Android TV apps, built from the Morphe
patches template. Globoplay (Android TV) is the first app; more will follow.

- Repo-level names and docs (Gradle group, bundle name and description, README) describe
  "patches for Brazilian apps", never one app alone.
- The bundle is named "Patches for Brazilian apps". "Morphe" may appear only in
  descriptive text, never in the project or bundle name (see `NOTICE`).

## Scope limits (every app)

Patches remove telemetry, ads and features unrelated to what the app is for. They do not
grant access the user's account does not have. Do not implement, and stop and report if a
task appears to need:

- unlocking paid content, quality tiers or subscriptions;
- bypassing DRM or its licence handling;
- defeating account-sharing, household or integrity checks (Play Integrity and similar);
- spoofing the original package or signature to sign-in, billing or integrity services.

If a server refuses something, that is a finding to record, not an obstacle to work
around.

## Where things are

| Path | What |
|---|---|
| `PLAN.md` | Current tasks, their status, findings log, open questions. Update it as you work |
| `docs/<app>/` | Per-app notes: `hook-map.md` (patch sites and evidence), `patch-status.md` (what is proven), `device-test.md`, `wishlist.md` (ideas, not tasks) |
| `patches/src/main/kotlin/brasil/morphe/patches/<app>/<area>/` | Patches and fingerprints for one app |
| `patches/src/main/kotlin/brasil/morphe/patches/<app>/shared/` | That app's compatibility constants and shared fingerprints |
| `patches/src/main/kotlin/brasil/morphe/patches/shared/` | Code shared between apps |
| `extensions/extension/src/main/java/brasil/morphe/extension/<app>/<area>/` | Extension code, referenced from smali as `Lbrasil/morphe/extension/<app>/...;` |

## Build

Requires JDK 21 (the version CI uses; newer JDKs fail the Android `jlink` step), the
Android SDK via `ANDROID_HOME`, and a GitHub token with `read:packages` supplied as the
`gpr.user` / `gpr.key` Gradle properties or the `GITHUB_ACTOR` / `GITHUB_TOKEN`
environment variables. Then:

```
./gradlew buildAndroid        # output: patches/build/libs/patches-*.mpp
```

Never print a token or write one to a file; pass it through the environment.

## Writing patches

- Fingerprint on unobfuscated class names, intact method names and stable strings. Never
  on short obfuscated names (`a`, `fb3`, `cq`); they change every release.
- A stub must complete the flow the caller is waiting on: take the completion, error or
  empty-result path rather than returning silently.
- Empty the data, not the control flow the caller waits on (an empty list, not null,
  unless the decompiled caller shows null is the "nothing" case).
- Use `.method` for the primary hook of each patch so a broken fingerprint fails loudly;
  use `methodOrNull` only for secondary hooks, and log which ones were skipped.
- Extension code must never throw into app code: wrap in try/catch, fall back to the
  original behaviour, log under one tag per patch.
- Blocked network requests should fail like an offline network (an `IOException`), not
  return fake success bodies.
- Prefer plain smali overrides; add extension code only where logic is needed.
- Debug or testing patches are off by default and say so in their name or description.
- Use `../morphe-patches` (upstream) for idioms where it is checked out. Other people's
  patch repos are for technique only: reimplement, do not copy.

## Evidence standard

A patch site is "confirmed" only when the method body and its callers have been read in
the decompiled app. A patch is "proven to apply" only when:

1. its fingerprints resolve when the built bundle is applied to the real app file with
   the Morphe CLI, and
2. the patched method bodies have been checked in the decoded output.

Record this per site in `docs/<app>/patch-status.md`. "Applies" is not "works": nothing
counts as working until it has run on a device, and docs must say which of the two is
meant. An entry marked "not found" or "unsure" is useful; a guessed one is harmful.

## Repo rules

- `dev` is the integration branch; `main` receives merges from it only (no squash).
- If you were given your own worktree and branch, commit only on that branch and never
  merge, rebase or push it yourself: your work reaches `dev` after review. Start each
  task with `git merge dev`, and stop and report if that conflicts. If you were not given
  a branch, work on `dev` directly.
- Semantic commit messages: `feat:`, `fix:`, `chore:`. One `feat:` or `fix:` commit per
  patch change; `chore:` for docs and housekeeping.
- Do not hand-edit generated files: `patches-list.json`, `patches-bundle.json`,
  `PATCHES.md`, `CHANGELOG.md`. Do not write release scripts or change workflows;
  `release.yml` handles releases.
- Never commit APK files, decompiled app code, patcher outputs or captures. Keep them
  outside the repo.
- Do not push or open pull requests unless the user asks.
- Do not use adb, a test device or network equipment unless the task says so; device
  testing may be running in another session.

## Reporting

Lead with what is proven, what could not be established, and what was skipped and why.
State plainly what you did not check.
