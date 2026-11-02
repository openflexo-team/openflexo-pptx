# CycloCity-Company-Presentation.pptx — expected elicitation

Fictional bike-sharing operator, 15 slides. Ground truth for Free Modelling Editor
tests: the concepts and the named instances a reader should elicit from the slides.

## Slide 4 — Offer (three cards)
Concept `Plan`: Pay-as-you-go, Monthly pass, Corporate fleet (price in each card).

## Slide 5 — Fleet (cards, one dashed = planned)
Concept `BikeModel`: City Classic (3,120 bikes, VeloNord), City E-Assist (1,680 bikes,
ArmorCycles), Cargo E-Bike (pilot, planned; dashed border).

## Slide 6 — Value chain (native chevrons + pictures)
Concept `ValueChainStep`: Procure, Deploy, Operate, Maintain, Serve (ordered by position).

## Slide 7 — Cities and stations (picture + 12 cards)
Concept `City` (name, launch year, bikes): Nantes 2018/640, Rennes 2019/480, Angers 2019/310,
Tours 2020/260, Lyon 2020/720, Grenoble 2021/340, Toulouse 2021/610, Bordeaux 2022/520,
Montpellier 2022/330, Lille 2023/280, Strasbourg 2024/190, Dijon 2025/120 (sum 4,800).
Concept `StationType`: Hub, Standard station, Mini station; example `Station` instances in
the legend: Nantes Gare Sud, Lyon Part-Dieu, Place Graslin, Cours Mirabeau, Campus Tertre,
Parc Blandan.

## Slide 8 — Platform architecture (groups, labelled connectors, can shape)
Concept `Component` with product instances: Smart bike = CC-Lock Gen2, Docking station =
DockOne 16, IoT gateway = StationLink 4G, Cloud platform = CycloCore v4, Mobile app =
CycloCity app 5.2, Fleet dashboard = FleetWatch, Rides database = PostgreSQL 15 cluster.
`ExternalSystem`: Payment provider = PayNova (dashed border).
Relation `dataFlow` with protocol: Smart bike -BLE-> Docking station, Docking station -Wired->
IoT gateway, IoT gateway -MQTT / 4G-> Cloud platform, Cloud platform -REST API-> Mobile app,
-SQL-> Rides database, -Alerts-> Fleet dashboard; Mobile app -Card token-> Payment provider.

## Slide 9 — Rider journey (ellipses, icons, connectors)
Concept `JourneyStep`: Find, Unlock, Ride, Dock, Pay; relation `next`; touchpoints App map,
QR code, Bike and app, Station, App wallet.

## Slide 10 — Fleet operations loop (cycle)
Concept `OperationActivity`: Monitor fleet (FleetWatch), Rebalance (12 electric vans,
3 dispatchers), Repair and recharge (Nantes workshop, 90 technicians), Redeploy; relation
`produces`: Rebalancing orders, Faulty bikes, Bikes ready, Updated availability.

## Slide 11 — Organization (elbow connectors, no arrows)
Concept `Person` with role / `OrgUnit`: Claire Moreau CEO; COO Julien Lefèvre, CTO Amina Benali,
CCO Sofia Rossi, CFO Marc Dubois; teams City managers (Hugo Martin), Workshop (Lena Roche)
under the COO; Platform team (Yan Chen), Embedded team (Tom Faure) under the CTO.

## Slide 12 — Ecosystem (hub and spoke, bidirectional connectors)
Partner categories (concepts) with named partner instances and relation label:
City councils (Concession contract): Nantes Métropole, Lyon Métropole, Toulouse Métropole;
Bike manufacturer (Supplies bikes): VeloNord, ArmorCycles; Insurer (Covers liability):
MutuelleOuest Assurances; Transit authority (Integrated ticketing): TransLoire, Rhônexpress Pass;
Payment provider (Processes fares): PayNova; Advertising partners (Sponsor stations):
AdMobilis, UrbanPanels.

## Slide 14 — Roadmap
Concept `Milestone`: Q2 2026 Launch Marseille and Nice, Q4 2026 Cargo e-bike pilot,
Q2 2027 Gen-3 smart lock, Q4 2027 20 cities served, 2028 SaaS in 10 countries.

## Other slides
1 title, 2 KPIs, 3 chart (graphicFrame, not mapped by the TA), 13 table (graphicFrame),
15 contact.
