# NordicEBikes-Design-Review-DR2.pptx — expected elicitation

Genre: engineering design review. Blueprint identity: grid pattern background, white line work, drawing
border and title block on every sheet, monospace text. 12 slides.

## Slide 2 — Entry criteria (checkboxes, freeform ticks, crosses): 5 of 7 met

## Slide 3 — Requirements (table)
REQ-01 … REQ-08 with target, verification method and status.

## Slide 4 — Exploded view
Balloons 1-7 with leader lines; parts list: Frame (AlpTube), Fork (RockStar), Battery pack (VoltCell),
Drive unit (DriveTech), Display (ClearView), Rear and front wheel (Nordgum).

## Slide 5 — SysML block definition diagram (each block = group of header and values compartments)
Fjell X2 composed of (diamond) Frame assembly, Drive unit, Battery pack, Controller, HMI display;
DriveTech M85 specialises Drive unit. Values: mass, range, torque, capacity, firmware…

## Slide 6 — Internal block diagram (connectors glued to small port shapes)
Battery.dc_out → Controller.dc_in (48 V DC); Controller.phase_out → Motor.phase_in (3-phase PWM);
Torque sensor.sig → Controller.sensor_in (analog); Controller.can ↔ Display.can (CAN); Display.usb.

## Slide 7 — State machine
initial → Off → Standby → Assist; Assist → Standby; Standby → Walk mode; Walk mode → Fault;
Assist → Fault; Off → Charging; Charging → Walk mode; Fault → Charging. Transitions whose ends share a
side are freeforms, not connectors.

## Slide 8 — Defect Pareto (combo chart)
Cable chafing 14, Battery latch 11, Display fogging 7, Brake squeal 5, Paint chips 3, Other 2.

## Slide 9 — Tolerance stack-up
Contributors Dropout, Chainstay tube, Weld fillet, BB shell, Bearing seat with dimension lines; worst
case FAIL, RSS PASS.

## Slide 10 — Pugh matrix
Options Integrated top-loading, Rack-mounted, Side-loading in down tube (retained).

## Slide 11 — FMEA excerpt (RPN coloured)

## Slide 12 — Outcome
Rotated "APPROVED WITH ACTIONS" stamp, actions A-01 … A-06, signatures.
