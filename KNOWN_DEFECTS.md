# openflexo-pptx — Known defects

Known bugs, not fixed yet. Features and evolutions go to `BACKLOG.md` (identifiers `PPTX-F-<n>`), not here.

Identifiers are stable and never reused: `PPTX-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

### PPTX-D-1 — Duplicated shape identifiers in three sample decks  ·  `TODO`

**Symptom.** In `pptx-ta-test-rc`, 9 slides hold two shapes with the same `cNvPr id`, which the OOXML format forbids
(PowerPoint renumbers them silently when it opens the file):
- `CycloCity-Company-Presentation.pptx`, slides 8, 9, 11, 12 and 14: the slide number placeholder takes id 25, already
  used by a card, a label or a connector;
- `NordicEBikes-Design-Review-DR2.pptx`, slides 3, 4 and 9, and `TalentLens-Incident-Postmortem.pptx`, slide 13: a table
  takes the id of a shape of the title block.

**Impact.** A connector designates the shapes it is glued to by their id. `PowerpointSlide.getShapeWithId()` returns
the first shape in z-order, which is the right one in all these cases (the placeholder and the tables come after the
shape the connector is glued to), but another tool may resolve them differently.

**Fix.** In the generator of the sample decks: give the slide number placeholder and the tables free ids, then
regenerate the decks (their `.expected.md` stay valid).

### PPTX-D-2 — White elements invisible on NordicEBikes  ·  `DONE`

**Symptom.** On every slide of `NordicEBikes-Design-Review-DR2.pptx` the background was white instead of a blue grid, so
that the white texts, the white lines of the drawings and the white "Cumulative %" line of the Pareto chart (slide 8)
were invisible.

**Cause.** Not a defect of the deck: its background is a pattern fill (`a:pattFill`, small grid), that POI does not
paint. Fixed by PPTX-F-4.

### PPTX-D-3 — A .pptx sometimes not found when the technology adapter is activated  ·  `DONE`

**Symptom.** After activating the PowerPoint technology adapter in the application, the folder `PPT/Business` of the
resource center `pptx-ta-test-rc/src/main/resources/TestResourceCenter` sometimes showed 6 or 7 of its 8 sample decks.
Not reproducible on demand.

**Cause (verified).** Not the technology adapter: inspected in the running application with a read-only agent while 2
decks were missing, the 8 resources were registered in the resource center, its folder, the repositories of the
technology adapter and the resource manager. The browser node of the folder had missed the last registrations, made
while it was being updated: GINA-D-6 (gina), fixed there.

### PPTX-D-4 — `PowerpointShapeRole` typed as a slide  ·  `DONE`

**Symptom.** `PowerpointShapeRole` extended `FlexoRole<PowerpointSlide>` and its type was `PowerpointSlide`: a role declared
`PowerpointShape shape with PPT::PowerpointShapeRole()` was typed as a slide, so that assigning it a shape (the result of
`AddPowerpointShape` or `select PowerpointShape from …`) mixed two types, and its actor reference was built for a slide.

**Fix.** The role is a `FlexoRole<PowerpointShape>` of type `PowerpointShape`. No FML of the repository used it before; it is
now exercised by `ShapeAnnotations.fml` (PPTX-F-5).

### PPTX-D-5 — No actor reference of a PowerPoint role could be saved  ·  `DONE`

**Symptom.** Assigning a slide or a shape to a `PowerpointSlideRole` / `PowerpointShapeRole` logged
`Unknown entity 'org.openflexo.technologyadapter.powerpoint.BasicPowerpointModelSlotURIProcessor'`, and saving the instance then
failed with a `NullPointerException` in `PowerpointActorReference.getObjectURI()`.

**Cause (verified).** The model slot created its URI processor through the FML model factory, which does not know that PAMELA
entity (and could not have built it: its only constructor took a parameter). The processor is now a plain helper class.
