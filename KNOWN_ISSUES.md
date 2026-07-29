# openflexo-pptx — Known issues / assumed debt

This technology adapter was modernized (2025) from a legacy state (binary `.ppt` via POI/HSLF,
POJO model, `.fml.xml` serialization, no `.fmlscript` tests) to: real `.pptx` (OOXML/XSLF, Apache
POI 3.17), a PAMELA `@ModelEntity` model, a modern `PamelaResource` layer, and declarative
`.fmlscript` use cases. The following debt is assumed and left for later iterations.

## Format & model coverage
- **Only OOXML `.pptx` is supported.** Legacy binary `.ppt` (POI HSLF) support was dropped on
  purpose. The `ResourceFactory` accepts only the `.pptx` extension.
- **Partial shape mapping.** On load, only these POI XSLF shapes are reflected into the model:
  text boxes (`XSLFTextBox`), auto-shapes (`XSLFAutoShape`), pictures (`XSLFPictureShape`),
  connectors/lines (`XSLFConnectorShape`) and groups (`XSLFGroupShape`). Tables, charts, SmartArt,
  graphic frames and other shapes are skipped (logged at FINE). `PowerpointShapeGroup` does not yet
  expose its children as a nested collection.
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
