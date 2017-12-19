import { Observable } from 'rxjs/Observable';

export interface ApiErrorMessage {
  userMessage: string;
  developerMessage: string;
}

export interface ApiErrors {
  errors?: {
    status: number;
    statusText: string;
    messages: Array<ApiErrorMessage>;
  };
}

export interface ApiResponse extends ApiErrors {
  readonly id?: string;
  kind?: string;
}

export interface ApiEndpoint {
  url: string;
  get<T>(): Observable<T>;
  post<T>(payload: any): Observable<T>;
  put<T>(payload: any): Observable<T>;
  delete<T>(payload?: any): Observable<T>;
  //TODO upload<T>(file?: File, payload?: any): Observable<T>;
}

export interface ApiRequestProgress {
  percentage: number;
  bytesLoaded: number;
  bytesPending: number;
  isComplete: boolean;
}
