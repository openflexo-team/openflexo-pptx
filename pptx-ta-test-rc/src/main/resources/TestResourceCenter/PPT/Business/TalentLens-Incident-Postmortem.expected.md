# TalentLens-Incident-Postmortem.pptx — expected elicitation

Genre: blameless incident post-mortem (engineering). Dark identity: gradient backgrounds, glow, gradient
and pattern fills, monospace text, custom geometry. 14 slides, speaker notes on every slide.

## Slide 1 — Title
Freeform open paths (metric line), rotated octagon badge "SEV-1". Roles: Inès Moreau (incident
commander), Bastien Roy (author).

## Slide 2 — TL;DR (doughnut charts, gradient callout)
Customers affected 37/380; sync jobs failed 1,284/2,960; availability 99.2 % vs 99.9 %.

## Slide 3 — Timeline (vertical axis, markers, pill-shaped actors)
Concept `Event` (time, description, actor, kind by colour: failure red, detection amber, response
cyan, resolution green): 06:58 disk 95 % (kafka-eu-2) · 07:12 consumer lag grows (ingestion-eu) ·
07:45 first ticket from Brightway Bank (Support desk) · 07:59 page acknowledged (Bastien Roy) ·
08:20 SEV-1 declared (Inès Moreau) · 08:41 status page update (Julia Santos) · 09:13 root cause found
(Tomás Oliveira) · 09:43 segments purged (Mei Lin) · 10:31 6.2 M events replayed (ingestion-eu) ·
11:10 resolved (Inès Moreau).

## Slide 4 — Duration (segments, rotated braces, pattern fills)
Concept `Phase`: Time to detect 47 min, Time to mitigate 118 min, Time to recover 87 min.
Targets: detect 10, mitigate 45, recover 60 (total 1 h 55).

## Slide 5 — Architecture (hexagons, drums, can, cloud, curved glued connectors, group)
Components: Customer HRIS (cloud), Connector workers (hexagon), Kafka cluster EU (group of the
container and brokers kafka-eu-1, kafka-eu-2 [failing], kafka-eu-3), Ingestion consumers (hexagon),
Warehouse (can, Snowflake EU), Dashboards web app, Retention job (disabled, "no" sign).
Flows: HRIS -REST-> Connector workers -> each broker -> Ingestion consumers -COPY-> Warehouse ->
Dashboards; Retention job ··> Kafka cluster EU. Failing flows through kafka-eu-2 are red and dashed.

## Slide 6 — Sequence diagram (lifelines, activations, messages, loop fragment, note)
Lifelines: Scheduler, Connector worker, kafka-eu-2, Ingestion consumer, Warehouse.
Messages: 1 trigger sync · 2 produce 12,400 events · 3 NOT_ENOUGH_REPLICAS (return) · 4 retry ×5
(self call, freeform) · 5 status "partial" (return) · 6 poll · 7 no new records (return) · 8 load 0 rows.
Fragment "loop [retries]" around 2-4; note (folded corner) on the design flaw.

## Slide 7 — Consumer lag line chart, translucent band, two callouts (07:45, 09:43)

## Slide 8 — Blast radius (three nested ellipses = containment)
Rings: Missed alerts (12) ⊂ Dashboards stale (25) ⊂ Unaffected (343).
Missed alerts: Altrix Logistics, Brightway Bank, Cobalt Retail, Delta Pharma, Evergreen Energy, Granite
Construction, Harbor Hotels, Ionic Telecom, Keystone Insurance, Northwind Foods, Orion Airlines, Polaris
Health. Stale dashboards (examples): Foxglove Media, Juniper Foods, Lumen Education, Maple Robotics,
Nimbus Games, Quartz Legal, Redwood Schools, Sierra Textiles.

## Slide 9 — Fishbone
Effect: Dashboards stale for 4 h 12. Concept `CauseCategory` with `Cause`s:
People (On-call new to Kafka; No runbook for disk alerts) · Process (Release checklist skips infra
config; Tickets triaged before paging) · Technology (Retention flag renamed in 4.18; No disk headroom
on one broker) · Monitoring (Disk alert threshold at 98 %; "partial" sync never alerts) · Release (No
canary for infra jobs; Config diff not reviewed) · Communication (Status page 56 min late; No ETA
given to customers).

## Slide 10 — Five whys (staircase, curvedConnector2 labelled "why?")
Chain Why 1 → Why 5; Why 5 is the root cause (release 4.18 renamed the retention flag).

## Slide 11 — Retrospective (speech-bubble callouts, initials avatars, groups)
Went well: Inès Moreau, Tomás Oliveira · Went wrong: Julia Santos, Mei Lin · Lucky: Bastien Roy,
Karim Saadi (one group per quote card).

## Slide 12 — Action items (table, coloured status cells)
AI-1 Tomás Oliveira Done · AI-2 Mei Lin In progress · AI-3 Bastien Roy Done · AI-4 Karim Saadi In
progress · AI-5 Ana Ferreira To do · AI-6 Bastien Roy To do · AI-7 Julia Santos In progress.

## Slide 13 — Alert routing (pills with icons, elbow connectors, tags, small table)
Prometheus, Sync monitor -> Alertmanager -> PagerDuty (SEV-1/2), Slack #inc-eu; PagerDuty -> On-call
SRE, Incident commander (escalate, dashed); Slack #inc-eu -> Status page. On-call rota W44-W46.

## Slide 14 — Closing (quote, hyperlink, gradient badge)
