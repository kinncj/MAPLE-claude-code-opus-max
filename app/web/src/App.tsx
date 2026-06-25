import { useTheme } from './hooks/useTheme';
import { TodoProvider, useTodo } from './state/TodoProvider';
import { Sidebar } from './components/Sidebar';
import { RightPanel } from './components/RightPanel';
import { ThemeToggle } from './components/ThemeToggle';

function AppBody() {
  const { state } = useTodo();
  return (
    <div className="app-grid">
      <Sidebar />
      {state.status === 'error' ? (
        <main className="right-panel">
          <p role="alert" className="empty-state">
            Could not load your tasks. Is the API running?
          </p>
        </main>
      ) : (
        <RightPanel />
      )}
    </div>
  );
}

export function App() {
  const [theme, toggleTheme] = useTheme();
  return (
    <TodoProvider>
      <div className="app">
        <header className="app-header">
          <h1 className="app-title">Tasks</h1>
          <ThemeToggle theme={theme} onToggle={toggleTheme} />
        </header>
        <AppBody />
      </div>
    </TodoProvider>
  );
}
