export interface BaseEntity { 
  readonly id?: string; 
  // TODO we'll make this optional for now
  kind?: string;
}
