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

package io.curity.identityserver.plugin.awssns;

import io.curity.identityserver.plugin.awssns.client.SnsClientManagedObject;
import io.curity.identityserver.plugin.awssns.config.AwsSnsSmsConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.errors.ErrorCode;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.sms.SmsSender;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

public final class AwsSnsSmsSender implements SmsSender
{
    private static final Logger _logger = LoggerFactory.getLogger(AwsSnsSmsSender.class);
    private static final org.apache.logging.log4j.Logger _maskedLogger = LogManager.getLogger(SnsClientManagedObject.class, ParameterizedMessageFactory.INSTANCE);

    private final ExceptionFactory _exceptionFactory;
    private final SnsClient _snsClient;

    public AwsSnsSmsSender(AwsSnsSmsConfig configuration, SnsClientManagedObject snsClientManagedObject)
    {
        _exceptionFactory = configuration.getExceptionFactory();
        _snsClient = snsClientManagedObject.getSnsClient();
    }

    @Override
    public boolean sendSms(String toNumber, String msg)
    {
        _maskedLogger.trace("Sending SMS to number = {}", toNumber);
        try
        {
            PublishRequest publishRequest = PublishRequest.builder().message(msg).phoneNumber(toNumber).build();
            PublishResponse result = _snsClient.publish(publishRequest);
            _logger.debug("SMS sent, id = {} and the status code is = {}", result.messageId(), result.sdkHttpResponse().statusCode());
            return true;
        }
        catch (SnsException snsException)
        {
            _logger.warn(snsException.awsErrorDetails().errorMessage());
            throw _exceptionFactory.internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }
}
