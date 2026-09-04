// Generates golden fixtures from the upstream JS library so the Swift and
// Kotlin ports can be asserted byte-for-byte against it.
//
//   npm i drawably@0.3.10 && node gen-goldens.mjs > goldens.json
//
// The control-layer section mirrors the layer `gen` closures in
// drawably/dist/controls.js verbatim, so the ports' component geometry
// constants are covered too, not just the raw primitives.

import {
  mulberry32,
  roughArrow,
  roughCheckmark,
  roughCircle,
  roughEllipse,
  roughLine,
  roughRoundedRect,
  scribbleFill,
  variants,
} from "drawably";

const UPSTREAM = "drawably@0.3.10";

// ---------------------------------------------------------------- prng

const PRNG_SEEDS = [0, 1, 42, 1337, 0xdeadbeef, 0xffffffff];

const prng = PRNG_SEEDS.map((seed) => {
  const rand = mulberry32(seed);
  return { seed, values: Array.from({ length: 20 }, () => rand()) };
});

// ------------------------------------------------------------- matrix

const SEEDS = [1, 42, 0xdeadbeef];
const ROUGHNESSES = [0, 0.5, 1, 2];
const BOILS = [0, 0.3];

function* optionMatrix(seeds = SEEDS, roughnesses = ROUGHNESSES) {
  for (const seed of seeds) {
    for (const roughness of roughnesses) {
      for (const boil of BOILS) {
        yield { seed, roughness, boil };
      }
    }
  }
}

// One seed is enough here: PRNG behaviour across seeds is already pinned by
// the `shapes` section, and what these cases pin is each control's geometry.
// The composite-layer cases replay the same engine as the primitives, so a
// narrower matrix there keeps the committed fixture small without losing
// coverage of the thing it actually pins down: each control's geometry.
const CONTROL_SEEDS = [42];
const CONTROL_ROUGHNESSES = [0, 1, 2];
const controlMatrix = () => optionMatrix(CONTROL_SEEDS, CONTROL_ROUGHNESSES);

// A bare RoughOptions has no boilSeed, so boilPass is a no-op; `variants`
// is what injects it. Both paths are exercised: `shapes` covers the raw
// generators, `variants`/`controls` cover the boil-frame wrapper.
const shapes = [];
const primitives = {
  roughLine: [
    [0, 0, 100, 0],
    [3, 18, 117, 18],
    [10, 4, 96, 52],
    [0, 0, 0, 40],
    [50, 50, 50, 50], // degenerate: zero length
  ],
  roughRoundedRect: [
    [3, 3, 114, 30, 8],
    [3, 3, 16, 16, 5],
    [-1, -1, 122, 38, 10],
    [0, 0, 20, 20, 40], // radius clamped to min(w/2, h/2)
  ],
  roughCircle: [
    [11, 11, 8],
    [12, 12, 3.96],
    [11, 11, 12],
  ],
  roughEllipse: [
    [40, 12, 50, 12],
    [40, 12, 27, 12.4],
    [10, 10, 1, 1], // tiny: sample count floor of 8
  ],
  roughArrow: [
    [0, 0, 120, 60],
    [120, 60, 0, 0],
    [10, 10, 10, 90],
  ],
  roughCheckmark: [
    [5.28, 4.4, 11.44, 11],
    [2, 2, 10, 10],
    [-18, 6, 10, 10],
  ],
  scribbleFill: [
    [5, 5, 110, 26],
    [0, 0, 60, 20],
    [4, 4, 2, 2], // narrower than the 6px gap: empty path
  ],
};

const FNS = {
  roughLine,
  roughRoundedRect,
  roughCircle,
  roughEllipse,
  roughArrow,
  roughCheckmark,
  scribbleFill,
};

for (const [fn, argsets] of Object.entries(primitives)) {
  for (const args of argsets) {
    for (const opts of optionMatrix()) {
      // the raw generators ignore boil without a boilSeed; keep one entry
      // per (seed, roughness) rather than duplicating across boil values
      if (opts.boil !== BOILS[0]) continue;
      shapes.push({
        fn,
        args,
        opts: { seed: opts.seed, roughness: opts.roughness },
        d: FNS[fn](...args, { seed: opts.seed, roughness: opts.roughness }),
      });
    }
  }
}

// ----------------------------------------------------------- variants

const variantCases = [];
for (const opts of controlMatrix()) {
  variantCases.push({
    fn: "roughRoundedRect",
    args: [3, 3, 114, 30, 8],
    opts,
    n: opts.boil ? 3 : 1,
    ds: variants((o) => roughRoundedRect(3, 3, 114, 30, 8, o), opts, opts.boil ? 3 : 1),
  });
  variantCases.push({
    fn: "roughCheckmark",
    args: [5.28, 4.4, 11.44, 11],
    opts,
    n: opts.boil ? 3 : 1,
    ds: variants((o) => roughCheckmark(5.28, 4.4, 11.44, 11, o), opts, opts.boil ? 3 : 1),
  });
  variantCases.push({
    fn: "scribbleFill",
    args: [5, 5, 110, 26],
    opts,
    n: opts.boil ? 3 : 1,
    ds: variants((o) => scribbleFill(5, 5, 110, 26, o), opts, opts.boil ? 3 : 1),
  });
}

// ----------------------------------------------------------- controls
// Verbatim transcription of the layer `gen` closures in controls.js.

