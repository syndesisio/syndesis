import { MetadataStore } from './metadata';

// Extend the PlatformStore interface with the specific interfaces
// of each slice of state managed by the store.
/* tslint:disable:no-empty-interface */
export interface PlatformStore extends MetadataStore { }
