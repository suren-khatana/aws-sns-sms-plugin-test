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

package io.curity.identityserver.plugin.awssns.config;

import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.OneOf;
import se.curity.identityserver.sdk.config.annotation.DefaultBoolean;
import se.curity.identityserver.sdk.config.annotation.Description;
import se.curity.identityserver.sdk.service.ExceptionFactory;

import java.util.Optional;

public interface AwsSnsSmsConfig extends Configuration
{
    @Description("The AWS Region where SNS service is deployed.")
    AwsRegion getAwsRegion();

    @Description("Choose how to access SNS service.")
    AWSAccessMethod getSnsAccessMethod();

    interface AWSAccessMethod extends OneOf
    {
        Optional<AccessKeyIdAndSecret> getAccessKeyIdAndSecret();

        interface AccessKeyIdAndSecret
        {
            @Description("AWS Access Key ID.")
            String getAccessKeyId();

            @Description("AWS Access Secret Key.")
            String getAccessKeySecret();

            @Description("Optional role ARN used when requesting temporary credentials, ex. arn:aws:iam::123456789012:role/sns-role")
            Optional<String> getAwsRoleARN();
        }

        Optional<AWSProfile> getAwsProfile();

        interface AWSProfile
        {
            @Description("AWS Profile Name.")
            String getAwsProfileName();

            @Description("Optional role ARN used when requesting temporary credentials, ex. arn:aws:iam::123456789012:role/sns-role")
            Optional<String> getAwsRoleARN();
        }

        @Description("EC2 instance that the Curity Identity Server is running on has been assigned an IAM Role with permissions to SNS.")
        Optional<Boolean> isEC2InstanceProfile();

        Optional<DefaultCredentialsProviderConfig> getDefaultCredentialsProvider();

        interface DefaultCredentialsProviderConfig
        {
            @Description("Controls whether the provider should reuse the last successful credentials provider in the chain. By default it is enabled")
            @DefaultBoolean(true) Boolean isReuseLastProvider();
        }
    }

    ExceptionFactory getExceptionFactory();
}
