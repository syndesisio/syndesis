// You can also use craco-alias plugin: https://github.com/risenforces/craco-alias

module.exports = {
    overrideCracoConfig: ({ cracoConfig, pluginOptions, context: { env, paths } }) => {
        // Always return the config object.
        return cracoConfig;
    },
    webpack: {
        alias: {
            "vscode": require.resolve('monaco-languageclient/lib/vscode-compatibility') 
        }
    }
};
