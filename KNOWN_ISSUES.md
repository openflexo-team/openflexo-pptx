# openflexo-pptx — Known issues / assumed debt

This technology adapter was modernized (2025) from a legacy state (binary `.ppt` via POI/HSLF,
POJO model, `.fml.xml` serialization, no `.fmlscript` tests) to: real `.pptx` (OOXML/XSLF, Apache
POI 3.17), a PAMELA `@ModelEntity` model, a modern `PamelaResource` layer, and declarative
`.fmlscript` use cases. The following debt is assumed and left for later iterations.

## Format & model coverage
- **Only OOXML `.pptx` is supported.** Legacy binary `.ppt` (POI HSLF) support was dropped on
  purpose. The `ResourceFactory` accepts only the `.pptx` extension.
- **Shape mapping.** On load, text boxes, auto-shapes, pictures, connectors, groups (with their children), tables
  (rows and cells) and charts (series) are reflected into the model, as well as the paragraphs of the text shapes and
  the speaker notes of the slides — everything the sample decks of `pptx-ta-test-rc` contain. Not reflected: SmartArt,
  OLE objects, media, ink (absent from the samples; skipped, logged at FINE), the paragraphs of table cells (a cell
  only exposes its text), slide masters, layouts and themes (only `layoutName`). POI does not render charts (it only
  draws the fallback picture of a graphic frame): they are drawn by our own Java2D renderer, see PPTX-F-3.
- **Nested elements are not listed at the top level.** The shapes of a group are listed in
  `PowerpointShapeGroup.children`, not in `PowerpointSlide.powerpointShapes` (nor in `select … from slideshow`).
- **Labels of connectors are separate text boxes.** Nothing in the file links a label such as "REST API" to its
  connector; only a proximity heuristic, in FML, could do it.
- **Slide title is read-only.** `PowerpointSlide.getTitle()` scans the title placeholder; there is
  no `setTitle()`. Authoring text is done through `addTextBox(...)`.

## FML surface
- **`AddPowerpointSlide` ignores positional index.** XSLF appends new slides at the end; the
  `slideIndex` binding is accepted but not honored.
- **Actor-reference URIs are session-stable only.** `PowerpointObject.getUri()` is hierarchical but
  recomputed per load; it is not guaranteed stable across reloads, so persisted actor references to
  individual slides/shapes are not round-trip stable.
- **Two model-slot levels kept.** `PowerpointModelSlot` (abstract) + `BasicPowerpointModelSlot`
  (concrete, FML-declared) were not consolidated into one.

## Tests
- **No `save`/`create` FML-script directive exists** in the fml-cli, so the disk save↔load
  round-trip (and empty-presentation creation) is proven by a Java integration test
  (`PowerpointResourceRoundTripTest`) rather than by an `.fmlscript`. The `.fmlscript` use cases
  cover read/navigation, in-memory mutation, and `select … from … where`.

## UI
- `pptx-ta-ui` was migrated to XSLF (`XMLSlideShow`/`XSLFSlide`, `XSLFSlide.draw(...)`), but its
  wizard/inspector coverage was not otherwise revisited.
