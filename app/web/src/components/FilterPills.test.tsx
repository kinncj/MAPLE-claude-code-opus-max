import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { FilterPills } from './FilterPills';

describe('FilterPills', () => {
  it('marks the selected option with aria-checked', () => {
    render(<FilterPills value="all" onChange={() => {}} />);
    expect(screen.getByRole('radio', { name: 'All' })).toHaveAttribute('aria-checked', 'true');
    expect(screen.getByRole('radio', { name: 'Todo' })).toHaveAttribute('aria-checked', 'false');
  });

  it('calls onChange on click', async () => {
    const onChange = vi.fn();
    render(<FilterPills value="all" onChange={onChange} />);
    await userEvent.click(screen.getByRole('radio', { name: 'Done' }));
    expect(onChange).toHaveBeenCalledWith('done');
  });

  it('navigates with arrow keys', async () => {
    const onChange = vi.fn();
    render(<FilterPills value="all" onChange={onChange} />);
    screen.getByRole('radio', { name: 'All' }).focus();
    await userEvent.keyboard('{ArrowRight}');
    expect(onChange).toHaveBeenCalledWith('todo');
  });

  it('exposes a radiogroup', () => {
    render(<FilterPills value="todo" onChange={() => {}} />);
    expect(screen.getByRole('radiogroup', { name: 'Filter tasks' })).toBeInTheDocument();
  });
});
