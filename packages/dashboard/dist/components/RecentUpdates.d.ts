import { IIntegration } from "@syndesis/models";
import * as React from 'react';
export interface IRecentUpdatesProps {
    loading: boolean;
    recentlyUpdatedIntegrations: IIntegration[];
}
export declare class RecentUpdates extends React.Component<IRecentUpdatesProps> {
    render(): JSX.Element;
}
