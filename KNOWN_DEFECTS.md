# openflexo-pptx — Known defects

Known bugs, not fixed yet. Features and evolutions go to `BACKLOG.md` (identifiers `PPTX-F-<n>`), not here.

Identifiers are stable and never reused: `PPTX-D-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

Fixed defects are moved to [`FIXED_DEFECTS.md`](./FIXED_DEFECTS.md) once their status is `DONE`.

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
