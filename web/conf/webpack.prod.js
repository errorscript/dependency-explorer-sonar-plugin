const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');
const path = require("path");

module.exports = merge(common, {

  mode: 'production',
  // Don't attempt to continue if there are any errors.
  output: {
    // The entry point files MUST be shipped inside the final JAR's static/
    // directory.
    path: path.join(__dirname, "..", "target"),
    filename: "[name].js"
  },
  bail: true,
  optimization: {
    minimize: true
  },
  externals: {
    // React 16.8 ships with SonarQube, and should be re-used to avoid
    // collisions at runtime.
    react: "React",
    "react-dom": "ReactDOM",
    // Register the Sonar* globals as packages, to simplify importing.
    // See src/main/js/common/api.js for more information on what is exposed
    // in SonarRequest.
    "sonar-request": "SonarRequest"
  },
});
