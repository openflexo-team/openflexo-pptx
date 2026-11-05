# openflexo-pptx — Backlog

Features and evolutions to develop in openflexo-pptx. Known bugs go to `KNOWN_DEFECTS.md` (identifiers `PPTX-D-<n>`),
not here; the assumed debt of the 2025 modernization is still listed in `KNOWN_ISSUES.md`.

Identifiers are stable and never reused: `PPTX-F-<n>`. Reference them from commits and from other entries.

Status legend: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `DEFERRED`.

---

## User interface

### PPTX-F-1 — Select and manipulate the elements of a slide  ·  `IN PROGRESS`

**Problem.** The slideshow view only displayed each slide as an image rendered by POI: nothing could be selected,
neither in the slide nor in a tree of its contents.

**Decision.** POI keeps rendering, Diana provides the object layer. The slide is painted by POI
(`XSLFSlide.draw`) as the decoration of a Diana drawing, and each `PowerpointShape` is a transparent, read-only
Diana shape covering its bounds (`PowerpointSlideDrawing`, `PowerpointSlideEditor`). Rendering stays exactly the
one of POI, Diana gives focus, selection and handles, and the `SelectionManagingDianaEditor` synchronizes the
selection with the `SelectionManager`. The Diana prototype `diana-ppt-editor` (binary `.ppt`, Diana re-rendering)
was evaluated and not retained: re-rendering a slide in Diana loses rich text, rotation, most preset geometries,
tables and charts.

**Done (step 1, read-only selection).** Every shape of slides 6, 8 and 11 of `CycloCity-Company-Presentation.pptx`
gets a node, and a click at its centre focuses it, or the shape drawn above it (the icons over the cards of slide
11). Lines and connectors get a minimal 6 pt area to stay clickable; rotated shapes are selected by the bounding box
of their rotated outline. Selecting a shape brings its slide to front.

**Done (step 2, group children).** `PowerpointShapeGroup.children` lists the shapes of a group (recursively, nested
groups included), each child knowing its `container`; the slide of a child is the slide of its group. In the slide
view the child shapes are nested in the Diana shape of their group, their anchors being mapped from the interior
coordinates of the group. Checked on CycloCity slide 8, NordicEBikes slides 1 and 5, TalentLens slides 5 and 11:
every child gets a node at the right place, and a click on a child selects it.

**Done (step 3, selecting a group).** A click keeps selecting the deepest shape under the mouse. ESCAPE then selects
the group containing the selected shape (the slide for a top-level shape), and ENTER selects the first shape of the
selected group.

**Done (step 4, contents browser).** A browser showed the slideshow, its slides, their shapes and the shapes of the
groups (rooted on the shown slide since step 6), in the bottom-left view of the perspective while the slideshow is
shown. It is bound to the selection manager, like the slide editors: a shape selected in the browser is selected in its
slide, and the other way round.

**Done (step 5, thumbnails, icons, inspectors).**
- The tabs are replaced by a column of slide thumbnails on the right of the view (rendered by POI, cached): choosing a
  thumbnail shows its slide and selects it; selecting a shape elsewhere shows its slide and highlights its thumbnail.
- Each kind of element has its own icon (slide, group, text box, auto-shape, picture, line).
- Read-only inspectors for every element, composed along the model hierarchy: name; page size and slide count; slide
  number, title, layout and shape count; position, size and group; geometry, rotation, fill and line; text; image
  format, file and preview; line decorations; group size. The model gained the derived getters they need
  (`pageWidth`, `layoutName`, `shapeTypeName`, `rotation`, `fillColor`, `lineColor`, `lineWidth`, `contentType`,
  `pictureFileName`, `image`, `headDecoration`, `tailDecoration`), also usable from FML.
- The transparent Diana shapes no longer carry a floating label (its empty label showed stray selection handles).

**Done (step 6, layout).**
- The browser (`Fib/Widget/PowerpointSlideBrowser.fib`) is rooted on the shown slide, and follows it.
- The shown slide and the thumbnails are in a `JXMultiSplitPane`. The slide is centered and zoomed to fit the space it
  is given, so it never needs a scroll bar; the thumbnails are rendered again at the width of their column (room is
  kept for the vertical scroll bar, which only appears when the thumbnails do not fit in height; never a horizontal one).

**Done (step 7, paragraphs).**
- In the browser, a text box with a single paragraph shows that paragraph's text directly and has no child; one with
  several paragraphs keeps its paragraphs as children.
- In the slide, each non-empty paragraph of a text box with several paragraphs is a node of its own, nested in the
  text box and selectable independently: a click selects the paragraph, ESCAPE its text box, ENTER the first paragraph.
  Paragraph bounds come from the POI text layout (`PowerpointDrawFactory.layoutParagraphs`), taken from the left of
  the laid-out text to the right inset of the text box. Checked on the 8 sample decks: 224 text boxes with several
  paragraphs, 542 paragraphs, every one gets a node; a click at its centre selects it except where another shape is
  drawn above it, or in a rotated text box (axis-aligned bounds of neighbouring paragraphs overlap).

