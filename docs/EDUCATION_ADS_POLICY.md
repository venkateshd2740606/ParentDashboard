# Education Apps — Google Play Families Ad Policy

Standard for all kids and education apps in this portfolio (including ParentDashboard).

Based on [Google Play Families Policy](https://support.google.com/googleplay/android-developer/answer/9893335) and Designed for Families ad requirements.

## Allowed (implemented)

| Ad type | Placement | Notes |
|---------|-----------|-------|
| **Banner** | Home screen only | Navigation/menu — not during lessons |
| **Interstitial** | After lesson/level complete | Between sessions only; every 3 completions by default |
| **Consent (UMP)** | Before personalized ads | Required for EEA/UK |
| **Ads toggle** | Settings | Parents can disable ads |

## Not allowed (removed from education apps)

| Ad type | Reason |
|---------|--------|
| Banner on lesson/game screen | Active learning content |
| App open ads | Disruptive on launch/return; poor fit for kids |
| Rewarded / rewarded interstitial | Hint refill during lessons = ads during active learning |
| “Watch ad to continue” patterns | Deceptive placement |

## AdMob setup (when you add real IDs)

Only configure these unit types per education app:

- `ADMOB_BANNER_ID` — home screen
- `ADMOB_INTERSTITIAL_ID` — after lesson complete

Do **not** create app-open, rewarded, or rewarded-interstitial units for education apps.

## Applying to new education apps

After scaffolding:

```powershell
.\tools\apply_education_ads_policy.ps1 -AppName KidsABC -PackageSuffix kidsabc
```

Education phase scaffold scripts call this automatically.

## Puzzle games vs education

Puzzle/board games (TileMatch, Sudoku, etc.) keep the full ad stack (banner, interstitial, rewarded, app open). This policy applies **only** to education app directories listed in the root README.
