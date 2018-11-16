package uk.gov.digital.ho.hocs.workflow.application.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "local"})
public class LocalStackConfiguration {

    @Bean("caseSqsClient")
    public AmazonSQS caseSqsClient() {
        return sqsClient();
    }

    @Bean("docsSqsClient")
    public AmazonSQS docsSqsClient() {
        return sqsClient();
    }

    @Value("${aws.local.host:localhost}")
    private String awsHost;

    @Bean
    public AmazonSQS sqsClient() {

        String host = String.format("http://%s:4576/", awsHost);

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(host, "eu-west-2");

        return AmazonSQSClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(awsCredentialsProvider)
                .withEndpointConfiguration(endpoint)
                .build();
    }

    @Bean
    public AmazonS3 s3Client() {

        String host = String.format("http://%s:4572/", awsHost);

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(host, "eu-west-2");

        return AmazonS3ClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(awsCredentialsProvider)
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .build();
    }

    private final AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {

        @Override
        public AWSCredentials getCredentials() {
            return new BasicAWSCredentials("test", "test");
        }

        @Override
        public void refresh() {

        }

    };

}