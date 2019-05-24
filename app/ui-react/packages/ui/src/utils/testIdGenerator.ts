/**
 * Generates an identifier suitable to use as a `data-testid`. Value arguments are separated by a
 * dash character.
 * @param values the values used to generate a test identifier
 * @returns a test identifier
 */
export function toTestId(...values: string[]): string {
  return values.map(generateId).join('-');
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
