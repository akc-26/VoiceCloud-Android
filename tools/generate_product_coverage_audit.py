#!/usr/bin/env python3
"""Generate R06 Android product coverage audit from locked contracts."""
from __future__ import annotations

import json
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCREEN = ROOT / "contracts/screens/VC-ANDROID-R06-SCREEN-PARITY-R01.json"
API = ROOT / "contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json"
RT = ROOT / "contracts/realtime/VC-ANDROID-R06-SOCKET-EVENTS-R01.json"
OUT = ROOT / "docs/VC-ANDROID-R06-PRODUCT-COVERAGE-AUDIT-R01.md"

ANDROID_ROUTES = [
    "bootstrap",
    "user",
    "creator",
    "user/home",
    "user/discover",
    "user/search",
    "user/room/preview",
    "user/live",
    "user/messages",
    "user/messages/thread",
    "user/profile",
    "user/wallet",
    "user/settings",
    "creator/dashboard",
    "creator/audience",
    "creator/live",
    "creator/analytics",
    "creator/workspace",
    "creator/settings",
]

# Heuristic mapping from website/creator page file names to implementation tier on voicecloud-ui-redesign.
UI_SHELL_KEYWORDS = {
    "Home": "user/home",
    "Explore": "user/discover",
    "Search": "user/search",
    "RoomPreview": "user/room/preview",
    "Live": "user/live",
    "Messages": "user/messages",
    "Conversation": "user/messages/thread",
    "Profile": "user/profile",
    "Wallet": "user/wallet",
    "Settings": "user/settings",
    "Dashboard": "creator/dashboard",
    "Audience": "creator/audience",
    "Analytics": "creator/analytics",
    "Workspace": "creator/workspace",
}


def page_name(path: str) -> str:
    return path.split("/")[-1].replace(".tsx", "").replace("Page", "")


def classify_page(path: str) -> str:
    name = page_name(path)
    route = UI_SHELL_KEYWORDS.get(name)
    if route:
        return "partial_ui_shell"
    if name in {"Login", "Register", "ForgotPassword", "ResetPassword", "Onboarding"}:
        return "auth_ph02"
    return "missing"


