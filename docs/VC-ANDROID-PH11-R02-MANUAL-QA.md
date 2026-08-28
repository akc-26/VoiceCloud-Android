# VC-ANDROID-PH11-R02 Manual QA

Run `scripts\VC-ANDROID-PH11-R02-ACCEPTANCE.cmd` first. Only after its final PASS, perform the PH11 runtime checks:

- Creator: room create/edit/start/pause/resume/end; Live Console; microphone publish; participant/stage controls; schedule create/edit/delete/start; local timezone and 12-hour display.
- Global copy: verify visible labels/headings/actions use Title Case where applicable.
- Feedback: verify save/success/error results use transient toasts and no raw HTTP/database/stack messages are shown.
- Profile media: select small and very large avatar/cover images; preview, zoom, reposition, crop and upload.
- Privacy: change values and Save; confirm persistence without HTTP 400.
- Security & Devices: open a listed device and revoke where allowed; confirm canonical device works.
- Contact Support: submit valid details/message and confirm persisted success toast.
- CMS: End User pages appear only for consumers; Creator/Host pages respect Creator/Host audience authority.
- Safety Center: confirm Communities is not duplicated there.
- Profile/Settings: verify simplified navigation and no duplicate Help/Legal/Contact/About paths.
- Economy: Wallet/VIP/Store/Gifts use concise visual cards while retaining real purchase/equip actions.
- Choose Your Portal: verify redesigned Royal Sapphire interactive portal selection.
