/*
 * Copyright 2023 Curity AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.curity.identityserver.plugin.awssns.descriptor;

import io.curity.identityserver.plugin.awssns.AwsSnsSmsSender;
import io.curity.identityserver.plugin.awssns.client.SnsClientManagedObject;
import io.curity.identityserver.plugin.awssns.config.AwsSnsSmsConfig;
import se.curity.identityserver.sdk.plugin.ManagedObject;
import se.curity.identityserver.sdk.plugin.descriptor.SmsPluginDescriptor;
import se.curity.identityserver.sdk.service.sms.SmsSender;

import java.util.Optional;

public final class AwsSnsSmsDescriptor implements SmsPluginDescriptor<AwsSnsSmsConfig>
{
    @Override
    public Class<? extends SmsSender> getSmsSenderType()
    {
        return AwsSnsSmsSender.class;
    }

    @Override
    public String getPluginImplementationType()
    {
        return "aws-sns";
    }

    @Override
    public Class<? extends AwsSnsSmsConfig> getConfigurationType()
    {
        return AwsSnsSmsConfig.class;
    }

    @Override
    public Optional<? extends ManagedObject<AwsSnsSmsConfig>> createManagedObject(AwsSnsSmsConfig configuration)
    {
        return Optional.of(new SnsClientManagedObject(configuration));
    }
}