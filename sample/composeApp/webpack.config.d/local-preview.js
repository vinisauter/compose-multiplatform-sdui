// The preview serves generated application resources on this computer only.
if (config.devServer) {
    const path = require('path');
    config.devServer.host = '127.0.0.1';
    config.devServer.open = false;
    config.devServer.static = config.devServer.static.filter(entry =>
        path.basename(entry.directory) === 'kotlin' ||
        entry.directory.includes(`${path.sep}processedResources${path.sep}`)
    );
}