**Next steps.**
- Rotated or flipped groups are not taken into account when placing their children.
- Tables and charts (`p:graphicFrame`) are not in the model, so they cannot be selected.
- Move / resize written back to the XSLF anchor; then style editing in the inspector. This requires painting each
  shape with its own POI drawable instead of the whole slide at once.
- Glued connectors (`stCxn` / `endCxn`) following a moved shape.

**Acceptance (steps 1-4).** In the application, on the CycloCity sample, clicking a shape selects it in the slide and
in the browser, an empty area selects the slide, ESCAPE / ENTER move between a shape and its group, and a shape
selected in the browser is selected in its slide.

### PPTX-F-6 — Embed the slideshow view in another module  ·  `DONE`

**Problem.** The FreeModellingEditor will show a presentation with `PowerpointSlideshowView` and an overlay: the user selects shapes
and declares them as instances of concepts. The view offered no contextual menu, no way to paint over the elements, and the elements
of a slideshow were not mapped to its view.

**Done.**
- A right click on the slide or on any of its elements (control-click as well, out of macOS) shows the contextual menu of the
  selection, as in the diagram editors: the FlexoActions available on `Powerpoint*` objects appear there.
- `PowerpointAdapterController`: a slide, a shape or any other element of a slideshow is represented in the view of its
  `PowerpointSlideshow` (its master object).
- `PowerpointElementDecorationProvider`: maps an element of a slide to an optional border color, fill tint (with the opacity of its
  alpha) and badge text, painted on the transparent Diana shape covering the element; `refresh()` makes the views ask again. It is
  installed with `PowerpointSlideshowView.setDecorationProvider(...)`. The view only relies on the `FlexoController` it is given
  (selection manager, removal of the module view), so that a client module can build it with its own controller.

**Checked** (`TestPowerpointElementDecorationProvider`). A provider changes the graphical representation of the shape it decorates
(border, tint, badge), follows its refreshes, leaves the other shapes transparent, and is removed with the view.

**Done (2026-11-03, for FME-F-2).** A module embedding the view may place the browser of the shown slide itself:
`getSlideBrowser()`, and `setInstallsBrowser(false)` so that the view no longer puts it in the bottom-left view of the perspective.

**Done (2026-11-04/05, for FME-F-2).** The thumbnails can be a row at the top or at the bottom of the shown slide
(`ThumbnailsPosition`, the column on the right stays the default). `setRepresentedElements(...)` gives the elements standing for an
object of the embedding module: selecting that object selects them in the editors of the slides, with their handles, and leaves the
selection of the application unchanged.

**Left.** The visuals are minimal on purpose: the exact rendering of the overlay will follow the screens of the FreeModellingEditor.
The contextual menu was not tried in the running application.

## Model

### PPTX-F-2 — Reflect every element of the sample decks  ·  `DONE`

**Problem.** The model only reflected text boxes, auto-shapes, pictures, connectors and groups, and the text of a shape
as a single string. The sample decks of `pptx-ta-test-rc` (8 decks, 106 slides) also hold tables, charts, connectors
glued to shapes and speaker notes, that their `.expected.md` rely on (RACI, glossary, requirements, FMEA, budget,
organization chart, quiz answer…).

**Done.** For each element: the model, its conversion on load, the browser, an icon, an inspector, the selection in
the slide view where the element has a location, and a `.fmlscript` use case.
- Identity and connectors: `PowerpointShape.shapeId` / `altText`, `PowerpointSlide.getShapeWithId()`,
  `PowerpointLine.startShape` / `endShape` and their connection sites (`TestConnectors.fmlscript`).
- Tables: `PowerpointTable` → `PowerpointTableRow` → `PowerpointTableCell` (text, indexes, spans, fill); rows and cells
  are nested Diana shapes, selectable (`TestTables.fmlscript`).
- Charts: `PowerpointChart` (title, kind) → `PowerpointChartSeries` (name, kind, categories, values, `getValue()`),
  read from the cache of the chart, multi-level categories included; a placeholder is painted in the slide view since
  POI does not render charts (`TestCharts.fmlscript`).
- Speaker notes: `PowerpointSlide.notes` (`TestNotes.fmlscript`).
- Paragraphs: `PowerpointTextShape.paragraphs` → `PowerpointParagraph` (text, level, bullet); not selectable in the
  slide view, which has no geometry for them (`TestParagraphs.fmlscript`).
- `PowerpointSlideElement` gives the slide of any of these elements; ESCAPE / ENTER navigate the nesting of the view.

**Checked.** On the 8 decks the model holds exactly what their XML holds: 2301 shapes, 36 groups, 266 connectors (133
starts and 120 ends glued), 12 tables and 388 cells, 12 charts and 18 series, 1776 paragraphs (97 bulleted), 60 slides
with notes; the slide view is built without error for the 106 slides.

**Left.** See `KNOWN_ISSUES.md` (paragraphs of table cells, masters and layouts, labels of connectors) and PPTX-D-1.

### PPTX-F-3 — Draw the charts  ·  `DONE`

