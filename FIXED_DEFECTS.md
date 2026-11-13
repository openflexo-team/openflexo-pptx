# openflexo-pptx — Fixed defects

Defects of openflexo-pptx that are fixed: entries moved from [`KNOWN_DEFECTS.md`](./KNOWN_DEFECTS.md) once their status is `DONE`,
with what was verified, the cause and the fix. Identifiers are those of `KNOWN_DEFECTS.md`, never reused.

---

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