def main() -> None:
    screen = json.loads(SCREEN.read_text())
    api = json.loads(API.read_text())
    rt = json.loads(RT.read_text())
    ops = api.get("operations", [])
    inbound = rt.get("inboundSubscribeMessages", [])
    outbound = rt.get("outboundEmitLiterals", [])

    pages = screen["websitePageSources"] + screen["creatorPageSources"]
    by_status: dict[str, list[str]] = defaultdict(list)
    for p in pages:
        by_status[classify_page(p)].append(p)

    lines: list[str] = [
        "# VoiceCloud Android — R06 Product Coverage Audit R01",
        "",
        f"Generated: {datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M UTC')}",
        "",
        "## Authority",
        "- Master plan: `docs/VC-ANDROID-R06-MASTER-DEVELOPMENT-PLAN-R01.md`",
        "- Screen parity: `contracts/screens/VC-ANDROID-R06-SCREEN-PARITY-R01.json`",
        "- API contract: `contracts/api/VC-ANDROID-R06-API-CONTRACT-R01.json`",
        "- Realtime contract: `contracts/realtime/VC-ANDROID-R06-SOCKET-EVENTS-R01.json`",
        "",
        "## Executive summary",
        "",
        f"| Metric | Count |",
        f"| --- | ---: |",
        f"| Website + Creator parity page sources | {len(pages)} |",
        f"| Backend operations in contract | {len(ops)} |",
        f"| Realtime inbound subscribe messages | {len(inbound)} |",
        f"| Realtime outbound emit literals | {len(outbound)} |",
        f"| Android navigation routes (current) | {len(ANDROID_ROUTES)} |",
        f"| Pages with partial UI shell only | {len(by_status['partial_ui_shell'])} |",
        f"| Auth/account pages (PH02 scope) | {len(by_status['auth_ph02'])} |",
        f"| Pages with no Android implementation | {len(by_status['missing'])} |",
        "",
        "**Status:** Android is **not** product-complete. Current branch delivers PH01 foundation + redesigned presentation shells for a subset of routes. Backend API/realtime wiring, auth lifecycle, live room authority, economy settlement, and the majority of parity pages remain to be implemented per master plan phases PH02–PH14.",
        "",
        "## A. Discovered screens/features (parity sources)",
        "",
        f"Total page sources: **{len(pages)}** (Consumer website {len(screen['websitePageSources'])}, Creator Studio {len(screen['creatorPageSources'])})",
        "",
        "## B–D. Implementation totals",
        "",
        "| Tier | Count | Meaning |",
        "| --- | ---: | --- |",
        f"| Partial UI shell (no production data/API) | {len(by_status['partial_ui_shell'])} | Compose layout exists; placeholders / empty states |",
        f"| Auth lifecycle (contracted, in progress PH02) | {len(by_status['auth_ph02'])} | Requires `feature:auth` + navigation gates |",
        f"| Missing Android surface | {len(by_status['missing'])} | No route/screen/workflow |",
        "",
        "## G. Current Android navigation routes",
        "",
    ]
    for r in ANDROID_ROUTES:
        lines.append(f"- `{r}`")
    lines.extend(
        [
            "",
            "## H. API areas (contract inventory)",
            "",
            "Top operation prefixes (first 4 path segments):",
            "",
        ]
    )
    prefix_counts: dict[str, int] = defaultdict(int)
    for o in ops:
        parts = o["path"].strip("/").split("/")
        prefix = "/".join(parts[:3]) if len(parts) >= 3 else o["path"]
        prefix_counts[prefix] += 1
    for prefix, count in sorted(prefix_counts.items(), key=lambda x: -x[1])[:40]:
        lines.append(f"- `{prefix}` — {count} operations")
    lines.extend(
        [
            "",
            "## I. Realtime contract",
            "",
            f"- Inbound subscribe messages: **{len(inbound)}**",
            f"- Outbound emit literals: **{len(outbound)}**",
            "",
            "## Missing parity pages (sample)",
            "",
        ]
    )
    for p in sorted(by_status["missing"])[:60]:
        lines.append(f"- `{p}`")
    if len(by_status["missing"]) > 60:
        lines.append(f"- … and {len(by_status['missing']) - 60} more")
    lines.extend(
        [
            "",
            "## Phase mapping (master plan)",
            "",
            "| Phase | Scope | Android status |",
            "| --- | --- | --- |",
            "| PH01 | Foundation, bootstrap, contracts | **Implemented** |",
            "| PH02 | Dual-portal auth & account lifecycle | **In progress** |",
            "| PH03 | Home, discovery, search, profile, social | UI shells only |",
            "| PH04 | Communities, events, messaging, notifications | UI shells / missing |",
            "| PH05–PH06 | Live room listener + host/stage | UI shells / missing RTC wiring |",
            "| PH07 | Consumer economy | UI shell only |",
            "| PH08–PH09 | Replays, security, safety, CMS | Missing |",
            "| PH10–PH13 | Creator product | UI shells only |",
            "| PH14 | Parity reconciliation & certification | Not started |",
            "",
            "## Blocked / backend-limited (from master plan BR items)",
            "",
            "- **VIP Google Play subscriptions (BR-02):** backend subscription mapping incomplete — Android must not fake VIP purchase.",
            "- **Paid creator subscriptions (BR-03):** platform economy not closed — display authorized state only.",
            "- **Agency (BR-04):** excluded — no Android agency product.",
            "",
            "---",
            "",
            "Regenerate: `python3 tools/generate_product_coverage_audit.py`",
            "",
        ]
    )
    OUT.write_text("\n".join(lines))
    print(f"Wrote {OUT}")


if __name__ == "__main__":
    main()
