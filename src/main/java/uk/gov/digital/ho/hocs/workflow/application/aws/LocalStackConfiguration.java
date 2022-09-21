package uk.gov.digital.ho.hocs.workflow.application.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local"})
public class LocalStackConfiguration {

    private final AWSCredentialsProvider awsCredentialsProvider;
    private final AwsClientBuilder.EndpointConfiguration endpoint;

    public LocalStackConfiguration(@Value("${localstack.base-url}") String baseUrl,
                                   @Value("${localstack.config.region}") String region) {
        this.awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test"));
        this.endpoint = new AwsClientBuilder.EndpointConfiguration(baseUrl, region);
    }

    @Primary
    @Bean
    public AmazonSNSAsync snsClient() {
        return AmazonSNSAsyncClientBuilder
                .standard()
                .withCredentials(awsCredentialsProvider)
                .withEndpointConfiguration(endpoint)
                .build();
    }
}
