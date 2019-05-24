declare module 'named-urls' {

  export function include<T extends {}>(base: string, routes: T): T;

  export function reverse(pattern: string, ...args: any[]): string;

  export function reverseForce(pattern: string, ...args: any[]): string;
}