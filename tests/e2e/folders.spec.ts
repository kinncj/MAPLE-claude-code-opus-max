import { test, expect, type Page } from '@playwright/test';

/**
 * E2E coverage for story folders-001. Each test maps 1:1 to a Gherkin scenario in
 * tests/features/folders-001-folder-organization.feature. State is reset to the canonical
 * Background (General=3, Work=1, Groceries=2) before each test via the test-support endpoint.
 */

const folder = (page: Page, name: string) =>
  page.getByRole('button', { name: new RegExp(`^${name},`) });

const deleteFolderBtn = (page: Page, name: string) =>
  page.getByRole('button', { name: `Delete folder ${name}` });

test.beforeEach(async ({ page, request }) => {
  await request.post('/api/v1/test/reset');
  await page.goto('/');
  await expect(folder(page, 'General')).toBeVisible();
});

// Scenario: Active folder is highlighted with count
test('active folder is highlighted, starred, and shows counts', async ({ page }) => {
  await expect(folder(page, 'General')).toHaveAttribute('aria-current', 'true');
  await expect(folder(page, 'General')).toContainText('★');
  await expect(folder(page, 'General')).toHaveAccessibleName(/General, 3 tasks/);
  await expect(folder(page, 'Work')).toHaveAccessibleName(/Work, 1 task/);
  await expect(folder(page, 'Groceries')).toHaveAccessibleName(/Groceries, 2 tasks/);
});

// Scenario: Selecting a folder scopes the list and input
test('selecting a folder scopes the list and the add-task placeholder', async ({ page }) => {
  await folder(page, 'Work').click();
  await expect(page.getByText('Prepare release deck')).toBeVisible();
  await expect(page.getByText('Buy milk')).toHaveCount(0);
  await expect(page.getByPlaceholder('Add new task in Work ...')).toBeVisible();
});

// Scenario: Add a task to the active folder
test('adds a task to the active folder and updates the count', async ({ page }) => {
  await folder(page, 'Work').click();
  await page.getByPlaceholder('Add new task in Work ...').fill('Ship release');
  await page.getByRole('button', { name: 'Add task' }).click();
  await expect(page.getByText('Ship release')).toBeVisible();
  await expect(folder(page, 'Work')).toHaveAccessibleName(/Work, 2 tasks/);
});

// Scenario: Toggle task completion
test('toggles task completion', async ({ page }) => {
  const checkbox = page.getByRole('checkbox', { name: 'Mark "Buy milk" done' });
  await expect(checkbox).not.toBeChecked();
  await checkbox.check();
  await expect(page.getByRole('checkbox', { name: 'Mark "Buy milk" not done' })).toBeChecked();
});

// Scenario: Delete a task
test('deletes a task and decrements the folder count', async ({ page }) => {
  await page.getByRole('button', { name: 'Delete "Walk dog"' }).click();
  await expect(page.getByText('Walk dog')).toHaveCount(0);
  await expect(folder(page, 'General')).toHaveAccessibleName(/General, 2 tasks/);
});

// Scenario: Filter by completion state
test('filters by completion state', async ({ page }) => {
  await page.getByRole('radio', { name: 'Done' }).click();
  await expect(page.getByText('Walk dog')).toBeVisible();
  await expect(page.getByText('Buy milk')).toHaveCount(0);

  await page.getByRole('radio', { name: 'Todo' }).click();
  await expect(page.getByText('Buy milk')).toBeVisible();
  await expect(page.getByText('Write report')).toBeVisible();
  await expect(page.getByText('Walk dog')).toHaveCount(0);
});

// Scenario: Delete a folder reassigns tasks to General
test('deletes a folder and reassigns its tasks to General', async ({ page }) => {
  await folder(page, 'Work').click();
  await deleteFolderBtn(page, 'Work').click();
  await expect(page.getByRole('alertdialog')).toContainText(
    'Delete folder "Work"? Tasks move to General.',
  );
  await page.getByRole('button', { name: 'Confirm' }).click();

  await expect(folder(page, 'Work')).toHaveCount(0);
  await expect(folder(page, 'General')).toHaveAttribute('aria-current', 'true');
  await expect(folder(page, 'General')).toHaveAccessibleName(/General, 4 tasks/);
});

// Scenario: Cancel folder deletion
test('cancels folder deletion leaving the folder unchanged', async ({ page }) => {
  await folder(page, 'Work').click();
  await deleteFolderBtn(page, 'Work').click();
  await page.getByRole('button', { name: 'Cancel' }).click();
  await expect(page.getByRole('alertdialog')).toHaveCount(0);
  await expect(folder(page, 'Work')).toHaveAccessibleName(/Work, 1 task/);
  await expect(page.getByText('Prepare release deck')).toBeVisible();
});

// Scenario: General cannot be deleted
test('does not offer a delete action for General', async ({ page }) => {
  await expect(folder(page, 'General')).toHaveAttribute('aria-current', 'true');
  await expect(page.getByRole('button', { name: /Delete folder/ })).toHaveCount(0);
});

// Scenario: Create a new folder
test('creates a new folder that becomes active with count 0', async ({ page }) => {
  await page.getByRole('button', { name: '+ New folder' }).click();
  await page.getByLabel('New folder name').fill('Reading');
  await page.getByRole('button', { name: 'Create' }).click();
  await expect(folder(page, 'Reading')).toHaveAccessibleName(/Reading, 0 tasks/);
  await expect(folder(page, 'Reading')).toHaveAttribute('aria-current', 'true');
});

// Scenario: Reject invalid folder names
test('rejects empty and duplicate folder names with inline validation', async ({ page }) => {
  await page.getByRole('button', { name: '+ New folder' }).click();
  await page.getByRole('button', { name: 'Create' }).click(); // empty
  await expect(page.getByRole('alert')).toBeVisible();
  await expect(folder(page, 'Reading')).toHaveCount(0);

  await page.getByLabel('New folder name').fill('work'); // duplicate (case-insensitive)
  await page.getByRole('button', { name: 'Create' }).click();
  await expect(page.getByRole('alert')).toContainText(/already exists/i);
});

// Scenario: Theme toggle persists
test('persists the theme across reloads', async ({ page }) => {
  await page.getByRole('button', { name: 'Toggle dark theme' }).click();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.reload();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
});

// Scenario: View all folders
test('shows all folders aggregate, labeled by folder, with delete hidden', async ({ page }) => {
  await page.getByRole('button', { name: '▼ All folders' }).click();
  await expect(page.getByText('Buy milk')).toBeVisible();
  const row = page.locator('li.task', { hasText: 'Prepare release deck' });
  await expect(row.getByText('Work')).toBeVisible();
  await expect(page.getByRole('button', { name: /Delete folder/ })).toHaveCount(0);
});
