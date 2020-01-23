// You can also use craco-alias plugin: https://github.com/risenforces/craco-alias

module.exports = {
    overrideCracoConfig: ({ cracoConfig, pluginOptions, context: { env, paths } }) => {
        if (pluginOptions.preText) {
            console.log(pluginOptions.preText);
        }

        console.log(JSON.stringify(craconfig, null, 4));

        // Always return the config object.
        return cracoConfig;
    },
    webpack: {
        alias: {
            "vscode": require.resolve('monaco-languageclient/lib/vscode-compatibility') 
        }
    }
};
