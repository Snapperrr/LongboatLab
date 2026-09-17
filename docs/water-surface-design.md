# Vanilla-material displaced water (0.9.4)

## 0.9.4 wave-front, crest breakup and impact presentation

The leading station now tapers width, amplitude and opacity to zero with a smoothstep distance
envelope. Three additional local stations resolve the leading transition even on short boats;
duplicates are removed before constructing node spans. Shared-node history from 0.9.3 remains.

Water ribbon, froth and impact-lamella textures are generated reproducibly at 64x64. They add
translucent water striations, discontinuous bubble clusters and thinning ejecta ligaments. Node
texture phase and small crest corrugation interpolate with the same previous/current state used
for geometry, including when a trailing node releases into the free wake.

Detached wave spreading uses a bounded speed-dependent lateral rate (speed*0.34, clamped to
0.035..0.22 blocks/tick), reduced by 1/sqrt(1+age*0.035). The ~19-degree deep-water Kelvin envelope
motivates the scale but is not imposed as an exact physical solution: finite depth, hull pressure
and dispersion are not fully solved by these decorative ribbons. Primary crests settle into weaker
cross-ribbon oscillations and separated residual froth bands, with smooth onset and decay. Small
droplets and low-alpha mist shed from strong crests under one six-particle-per-tick budget.

Impact sheets originate on the contact footprint perimeter. Normal entry speed controls radial
ejection energy; tangential motion biases the crown into the impact direction. Uneven water fingers
rise ballistically, shed coarse/fine droplets near the apex and leave thin fading lamellae, rather
than forming a persistent upright curtain. Most initial ejecta travel at low angles. Fine mist is
emitted later at the breaking rim, not as a large central cloud. Existing droplet collision and
secondary landing ripples remain in use.

This update changes visual presentation only. Resource presence/JSON and source structure were
checked statically; no shader/Java compilation, simulation or game validation was performed.

## 0.9.3 shared-node ribbon repair (supersedes 0.9.2 ribbon lifecycle)

The 0.9.2 attached ribbon rebuilt birth times every tick, giving age-dependent width/amplitude a
repeating sawtooth. Detached history started at zero age with different offsets and sampling cadence.
Budget shortfalls and low-speed central turning stations also deleted whole spans. These were source
defects; this revision has not been visually validated in a running game.

WakeNode now owns previous/current inner/outer positions, amplitude, alpha, appearance and detached
water-height cache. Every adjacent span references the same endpoint object. Nodes advance once per
tick using an identity set, and all rendering uses one interpolation path. Hull station maps have
stable local-coordinate keys. Strength/height targets smooth over time, with no synthetic birth-time
reset. Slow central stations reduce strength without breaking connectivity; leading-end selection
has a +/-0.04 longitudinal-speed dead band.

At the trailing station the old node freezes and releases into world space, and the new node inherits
its previous/current state. Both the final hull span and bridge to free history reference that new
node. Release preserves actual amplitude/alpha/width and then decays. Duplicate event-driven history
emission is suppressed when the hull ribbon is current. Normal impact/skim solver events remain.

Query and raycast shares are reserved for live hulls within the existing total limits. Budget
shortfalls preserve an entire cached ribbon; confirmed dry/colliding samples still break it. The
aggressive 0.9.2 whole-span hull-overlap deletion is removed; hull occlusion uses depth, and extreme
turning overlaps remain an acceptance item. Stopped/unrefreshed ribbons release and fade. Teleport or
morph clears attachment/head connectivity. No changes were made to first-person item presentation.

Validation performed: static lifecycle, caller/signature and shader/resource JSON inspection only.
No Java compilation, game launch, simulation or performance tests were performed, per user request.

## 0.9.2 hull-length and turning wake correction

Attached water ribbons now sample both gunwales along the actual visible hull. Each station derives
velocity from current/previous pose-transformed local points, including rotation. Outward motion
strengthens the pushing side; retreating stations have weaker waves. Free historical ribbons are
emitted from the trailing end rather than the leading bow, including reverse travel. Existing free
ribbon endpoints/midpoints covered by the current hull are retired when it turns across them.

Attached ribbons interpolate prior/current geometry and validate outer endpoints/midpoints against
water and banks. Discontinuities clear attachment/head history. Work is bounded to eight nearby
hulls, seventeen stations per side, sixty-four live spans total and the existing query/raycast caps.
Extreme hulls are clipped to a camera-local length window. This is a visual contact approximation,
not a fluid boundary-condition solver. No game execution or performance measurement was performed.

## Open-source research

Looked up on GitHub on 2026-09-15:

