import type { Theme } from '../hooks/useTheme';

interface Props {
  theme: Theme;
  onToggle: () => void;
}

export function ThemeToggle({ theme, onToggle }: Props) {
  const dark = theme === 'dark';
  return (
    <button
      type="button"
      className="theme-btn"
      onClick={onToggle}
      aria-label="Toggle dark theme"
      aria-pressed={dark}
      title={dark ? 'Switch to light theme' : 'Switch to dark theme'}
    >
      <span aria-hidden="true">{dark ? '☾' : '☀'}</span>
    </button>
  );
}
