/// <reference types="react" />
import { IIntegration, IIntegrationsMetricsTopIntegration, IMonitoredIntegration } from "@syndesis/models";
export interface IIntegrationCountsByState {
    Error: number;
    Pending: number;
    Published: number;
    Unpublished: number;
}
export declare function getIntegrationsCountsByState(integrations: IMonitoredIntegration[]): IIntegrationCountsByState;
export declare function getTimestamp(integration: IIntegration): number;
export declare function byTimestamp(a: IIntegration, b: IIntegration): number;
export declare function getRecentlyUpdatedIntegrations(integrations: IMonitoredIntegration[]): IIntegration[];
export declare function getTopIntegrations(integrations: IMonitoredIntegration[], topIntegrations?: IIntegrationsMetricsTopIntegration): IMonitoredIntegration[];
declare const _default: () => JSX.Element;
export default _default;