**Problem.** POI does not render charts: for a graphic frame it only draws the fallback picture that some editors join
(`mc:Fallback`), which none of the sample decks carries. Charts were blank in the slide view and in the thumbnails.

**Done.** `PowerpointChartRenderer` draws a chart with Java2D from the series of the model: column, bar, line and their
combinations (one value scale per kind of chart, on the left and right axes, or shared when they use the same axis),
pie, doughnut (hole size) and radar (filled or not). It follows the series and point colors, the value labels, the
legend position (wrapped in rows), the visibility and bounds of the value axes, the text color and the plot area fill
of the chart. `PowerpointDrawFactory` plugs it into POI (through the `Drawable.DRAW_FACTORY` rendering hint), so that
`XSLFSlide.draw()` draws the charts at their place in z-order, inside groups as well, both in the slide view and in
the thumbnails. The model gained `PowerpointChart.legendPosition` / `textColor` / `plotAreaColor` and
`PowerpointChartSeries.color` / `lineColor` / `pointColors` / `showValues`.

**Checked.** The 12 charts of the sample decks rendered and compared with their data (the white line of PPTX-D-2 is
drawn as defined).

**Limits.** A simplified rendering: no number formats, fonts, gradients, effects, stacked groupings, data tables or
trend lines; scheme colors fall back to the default Office palette (the sample decks only use explicit colors).

### PPTX-F-4 — Paint the pattern fills  ·  `DONE`

**Problem.** POI does not paint pattern fills (`a:pattFill`): it only knows solid, picture, gradient and theme fills,
and silently leaves a pattern unpainted. The sample decks hold 33 of them: the blue grid background of every slide of
NordicEBikes (whose white texts were then invisible, PPTX-D-2), the diagonal hatchings of some of its shapes, the
hazard bands of ShipFast and the target boxes of TalentLens.

**Done.** `PowerpointPatternFill` builds a `TexturePaint` from a preset pattern (8x8 tile, foreground and background
colors; a pattern pixel is a device pixel, as in PowerPoint) — grids, horizontal, vertical and diagonal lines, crosses,
checks and percentages; the other presets fall back to an even mix of both colors. `PowerpointDrawFactory` paints it for
backgrounds (`DrawBackground`) and shapes (text boxes, auto-shapes, freeforms, connectors: the outline is filled with
the pattern, then POI draws the rest of the shape), in the slide view and the thumbnails.

**Checked.** The slides of the three decks rendered: blue grid of NordicEBikes, hatchings of ShipFast and TalentLens.

**Limits.** Pattern fills of table cells and theme or scheme colors of patterns are not handled (none in the samples).

### PPTX-F-5 — Identify the elements of a slideshow across reloads  ·  `DONE`

**Problem.** A VirtualModelInstance pointing at slides or shapes could not be reloaded: the URI of an element was a positional path
(`<resource>/slide[n]/shape[i]`), and the model slot only resolved it from an in-memory cache, empty after a reload. A positional
path would also designate another slide once the slides are reordered. The FreeModellingEditor needs this to annotate an existing
presentation, one free model instance federating a whole slideshow.

**Done.**
- URIs built from the identifiers stored in the file (`PowerpointObjectURI`):
  `<resource URI>#/slide[id=258]/shape[id=12]/shape[id=14]`. A slide is designated by the `id` of its `p:sldId` in
  `presentation.xml` (`PowerpointSlide.slideId`, also usable from FML), a shape by its `cNvPr id`, under its group for a nested shape.
  Shapes sharing an id with a sibling (PPTX-D-1) get their rank among those siblings, in document order (`shape[id=25,n=2]`). Rows,
  cells, paragraphs and series keep an index inside their shape.
- The model slot resolves a URI by navigating the loaded slideshow; its cache is only an optimization, checked against the current
  slideshow. A shape moved into or out of a group is still found by its id when no other shape shares it. Positional URIs written
  before are still resolved.
- `ShapeAnnotations.fml` (pptx-ta-test-rc): concepts with a `PowerpointShapeRole` and a `PowerpointSlideRole`. A role finds its
  model slot through its container: `PowerpointShape shape with PPT::PowerpointShapeRole(container=slideshow)`.

**Checked** (`TestShapeActorReferenceReload`). An instance pointing at a shape nested in a group, at the two shapes sharing id 25 of
CycloCity slide 8 and at the slide itself is saved, reloaded in a fresh service manager, then reloaded again after slide 8 was moved
to the front of the deck: every note points at the same element. Every element of the 9 sample decks (4677 elements, 134 of them
through a rank among siblings sharing an id) is found again from its URI.

**Left.**
- Done (2026-11-03): `PowerpointShapeRole` no longer deletes its shape with the instance, and a cloned instance references the
  same shape: a shape is an element of an existing presentation, which an instance annotates. `PowerpointSlideRole` keeps the
  default behaviour.
- A `PowerpointActorReference` keeps the object it resolved: after the slideshow resource is unloaded and loaded again in the same
  session, it still holds the former object.
- An element whose id changed (a deck regenerated, or renumbered by PowerPoint because of PPTX-D-1) is no longer found.
