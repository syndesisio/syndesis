import * as React from 'react';
export interface IApiContext {
    apiUri: string;
    token: string | null;
}
export declare const ApiContextDefaultValue: IApiContext;
export declare const ApiContext: React.Context<IApiContext>;
