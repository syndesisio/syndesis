import { OptionalInt } from '@syndesis/models';

export class OptionalIntUtil {
  /**
   *
   * @param value the value being used
   * @returns the `OptionalInt` object created from the specified value
   */
  public static create(value: number): OptionalInt {
    return { present: true, asInt: value };
  }

  /**
   * @param optional the `OptionalInt` whose value is being requested
   * @param defaultValue the value to return if no value exists
   * @returns the int value (defaults to zero)
   */
  public static getValue(
    optional: OptionalInt | undefined,
    defaultValue: number = 0
  ): number {
    if (optional && optional.present) {
      const value = optional.asInt;
      return value ? value : defaultValue;
    }

    return defaultValue;
  }

  /**
   * @param optional the value whose value is being checked
   * @returns true if the optional exists and there is a value
   */
  public static hasValue(optional: OptionalInt | undefined): boolean {
    return optional && optional.present ? optional.present : false;
  }
}
