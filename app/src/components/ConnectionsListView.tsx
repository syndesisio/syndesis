import { IConnection } from "@syndesis/app/containers";
import { ConnectionCard, ConnectionsGrid } from "@syndesis/ui";
import * as React from "react";
import { Link } from "react-router-dom";
import { IListViewToolbarProps, ListViewToolbar } from "./ListViewToolbar";

export function getConnectionIcon(connection: IConnection) {
  return connection.icon.startsWith("data:")
    ? connection.icon
    : `${process.env.PUBLIC_URL}/icons/${connection.id}.connection.png`;
}

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  match: any;
  loading: boolean;
  connections: IConnection[];
}

export class ConnectionsListView extends React.Component<IConnectionsListViewProps> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <Link
              to={`${this.props.match.url}/new`}
              className={"btn btn-primary"}
            >
              Create Connection
            </Link>
          </div>
        </ListViewToolbar>
        <div className={"container-fluid"}>
          <ConnectionsGrid
            loading={this.props.loading}
          >
            {this.props.connections.map((c, index) =>
              <ConnectionCard
                name={c.name}
                description={c.description || ""}
                icon={getConnectionIcon(c)}
                key={index}
              />
            )}
          </ConnectionsGrid>
        </div>
      </>
    );
  }

}