module.exports = function({ env, paths }) {
  return {
    // disable the custom loading of sourcemaps, it's too slow
    /*
    webpack: {
      configure: (webpackConfig, { env, paths }) => {
        webpackConfig.module.rules[2].oneOf = webpackConfig.module.rules[2].oneOf.map(
          r => {
            if (r.loader && r.loader.indexOf('babel-loader') >= 0) {
              r.options.sourceMaps = true;
            }
            return r;
          }
        );
        return webpackConfig;
      },
    },
    */
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
};
