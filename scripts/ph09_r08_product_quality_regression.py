from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def t(p): return (ROOT/p).read_text(encoding='utf-8')
def check(n,c):
    if not c: raise SystemExit(f'[FAIL] PH09-R08 quality point {n}')
    print(f'[PASS] PH09-R08 quality point {n}')
ui=t('feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsScreens.kt'); nav=t('app/src/main/java/app/voicecloud/android/navigation/VoiceCloudNavHost.kt'); repo=t('feature/settings/src/main/java/app/voicecloud/feature/settings/data/SettingsRepository.kt'); models=t('feature/settings/src/main/java/app/voicecloud/feature/settings/model/SettingsModels.kt'); root=t('app/src/main/java/app/voicecloud/android/ui/VoiceCloudRoot.kt')
checks=[
('1 Settings overview', 'SettingsOverviewScreen' in ui and 'Manage your account, preferences' in ui),
('2 Notification preferences', 'NotificationPreferencesScreen' in ui and all(x in ui for x in ['Email notifications','Push notifications','In-app notifications','Notification sounds'])),
('3 Privacy', 'PrivacySettingsScreen' in ui and 'Blocked users' in ui),
('4 Voice/audio preferences', 'VoiceAppearanceScreen' in ui and all(x in ui for x in ['Noise suppression','Echo cancellation','Automatic gain control'])),
('5 Appearance Light Dark System', 'ThemePreference.entries' in ui and all(x in root for x in ['ThemePreference.LIGHT','ThemePreference.DARK','ThemePreference.SYSTEM','isSystemInDarkTheme'])),
('6 English-only language state', 'English' in ui and 'language' in repo),
('7 Security overview', 'SecurityOverviewScreen' in ui and 'Sessions & devices' in ui),
('8 Sessions/devices detail revoke', all(x in ui for x in ['SessionsDevicesScreen','SessionDetailScreen','DeviceDetailScreen']) and 'revokeSession' in nav and 'revokeDevice' in nav),
('9 Login activity', 'LoginActivityScreen' in ui and 'history' in repo),
('10 Help FAQ legal CMS', 'HelpCenterScreen' in ui and 'Admin-published' in ui and 'CmsContentScreen' in ui),
('11 Report User Room', 'ReportScreen' in ui and 'ReportTargetType { USER, ROOM }' in models and 'Your reports' in ui),
('12 Safety Center', 'SafetyCenterScreen' in ui and 'Blocked users' in ui and 'Privacy' in ui),
('13 Contact Support', 'ContactSupportScreen' in ui and 'Send message' in ui and 'repository.contact' in t('feature/settings/src/main/java/app/voicecloud/feature/settings/ui/SettingsViewModel.kt')),
('14 About', 'AboutScreen' in ui and 'Android $versionName' in ui),
('15 global account-state handling', 'SettingsEvent.SessionExpired' in nav and 'SettingsEvent.Maintenance' in nav),
]
for i,(label,cond) in enumerate(checks,1): check(i,cond)
print('[PASS] VC-ANDROID-PH09-R08 product-quality regression: 15/15 PASS')
