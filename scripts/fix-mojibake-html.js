/**
 * Fix common UTF-8 mojibake in static HTML (files saved with wrong decoding).
 * Run: node scripts/fix-mojibake-html.js
 */const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..', 'frontend');

const replacements = [
  ['\u00e2\u20ac\u201d', '\u2014'], // em dash — (UTF-8 E2 80 94 mis-saved)
  ['\u00e2\u20ac\u201c', '\u2013'], // en dash – (UTF-8 E2 80 93)
  ['\u00e2\u20ac\u00a6', '\u2026'], // ellipsis …
  ['\u00e2\u201d\u20ac', '\u2500'], // box light horizontal (UTF-8 E2 94 80) in comments
  ['\u00e2\u201a\u00ac', '\u20ac'], // euro € (UTF-8 E2 82 AC)
  ['\u00e2\u0153\u00a6', '\u2726'], // ✦ (UTF-8 E2 9C A6)
];

function walk(dir) {
  for (const ent of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, ent.name);
    if (ent.isDirectory()) walk(p);
    else if (ent.name.endsWith('.html')) fixFile(p);
  }
}

function fixFile(p) {
  let s = fs.readFileSync(p, 'utf8');
  const orig = s;
  for (const [bad, good] of replacements) {
    s = s.split(bad).join(good);
  }
  if (s !== orig) {
    fs.writeFileSync(p, s, 'utf8');
    console.log('fixed', p);
  }
}

walk(root);
