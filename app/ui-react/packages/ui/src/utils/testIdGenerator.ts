/**
 * Generates an identifier suitable to use as a `data-testid`. Value arguments are separated by two
 * dash characters.
 * @param values the values used to generate a test identifier
 * @returns a test identifier
 */
export function toTestId(...values: string[]): string {
  let testId = '';

  for (let i = 0; i < values.length; i++) {
    testId += generateId(values[i]);

    // add separator
    if (i < values.length - 1) {
      testId += '--';
    }
  }

  return testId;
}

/**
 * Replaces all non-alphanumeric characters with a dash and lowercases all alpha characters.
 * @param value the value whose ID is being generated
 * @returns the test ID
 */
function generateId(value: string): string {
  return value
    ? value.replace(/[^a-zA-Z0-9]+/g, '-').toLowerCase()
    : ((value || '') as string);
}
