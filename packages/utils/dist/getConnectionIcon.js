export function getConnectionIcon(connection, publicUrl) {
    return (connection.icon || '').startsWith('data:')
        ? connection.icon
        : `${publicUrl}/icons/${connection.id}.connection.png`;
}
//# sourceMappingURL=getConnectionIcon.js.map