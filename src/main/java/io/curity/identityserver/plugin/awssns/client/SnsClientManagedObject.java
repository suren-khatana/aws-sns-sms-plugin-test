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

package io.curity.identityserver.plugin.awssns.client;

import io.curity.identityserver.plugin.awssns.config.AwsSnsSmsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.plugin.ManagedObject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

public class SnsClientManagedObject extends ManagedObject<AwsSnsSmsConfig>
{
    private static final Logger _logger = LoggerFactory.getLogger(SnsClientManagedObject.class);
    private final SnsClient _snsClient;

    public SnsClientManagedObject(AwsSnsSmsConfig configuration)
    {
        super(configuration);
        String awsRegion = configuration.getAwsRegion().awsRegion;
        _logger.debug("AWS Region = {}", awsRegion);

        AwsSnsSmsConfig.AWSAccessMethod accessMethod = configuration.getSnsAccessMethod();

        if (accessMethod.isEC2InstanceProfile().isPresent() && accessMethod.isEC2InstanceProfile().get())
        {
            _logger.debug("Using EC2 instance profile to configure SNS client");
            _snsClient = SnsClient.builder().region(Region.of(awsRegion)).credentialsProvider(InstanceProfileCredentialsProvider.builder().build()).build();
        }
        else if (accessMethod.getAwsProfile().isPresent())
        {
            String awsProfileName = accessMethod.getAwsProfile().get().getAwsProfileName();
            _logger.debug("Using local AWS profile '{}' to configure SNS client", awsProfileName);
            AwsCredentialsProvider awsCredentialsProvider = ProfileCredentialsProvider.builder().profileName(awsProfileName).build();

            /* If roleARN is present, get temporary credentials through AssumeRole */
            if (accessMethod.getAwsProfile().get().getAwsRoleARN().isPresent())
            {
                awsCredentialsProvider = getTemporaryCredentialsFromAssumeRole(awsCredentialsProvider, accessMethod.getAwsProfile().get().getAwsRoleARN().get(), awsRegion);
            }
            _snsClient = SnsClient.builder().region(Region.of(awsRegion)).credentialsProvider(awsCredentialsProvider).build();
        }
        else if (accessMethod.getAccessKeyIdAndSecret().isPresent())
        {
            _logger.debug("Using access key Id and secret to configure SNS client");
            AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessMethod.getAccessKeyIdAndSecret().get().getAccessKeyId(), accessMethod.getAccessKeyIdAndSecret().get().getAccessKeySecret()));

            /* If roleARN is present, get temporary credentials through AssumeRole */
            if (accessMethod.getAccessKeyIdAndSecret().get().getAwsRoleARN().isPresent())
            {
                awsCredentialsProvider = getTemporaryCredentialsFromAssumeRole(awsCredentialsProvider, accessMethod.getAccessKeyIdAndSecret().get().getAwsRoleARN().get(), awsRegion);
            }
            _snsClient = SnsClient.builder().region(Region.of(awsRegion)).credentialsProvider(awsCredentialsProvider).build();
        }
        else if (accessMethod.getDefaultCredentialsProvider().isPresent())
        {
            _logger.debug("Using default credential provider to configure SNS client");
            Boolean reuseLastProviderEnabled = configuration.getSnsAccessMethod().getDefaultCredentialsProvider().get().isReuseLastProvider();
            _snsClient = SnsClient.builder().region(Region.of(awsRegion)).credentialsProvider(DefaultCredentialsProvider.builder().reuseLastProviderEnabled(reuseLastProviderEnabled).build()).build();
        }
        else
        {
            throw new IllegalStateException("SNS configuration's access method is not valid");
        }
    }

    private AwsCredentialsProvider getTemporaryCredentialsFromAssumeRole(AwsCredentialsProvider credentialsProvider, String roleArn, String region)
    {
        StsClient stsClient = StsClient.builder().credentialsProvider(credentialsProvider).region(Region.of(region)).build();
        AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder().roleArn(roleArn).durationSeconds(3600).roleSessionName("sns-access").build();
        AssumeRoleResponse assumeRoleResponse = stsClient.assumeRole(assumeRoleRequest);

        if (!assumeRoleResponse.sdkHttpResponse().isSuccessful())
        {
            _logger.warn("Assume Role request was not successful : {}", assumeRoleResponse.sdkHttpResponse().statusText().get());
            return credentialsProvider;
        }

        AwsSessionCredentials awsSessionCredentials = AwsSessionCredentials.create(assumeRoleResponse.credentials().accessKeyId(), assumeRoleResponse.credentials().secretAccessKey(), assumeRoleResponse.credentials().sessionToken());
        _logger.debug("AssumeRole Request successful: {}", assumeRoleResponse.sdkHttpResponse().statusText());

        return StaticCredentialsProvider.create(awsSessionCredentials);
    }

    public SnsClient getSnsClient()
    {
        return _snsClient;
    }

    @Override
    public void close()
    {
        _snsClient.close();
    }
}
