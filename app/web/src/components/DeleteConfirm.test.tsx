import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DeleteConfirm } from './DeleteConfirm';

describe('DeleteConfirm', () => {
  it('renders the exact confirmation message', () => {
    render(<DeleteConfirm folderName="Work" onConfirm={() => {}} onCancel={() => {}} />);
    expect(screen.getByRole('alertdialog')).toHaveTextContent(
      'Delete folder "Work"? Tasks move to General.',
    );
  });

  it('focuses Cancel (the safe default) on open', () => {
    render(<DeleteConfirm folderName="Work" onConfirm={() => {}} onCancel={() => {}} />);
    expect(screen.getByRole('button', { name: 'Cancel' })).toHaveFocus();
  });

  it('confirms and cancels via buttons', async () => {
    const onConfirm = vi.fn();
    const onCancel = vi.fn();
    render(<DeleteConfirm folderName="Work" onConfirm={onConfirm} onCancel={onCancel} />);
    await userEvent.click(screen.getByRole('button', { name: 'Confirm' }));
    expect(onConfirm).toHaveBeenCalled();
    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(onCancel).toHaveBeenCalled();
  });

  it('cancels on Escape', async () => {
    const onCancel = vi.fn();
    render(<DeleteConfirm folderName="Work" onConfirm={() => {}} onCancel={onCancel} />);
    await userEvent.keyboard('{Escape}');
    expect(onCancel).toHaveBeenCalled();
  });
});
