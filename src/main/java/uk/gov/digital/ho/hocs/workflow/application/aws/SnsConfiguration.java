package uk.gov.digital.ho.hocs.workflow.application.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({ "sns" })
public class SnsConfiguration {

    @Primary
    @Bean
    public AmazonSNSAsync snsClient(@Value("${aws.sns.audit-search.account.access-key}") String accessKey,
                                    @Value("${aws.sns.audit-search.account.secret-key}") String secretKey,
                                    @Value("${aws.sns.config.region}") String region) {
        return AmazonSNSAsyncClientBuilder.standard().withRegion(region).withCredentials(
            new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))).withClientConfiguration(
            new ClientConfiguration()).build();
    }

}
