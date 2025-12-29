import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Autocomplete from '../app/components/Autocomplete';
import userEvent from '@testing-library/user-event';

// Mock fetch
global.fetch = jest.fn();

describe('Autocomplete Component', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });
    jest.clearAllMocks();
  });

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  );

  test('renders input field with placeholder', () => {
    const mockFetch = jest.fn().mockResolvedValue([]);
    
    render(
      <Autocomplete
        value=""
        onChange={jest.fn()}
        fetchOptions={mockFetch}
        placeholder="Search airports..."
      />,
      { wrapper }
    );

    const input = screen.getByPlaceholderText('Search airports...');
    expect(input).toBeInTheDocument();
  });

  test('shows label when provided', () => {
    const mockFetch = jest.fn().mockResolvedValue([]);
    
    render(
      <Autocomplete
        value=""
        onChange={jest.fn()}
        fetchOptions={mockFetch}
        label="Airport"
        placeholder="Search..."
      />,
      { wrapper }
    );

    expect(screen.getByText('Airport')).toBeInTheDocument();
  });

  test('calls fetchOptions after debounce delay', async () => {
    const mockFetch = jest.fn().mockResolvedValue([
      { value: 'LAX', label: 'Los Angeles', sublabel: 'USA' }
    ]);
    
    const user = userEvent.setup();
    
    render(
      <Autocomplete
        value=""
        onChange={jest.fn()}
        fetchOptions={mockFetch}
        placeholder="Search..."
        debounceMs={300}
        minChars={2}
      />,
      { wrapper }
    );

    const input = screen.getByPlaceholderText('Search...');
    await user.type(input, 'LA');

    // Should not call immediately
    expect(mockFetch).not.toHaveBeenCalled();

    // Should call after debounce delay
    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledWith('LA');
    }, { timeout: 500 });
  });

  test('displays dropdown with search results', async () => {
    const mockFetch = jest.fn().mockResolvedValue([
      { value: 'LAX', label: 'LAX - Los Angeles', sublabel: 'California, USA' },
      { value: 'JFK', label: 'JFK - New York', sublabel: 'New York, USA' }
    ]);
    
    const user = userEvent.setup();
    
    render(
      <Autocomplete
        value=""
        onChange={jest.fn()}
        fetchOptions={mockFetch}
        placeholder="Search..."
        debounceMs={100}
        minChars={2}
      />,
      { wrapper }
    );

    const input = screen.getByPlaceholderText('Search...');
    await user.type(input, 'LA');

    await waitFor(() => {
      expect(screen.getByText('LAX - Los Angeles')).toBeInTheDocument();
    });
  });

  test('calls onChange when option is selected', async () => {
    const mockFetch = jest.fn().mockResolvedValue([
      { value: 'LAX', label: 'LAX - Los Angeles', data: { icao: 'KLAX' } }
    ]);
    
    const mockOnChange = jest.fn();
    const user = userEvent.setup();
    
    render(
      <Autocomplete
        value=""
        onChange={mockOnChange}
        fetchOptions={mockFetch}
        placeholder="Search..."
        debounceMs={100}
        minChars={2}
      />,
      { wrapper }
    );

    const input = screen.getByPlaceholderText('Search...');
    await user.type(input, 'LA');

    await waitFor(() => {
      expect(screen.getByText('LAX - Los Angeles')).toBeInTheDocument();
    });

    const option = screen.getByText('LAX - Los Angeles');
    await user.click(option);

    expect(mockOnChange).toHaveBeenCalledWith('LAX', { icao: 'KLAX' });
  });

  test('does not search if input is below minChars', async () => {
    const mockFetch = jest.fn().mockResolvedValue([]);
    const user = userEvent.setup();
    
    render(
      <Autocomplete
        value=""
        onChange={jest.fn()}
        fetchOptions={mockFetch}
        placeholder="Search..."
        debounceMs={100}
        minChars={3}
      />,
      { wrapper }
    );

    const input = screen.getByPlaceholderText('Search...');
    await user.type(input, 'LA');

    // Wait for debounce
    await new Promise(resolve => setTimeout(resolve, 200));

    // Should not have called fetch (only 2 chars)
    expect(mockFetch).not.toHaveBeenCalled();
  });

  test('shows loading indicator while fetching', async () => {
    const mockFetch = jest.fn().mockImplementation(() => 
      new Promise(resolve => setTimeout(() => resolve([]), 1000))
    );
    
    const user = userEvent.setup();
    
    render(
      <Autocomplete
        value=""
        onChange={jest.fn()}
        fetchOptions={mockFetch}
        placeholder="Search..."
        debounceMs={100}
        minChars={2}
      />,
      { wrapper }
    );

    const input = screen.getByPlaceholderText('Search...');
    await user.type(input, 'LAX');

    // After debounce, should show loading state
    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalled();
    });
  });

  test('clears dropdown when input is cleared', async () => {
    const mockFetch = jest.fn().mockResolvedValue([
      { value: 'LAX', label: 'LAX - Los Angeles' }
    ]);
    
    const user = userEvent.setup();
    
    render(
      <Autocomplete
        value=""
        onChange={jest.fn()}
        fetchOptions={mockFetch}
        placeholder="Search..."
        debounceMs={100}
        minChars={2}
      />,
      { wrapper }
    );

    const input = screen.getByPlaceholderText('Search...') as HTMLInputElement;
    
    // Type and show results
    await user.type(input, 'LA');
    await waitFor(() => {
      expect(screen.getByText('LAX - Los Angeles')).toBeInTheDocument();
    });

    // Clear input
    await user.clear(input);
    
    // Dropdown should close
    await waitFor(() => {
      expect(screen.queryByText('LAX - Los Angeles')).not.toBeInTheDocument();
    });
  });

  test('disabled state prevents interaction', () => {
    const mockFetch = jest.fn().mockResolvedValue([]);
    
    render(
      <Autocomplete
        value=""
        onChange={jest.fn()}
        fetchOptions={mockFetch}
        placeholder="Search..."
        disabled={true}
      />,
      { wrapper }
    );

    const input = screen.getByPlaceholderText('Search...') as HTMLInputElement;
    expect(input).toBeDisabled();
  });
});
