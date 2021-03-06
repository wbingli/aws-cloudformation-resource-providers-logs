package software.amazon.logs.loggroup;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {
    ListHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        handler = new ListHandler();
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_Success() {
        final LogGroup logGroup = LogGroup.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();
        final LogGroup logGroup2 = LogGroup.builder()
                .logGroupName("LogGroup2")
                .retentionInDays(2)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
                .build();
        final DescribeLogGroupsResponse describeResponse = DescribeLogGroupsResponse.builder()
                .logGroups(Arrays.asList(logGroup, logGroup2))
                .nextToken("token2")
                .build();

        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model1 = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();

        final ResourceModel model2 = ResourceModel.builder()
                .logGroupName("LogGroup2")
                .retentionInDays(2)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .nextToken("token")
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).containsAll(Arrays.asList(model1, model2));
        assertThat(response.getNextToken()).isEqualTo("token2");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