- [evanw/webgl-water](https://github.com/evanw/webgl-water), revision
  `73eda8be832b649367b25ea5690c1f0181bb56ad`.
  [water.js](https://github.com/evanw/webgl-water/blob/73eda8be832b649367b25ea5690c1f0181bb56ad/water.js)
  stores height, vertical velocity and normal components, advances a damped neighbor wave field,
  and injects localized displacement. Both water.js and renderer.js explicitly state
  “Copyright 2011 Evan Wallace / Released under the MIT license”. GitHub's repository-level
  license metadata is empty; the source header is the evidence used here. The attribution and
  MIT terms are included in `src/main/resources/META-INF/licenses/webgl-water-MIT.txt` and ship in the JAR.
- [sirxemic/jquery.ripples](https://github.com/sirxemic/jquery.ripples): checked LICENSE (MIT,
  Copyright 2017 Pim Schreurs) and src/main.js. It also uses a damped height/velocity field
  and perturbs background texture coordinates to suggest refraction. No source from this project
  was copied into the mod.
- Also found cbiasco/shallow-water-simulation and several shallow-water GPU experiments through
  repository search; no code was incorporated from projects with unclear license metadata.

The Java simulation adapts the height/velocity concept, not a WebGL runtime. Minecraft-specific
masking, shader registration, collision-water queries, rendering and resource lifetime are implemented
here. No remote code is executed and no external runtime mod/library dependency is added.

## 0.9.1 changes (supersede the numeric parameters below)

- Navigation no longer emits overlapping fan-shaped sheets. Connected side-wake strips have a
  six-section curved water body and narrow raised foam crest; the body settles before residual
  surface foam fades out. A seamless-along-track ribbon texture replaces noisy fog-like overlap.
  Navigation emits no mist; sparse droplets appear only for stronger breaking wakes. ENTRY
  retains curved splash sheets and reserves mist for strong impacts.
- Ordinary bow wakes start at 0.12 blocks/tick, stop below 0.09 and scale softly with speed.
- Built-in entry detection uses underlying fluid height/UP normal rather than displaced slopes;
  this removes feedback where horizontal travel into a visual slope emits another ENTRY impulse.
- ENTRY/SKIM inputs feed a decaying pressure target, never modify rendered height instantaneously.
  The impact footprint extends along the previous tick's travel, bounded to five blocks.
- Hull footprints sweep the measured per-tick translation. Persistent world-cell pressure is
  loaded with 0.42 attack, reduced with 0.10 smoothing, held for eight ticks after forcing ends,
  then multiplied by 0.94/tick. Impact forcing decays by 0.90/tick. Overlapping fields are retained
  across patch shifts; teleport/morph changes still skip source submission.
- Wave speed is now 0.24 blocks/tick (CFL 0.16), base damping 0.18, restoring coefficient 0.065.
  Velocity is limited to +/-0.22 blocks/tick, depressions to local clearance or 1.6 blocks, crests
  to 0.9 blocks. The shader encoding remains +/-2. These are configuration values, not measured
  depth or duration guarantees.
- Splash sheets use correlated silhouette variation, curved subdivisions, continuous tear/fall
  fades and interpolated feet. Ordinary wakes use lower launch speeds and alpha. Foam follows
  deeper displaced heights. Query/raycast/pool caps remain, but extra subdivisions add draw work.

No runtime validation was performed for this revision. Check straight/reverse travel, turning wake
history, stopping/refill, repeated impacts, shallow floors and ordinary-speed wake continuity in game.

## Visual-only scope

All simulation, hull pressure, mesh displacement and slope shading are client-side. No server
forces, water blocks, swimming, underwater fog detection or boat buoyancy are changed. This is a
bounded height-field visual simulation, not a volumetric fluid solver. Parallax comes from actual
mesh displacement; no parallax-occlusion texture is used to fake a hole on a flat plane.

## Original water appearance

The 0.8.x physical-material shader has been removed: no screen refraction, sky reflection, Fresnel,
deep-blue absorption, extra glints or opaque scene composition. No scene FBO is allocated or copied.
The surface uses the animated vanilla water-still atlas sprite, local biome tint, vanilla lightmap,
UP face brightness, texture alpha, translucent blending and fog. UVs repeat once per block, use the
same sprite animation-frame inset as FluidRenderer, and stay fixed to integer world coordinates.
Explicit UV derivatives avoid artificial mip seams at each repeated block. Flat water uses the
vanilla color calculation. Deformed slopes get at most 12% symmetric darkening, with no one-sided
sun highlight. A zero slope has no additional shading.

Light coordinates match FluidRenderer's component-wise maximum of the water block and block above.
Tint, light coordinates and shallow-water depth limits refresh with the bounded row scan; the live
Minecraft lightmap supplies time-of-day/torch appearance. This is not a guarantee of exact output
with third-party renderers or resource packs altering the vanilla fluid rendering pipeline.

## Displacement, hull pressure and wakes

HeightfieldSurface owns one 48 x 48 block patch, with half-block samples (97 x 97 nodes). Only exposed,
flat 3 x 3 neighborhoods of source water are replaced. Flowing shores, waterlogged collision shapes
and other elevations remain vanilla. An eight-node distance-to-dry-edge ramp uses smoothstep to
flatten displacement near both patch borders and shore boundaries. Static vertices and zero-motion
material match the original surface level, including FluidRenderer's -0.001 block offset.

The wave solver uses three fixed substeps per tick. c=0.65, dt=1/3, dx=0.5 gives c*dt/dx=0.433,
below 1/sqrt(2). A damped height/velocity field propagates and superposes disturbances. ENTRY impulses
push water down locally and lift the surrounding ring; SKIM impulses remain weaker side disturbances.

The new acceptHull interface receives actual world-space keel endpoints and client-measured travel,
after teleport/morph discontinuities are excluded. Up to eight nearby hulls contribute pressure per
tick, regardless of spray event budget. Pressure produces a depressed keel footprint and raised
shoulders; a speed-dependent trough widens behind the current trailing end, including reverse travel.
The pressure profile has smooth spatial cutoffs and removes its discrete weighted mean before
accumulation. Height/velocity/depth clamps may introduce some volume error under extreme inputs.

Each substep applies wave * (laplacian(height) - laplacian(pressure)) and a weak restoring term toward
pressure. This sustains a hull-shaped displacement while contact continues. When a boat leaves or
slows, the reduced pressure lets the free wave field refill, rebound and decay. Pressure is rebuilt
from current hulls each tick, does not follow the camera, and is shifted with the overlapping field
when crossing patch origins. Sources affect simulation on the next client tick.

Static hulls have a small pressure footprint; speed scales its target depth from 0.16 toward 0.94
blocks before volume correction and solver response. These parameters are not measured output
amplitudes. Actual depressions are limited by the shallow-water floor clearance and a maximum of
1.25 blocks; crests are limited to 0.90 blocks, with stronger damping near the boundary. Height pairs
encode [-2, 2] into RG/BA 16-bit values, avoiding the old encoding's one-block clipping.

The spray threshold remains 0.48 blocks/tick with 0.44 hysteresis. Quiet hull pressure can deform the
surface without spraying water. Existing spray samples the same visual height field.

## Rendering and coordinates

WaterTerrainShaderMixin substitutes the translucent terrain shader while a patch is ready. It
recognizes water atlas UVs, horizontal top faces and the patch water level/mask, and discards those
original top fragments and their depth. All other terrain continues through the vanilla calculation.

A persistent 9,216-quad VBO draws the displaced surface at LAST before the existing late spray pass.
It uses normal alpha blending and depth writes. Both terrain masking and mesh use
clip = projection * view * (worldPosition - cameraPosition). The direct VBO draw explicitly receives
context.positionMatrix(), not the identity/local entity matrixStack(). Camera subtraction happens
once in PatchOrigin. This retains the 0.8.2 fix for the camera-following blue plane.

## Budgets and limitations

- One patch, one reusable static mesh, small height/mask/light textures; no per-frame scene copy.
- Three solver substeps; at most eight bounded pressure footprints per tick, independent of boat
  segment count. Other boats can still contribute through the existing bounded impact budget.
- Two source rows refreshed per tick; lighting and terrain changes can take about 1.3 seconds to
  propagate through the full patch. Full patch opening/shift still performs a bounded initial scan.
- Integer patch shifts preserve overlapping world-space fields. Waves outside the patch are lost;
  this is not an infinite ocean simulation. Idle patches close after 260 ticks without contact/impact.
- Sodium/Iris or -Dlongboatlab.disableWaterSurface=true still disables this vanilla-pipeline adapter.
- The late water mesh is not integrated into vanilla's per-chunk transparent sorting. Overlapping
  glass, underwater translucent blocks and Fabulous compositing still need in-game acceptance.
- Only one source-water elevation is active. No overturning waves, spray-fluid volume coupling,
  scene refraction or altered server buoyancy. Underwater fog still follows vanilla fluid blocks.

## Validation status and diagnostics

Static inspection used cached 1.21.1 FluidRenderer, WorldRenderer and ShaderProgram signatures and
bytecode, plus shader uniform/sampler/varying and JSON checks. No Java/GLSL compilation, simulation
execution, performance test or Minecraft launch was performed for 0.9.0, as requested by the user.

The previous runtime log confirmed loading and draw submission in 0.8.1; it is not visual evidence
for this revision. The former GLSL `flat` reserved-name and direct-VBO view-matrix bugs remain fixed.
Loading/fallback messages and the first draw submission are recorded under LongboatLab/Water.

Game acceptance should compare flat patch edges to untouched water by day/night and torch light;
rotate a stationary third-person camera; inspect a fast boat's trailing trough, side shoulders,
reverse travel, stopping/refill, impact waves, shallows, world changes and water-level transitions.
