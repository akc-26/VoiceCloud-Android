# VC-ANDROID-PH13-R20 — Manual Physical-Device UI QA

Check the delivered application as a product, not only as a build:

1. Startup/onboarding artwork contains no cropped screenshot text or UI chrome; title, illustration and actions fit without collision.
2. Sign-in fields, labels, buttons and keyboard interaction fit cleanly on 360dp-class phones.
3. Home shows readable content with no card-child overlap.
4. Discover shows search, rooms, creators, categories and secondary discovery actions rather than a mostly empty page.
5. Bottom navigation reads Home / Discover / Live / Messages / Profile and the center icon is a microphone/live action.
6. Profile action cards, settings rows and metrics have no overlapping text/icons.
7. Creator Dashboard cards, quick actions and tool rows have no overlap and use clean icons.
8. Creator center navigation uses a microphone for Studio/Live and retains Host authority gating.
9. Listener Live, Economy, Settings, Creator/Host Studio and secondary screens retain usable spacing, readable typography and real backend data.
10. Report any runtime/business defect separately from visual fidelity; both must pass before Git freeze.
