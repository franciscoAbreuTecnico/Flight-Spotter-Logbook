// Learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom'
// Suppress React 18 act() warnings in tests
// These warnings are expected when testing async updates and debounced operations
const originalError = console.error;
beforeAll(() => {
  console.error = (...args) => {
    if (
      typeof args[0] === 'string' &&
      args[0].includes('Warning: An update to') &&
      args[0].includes('was not wrapped in act')
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});