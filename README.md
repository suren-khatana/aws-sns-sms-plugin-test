# AWS SNS SMS Sender Plugin

[![Quality](https://img.shields.io/badge/quality-experiment-red)](https://curity.io/resources/code-examples/status/)
[![Availability](https://img.shields.io/badge/availability-source-blue)](https://curity.io/resources/code-examples/status/)

A custom SMS Sender plugin for the Curity Identity Server that uses AWS SNS to send SMS messages.

## Building the Plugin

You can build the plugin by issuing the command `mvn package`. This will produce a JAR and copy all of the dependencies in the `target` directory,
which can be installed.

## Installing the Plugin

To install the plugin, copy the compiled JAR (and all of its dependencies) into the :file:`${IDSVR_HOME}/usr/share/plugins/${pluginGroup}`
on each node, including the admin node. For more information about installing plugins, refer to the `https://curity.io/docs/idsvr/latest/developer-guide/plugins/index.html#plugin-installation`.

## Configuraton

Add a new SMS provider of type `aws-sns` under Facilities & configure the `Sns Access Method` as desired.

```
<config xmlns="http://tail-f.com/ns/config/1.0">
  <facilities xmlns="https://curity.se/ns/conf/base">
  <sms-providers>
  <sms-provider>
    <id>aws-sns</id>
    <aws-sns xmlns="https://curity.se/ns/ext-conf/aws-sns">
      <aws-region>eu-west-1</aws-region>
      <default-credentials-provider>
      </default-credentials-provider>
    </aws-sns>
  </sms-provider>
  </sms-providers>
  </facilities>
</config>

```

Add the newly added SMS provider to the Authentication -> General -> SMS Provider. Also under the SMS Authenticator settings, set `Account Manager or Intermediate Attribute` to `intermediate-attribute-name` and `Intermediate Attribute Name` to value `Subject`.

Example Authenticator configuration :

```
<config xmlns="http://tail-f.com/ns/config/1.0">
    <profiles xmlns="https://curity.se/ns/conf/base">
    <profile>
    <id>authentication</id>
    <type xmlns:auth="https://curity.se/ns/conf/profile/authentication">auth:authentication-service</type>
      <settings>
      <authentication-service xmlns="https://curity.se/ns/conf/profile/authentication">
      <authenticators>
      <authenticator>
        <id>sms-test</id>
        <sms>
          <intermediate-attribute-name>subject</intermediate-attribute-name>
          <send-otp-as-code>true</send-otp-as-code>
          <allow-registration-during-login>false</allow-registration-during-login>
        </sms>
      </authenticator>
      </authenticators>
      </authentication-service>
      </settings>
  </profile>
  </profiles>
</config>
```

## More Information

Please visit [curity.io](https://curity.io/) for more information about the Curity Identity Server.

Copyright (C) 2023 Curity AB.
