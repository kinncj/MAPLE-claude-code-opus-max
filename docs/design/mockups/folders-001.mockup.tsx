/*
 * High-fidelity mockup for story folders-001 — TODO SPA Folders feature.
 * status: approved
 * Presentational reference only (static data). The production components under
 * app/web/src are derived from this layout and consume docs/design/identity/tokens.css.
 */
import { useState } from 'react';

type Filter = 'all' | 'todo' | 'done';
type Task = { id: string; title: string; done: boolean; folder: string };

const FOLDERS = [
  { name: 'General', count: 3, isDefault: true },
  { name: 'Work', count: 1, isDefault: false },
  { name: 'Groceries', count: 2, isDefault: false },
];
const TASKS: Task[] = [
  { id: '1', title: 'Buy milk', done: false, folder: 'General' },
  { id: '2', title: 'Walk dog', done: true, folder: 'General' },
  { id: '3', title: 'Write report', done: false, folder: 'General' },
];

export function FoldersMockup() {
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [active, setActive] = useState('General');
  const [filter, setFilter] = useState<Filter>('all');
  const [confirming, setConfirming] = useState<string | null>('Work');

  const isDefault = FOLDERS.find((f) => f.name === active)?.isDefault;

  return (
    <div data-theme={theme} className="app">
      <header>
        <h1>Tasks</h1>
        <button
          className="theme-btn"
          aria-label="Toggle dark theme"
          aria-pressed={theme === 'dark'}
          onClick={() => setTheme((t) => (t === 'dark' ? 'light' : 'dark'))}
        >
          {theme === 'dark' ? '☾' : '☀'}
        </button>
      </header>

      <div className="grid">
        <aside aria-label="Folders">
          <h2>Folders</h2>
          {FOLDERS.map((f) => (
            <button
              key={f.name}
              className={`folder${f.name === active ? ' active' : ''}`}
              aria-current={f.name === active}
              onClick={() => setActive(f.name)}
            >
              <span>
                {f.name === active && <span className="star" aria-hidden>★</span>}
                {f.name}
              </span>
              <span className="badge">{f.count}</span>
            </button>
          ))}
          <div className="spacer" />
          <button className="side-btn">+ New folder</button>
          <button className="side-btn">▼ All folders</button>
        </aside>

        <main>
          <div className="addrow">
            <input placeholder={`Add new task in ${active} …`} aria-label={`Add new task in ${active}`} />
            <button className="plus" aria-label="Add task">+</button>
          </div>

          <div className="toolbar" role="radiogroup" aria-label="Filter tasks">
            <div className="pills">
              {(['all', 'todo', 'done'] as Filter[]).map((f) => (
                <button
                  key={f}
                  className="pill"
                  role="radio"
                  aria-checked={filter === f}
                  onClick={() => setFilter(f)}
                >
                  {f[0].toUpperCase() + f.slice(1)}
                </button>
              ))}
            </div>
            {!isDefault && <button className="del-folder" onClick={() => setConfirming(active)}>Delete folder</button>}
          </div>

          <div className="list">
            {TASKS.map((t) => (
              <div key={t.id} className={`task${t.done ? ' done' : ''}`}>
                <input type="checkbox" defaultChecked={t.done} aria-label={`Mark ${t.title} done`} />
                <span className="title">{t.title}</span>
                <button className="del" aria-label={`Delete ${t.title}`}>Del</button>
              </div>
            ))}
            {confirming && (
              <div className="confirm" role="alertdialog" aria-label={`Confirm delete folder ${confirming}`}>
                <span className="grow">Delete folder "{confirming}"? Tasks move to General.</span>
                <button className="danger">Confirm</button>
                <button autoFocus onClick={() => setConfirming(null)}>Cancel</button>
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}
