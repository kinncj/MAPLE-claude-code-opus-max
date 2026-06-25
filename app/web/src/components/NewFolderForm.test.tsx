import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { NewFolderForm } from './NewFolderForm';

describe('NewFolderForm', () => {
  it('submits the entered name', async () => {
    const onSubmit = vi.fn().mockResolvedValue(true);
    render(<NewFolderForm error={null} onSubmit={onSubmit} onCancel={() => {}} />);
    await userEvent.type(screen.getByLabelText('New folder name'), 'Reading');
    await userEvent.click(screen.getByRole('button', { name: 'Create' }));
    expect(onSubmit).toHaveBeenCalledWith('Reading');
  });

  it('shows a validation error and marks the input invalid', () => {
    render(
      <NewFolderForm error='A folder named "work" already exists.' onSubmit={vi.fn()} onCancel={() => {}} />,
    );
    expect(screen.getByRole('alert')).toHaveTextContent('already exists');
    expect(screen.getByLabelText('New folder name')).toHaveAttribute('aria-invalid', 'true');
  });

  it('cancels on Escape', async () => {
    const onCancel = vi.fn();
    render(<NewFolderForm error={null} onSubmit={vi.fn()} onCancel={onCancel} />);
    screen.getByLabelText('New folder name').focus();
    await userEvent.keyboard('{Escape}');
    expect(onCancel).toHaveBeenCalled();
  });
});