const INSET = 3;
const outlineRect = (r) => (w, h, o) => roughRoundedRect(INSET, INSET, w - 2 * INSET, h - 2 * INSET, r, o);
const focusRect = (r) => (w, h, o) => roughRoundedRect(-1, -1, w + 2, h + 2, r, o);

const CHEVRON_W = 12;
const CHEVRON_H = 6;
const CHEVRON_RIGHT = 12;
const CHEVRON_ROUGHNESS = 0.4;
const UNDERLINE_GAP = 2;
const CIRCLE_PAD_X = 1.15;
const CIRCLE_PAD_Y = 1.4;
const CIRCLE_PAD = 4;
const MARKER_LEFT = -18;
const MARKER_W = 10;
const MARKER_LINE = 22;
const CHECK_BOX = 14;
const CHECK_INSET = 2;

const layers = {
  "button.outline": outlineRect(8),
  "button.blob": outlineRect(8),
  "button.scribble": (w, h, o) =>
    scribbleFill(INSET + 2, INSET + 2, w - 2 * INSET - 4, h - 2 * INSET - 4, o),
  "button.focus": focusRect(10),
  "card.outline": outlineRect(10),
  "checkbox.outline": outlineRect(5),
  "checkbox.check": (w, h, o) => roughCheckmark(w * 0.24, h * 0.2, w * 0.52, h * 0.5, o),
  "checkbox.focus": focusRect(7),
  "radio.outline": (w, h, o) => roughCircle(w / 2, h / 2, Math.min(w, h) / 2 - INSET, o),
  "radio.dot": (w, h, o) => roughCircle(w / 2, h / 2, Math.min(w, h) * 0.18, o),
  "radio.focus": (w, h, o) => roughCircle(w / 2, h / 2, Math.min(w, h) / 2 + 1, o),
  "toggle.outline": (w, h, o) => outlineRect((h - 2 * INSET) / 2)(w, h, o),
  "toggle.knob": (_w, h, o) => roughCircle(h / 2, h / 2, h / 2 - INSET - 3, o),
  "toggle.focus": focusRect(12),
  "divider.outline": (w, h, o) => roughLine(INSET, h / 2, w - INSET, h / 2, o),
  "field.outline": outlineRect(6),
  "field.focus": focusRect(8),
  "select.chevron": (w, h, o) => {
    const x = w - CHEVRON_RIGHT - CHEVRON_W;
    const y = h / 2 - CHEVRON_H / 2;
    const co = { ...o, roughness: o.roughness * CHEVRON_ROUGHNESS };
    return (
      roughLine(x, y, x + CHEVRON_W / 2, y + CHEVRON_H, co) +
      roughLine(x + CHEVRON_W / 2, y + CHEVRON_H, x + CHEVRON_W, y, { ...co, seed: o.seed + 1 })
    );
  },
  "select.checkMask": (_w, _h, o) =>
    roughCheckmark(CHECK_INSET, CHECK_INSET, CHECK_BOX - CHECK_INSET * 2, CHECK_BOX - CHECK_INSET * 2, o),
  "badge.outline": outlineRect(2),
  "badge.scribble": (w, h, o) =>
    scribbleFill(INSET + 1, INSET + 1, w - 2 * INSET - 2, h - 2 * INSET - 2, o),
  "list.dash": (_w, _h, o) =>
    roughLine(MARKER_LEFT, MARKER_LINE / 2, MARKER_LEFT + MARKER_W, MARKER_LINE / 2, o),
  "list.check": (_w, _h, o) =>
    roughCheckmark(MARKER_LEFT, MARKER_LINE / 2 - MARKER_W / 2, MARKER_W, MARKER_W, o),
  "underline.outline": (w, h, o) => roughLine(0, h + UNDERLINE_GAP, w, h + UNDERLINE_GAP, o),
  "highlight.wash": (w, h, o) => scribbleFill(0, 0, w, h, o),
  "circle.outline": (w, h, o) =>
    roughEllipse(w / 2, h / 2, (w / 2) * CIRCLE_PAD_X + CIRCLE_PAD, (h / 2) * CIRCLE_PAD_Y + CIRCLE_PAD, o),
};

// representative box sizes, matching the CSS defaults where there are any
const boxes = {
  button: [120, 36],
  card: [160, 100],
  checkbox: [22, 22],
  radio: [22, 22],
  toggle: [44, 24],
  divider: [200, 10],
  field: [220, 38],
  select: [220, 38],
  badge: [56, 20],
  list: [200, 22],
  underline: [140, 20],
  highlight: [140, 20],
  circle: [140, 20],
};

const controls = [];
for (const [name, gen] of Object.entries(layers)) {
  const [w, h] = boxes[name.split(".")[0]];
  for (const opts of controlMatrix()) {
    controls.push({
      layer: name,
      w,
      h,
      opts,
      ds: variants((o) => gen(w, h, o), opts, opts.boil ? 3 : 1),
    });
  }
}

// the arrow layer takes absolute endpoints rather than a box
const arrows = [];
for (const opts of controlMatrix()) {
  arrows.push({
    layer: "arrow.outline",
    args: [30, 20, 190, 96],
    opts,
    ds: variants((o) => roughArrow(30, 20, 190, 96, o), opts, opts.boil ? 3 : 1),
  });
}

// ------------------------------------------------------------- output

process.stdout.write(
  JSON.stringify(
    { upstream: UPSTREAM, prng, shapes, variants: variantCases, controls, arrows },
  ) + "\n",
);
