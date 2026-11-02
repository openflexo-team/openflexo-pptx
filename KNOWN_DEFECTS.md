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

### PPTX-D-3 — A .pptx sometimes not found when the technology adapter is activated  ·  `TODO`

**Symptom.** Once (2026-11-02), after activating the PowerPoint technology adapter in the application run from Eclipse,
the resource center `pptx-ta-test-rc/src/main/resources/TestResourceCenter` showed 7 of its 8 sample decks:
`NordicEBikes-Design-Review-DR2.pptx` was missing. Not reproduced in about ten later runs: most likely a race.

**Verified.**
- Not the files: a resource center scanned in a test, on a copy of the folder and on the folder itself, finds the 8
  decks, also with the PowerPoint lock files (`~$…pptx`, rejected by the factory) and with PowerPoint holding the decks.
- Not a second resource center masking the first: the private resource center of the TA is not loaded under Eclipse.
- No error in the logs of the session; they did not trace the registration of resources then.

**Suspects (core, not proven).**
- `ResourceRepositoryImpl.registerResource` adds to an unsynchronized `HashMap` and to the resource list of a
  `RepositoryFolder`, from the thread that activates the technology, while the Swing thread may create the folders of
  the repositories at the same time (`TechnologyAdapter.notifyRepositoryStructureChanged`, see CORE-D-25): a resource can
  end up in a folder object that is then replaced, or be lost by a concurrent add.
- `FileSystemBasedResourceCenterImpl.registeredResources` is an unsynchronized `HashMap` too, also written by the
  directory watcher thread (the session followed modifications of the `Business` folder by PowerPoint).

**To diagnose when it happens again.** Trace each step of a `.pptx` in `PowerpointSlideshowResourceFactory`
(`isValidArtefact`, `retrieveResource`, `registerResource`, `unregisterResource`), `PowerpointSlideshowResourceImpl.delete`
and `PowerpointTechnologyAdapter.resourceCenterHasBeenInitialized` (count of the repository at the end of the scan):
no line, a rejection, an exception, or a removal each point to a different cause; a deck registered in the repository
but absent from the browser points to the folder race above.

