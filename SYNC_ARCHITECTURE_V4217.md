# Parion v4.2.17 canonical realtime sync

- Local SQLite remains the UI source of truth.
- SQLite triggers create a durable `pending_sync` athlete queue for athlete/payment/fee mutations.
- Exactly one canonical athlete cloud writer is used: `parion_sync_one_athlete_lww_v411`.
- Legacy bulk snapshot writers and the old per-athlete delta RPC are blocked in normal operation.
- Supabase Realtime Postgres Changes is used as an invalidation channel; payloads do not directly overwrite SQLite.
- Realtime athlete/payment/fee events trigger a targeted canonical pull for the affected athlete.
- Attendance, membership events, and material tables use Realtime invalidation and their existing safe pull paths.
- Foreground 4-second recent-payment network polling is disabled; `payment_recent` is refreshed during canonical sync.
- On resume/reconnect a bulk read/merge catch-up runs only after the pending local queue is empty.
- WorkManager remains a fallback/safety layer and never performs destructive snapshot writes.
