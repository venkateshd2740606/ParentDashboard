# ParentDashboard — Google Play Data Safety Form Reference

Use this document when completing the Play Console **Data safety** section.

## Data Collection Summary

| Data type | Collected | Shared | Purpose | Required / Optional |
|-----------|-----------|--------|---------|---------------------|
| App activity (gameplay events) | Yes | No | Analytics, app functionality | Optional (consent) |
| App info and performance (crashes) | Yes | No | Crash reporting | Optional (consent) |
| Device or other IDs (advertising ID) | Yes | Yes (AdMob) | Advertising | Optional (consent) |
| User-generated content (local saves) | Yes | No | App functionality | Required for progress |

## Data Handling

- **Encrypted in transit:** Yes (HTTPS for Firebase/AdMob)
- **Encrypted at rest:** Sensitive prefs via EncryptedSharedPreferences; Room DB on device
- **Users can request deletion:** Contact privacy@parentdashboard.game; local data cleared via app settings or uninstall
- **Committed to Play Families Policy:** No (not a Designed for Families app)

## Security Practices

- Data encrypted in transit
- Users can request data deletion
- Independent security review planned before major releases

## Third-Party SDKs

| SDK | Data types | Link |
|-----|------------|------|
| Firebase Analytics | App activity, device info | https://firebase.google.com/support/privacy |
| Firebase Crashlytics | Crash logs, device info | https://firebase.google.com/support/privacy |
| Google AdMob | Advertising ID, ad interactions | https://policies.google.com/privacy |

## Consent Flow

- GDPR/CCPA consent screen on first launch (`ConsentScreen`)
- Analytics and ads disabled until user accepts applicable consents
- Consent state persisted in DataStore

## COPPA Considerations

- App rated Everyone; not targeted at children under 13
- No knowingly collected personal information from children
- Age-appropriate ads when ads are enabled
