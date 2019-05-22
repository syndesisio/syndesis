/**
 * Generates an identifier suitable to use as a `data-testid`. The prefix is separated by a dot character.
 * @param prefix text that will be used as a prefix (typically the component's class name)
 * @param uniqueId unique text within a prefix
 * @returns a generated test identifier
 */
export function toTestId(prefix: string, uniqueId: string): string {
  if (prefix && uniqueId) {
    return generateId(prefix) + '--' + generateId(uniqueId);
  }

  if (prefix) {
    return generateId(prefix);
  }

  if (uniqueId) {
    return generateId(uniqueId);
  }

  return '';
}

/**
 * Replaces all non-alphanumeric characters with a dash and lowercases all alpha characters.
 * @param name the name whose ID is being generated
 * @returns the test ID
 */
function generateId(name: string): string {
  return name ? name.replace(/[^a-zA-Z0-9]+/g, '-').toLowerCase() : name; // from angular codebase
}
