// Draws the showcase icon's "d" with the library's own stroke engine, so the
// glyph is sketched exactly the way a button's border is.
//
//   node gen-icon.mjs sweep      # variants to look at
//   node gen-icon.mjs emit       # the two final SVGs
//
// The glyph is defined in a 100-unit em box and mapped onto whatever canvas is
// asked for. Roughness is an absolute amplitude in the engine, so it is scaled
// with the glyph — that is what keeps the wobble looking the same at any size.

import { roughCircle, roughLine } from "drawably";
import { writeFileSync } from "node:fs";

const INK = "#2724D1";
const PAPER = "#FFFFFF";

// em box: bowl on the left, stem on the right, content spanning y 8..92
const EM = {
  content: { height: 84, centreX: 50, centreY: 50 },
  bowl: { cx: 50, cy: 66, r: 26 },
  stem: { x: 76, top: 8, bottom: 92 },
};

function glyph({ canvas, contentHeight, stroke, seed, roughness }) {
  const k = contentHeight / EM.content.height;
  const map = (x, y) => [
    canvas / 2 + (x - EM.content.centreX) * k,
    canvas / 2 + (y - EM.content.centreY) * k,
  ];
  const [bowlX, bowlY] = map(EM.bowl.cx, EM.bowl.cy);
  const [stemX, stemTop] = map(EM.stem.x, EM.stem.top);
  const [, stemBottom] = map(EM.stem.x, EM.stem.bottom);
  const o = { seed, roughness: roughness * k };
  return {
    bowl: roughCircle(bowlX, bowlY, EM.bowl.r * k, o),
    stem: roughLine(stemX, stemTop, stemX, stemBottom, { ...o, seed: seed + 1 }),
    strokeWidth: +(stroke * k).toFixed(2),
  };
}

function svg(options, { background = true } = {}) {
  const { canvas } = options;
  const { bowl, stem, strokeWidth } = glyph(options);
  const paper = background
    ? `\n  <rect width="${canvas}" height="${canvas}" fill="${PAPER}"/>`
    : "";
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${canvas} ${canvas}" width="1024" height="1024">${paper}
  <g fill="none" stroke="${INK}" stroke-width="${strokeWidth}" stroke-linecap="round" stroke-linejoin="round">
    <path d="${bowl}"/>
    <path d="${stem}"/>
  </g>
</svg>`;
}

// An adaptive icon's foreground is a 108 canvas whose guaranteed area is a
// 66-wide circle, so the glyph is sized to fit inside it.
const ANDROID = { canvas: 108, contentHeight: 46, stroke: 10, seed: 19, roughness: 2.2 };
// An iOS icon is masked to a rounded square, so the glyph can fill much more.
const IOS = { canvas: 108, contentHeight: 64, stroke: 10, seed: 19, roughness: 2.2 };

if (process.argv[2] === "seeds") {
  for (const seed of [3, 7, 11, 19, 23, 42, 77, 101, 404]) {
    writeFileSync(`seed-${seed}.svg`, svg({ ...ANDROID, seed }));
  }
  console.log("seeds écrits");
}

if (process.argv[2] === "paths") {
  const { bowl, stem, strokeWidth } = glyph(ANDROID);
  writeFileSync("android-paths.json", JSON.stringify({ bowl, stem, strokeWidth }, null, 1));
  console.log("paths écrits");
}

if (process.argv[2] === "emit") {
  writeFileSync("icon-android-foreground.svg", svg(ANDROID, { background: false }));
  writeFileSync("icon-android.svg", svg(ANDROID));
  writeFileSync("icon-ios.svg", svg(IOS));
  console.log("écrits");
}
