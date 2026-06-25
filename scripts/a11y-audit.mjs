// WCAG 2.2 AA accessibility audit for the TODO SPA (story folders-001).
// Runs axe-core against the running app in light + dark themes and the delete-confirm state.
// Writes docs/design/mockups/folders-001.a11y.json and screenshots. Exits non-zero on
// critical/serious violations. Intended to run in the Playwright Docker image with --network host.
import { chromium } from '@playwright/test';
import { AxeBuilder } from '@axe-core/playwright';
import { mkdirSync, writeFileSync } from 'node:fs';

const BASE = process.env.BASE_URL ?? 'http://localhost:8080';
const TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'];
const OUT = 'docs/design/mockups';
mkdirSync(OUT, { recursive: true });

async function analyze(page, label) {
  const results = await new AxeBuilder({ page }).withTags(TAGS).analyze();
  const summary = results.violations.map((v) => ({
    id: v.id,
    impact: v.impact,
    help: v.help,
    nodes: v.nodes.length,
  }));
  console.log(`[${label}] violations: ${results.violations.length}`);
  for (const v of summary) console.log(`  - ${v.impact}: ${v.id} (${v.nodes})`);
  return { state: label, violations: summary };
}

const browser = await chromium.launch();
const ctx = await browser.newContext();
const page = await ctx.newPage();

// Deterministic Background.
await page.request.post(`${BASE}/api/v1/test/reset`);

const report = { tool: 'axe-core', standard: 'WCAG 2.2 AA', generatedFor: 'folders-001', states: [] };

// Light, default view.
await page.goto(BASE);
await page.getByRole('button', { name: /^General,/ }).waitFor();
report.states.push(await analyze(page, 'light-default'));
await page.screenshot({ path: `${OUT}/folders-001.light.png`, fullPage: true });

// Delete-confirmation open (alertdialog) — audit the dialog + focus state.
await page.getByRole('button', { name: /^Work,/ }).click();
await page.getByRole('button', { name: 'Delete folder Work' }).click();
await page.getByRole('alertdialog').waitFor();
report.states.push(await analyze(page, 'light-delete-confirm'));
await page.getByRole('button', { name: 'Cancel' }).click();

// Dark theme.
await page.getByRole('button', { name: 'Toggle dark theme' }).click();
await page.waitForFunction(() => document.documentElement.getAttribute('data-theme') === 'dark');
report.states.push(await analyze(page, 'dark-default'));
await page.screenshot({ path: `${OUT}/folders-001.dark.png`, fullPage: true });

await browser.close();

const all = report.states.flatMap((s) => s.violations);
const blocking = all.filter((v) => v.impact === 'critical' || v.impact === 'serious');
report.totalViolations = all.length;
report.blocking = blocking.length;
report.passed = blocking.length === 0;
writeFileSync(`${OUT}/folders-001.a11y.json`, JSON.stringify(report, null, 2));

console.log(`\nTotal violations: ${all.length}; blocking (critical/serious): ${blocking.length}`);
if (blocking.length > 0) {
  console.error('A11Y AUDIT FAILED — critical/serious violations present.');
  process.exit(1);
}
console.log('A11Y AUDIT PASSED — no critical/serious violations.');
