module.exports = ({ config, mode }) => {
  config.module.rules.push({
    test: /\.tsx?$/,
    exclude: /node_modules/,
    use: [
      'awesome-typescript-loader?configFileName=tsconfig.storybook.json',
      'react-docgen-typescript-loader',
    ],
  });
  config.resolve.extensions = ['.ts', '.tsx', '.js', '.jsx'];
  config.node = {
    fs: 'empty',
  };
  return config;
};
