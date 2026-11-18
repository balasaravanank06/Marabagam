import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from '../app/App';

describe('Frontend Shell Smoke Test', () => {
  it('renders Marabagam header title', () => {
    render(<App />);
    const elements = screen.getAllByText(/MARABAGAM/i);
    expect(elements.length).toBeGreaterThan(0);
  });

  it('renders development environment ready heading', () => {
    render(<App />);
    expect(screen.getByText(/Marabagam Development Environment Ready/i)).toBeInTheDocument();
  });
});
