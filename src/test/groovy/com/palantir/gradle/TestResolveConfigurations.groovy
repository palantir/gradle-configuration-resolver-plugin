package com.palantir.gradle

import nebula.test.PluginProjectSpec

class TestResolveConfigurations extends PluginProjectSpec {

  @Override
  String getPluginName() {
    return 'com.palantir.gradle-configuration-resolver'
  }

}
